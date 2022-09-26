package matt.fx.graphics.lang

import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.control.Control
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.wrapper.control.button.ButtonWrapper
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.hurricanefx.wrapper.scene.SceneWrapper
import matt.hurricanefx.wrapper.target.EventTargetWrapper
import matt.lang.err
import matt.lang.setAll

fun NodeWrapper.setOnFocusLost(op: ()->Unit) {
  focusedProperty().onChange { it: Boolean? ->
	if (it == null) err("here it is")
	if (!it) {
	  op()
	}
  }
}

fun NodeWrapper.setOnFocusGained(op: ()->Unit) {
  focusedProperty().onChange { it: Boolean? ->
	if (it == null) err("here it is")
	if (it) {
	  op()
	}
  }
}

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

fun <T> MutableList<T>.setToSublist(start: Int, Stop: Int) {
  setAll(subList(start, Stop).toList())
}

fun <T> MutableList<T>.removeAllButLastN(num: Int) {
  val siz = size
  setToSublist(siz - num, siz)
}


@Suppress("unused")
fun SceneWrapper<*>.onDoubleClickConsume(action: ()->Unit) {
  node.setOnMouseClicked {
	if (it.clickCount == 2) {
	  action()
	  it.consume()
	}
  }
}
