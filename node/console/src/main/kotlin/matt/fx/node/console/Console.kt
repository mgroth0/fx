package matt.fx.node.console

import javafx.application.Platform.runLater
import javafx.geometry.Insets
import javafx.scene.control.ScrollPane.ScrollBarPolicy
import javafx.scene.input.Clipboard
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import matt.async.safe.SemaphoreString
import matt.async.safe.sync
import matt.async.thread.daemon
import matt.async.thread.queue.QueueWorker
import matt.async.thread.schedule.every
import matt.auto.ascript.AppleScriptString
import matt.auto.macapp.SublimeText
import matt.file.MFile
import matt.file.commons.DATA_FOLDER
import matt.file.commons.mattLogContext
import matt.file.construct.mFile
import matt.fx.control.wrapper.scroll.ScrollPaneWrapper
import matt.fx.graphics.clip.copyToClipboard
import matt.fx.graphics.fxthread.ensureInFXThreadInPlace
import matt.fx.graphics.hotkey.hotkeys
import matt.fx.graphics.hotkey.plus
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.node.console.Console.RefreshRate.NORMAL
import matt.fx.node.console.mem.ConsoleMemory
import matt.fx.node.console.text.ConsoleTextFlow
import matt.gui.menu.context.mcontextmenu
import matt.lang.err
import matt.lang.go
import matt.lang.require.requireNot
import matt.lang.seq.charSequence
import matt.obs.bindings.bool.not
import matt.obs.prop.BindableProperty
import matt.prim.str.throttled
import matt.shell.proc.forEachErrChar
import matt.shell.proc.forEachOutChar
import matt.stream.ReaderEndReason
import matt.stream.piping.redirectErr
import matt.stream.piping.redirectOut
import matt.time.dur.ms
import matt.time.dur.sec
import matt.time.dur.sleep
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintWriter
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.channels.FileChannel
import java.nio.file.NoSuchFileException
import kotlin.time.Duration.Companion.seconds

val YesIUse = AppleScriptString::class

fun ParentWrapper<NodeWrapper>.processConsole(
    process: Process? = null,
    name: String = "new process console",
    op: ProcessConsole.() -> Unit = {}
): ProcessConsole {
    return addr(ProcessConsole(name).apply {
        process?.let(::attachProcess)
        op()
    })
}

fun ParentWrapper<NodeWrapper>.interceptConsole(
    name: String = "new intercept console",
    op: SystemRedirectConsole.() -> Unit = {}
): SystemRedirectConsole {
    return addr(SystemRedirectConsole(name).apply(op))
}

fun ParentWrapper<NodeWrapper>.customConsole(
    name: String = "new custom console",
    takesInput: Boolean = true,
    op: CustomConsole.() -> Unit = {}
): CustomConsole {

    return addr(CustomConsole(name, takesInput).apply(op))
}

val CONSOLE_MEM_FOLD = DATA_FOLDER + "ConsoleMemory"

private val DEFAULT_MAX_LINES: Int = 1000

sealed class Console(
    val name: String,
    val takesInput: Boolean = true,
    throttle: Boolean = true,
    maxLines: Int? = DEFAULT_MAX_LINES
) : ScrollPaneWrapper<ConsoleTextFlow>(), NodeWrapper {

    companion object {
        private const val THROTTLE_THRESHOLD = 100_000
    }

    object RefreshRate {
        const val NORMAL = 500L // ms
        const val HASTE = 10L // ms
        private const val HASTE_DUR = 1000L // ms
        const val HASTE_COUNT = (HASTE_DUR / HASTE).toInt()
    }

    private val hScrollOption = BindableProperty(true)
    private val myLogFolder = mattLogContext.logFolder + name
    protected val logfile = myLogFolder + "$name.log"
    protected val errFile = mFile(logfile.absolutePath + ".err")
    protected var writer: BufferedWriter? = null
    private val mem = ConsoleMemory(CONSOLE_MEM_FOLD + "$name.txt")
    private val consoleTextFlow = ConsoleTextFlow(takesInput, maxLines = maxLines)

    private val autoscrollProp = BindableProperty(true)
    private val throttleProp = BindableProperty(throttle)
    private val enableProp = BindableProperty(true)
    private var autoscroll by autoscrollProp
    private var throttle by throttleProp

    fun hasText() = consoleTextFlow.hasText()

    private fun sendInput() {
        if (!takesInput) err("bad")
        val theInput = consoleTextFlow.prepStoredInput()

        writer?.apply {
            try {
                write(theInput)
                flush()
            } catch (e: IOException) {
                unshownOutput += "disconnecting writer due to IOException"
                writer = null
            }
        }

        consoleTextFlow.displayInputAsSentAndClearStoredInput()
        mem.handle_sent_input(theInput)

    }

    protected var unshownOutput = SemaphoreString("")
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
    private val refresh: () -> Unit = sync {
        if (enableProp.value) {
            var newString = unshownOutput.takeAndClear()
            if (newString.isNotEmpty()) {

                if (throttle && newString.length > THROTTLE_THRESHOLD) {
                    newString =
                        newString.throttled() // when I accidentally printed a huge array in python console, whole app crashed
                }
                runLater {
                    consoleTextFlow.displayNewText(newString)
                    if (takesInput) consoleTextFlow.clearStoredAndDisplayedInput()
                }
                logWorker.schedule {
                    if (clearLogI == 1000) {
                        logfile.clearIfTooBigThenAppendText(newString)
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

            consoleTextFlow.requestLayout() // this works!!
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



        content = consoleTextFlow.apply {
            minWidthProperty.bind(this@Console.widthProperty)
            minHeightProperty.bind(this@Console.heightProperty)
        }
        backgroundFill = Color.BLACK
        every(1.sec) {
            runLater {        //                THIS ACTUALLY WORKS!!!
                //                 THIS SOLVES THE PROBLEM WHERE THE CONSOLE IS TOO SMALL
                //                ITS ABSOLUTELY AN INTERNAL JFX BUG
                autosize() // matt.log.level.getDEBUG
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



        if (takesInput) {        /*parent?.apply {*/
            addEventFilter(KeyEvent.KEY_TYPED) {
//                println("console got key typed")
                if (!it.isMetaDown) {
                    if (it.character == "\r") Unit /*hitEnter()*/
                    else consoleTextFlow.displayAndHoldNewUnsentInputChar(it.character)
                }
                it.consume()
            }        /*}*/
        }

        hotkeys(filter = true) {
            if (takesInput) {
                ENTER.bare op {
                    hitEnter()
                }
                (BACK_SPACE + DELETE) op consoleTextFlow::deleteAnInputCharIfPossible
                C.meta op ::copy
                V.meta op ::paste
                X.meta op ::cut
            }
            UP.meta { vvalue = 0.0 }
            UP op { mem.up()?.go { consoleTextFlow.setInputToMem(it) } }
            DOWN.meta { vvalue = consoleTextFlow.height }
            DOWN op { consoleTextFlow.setInputToMem(mem.down()) }
            RIGHT.meta { hvalue = consoleTextFlow.width }
            LEFT.meta { hvalue = 0.0 }
            K.meta op consoleTextFlow::clearOutputAndStoredInput
            (PLUS + EQUALS).meta op consoleTextFlow::tryIncreaseFontSize
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
                unshownOutput += "stream ended"
            }

            ReaderEndReason.TYPE.IO_EXCEPTION  -> {
                unshownOutput += "stream ended with IO Exception"
                logfile.append(endReason.exception!!.toString())
                errFile.append(endReason.exception.toString())
            }
        }
    }
}


sealed class TailCapableConsole(
    name: String,
    takesInput: Boolean,
    throttle: Boolean,
    maxLines: Int? = DEFAULT_MAX_LINES
) : Console(name, takesInput = takesInput, throttle = throttle, maxLines = maxLines) {
    companion object {
        val BUFF_SIZE = 1000
    }

    private val interval = 1.seconds
    protected var shouldContinue = true
    protected fun tail(logFile: MFile) = daemon {
        val decoder = Charsets.UTF_8.newDecoder()
        var reader: FileChannel? = null
        try {
            var pos = 0
            val charBuffer = CharBuffer.allocate(BUFF_SIZE)
            val byteBuffer = ByteBuffer.allocate(BUFF_SIZE)
            val charArray = CharArray(BUFF_SIZE)
            var lastSize = logFile.size()

            fun localReset() {
                reader?.close()
                reader = null
                pos = 0
                charBuffer.clear()
                byteBuffer.clear()
                reset()
            }


            while (shouldContinue) {

                val newSize = try {
                    logFile.size()
                } catch (e: NoSuchFileException) {
                    localReset()
                    sleep(interval)
                    continue
                }


                val theReader = reader ?: logFile.readChannel().also {
                    reader = it
                }

                /*LOOPHOLE: The file contents may have changed before `position`, without the file size shrinking, and in that case I would not catch it*/
                if (newSize < lastSize) {
                    theReader.position(0)
                }
                lastSize = newSize


                val numBytesRead = theReader.read(byteBuffer)
                pos += numBytesRead
                byteBuffer.flip()
                decoder.decode(byteBuffer, charBuffer, false)
                byteBuffer.clear()
                @Suppress("UNUSED_VARIABLE") val endOfStream = numBytesRead == -1
                if (numBytesRead > 0) {
                    charBuffer.flip()
                    val numChars = charBuffer.remaining()
                    charBuffer.get(charArray, 0, numChars)
                    charBuffer.clear()
                    unshownOutput += charArray.concatToString(0, numChars)
                } else {
                    sleep(interval)
                }
            }
        } finally {
            reader?.close()
        }

    }
}

class ProcessConsole(name: String) : TailCapableConsole(name, takesInput = true, throttle = true) {
    fun alsoTail(logFile: MFile) = tail(logFile)

    fun attachProcess(p: Process) {
        logfile.doubleBackupWrite("")
        errFile.doubleBackupWrite("")
        writer = p.outputStream.bufferedWriter()
        daemon {
            val endReason = p.forEachOutChar {
                unshownOutput += it
            }
            handleEndOfStream(endReason)
        }
        daemon {
            val endReason = p.forEachErrChar {
                unshownOutput += it
                errFile.append(it)
            }
            handleEndOfStream(endReason)
        }
    }
}

class TailConsole(
    name: String,
    val file: MFile
) : TailCapableConsole(name, takesInput = false, throttle = false, maxLines = null) {

    private var started = false
    private var stopped = false

    @Synchronized
    fun start() {
        requireNot(started)
        requireNot(stopped)
        started = true
        tail(file)
    }

    @Synchronized
    fun stop() {
        shouldContinue = false
        stopped = true
    }

}

class SystemRedirectConsole(name: String) : Console(name, takesInput = false) {
    fun interceptStdOutErr() {
        logfile.doubleBackupWrite("")
        errFile.doubleBackupWrite("")
        redirectOut { unshownOutput += it }
        redirectErr {
            unshownOutput += it
            errFile.append(it)
        }
    }
}

class CustomConsole(
    name: String,
    takesInput: Boolean
) : Console(name, takesInput) {
    fun custom(): Pair<PrintWriter, BufferedReader?> {
        val userInput = if (takesInput) {
            val inpUser = PipedInputStream()
            val outUser = PipedOutputStream(inpUser)
            writer = outUser.bufferedWriter()
            inpUser.bufferedReader()
        } else null

        val inpConsole = PipedInputStream()
        val outConsole = PipedOutputStream(inpConsole)
        daemon {
            inpConsole.bufferedReader().charSequence().forEach {
                unshownOutput += it.toString()
            }
        }
        val pw = PrintWriter(outConsole, true)
        return pw to userInput
    }
}




