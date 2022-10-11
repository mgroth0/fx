package matt.fx.graphics.wrapper.transform

import javafx.collections.ObservableMap
import javafx.scene.transform.Affine
import javafx.scene.transform.Rotate
import javafx.scene.transform.Scale
import javafx.scene.transform.Shear
import javafx.scene.transform.Transform
import javafx.scene.transform.Translate
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper

abstract class TransformWrapper(node: Transform): SingularEventTargetWrapper<Transform>(node) {
  override val properties: ObservableMap<Any, Any?>
	get() = TODO("Not yet implemented")

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }

  override fun removeFromParent() {
	TODO("Not yet implemented")
  }

  override fun isInsideRow(): Boolean {
	TODO("Not yet implemented")
  }
}

class RotateWrapper(node: Rotate): TransformWrapper(node)
class TranslateWrapper(node: Translate): TransformWrapper(node)
class ShearWrapper(node: Shear): TransformWrapper(node)
class AffineWrapper(node: Affine): TransformWrapper(node)
class ScaleWrapper(node: Scale): TransformWrapper(node)
