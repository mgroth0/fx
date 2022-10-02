package matt.fx.control.lang

import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.control.Control
import matt.fx.control.wrapper.control.button.ButtonWrapper
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper

fun actionbutton(text: String = "", graphic: NodeWrapper? = null, action: ButtonWrapper.(ActionEvent)->Unit) =
  ButtonWrapper(text, graphic).apply {
	setOnAction {
	  action(it)
	  it.consume()
	}
  }

fun NodeWrapper.actionbutton(
  text: String = "",
  graphic: NodeWrapper? = null,
  action: ButtonWrapper.(ActionEvent)->Unit
) = ButtonWrapper(text, graphic).apply {
  setOnAction {
	action(it)
	it.consume()
  }
  this@actionbutton.addChild(this)
}

infix fun ButtonWrapper.withAction(newOp: ()->Unit) = this.apply { op = newOp }

fun EventTargetWrapper.removecontextmenu() {
  if (this is Control) {
	contextMenu = null
  } else (this as? Node)?.apply {
	setOnContextMenuRequested {
	}
  }
}