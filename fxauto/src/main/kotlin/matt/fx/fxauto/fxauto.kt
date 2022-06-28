package matt.fx.fxauto

import javafx.scene.control.MenuItem
import matt.auto.Action
import matt.fx.graphics.clip.copyToClipboard
import matt.fx.graphics.menu.context.MContextMenuBuilder
import matt.file.MFile

fun MFile.fxActions() = listOf(
  Action("copy full path") {
	absolutePath.copyToClipboard()
  },
  Action("copy as file") {
	copyToClipboard()
  }
)

fun MContextMenuBuilder.actionitem(action: Action, op: MenuItem.()->Unit = {}) = actionitem(action.name) {
  action.op()
}.op()
