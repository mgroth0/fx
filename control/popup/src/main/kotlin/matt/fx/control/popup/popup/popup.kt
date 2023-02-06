package matt.fx.control.popup.popup

import javafx.stage.Popup
import matt.fx.control.popup.popwinwrap.PopupWindowWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper

class PopupWrapper(node: Popup): PopupWindowWrapper<Popup>(node) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}