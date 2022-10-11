package matt.fx.graphics.wrapper.node.shape.threed.mesh

import javafx.scene.shape.MeshView
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.NodeWrapperImpl

class MeshViewWrapper(
  node: MeshView = MeshView(),
): NodeWrapperImpl<MeshView>(node) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}
