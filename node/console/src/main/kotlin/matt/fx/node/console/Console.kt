package matt.fx.node.console

import javafx.application.Platform.runLater
import javafx.geometry.Insets
import javafx.scene.control.ScrollPane.ScrollBarPolicy
import javafx.scene.input.Clipboard
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import matt.async.safe.SemaphoreString
import matt.async.safe.sync
import matt.async.thread.daemon
import matt.async.thread.queue.QueueWorker
import matt.async.thread.schedule.every
import matt.auto.console.mem.CONSOLE_MEM_FOLD
import matt.auto.console.mem.ConsoleMemory
import matt.auto.macapp.sublime.SublimeText
import matt.file.commons.reg.mattLogContext
import matt.file.construct.mFile
import matt.file.ext.backup.doubleBackupWrite
import matt.file.ext.j.clearIfTooBigThenAppendText
import matt.file.ext.j.mkparents
import matt.file.toJioFile
import matt.file.watch.tailflow.NewContent
import matt.file.watch.tailflow.Reset
import matt.file.watch.tailflow.tailFlow
import matt.fx.control.wrapper.scroll.ScrollPaneWrapper
import matt.fx.graphics.clip.copyToClipboard
import matt.fx.graphics.fxthread.ensureInFXThreadInPlace
import matt.fx.graphics.hotkey.hotkeys
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.node.console.Console.RefreshRate.NORMAL
import matt.fx.node.console.text.ConsoleTextFlow
import matt.gui.menu.context.mcontextmenu
import matt.kjlib.socket.client.clients.InterAppServices
import matt.lang.common.err
import matt.lang.common.go
import matt.lang.model.file.FsFile
import matt.lang.model.file.MacFileSystem
import matt.lang.seq.charSequence
import matt.obs.bindings.bool.not
import matt.obs.prop.writable.BindableProperty
import matt.prim.str.throttled
import matt.shell.commonj.context.ReapingShellExecutionContext
import matt.shell.proc.forEachErrChar
import matt.shell.proc.forEachOutChar
import matt.stream.ReaderEndReason
import matt.time.dur.common.ms
import matt.time.dur.common.sec
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintWriter


context(ReapingShellExecutionContext, InterAppServices)
fun ParentWrapper<NodeWrapper>.processConsole(
    process: Process? = null,
    name: String = "new process console",
    op: ProcessConsole.() -> Unit = {}
): ProcessConsole =
    addr(
        ProcessConsole(name, this@InterAppServices).apply {
            process?.let(::attachProcess)
            op()
        }
    )


context(ReapingShellExecutionContext, InterAppServices)
fun ParentWrapper<NodeWrapper>.customConsole(
    name: String = "new custom console",
    takesInput: Boolean = true,
    op: CustomConsole.() -> Unit = {}
): CustomConsole = addr(CustomConsole(name, takesInput, this@InterAppServices).apply(op))


private const val DEFAULT_MAX_LINES: Int = 1000

context(ReapingShellExecutionContext)
sealed class Console(
    val name: String,
    services: InterAppServices,
    val takesInput: Boolean = true,
    throttle: Boolean = true,
    maxLines: Int? = DEFAULT_MAX_LINES
) : ScrollPaneWrapper<ConsoleTextFlow>(contentCls = ConsoleTextFlow::class), NodeWrapper {

    companion object {
        private const val THROTTLE_THRESHOLD = 100_000
    }

    object RefreshRate {
        const val NORMAL = 500L /* ms */
        const val HASTE = 10L /* ms */
        private const val HASTE_DUR = 1000L /* ms */
        const val HASTE_COUNT = (HASTE_DUR / HASTE).toInt()
    }

    private val hScrollOption = BindableProperty(true)
    private val myLogFolder = mattLogContext.logFolder + name
    protected val logfile by lazy { (myLogFolder + "$name.log").also { it.mkparents() } }
    protected val errFile by lazy {
        mFile(logfile.absolutePath + ".err", MacFileSystem).also {
            it.toJioFile().mkparents()
        }
    }
    protected var writer: BufferedWriter? = null
    private val mem = ConsoleMemory(CONSOLE_MEM_FOLD + "$name.txt")
    private val consoleTextFlow = ConsoleTextFlow(takesInput, maxLines = maxLines)

    private val autoscrollProp = BindableProperty(true)
    private val throttleProp = BindableProperty(throttle)
    private val enableProp = BindableProperty(true)
    private val autoscroll by autoscrollProp
    private val throttle by throttleProp

    fun hasText() = consoleTextFlow.hasText()

    private fun sendInput() {
        if (!takesInput) err("bad")
        val theInput = consoleTextFlow.prepStoredInput()

        writer?.apply {
            try {
                write(theInput)
                flush()
            } catch (e: IOException) {
                unShownOutput += "disconnecting writer due to IOException"
                writer = null
            }
        }

        consoleTextFlow.displayInputAsSentAndClearStoredInput()
        runBlocking {
            mem.launchSentInputHandler(theInput)
        }
    }

    protected var unShownOutput = SemaphoreString("")
    private fun copy() {
        consoleTextFlow.unsentInput.copyToClipboard()
    }


    private fun paste() {
        val clipboard = Clipboard.getSystemClipboard()
        val content = clipboard.string
        content?.forEach { c ->
            consoleTextFlow.displayAndHoldNewUnsentInputChar(c.toString())
        }
    }

    private fun cut() {
        copy()
        consoleTextFlow.clearStoredAndDisplayedInput()
    }


    private var clearLogI = 0

    private val logWorker = QueueWorker()
    private val refresh: () -> Unit =
        sync {
            if (enableProp.value) {
                var newString = unShownOutput.takeAndClear()
                if (newString.isNotEmpty()) {

                    if (throttle && newString.length > THROTTLE_THRESHOLD) {
                        newString =
                            newString.throttled() /* when I accidentally printed a huge array in python console, whole app crashed */
                    }
                    runLater {
                        with(services) {
                            consoleTextFlow.displayNewText(newString)
                        }
                        if (takesInput) consoleTextFlow.clearStoredAndDisplayedInput()
                    }
                    logWorker.schedule {
                        if (clearLogI == 1000) {
                            logfile.toJioFile().clearIfTooBigThenAppendText(newString)
                            clearLogI = 0
                        } else {
                            logfile.append(newString)
                            clearLogI += 1
                        }
                    }
                }
            }
        }

    init {

        hScrollOption.onChange {

            consoleTextFlow.requestLayout() /* this works!! */
        }


        vbarPolicy = ScrollBarPolicy.NEVER
        hbarPolicy = ScrollBarPolicy.NEVER

        consoleTextFlow.padding = Insets(15.0, 30.0, 15.0, 15.0)
        fitToWidthProperty.bind(hScrollOption.not())

        var old = consoleTextFlow.height
        consoleTextFlow.heightProperty.onChange { newValue ->
            if ((newValue > old) && autoscroll) {
                vvalue = consoleTextFlow.height
            }
            old = newValue
        }



        content =
            consoleTextFlow.apply {
                minWidthProperty.bind(this@Console.widthProperty)
                minHeightProperty.bind(this@Console.heightProperty)
            }
        backgroundFill = Color.BLACK
        every(1.sec) {
            runLater {
                /*
THIS ACTUALLY WORKS!!!
THIS SOLVES THE PROBLEM WHERE THE CONSOLE IS TOO SMALL
ITS ABSOLUTELY AN INTERNAL JFX BUG
*/
                autosize() /* DEBUG */
            }
        }



        fun hitEnter() {
            sendInput()
            var count = 0
            every(RefreshRate.HASTE.ms, ownTimer = true) {
                refresh()
                count += 1
                if (count == RefreshRate.HASTE_COUNT) cancel()
            }
        }



        if (takesInput) {
            /*parent?.apply {*/
            addEventFilter(KeyEvent.KEY_TYPED) {
                if (!it.isMetaDown) {
                    if (it.character == "\r") Unit /*hitEnter()*/
                    else consoleTextFlow.displayAndHoldNewUnsentInputChar(it.character)
                }
                it.consume()
            }        /*}*/
        }

        hotkeys(filter = true) {
            if (takesInput) {
                RETURN.bare op {
                    hitEnter()
                }
                println("I kinda miss: listOf(BACK_SPACE + DELETE) op consoleTextFlow::deleteAnInputCharIfPossible")
                listOf(BACK_SPACE, DELETE).forEach {
                    it op consoleTextFlow::deleteAnInputCharIfPossible
                }
                C.meta op ::copy
                V.meta op ::paste
                X.meta op ::cut
            }
            UP.meta { vvalue = 0.0 }
            UP op {
                runBlocking { mem.up() }?.go { consoleTextFlow.setInputToMem(it) }
            }
            DOWN.meta { vvalue = consoleTextFlow.height }
            DOWN op { consoleTextFlow.setInputToMem(runBlocking { mem.down() }) }
            RIGHT.meta { hvalue = consoleTextFlow.width }
            LEFT.meta { hvalue = 0.0 }
            K.meta op consoleTextFlow::clearOutputAndStoredInput
            EQUALS.meta op consoleTextFlow::tryIncreaseFontSize
            MINUS.meta op consoleTextFlow::tryDecreaseFontSize
        }    /*}*/
        mcontextmenu {
            item("increase font size") {
                enableWhen { this@Console.consoleTextFlow.canIncreaseFontSize }
                setOnAction {
                    this@Console.consoleTextFlow.tryIncreaseFontSize()
                }
            }
            item("decrease font size") {
                enableWhen { this@Console.consoleTextFlow.canDecreaseFontSize }
                setOnAction {
                    this@Console.consoleTextFlow.tryDecreaseFontSize()
                }
            }
            checkitem("autoscroll", this@Console.autoscrollProp)
            checkitem("throttle", this@Console.throttleProp)
            checkitem("enabled", this@Console.enableProp)
            actionitem("Open Log") {
                SublimeText.open(this@Console.logfile)
                SublimeText.open(this@Console.errFile)
            }
            "copy text" does {
                this@Console.consoleTextFlow.fullText().copyToClipboard()
            }
            checkitem("hscroll", this@Console.hScrollOption)
        }
        every(NORMAL.ms, ownTimer = true) { refresh() }
    }


    fun reset() {
        ensureInFXThreadInPlace {
            consoleTextFlow.clearOutputAndStoredInput()
        }
    }


    protected fun handleEndOfStream(endReason: ReaderEndReason) {
        when (endReason.type) {
            ReaderEndReason.TYPE.END_OF_STREAM -> {
                unShownOutput += "stream ended"
            }

            ReaderEndReason.TYPE.IO_EXCEPTION  -> {
                unShownOutput += "stream ended with IO Exception"
                logfile.append(endReason.exception!!.toString())
                errFile.toJioFile().append(endReason.exception.toString())
            }
        }
    }
}




context(ReapingShellExecutionContext)
sealed class TailCapableConsole(
    name: String,
    takesInput: Boolean,
    throttle: Boolean,
    services: InterAppServices,
    maxLines: Int? = DEFAULT_MAX_LINES
) : Console(name, takesInput = takesInput, throttle = throttle, maxLines = maxLines, services = services) {




    protected fun CoroutineScope.tail(logFile: FsFile) =
        launch(IO) {
            logFile.tailFlow().collect {
                when (it) {
                    is NewContent -> {
                        unShownOutput += it.content
                    }
                    Reset         -> reset()
                }
            }
        }
}





context(ReapingShellExecutionContext)
class ProcessConsole(
    name: String,
    services: InterAppServices
) :
    TailCapableConsole(name, takesInput = true, throttle = true, services = services) {


    fun CoroutineScope.alsoTail(logFile: FsFile) = tail(logFile)

    fun attachProcess(p: Process) {
        logfile.toJioFile().doubleBackupWrite("")
        errFile.toJioFile().doubleBackupWrite("")
        writer = p.outputStream.bufferedWriter()
        daemon(name = "attachProcess Thread 1") {
            val endReason =
                p.forEachOutChar {
                    unShownOutput += it
                }
            handleEndOfStream(endReason)
        }
        daemon(name = "attachProcess Thread 2") {
            val endReason =
                p.forEachErrChar {
                    unShownOutput += it
                    errFile.toJioFile().append(it)
                }
            handleEndOfStream(endReason)
        }
    }
}


context(ReapingShellExecutionContext)
class CustomConsole(
    name: String,
    takesInput: Boolean,
    services: InterAppServices
) : Console(name, takesInput = takesInput, services = services) {
    fun custom(): Pair<PrintWriter, BufferedReader?> {
        val userInput =
            if (takesInput) {
                val inpUser = PipedInputStream()
                val outUser = PipedOutputStream(inpUser)
                writer = outUser.bufferedWriter()
                inpUser.bufferedReader()
            } else null

        val inpConsole = PipedInputStream()
        val outConsole = PipedOutputStream(inpConsole)
        daemon(name = "CustomConsole.custom Thread") {
            inpConsole.bufferedReader().charSequence().forEach {
                unShownOutput += it.toString()
            }
        }
        val pw = PrintWriter(outConsole, true)
        return pw to userInput
    }
}




