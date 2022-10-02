package matt.fx.control.wrapper.control.button

import javafx.scene.control.Button
import matt.fx.control.wrapper.control.button.base.ButtonBaseWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.lang.NEVER

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

