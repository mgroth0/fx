package matt.fx.graphics.wrapper.node.shape.threed.cylinder

import javafx.scene.shape.Cylinder
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl

class CylinderWrapper(
  node: Cylinder = Cylinder(),
): NodeWrapperImpl<Cylinder>(node) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO()
  }


}
