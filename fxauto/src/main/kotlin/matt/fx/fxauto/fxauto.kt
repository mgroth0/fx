package matt.fx.fxauto

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

fun MContextMenuBuilder.actionitem(action: Action, op: MenuItemWrapper<*>.()->Unit = {}) = actionitem(action.name) {
  action.op()
}.op()
