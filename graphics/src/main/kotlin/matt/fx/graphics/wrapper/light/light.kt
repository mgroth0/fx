package matt.fx.graphics.wrapper.light

import javafx.scene.AmbientLight
import javafx.scene.DirectionalLight
import javafx.scene.LightBase
import javafx.scene.PointLight
import javafx.scene.SpotLight
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.NodeWrapperImpl

abstract class LightBaseWrapper(node: LightBase): NodeWrapperImpl<LightBase>(node)

class AmbientLightWrapper(node: AmbientLight): LightBaseWrapper(node) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}

class PointLightWrapper(node: PointLight): LightBaseWrapper(node) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}

class SpotLightWrapper(node: SpotLight): LightBaseWrapper(node) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}

class DirectionalLightWrapper(node: DirectionalLight): LightBaseWrapper(node) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}