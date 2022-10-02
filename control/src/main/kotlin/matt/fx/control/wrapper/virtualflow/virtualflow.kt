package matt.fx.control.wrapper.virtualflow

import javafx.scene.control.IndexedCell
import javafx.scene.control.skin.VirtualFlow
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl

class VirtualFlowWrapper<T: IndexedCell<*>>(node: VirtualFlow<T>): RegionWrapperImpl<VirtualFlow<T>, NodeWrapper>(node) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}