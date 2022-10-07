package matt.fx.fxauto

import matt.auto.Action
import matt.auto.jumpToSource
import matt.file.MFile
import matt.fx.control.menu.context.MContextMenuBuilder
import matt.fx.control.menu.context.mcontextmenu
import matt.fx.control.wrapper.menu.item.MenuItemWrapper
import matt.fx.graphics.clip.copyToClipboard
import matt.fx.graphics.wrapper.node.NW
import kotlin.concurrent.thread

fun MFile.fxActions() = listOf(Action("copy full path") {
  absolutePath.copyToClipboard()
}, Action("copy as file") {
  copyToClipboard()
})

fun MContextMenuBuilder.actionitem(action: Action, op: MenuItemWrapper<*>.()->Unit = {}) = actionitem(action.name) {
  action.op()
}.op()


fun NW.jumpFromContextMenu() {
  mcontextmenu {
	actionitem("jump to source code of ${this@jumpFromContextMenu::class.simpleName!!}") {
	  thread {
		this@jumpFromContextMenu::class.jumpToSource()
	  }
	}
  }
}
