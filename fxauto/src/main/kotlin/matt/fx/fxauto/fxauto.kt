package matt.fx.fxauto

import matt.async.thread.namedThread
import matt.auto.AutoAction
import matt.auto.jumpToSource
import matt.file.commons.IdeProject
import matt.fx.control.wrapper.menu.item.MenuItemWrapper
import matt.fx.graphics.clip.copyToClipboard
import matt.fx.graphics.wrapper.node.NW
import matt.gui.menu.context.MContextMenuBuilder
import matt.gui.menu.context.mcontextmenu
import matt.shell.context.ReapingShellExecutionContext

fun matt.file.JioFile.fxActions() = listOf(AutoAction("copy full path") {
    absolutePath.copyToClipboard()
}, AutoAction("copy as file") {
    copyToClipboard()
})

fun MContextMenuBuilder.actionitem(
    action: AutoAction,
    op: MenuItemWrapper<*>.() -> Unit = {}
) = actionitem(action.name) {
    action.op()
}.op()


context(ReapingShellExecutionContext)
fun NW.jumpFromContextMenu() {
    mcontextmenu {
        actionitem("jump to source code of ${this@jumpFromContextMenu::class.simpleName!!}") {
            namedThread("jumpFromContextMenu Thread") {
                with(IdeProject.all) {
                    this@jumpFromContextMenu::class.jumpToSource()
                }

            }
        }
    }
}
