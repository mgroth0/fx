package matt.fx.control.wrapper.control.button

import javafx.scene.control.Button
import matt.fx.control.wrapper.control.button.base.ButtonBaseWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.lang.NEVER
import matt.fx.graphics.wrapper.ET



fun ET.button(
  text: ValProp<String>, graphic: NodeWrapper? = null, op: ButtonWrapper.()->Unit = {}
) = ButtonWrapper().attachTo(this, op) {
  it.textProperty.bind(text)
  if (graphic != null) it.graphic = graphic
}

// Buttons
fun ET.button(
  text: String = "", graphic: NodeWrapper? = null, op: ButtonWrapper.()->Unit = {}
): ButtonWrapper {
  return ButtonWrapper().apply {
	this.text = text
	if (graphic != null) this.graphic = graphic
	apply(op)    //	op()
  }.attachTo(this, op)
}


open class ButtonWrapper(
   node: Button = Button(),
): ButtonBaseWrapper<Button>(node) {
  companion object {
	fun Button.wrapped() = ButtonWrapper(this)
  }

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
}

