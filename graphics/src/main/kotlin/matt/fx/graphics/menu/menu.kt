package matt.fx.graphics.menu

import javafx.scene.control.ContextMenu
import javafx.scene.control.Menu
import matt.hurricanefx.tornadofx.menu.item
import kotlin.concurrent.thread


fun Menu.actionitem(s: String, threaded: Boolean = false, op: ()->Unit) {
  item(s) {
	setOnAction {
	  if (threaded) thread { op() }
	  else op()
	}
  }
}


fun ContextMenu.actionitem(s: String, threaded: Boolean = false, op: ()->Unit) {
  item(s) {
	setOnAction {
	  if (threaded) thread { op() }
	  else op()
	}
  }
}