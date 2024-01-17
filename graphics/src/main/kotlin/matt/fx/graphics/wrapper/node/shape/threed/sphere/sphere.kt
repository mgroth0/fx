package matt.fx.graphics.wrapper.node.shape.threed.sphere

import javafx.scene.shape.Sphere
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl

class SphereWrapper(
  node: Sphere = Sphere(),
): NodeWrapperImpl<Sphere>(node) {
  override fun addChild(child: NodeWrapper, index: Int?) {
    TODO()
  }


}
