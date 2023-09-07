package matt.fx.node.console.text

import javafx.animation.Animation.Status.STOPPED
import javafx.animation.Timeline
import javafx.scene.Cursor.HAND
import javafx.scene.paint.Color
import javafx.scene.paint.Color.YELLOW
import javafx.scene.text.Font
import matt.auto.macapp.Idea
import matt.color.rgb
import matt.fx.base.time.toFXDuration
import matt.fx.base.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.graphics.anim.animation.keyframe
import matt.fx.graphics.anim.animation.timeline
import matt.fx.graphics.font.fixed
import matt.fx.graphics.lang.removeAllButLastN
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.onLeftClick
import matt.fx.graphics.wrapper.style.FXColor
import matt.fx.graphics.wrapper.style.toFXColor
import matt.fx.graphics.wrapper.text.TextWrapper
import matt.fx.graphics.wrapper.text.textlike.DarkLightFXColor
import matt.fx.graphics.wrapper.text.textlike.MONO_FONT
import matt.fx.graphics.wrapper.text.textlike.highlightOnHover
import matt.fx.graphics.wrapper.textflow.TextFlowWrapper
import matt.fx.node.console.text.parseterm.StackTraceLine
import matt.fx.node.console.text.parseterm.TransformableOutput
import matt.fx.node.console.text.parseterm.parseTerminalOutput
import matt.lang.function.Op
import matt.lang.ifTrueOrNull
import matt.model.data.message.OpenSpecific
import matt.obs.bind.binding
import matt.obs.bind.deepBinding
import matt.obs.bindings.bool.and
import matt.obs.bindings.comp.gt
import matt.obs.bindings.comp.lt
import matt.obs.listen.whenEqualsOnce
import matt.obs.prop.BindableProperty
import matt.obs.prop.ObsVal
import matt.obs.prop.VarProp
import matt.time.dur.ms

private const val PROMPT = "> "
private val PROMPT_COLOR: Color = Color.GREEN
private val INPUT_COLOR = rgb(100, 100, 255).toFXColor()
private val CLICKABLE_COLOR: Color = Color.BLUE
private val HOVER_COLOR = DarkLightFXColor(
    darkColor = Color.LIGHTBLUE, lightColor = Color.DARKBLUE
)
private val NEWLINES = listOf('\n', '\r')

class ConsoleTextFlow(
    private val takesInput: Boolean = true,
    private val maxLines: Int?
) : TextFlowWrapper<ConsoleBlock>() {

    companion object {
        private const val MAX_FONT_SIZE = 50.0
        private const val MIN_FONT_SIZE = 5.0
        private val DEFAULT_FONT = MONO_FONT.fixed().copy(size = 12.0).fx()
    }

    private var currentLineCount = 0

    private val font = BindableProperty(DEFAULT_FONT)
    private val fontSize = font.binding { it.size }

    val canIncreaseFontSize by lazy {
        fontSize lt MAX_FONT_SIZE
    }
    val canDecreaseFontSize by lazy {
        fontSize gt MIN_FONT_SIZE
    }

    fun hasText() =
        unsentInput.isNotBlank() || children.asSequence().filterIsInstance<OutputText>().any { it.text.isNotBlank() }


    fun tryIncreaseFontSize() {
        if (canIncreaseFontSize.value) {
            font.value = font.value.fixed().withFontSizeAdjustedBy(1).fx()
        }
    }

    fun tryDecreaseFontSize() {
        if (canDecreaseFontSize.value) {
            font.value = font.value.fixed().withFontSizeAdjustedBy(-1).fx()
        }
    }

    private var currentLine = OutputText()
    private var blinkRemote: BlinkRemote? = null
    private var separatorText = takesInput ifTrueOrNull {
        SepText().also {
            blinkRemote = it.blink()
        }
    }
    private var unsentInputText = takesInput ifTrueOrNull { InputText() }
    fun prepStoredInput() = unsentInputText!!.text + "\n"


    init {
        add(currentLine)
        currentLineCount++
        if (takesInput) {
            add(separatorText!!)
            add(unsentInputText!!)
        }
    }

    val unsentInput: String
        get() = unsentInputText!!.text

    fun displayInputAsSentAndClearStoredInput() {

        blinkRemote!!.stop()
        currentLine = OutputText().also {
            it.text = "\n"
            add(it)
        }
        if (takesInput) {
            separatorText = SepText(newline = false).also {
                blinkRemote = it.blink()
                add(it)
            }
            unsentInputText = InputText().also {
                add(it)
            }
        }
        currentLineCount++
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


    fun displayNewText(newText: String) {
        newText.forEach { c ->
            currentLine.text += c
            if (c in NEWLINES) {
                val finishedLine = currentLine
                val finishedLineText = currentLine.text
                val transformableParts = parseTerminalOutput(finishedLineText)
                if (transformableParts.isNotEmpty()) {
                    finishedLine.removeFromParent()
                    var nextIndexToAdd = if (takesInput) children.size - 2 else children.size

                    val partItr = transformableParts.iterator()
                    val firstPart = partItr.next()
                    fun maybeAddBasicText(text: String) {
                        if (text.isNotEmpty()) {
                            children.add(nextIndexToAdd++, OutputText(text))
                        }
                    }

                    fun TransformableOutput.add() {
                        children.add(nextIndexToAdd++, toNode())
                    }

                    maybeAddBasicText(finishedLineText.substring(0, firstPart.indices.first))
                    firstPart.add()
                    var lastPart = firstPart
                    while (partItr.hasNext()) {
                        val nextPart = lastPart
                        maybeAddBasicText(finishedLineText.substring(lastPart.indices.last + 1, nextPart.indices.first))
                        nextPart.add()
                        lastPart = nextPart
                    }
                    maybeAddBasicText(finishedLineText.substring(lastPart.indices.last + 1))
                }
                currentLine = OutputText().also {
                    children.add(
                        if (takesInput) children.size - 2 else children.size, it
                    )
                }
                currentLineCount++
                checkLineBuffer()
            }
        }
    }

    private fun TransformableOutput.toNode() = when (this) {
        is StackTraceLine -> ClickableText(text) {
            Idea.open(
                OpenSpecific(
                    qualifiedName = qualifiedName, fileName = fileName, lineNumber = lineNumber
                )
            )
        }
    }

    private fun checkLineBuffer() {
        if (maxLines != null) {
            if (currentLineCount > maxLines) {
                do {
                    val removed = children.removeAt(0)
                } while (!removed.isEndOfLine)
                currentLineCount--
            }
        }
    }

    fun clearStoredAndDisplayedInput() {
        unsentInputText!!.text = ""
    }

    fun clearOutputAndStoredInput() {
        if (takesInput) {
            children.removeAllButLastN(2)
            currentLine = OutputText().also { children.add(0, it) }
            clearStoredAndDisplayedInput()
        } else {
            children.clear()
            currentLine = OutputText().also { children.add(it) }
        }
        currentLineCount = 1
    }

    private fun SepText(newline: Boolean = true) = SepBlock(this@ConsoleTextFlow.font, newline = newline)
    private fun InputText() = InputBlock(this@ConsoleTextFlow.font)
    private fun OutputText(text: String? = null) = BasicOutputText(this@ConsoleTextFlow.font).apply {
        if (text != null) {
            this.text = text
        }
    }

    private fun ClickableText(
        text: String,
        op: Op
    ) = ClickableOutputText(this@ConsoleTextFlow.font, op).apply {
        this.text = text
    }


}


private abstract class OutputText(
    font: ObsVal<Font>,
    color: FXColor
) : BaseText(font, color), ConsoleBlock

private class BasicOutputText(font: ObsVal<Font>) : OutputText(font, YELLOW), ConsoleBlock
private class ClickableOutputText(
    font: ObsVal<Font>,
    action: Op
) : OutputText(font, CLICKABLE_COLOR), ConsoleBlock {

    init {
        highlightOnHover(
            hoverColor = HOVER_COLOR, nonHoverColor = CLICKABLE_COLOR
        )
        cursor = HAND
        onLeftClick {
            action()
        }
    }
}


//sealed interface OutputSubBlock : NodeWrapper

private class SepBlock(
    font: ObsVal<Font>,
    newline: Boolean
) : BaseText(font, PROMPT_COLOR), ConsoleBlock {
    init {
        text = if (newline) "\n$PROMPT" else PROMPT
    }
}

private class InputBlock(font: ObsVal<Font>) : BaseText(font, INPUT_COLOR), ConsoleBlock


sealed interface ConsoleBlock : NodeWrapper {
    val isEndOfLine: Boolean
}


private abstract class BaseText(
    font: ObsVal<Font>,
    color: FXColor
) : TextWrapper(), ConsoleBlock {
    override val isEndOfLine get() = text.last() in NEWLINES

    init {
        fontProperty.bindWeakly(font)
        fillViaStyleSinceThereIsSomeBug = color
    }
}


class BlinkRemote(val stop: Op)

private fun TextWrapper.blink(): BlinkRemote {
    val theTimeline = timeline {
        isAutoReverse = true
        cycleCount = Timeline.INDEFINITE
        val fillProp = node.fillProperty()
        keyframe(0.ms.toFXDuration()) {
            keyvalue(fillProp, PROMPT_COLOR)
        }
        keyframe(350.ms.toFXDuration()) {
            keyvalue(fillProp, Color.BLUE)
        }
    }
    val remoteEnabled = BindableProperty(true)
    val preShouldBlink = sceneProperty.deepBinding { it?.windowProperty ?: VarProp(null) }
    val shouldBlink = preShouldBlink.deepBinding {
        it?.focusedProperty ?: VarProp(false)
    }
    val reallyShouldBlink = shouldBlink and remoteEnabled
    if (reallyShouldBlink.value) theTimeline.play()
    val listener = reallyShouldBlink.onChange {
        if (it) theTimeline.play()
        else {
            theTimeline.stop()
            theTimeline.statusProperty().toNonNullableROProp().whenEqualsOnce(STOPPED) {
                /*ironically, if I use fillViaStyleSinceThereIsSomeBug here it will not work*/
                fill = PROMPT_COLOR
            }
        }
    }
    return BlinkRemote(stop = {
        remoteEnabled.value = false
        listener.removeListener()
        shouldBlink.removeAllDependencies()
        preShouldBlink.removeAllDependencies()
    })
}

