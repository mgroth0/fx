package matt.fx.control.wrapper.menu.splitbutton

import javafx.scene.control.SplitMenuButton
import matt.hurricanefx.wrapper.menu.button.MenuButtonWrapper

class SplitMenuButtonWrapper(
   node: SplitMenuButton = SplitMenuButton(),
): MenuButtonWrapper(node) {
  companion object {
	fun SplitMenuButton.wrapped() = SplitMenuButtonWrapper(this)
  }

}