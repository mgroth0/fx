package matt.fx.node.console.text

import javafx.animation.Timeline
import javafx.scene.paint.Color
import javafx.scene.text.Text
import matt.async.schedule.AccurateTimer
import matt.fx.graphics.anim.animation.keyframe
import matt.fx.graphics.anim.animation.timeline
import matt.fx.graphics.font.fixed
import matt.fx.graphics.lang.removeAllButLastN
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.text.TextWrapper
import matt.fx.graphics.wrapper.text.textlike.applyConsoleStyle
import matt.fx.graphics.wrapper.textflow.TextFlowWrapper
import matt.hurricanefx.eye.mtofx.createWritableFXPropWrapper
import matt.hurricanefx.eye.time.toFXDuration
import matt.lang.ifTrueOrNull
import matt.obs.bind.deepBinding
import matt.obs.prop.VarProp
import matt.time.dur.ms

private const val PROMPT = "> "
private val PROMPT_COLOR: Color = Color.GREEN

const val MAX_FONT_SIZE = 50.0
const val MIN_FONT_SIZE = 5.0

class ConsoleTextFlow(val takesInput: Boolean = true): TextFlowWrapper<NodeWrapper>() {

  fun hasText() = children.size > 3

  fun tryIncreaseFontSize() {
	println("trying to increase font size")
	if (fontSize < MAX_FONT_SIZE) {
	  fontSize += 1
	  updateFonts()
	}
  }

  fun tryDecreaseFontSize() {
	println("trying to decrease font size")
	if (fontSize > MIN_FONT_SIZE) {
	  fontSize -= 1
	  updateFonts()
	}
  }

  private fun updateFonts() {
	children.filterIsInstance<Text>().forEach {
	  it.font = it.font.fixed().copy(size = fontSize).fx()
	}
  }


  private var currentLine = OutputText()
  private var separatorText = takesInput ifTrueOrNull {
	SepText().also { sepText ->
	  val blink = timeline {
		isAutoReverse = true
		cycleCount = Timeline.INDEFINITE
		keyframe(0.ms.toFXDuration()) {
		  keyvalue(sepText.fillProperty.createWritableFXPropWrapper(), PROMPT_COLOR)
		}
		keyframe(350.ms.toFXDuration()) {
		  keyvalue(sepText.fillProperty.createWritableFXPropWrapper(), Color.BLUE)
		}
	  }
	  val shouldBlink = sceneProperty.deepBinding { it?.windowProperty ?: VarProp(null) }.deepBinding {
		it?.focusedProperty ?: VarProp(false)
	  }
	  if (shouldBlink.value) blink.play()
	  shouldBlink.onChange {
		if (it) {
		  blink.play()
		} else {
		  blink.stop()
		  sepText.fill = PROMPT_COLOR
		}
	  }


	}
  }
  private var unsentInputText = takesInput ifTrueOrNull { InputText() }
  fun prepStoredInput() = unsentInputText!!.text + "\n"

  private fun isReallyFocused() = scene?.window?.focused ?: false

  companion object {
	private val NEWLINES = listOf('\n', '\r')
	private const val BUFFER_NUM_LINES = 1000


	/*init {
	  every(350.ms,ownTimer = true) {

	  }
	}*/

	private val timer = AccurateTimer(this::class.simpleName!!)

  }


  //  private var blink = false
  //  private var myWindow: Stage? = null
  //	set(value) {
  //
  //
  //	  if (field != null && value != null) err("too many listeners")
  //	  field = value
  //	  try {
  //		/*javafx internals sometimes make the focused property null. don't know why*/
  //		value?.focusedProperty()?.onNonNullChange { it: Boolean ->
  ////		  if (it == null) err("here it is")
  //		  if (it && takesInput) {
  //			separatorText!!.fill = matt.css.Color.BLUE
  //			var toggle = true
  //			daemon {
  //			  while (value.isFocused) {
  //				runLater {
  //				  separatorText!!.fill = if (toggle) matt.css.Color.BLUE else PROMPT_COLOR
  //				}
  //				toggle = !toggle
  //				matt.time.dur.sleep(350)
  //			  }
  //			  runLater {
  //				separatorText!!.fill = PROMPT_COLOR
  //			  }
  //
  //			}
  //		  } else {
  //			separatorText!!.fill = PROMPT_COLOR
  //		  }
  //		}
  //	  } catch (e: ArrayIndexOutOfBoundsException) {
  //		e.printStackTrace()
  //		println("it happened again")
  //		println("this has to be an internal java bug and it doesnt really affect me so I'm trying to ignore it")
  //	  }
  //	}

  init {
	add(currentLine)
	if (takesInput) {
	  add(separatorText!!)
	  add(unsentInputText!!)
	}
	//	if (takesInput) {
	//	  daemon {
	//		while (myWindow == null) {
	//		  myWindow = this@ConsoleTextFlow.scene?.window as Stage?
	//		  matt.time.dur.sleep(200)
	//		}
	//	  }
	//	}
  }

  val unsentInput: String
	get() = unsentInputText!!.text

  fun displayInputAsSentAndClearStoredInput() {
	currentLine = OutputText().also { // separator_text is being set to this color!!!
	  it.text = "D1D1D1" // DO    CHANGE   TWO
	  it.text = "\n" //      NOT   THESE    LINES       ... just trust me
	  add(it)
	}

	if (takesInput) {
	  separatorText!!.fill =
		PROMPT_COLOR // if we are keeping this old one, keep it the original color since it may have been blinking

	  separatorText = SepText(newline = false).also {
		it.fill =
		  PROMPT_COLOR // for some reason this needs to be set again. I suspect the css styling is overwriting the setting from below?
		//            style = style + -fx-text-fill: #00ff00;
		add(it)
	  }
	  unsentInputText = InputText().also {
		add(it)
	  }
	  separatorText!!.fill = PROMPT_COLOR // matt.log.level.getDEBUG, DIDN'T WORK
	}
  }

  fun displayAndHoldNewUnsentInputChar(c: String) {
	unsentInputText!!.text += c
  }

  fun deleteAnInputCharIfPossible() {
	if (unsentInputText!!.text.isNotEmpty()) {
	  unsentInputText!!.text = unsentInputText!!.text.substring(0, unsentInputText!!.text.length - 1)
	}
  }

  fun setInputToMem(up: String) {
	unsentInputText!!.text = up
  }


  private var reachedBufferNumLines = false
  fun displayNewText(newText: String) {
	newText.forEach { c ->
	  currentLine.text = currentLine.text + c
	  if (c in NEWLINES) {
		currentLine = OutputText().also {

		  children.add(
			if (takesInput) children.size - 2 else children.size,
			it
		  )
		}
		checkLineBuffer()
	  }
	}
  }

  private fun checkLineBuffer() {
	if (reachedBufferNumLines) {
	  children.removeAt(0)
	} else {
	  reachedBufferNumLines = children.size >= BUFFER_NUM_LINES
	}
  }

  fun clearStoredAndDisplayedInput() {
	unsentInputText!!.text = ""
  }

  fun clearOutputAndStoredInput() {
	if (takesInput) {
	  children.removeAllButLastN(3)
	  currentLine = OutputText().also { children.add(children.size - 2, it) }
	  clearStoredAndDisplayedInput()
	} else {
	  children.removeAllButLastN(1)
	  currentLine = OutputText().also { children.add(children.size, it) }
	}
	reachedBufferNumLines = false
  }

  private fun SepText(newline: Boolean = true) = TextWrapper().apply {
	applyConsoleStyle(
	  size = this@ConsoleTextFlow.fontSize,
	  color = PROMPT_COLOR
	)
	text = if (newline) "\n$PROMPT" else PROMPT
  }

  private fun InputText() = TextWrapper().applyConsoleStyle(size = fontSize, color = Color.LIGHTBLUE)
  private fun OutputText() = TextWrapper().applyConsoleStyle(size = fontSize, color = Color.YELLOW)


  private var fontSize = 12.0


  init {
	updateFonts() /*yes, this is necessary. Severe bugs without this. I don't know why.*/
  }

}


