package matt.fx.node.console

import javafx.application.Platform.runLater
import javafx.geometry.Insets
import javafx.scene.control.ScrollPane.ScrollBarPolicy
import javafx.scene.input.Clipboard
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import matt.async.queue.QueueThread
import matt.async.queue.QueueThread.SleepType
import matt.async.safe.SemaphoreString
import matt.async.safe.sync
import matt.async.schedule.every
import matt.async.thread.daemon
import matt.auto.macapp.SublimeText
import matt.file.commons.DATA_FOLDER
import matt.file.commons.LOG_FOLDER
import matt.file.construct.mFile
import matt.fx.control.menu.context.mcontextmenu
import matt.fx.control.wrapper.scroll.ScrollPaneWrapper
import matt.fx.graphics.clip.copyToClipboard
import matt.fx.graphics.hotkey.hotkeys
import matt.fx.graphics.hotkey.plus
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.node.console.Console.RefreshRate.NORMAL
import matt.fx.node.console.mem.ConsoleMemory
import matt.fx.node.console.text.ConsoleTextFlow
import matt.hurricanefx.eye.mtofx.createROFXPropWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapperImpl
import matt.kjlib.shell.forEachErrChar
import matt.kjlib.shell.forEachOutChar
import matt.lang.err
import matt.lang.go
import matt.log.tab
import matt.log.todo.todoOnce
import matt.obs.bindings.bool.not
import matt.obs.prop.BindableProperty
import matt.prim.str.throttled
import matt.stream.ReaderEndReason
import matt.stream.forEachChar
import matt.stream.piping.redirectErr
import matt.stream.piping.redirectOut
import matt.time.dur.ms
import matt.time.dur.sec
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintWriter

fun ParentWrapper<NodeWrapper>.processConsole(
  name: String = "new console", op: ProcessConsole.()->Unit = {}
): ProcessConsole {
  return addr(ProcessConsole(name).apply(op))
}

fun ParentWrapper<NodeWrapper>.interceptConsole(
  name: String = "new console", op: SystemRedirectConsole.()->Unit = {}
): SystemRedirectConsole {
  return addr(SystemRedirectConsole(name).apply(op))
}

fun ParentWrapper<NodeWrapper>.customConsole(
  name: String = "new console", takesInput: Boolean = true, op: CustomConsole.()->Unit = {}
): CustomConsole {

  return addr(CustomConsole(name, takesInput).apply(op))
}

val CONSOLE_MEM_FOLD = DATA_FOLDER + "ConsoleMemory"

sealed class Console(
  val name: String, val takesInput: Boolean = true
): ScrollPaneWrapper<ConsoleTextFlow>(), NodeWrapper {

  object RefreshRate {
	const val NORMAL = 500L // ms
	const val HASTE = 10L // ms
	private const val HASTE_DUR = 1000L // ms
	const val HASTE_COUNT = (HASTE_DUR/HASTE).toInt()
  }

  private val hscrollOption = BindableProperty(true)
  private val myLogFolder = LOG_FOLDER + name
  protected val logfile = myLogFolder + "$name.log"
  protected val errFile = mFile(logfile.absolutePath + ".err")
  protected var writer: BufferedWriter? = null
  private val mem = ConsoleMemory(CONSOLE_MEM_FOLD + "$name.txt")
  private val consoleTextFlow = ConsoleTextFlow(takesInput)

  private val autoscrollProp = BindableProperty(true)
  private val throttleProp = BindableProperty(true)
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

  private val logWorker = QueueThread(sleepPeriod = NORMAL.ms, SleepType.WHEN_NO_JOBS)
  private val refresh: ()->Unit = sync {
	if (enableProp.value) {
	  var newString = unshownOutput.takeAndClear()
	  if (newString.isNotEmpty()) {
		if (throttle && newString.length > 10_000) {
		  newString =
			newString.throttled() // when I accidentally printed a huge array in python console, whole app crashed
		}
		runLater {
		  consoleTextFlow.displayNewText(newString)
		  if (takesInput) consoleTextFlow.clearStoredAndDisplayedInput()
		}
		logWorker.with {
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

	hscrollOption.onChange {

	  consoleTextFlow.requestLayout() // this works!!
	}


	vbarPolicy = ScrollBarPolicy.NEVER
	hbarPolicy = ScrollBarPolicy.NEVER

	consoleTextFlow.padding = Insets(15.0, 30.0, 15.0, 15.0)
	fitToWidthProperty().bind(hscrollOption.not().createROFXPropWrapper())

	var old = consoleTextFlow.height
	consoleTextFlow.heightProperty.onChange { newValue ->
	  if ((newValue.toDouble() > old.toDouble()) && autoscroll) {
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


	todoOnce("removed parentProperty listener here... make sure the key handlers below still work")
	/*parentProperty().onChange { parent ->*/

	if (takesInput) {        /*parent?.apply {*/
	  addEventFilter(KeyEvent.KEY_TYPED) {
		println("console got key typed")
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
	  checkitem("autoscroll", autoscrollProp)
	  checkitem("throttle", throttleProp)
	  checkitem("enabled", enableProp)
	  actionitem("Open Log") {
		SublimeText.open(logfile)
		SublimeText.open(errFile)
	  }
	  "copy text" does {
		consoleTextFlow.fullText().copyToClipboard()
	  }
	  checkitem("hscroll", hscrollOption)
	  actionitem("matt.log.level.getDEBUG: remove last child") {
		consoleTextFlow.children.removeAt(consoleTextFlow.children.size - 1)
	  }
	  actionitem("print debug info on all texts") {
		println("consoleTextFlow children info:")
		consoleTextFlow.children.forEach {
		  tab(it)
		}
	  }
	}
	every(NORMAL.ms, ownTimer = true) { refresh() }
  }


  fun reset() {
	consoleTextFlow.clearOutputAndStoredInput()
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

class ProcessConsole(name: String): Console(name) {
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

class SystemRedirectConsole(name: String): Console(name, takesInput = false) {
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

class CustomConsole(name: String, takesInput: Boolean): Console(name, takesInput) {
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
	  inpConsole.bufferedReader().forEachChar {
		unshownOutput += it
	  }
	}
	val pw = PrintWriter(outConsole, true)
	return pw to userInput
  }
}




