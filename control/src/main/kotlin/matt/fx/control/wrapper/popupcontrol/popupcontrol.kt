package matt.fx.control.wrapper.popupcontrol

import javafx.scene.control.PopupControl
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.window.WindowWrapper

class PopupControlWrapper(node: PopupControl): WindowWrapper<PopupControl>(node) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}