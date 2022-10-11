package matt.fx.control.wrapper.tooltip

import javafx.scene.control.Tooltip
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.window.WindowWrapper

class TooltipWrapper(node: Tooltip): WindowWrapper<Tooltip>(node) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}