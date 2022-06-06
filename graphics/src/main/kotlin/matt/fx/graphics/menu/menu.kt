package matt.fx.graphics.menu

import javafx.scene.control.ContextMenu
import javafx.scene.control.Menu
import matt.hurricanefx.tornadofx.menu.item


fun Menu.actionitem(s: String, op: () -> Unit) {
  item(s) {
	setOnAction {
	  op()
	}
  }
}



fun ContextMenu.actionitem(s: String, op: () -> Unit) {
  item(s) {
	setOnAction {
	  op()
	}
  }
}