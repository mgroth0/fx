package matt.fx.control.wrapper.virtualflow.clip

import javafx.scene.layout.Region
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl

const val CLIPPED_CONTAINER_QNAME = "javafx.scene.control.skin.VirtualFlow.ClippedContainer"

class ClippedContainerWrapper(node: Region): RegionWrapperImpl<Region, NodeWrapper>(node) {
  init {
	require(node::class.qualifiedName == CLIPPED_CONTAINER_QNAME) {
	  "this class is reserved for ClippedContainer, but was created with a ${node::class.qualifiedName}"
	}
  }

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}