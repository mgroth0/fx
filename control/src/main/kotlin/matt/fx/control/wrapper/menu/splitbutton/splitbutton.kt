package matt.fx.control.wrapper.menu.splitbutton

import javafx.scene.control.SplitMenuButton
import matt.fx.control.wrapper.menu.button.MenuButtonWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.ET

fun ET.splitmenubutton(
  text: String? = null, graphic: NodeWrapper? = null, op: SplitMenuButtonWrapper.()->Unit = {}
) = SplitMenuButtonWrapper().attachTo(this, op) {
  if (text != null) it.text = text
  if (graphic != null) it.graphic = graphic
}

class SplitMenuButtonWrapper(
   node: SplitMenuButton = SplitMenuButton(),
): MenuButtonWrapper(node) {
  companion object {
	fun SplitMenuButton.wrapped() = SplitMenuButtonWrapper(this)
  }

}