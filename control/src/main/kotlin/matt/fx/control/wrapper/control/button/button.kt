package matt.fx.control.wrapper.control.button

import javafx.scene.control.Button
import matt.fx.control.inter.graphic
import matt.fx.control.wrapper.control.button.base.ButtonBaseWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.onHover
import matt.fx.graphics.wrapper.style.FXColor
import matt.fx.graphics.wrapper.style.hex
import matt.lang.NEVER
import matt.obs.bindings.str.ObsS


fun ET.button(
    text: ObsS, graphic: NodeWrapper? = null, op: ButtonWrapper.()->Unit = {}
) = ButtonWrapper().attachTo(this, op) {
    it.textProperty.bind(text)
    if (graphic != null) it.graphic = graphic
}

// Buttons
fun ET.button(
    text: String = "", graphic: NodeWrapper? = null, op: ButtonWrapper.()->Unit = {}
): ButtonWrapper = ButtonWrapper().apply {
    this.text = text
    if (graphic != null) this.graphic = graphic
    apply(op)    //	op()
}.attachTo(this, op)


open class ButtonWrapper(
    node: Button = Button(),
): ButtonBaseWrapper<Button>(node) {

    constructor(text: String?, graphic: NodeWrapper? = null): this(Button(text, graphic?.node))

    var op: ()->Unit
        set(value) {
            setOnAction {
                value()
            }
        }
        get() = NEVER

    fun disable() {
        node.isDisable = true
    }

    fun enable() {
        node.isDisable = false
    }

    fun doMyOwnBackgroundStuff(
        hoverColor: FXColor,
        clickColor: FXColor
    ) {
        style = "-fx-background-color: transparent;"


        var mouseIsPressed = false
        fun updateBackground() {
            style = if (mouseIsPressed) {
                "-fx-background-color: ${clickColor.hex()};"
            } else if (hoverProperty.value) {
                "-fx-background-color: ${hoverColor.hex()};"
            } else {
                "-fx-background-color: transparent;"
            }
        }


        onHover {
            updateBackground()
        }

        setOnMousePressed {
            mouseIsPressed = true
            updateBackground()
        }

        setOnMouseReleased {
            mouseIsPressed = false
            updateBackground()
        }
    }


}

