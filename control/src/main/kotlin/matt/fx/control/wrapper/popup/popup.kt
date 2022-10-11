package matt.fx.control.wrapper.popup

import javafx.stage.Popup
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.window.WindowWrapper

class PopupWrapper(node: Popup): WindowWrapper<Popup>(node) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}