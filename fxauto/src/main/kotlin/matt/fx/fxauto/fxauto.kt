package matt.fx.fxauto

import matt.auto.AutoAction
import matt.auto.jumpToSource
import matt.file.MFile
import matt.fx.control.wrapper.menu.item.MenuItemWrapper
import matt.fx.graphics.clip.copyToClipboard
import matt.fx.graphics.wrapper.node.NW
import matt.gui.menu.context.MContextMenuBuilder
import matt.gui.menu.context.mcontextmenu
import kotlin.concurrent.thread

fun MFile.fxActions() = listOf(AutoAction("copy full path") {
  absolutePath.copyToClipboard()
}, AutoAction("copy as file") {
  copyToClipboard()
})

fun MContextMenuBuilder.actionitem(action: AutoAction, op: MenuItemWrapper<*>.()->Unit = {}) = actionitem(action.name) {
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
