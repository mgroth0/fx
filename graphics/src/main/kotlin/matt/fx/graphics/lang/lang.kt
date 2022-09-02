package matt.fx.graphics.lang

import javafx.beans.property.StringProperty
import javafx.event.ActionEvent
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Control
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.wrapper.control.button.ButtonWrapper
import matt.hurricanefx.wrapper.node.NodeWrapperImpl
import matt.klib.lang.err
import matt.klib.lang.setAll

fun NodeWrapperImpl<*>.setOnFocusLost(op: ()->Unit) {
  focusedProperty().onChange { it: Boolean? ->
	if (it == null) err("here it is")
	if (!it) {
	  op()
	}
  }
}

fun NodeWrapperImpl<*>.setOnFocusGained(op: ()->Unit) {
  focusedProperty().onChange { it: Boolean? ->
	if (it == null) err("here it is")
	if (it) {
	  op()
	}
  }
}

fun actionbutton(text: String, graphic: Node? = null, action: ButtonWrapper.(ActionEvent)->Unit) =
  ButtonWrapper(text, graphic).apply {
	setOnAction {
	  action(it)
	  it.consume()
	}
  }

fun NodeWrapperImpl<*>.actionbutton(
  text: String = "",
  graphic: Node? = null,
  action: ButtonWrapper.(ActionEvent)->Unit
) = ButtonWrapper(text, graphic).apply {
  setOnAction {
	action(it)
	it.consume()
  }
  this@actionbutton.addChild(this)
}

infix fun ButtonWrapper.withAction(newOp: ()->Unit) = this.apply { op = newOp }

fun EventTarget.removecontextmenu() {
  if (this is Control) {
	contextMenu = null
  } else (this as? Node)?.apply {
	setOnContextMenuRequested {
	}
  }
}


@Suppress("unused")
fun StringProperty.appendln(s: String) {
  append("\n" + s)
}

fun StringProperty.append(s: String) {
  set(get() + s)
}

@Suppress("unused")
fun StringProperty.append(c: Char) {
  set(get() + c)
}


fun <T> MutableList<T>.setToSublist(start: Int, Stop: Int) {
  setAll(subList(start, Stop).toList())
}

fun <T> MutableList<T>.removeAllButLastN(num: Int) {
  val siz = size
  setToSublist(siz - num, siz)
}


@Suppress("unused")
fun Scene.onDoubleClickConsume(action: ()->Unit) {
  setOnMouseClicked {
	if (it.clickCount == 2) {
	  action()
	  it.consume()
	}
  }
}
