package matt.fx.graphics.lang

import javafx.beans.property.StringProperty
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.Control
import javafx.scene.control.MenuItem
import javafx.scene.input.KeyCombination
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.op
import matt.hurricanefx.tornadofx.fx.addChildIfPossible
import matt.hurricanefx.tornadofx.menu.item
import matt.hurricanefx.wrapper.ButtonWrapper
import matt.hurricanefx.wrapper.MenuItemWrapper
import matt.hurricanefx.wrapper.NodeWrapper
import matt.hurricanefx.wrapper.wrapped
import matt.klib.lang.err

fun NodeWrapper<*>.setOnFocusLost(op: ()->Unit) {
  focusedProperty().onChange { it: Boolean? ->
	if (it == null) err("here it is")
	if (!it) {
	  op()
	}
  }
}

fun NodeWrapper<*>.setOnFocusGained(op: ()->Unit) {
  focusedProperty().onChange { it: Boolean? ->
	if (it == null) err("here it is")
	if (it) {
	  op()
	}
  }
}

fun actionbutton(text: String, graphic: Node? = null, action: ButtonWrapper.(ActionEvent)->Unit) = ButtonWrapper(text, graphic).apply {
  setOnAction {
	action(it)
	it.consume()
  }
}

fun NodeWrapper<*>.actionbutton(text: String = "", graphic: Node? = null, action: ButtonWrapper.(ActionEvent)->Unit) =   ButtonWrapper(text, graphic).apply {
  setOnAction {
	action(it)
	it.consume()
  }
  this@actionbutton.addChildIfPossible(this)
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

// because when in listcells, "item" is taken
@Suppress("unused")
fun ContextMenu.menuitem(
  name: String, keyCombination: KeyCombination? = null, graphic: Node? = null, op: MenuItemWrapper.()->Unit = {}
) = item(name, keyCombination, graphic, op)


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


fun <T> ObservableList<T>.setToSublist(start: Int, Stop: Int) {
  setAll(subList(start, Stop).toList())
}

fun <T> ObservableList<T>.removeAllButLastN(num: Int) {
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
