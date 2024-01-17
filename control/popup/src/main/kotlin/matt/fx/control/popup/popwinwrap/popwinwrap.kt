package matt.fx.control.popup.popwinwrap

import javafx.stage.PopupWindow
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.window.WindowWrapper

open class PopupWindowWrapper<W: PopupWindow>(node: W): WindowWrapper<W>(node) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO()
  }
}