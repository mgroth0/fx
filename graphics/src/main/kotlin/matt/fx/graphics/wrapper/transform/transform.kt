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
import matt.lang.classname.JvmQualifiedClassName
import matt.lang.classname.jvmQualifiedClassName
import matt.lang.assertions.require.requireEquals

abstract class TransformWrapper<E : Transform>(node: E) : SingularEventTargetWrapper<E>(node) {
    final override val properties: ObservableMap<Any, Any?>
        get() = TODO()

    final override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }

    final override fun removeFromParent() {
        TODO()
    }

    final override fun isInsideRow(): Boolean {
        TODO()
    }
}

class RotateWrapper(node: Rotate = Rotate()) : TransformWrapper<Rotate>(node)
class TranslateWrapper(node: Translate = Translate()) : TransformWrapper<Translate>(node)
class ShearWrapper(node: Shear = Shear()) : TransformWrapper<Shear>(node)
class AffineWrapper(node: Affine = Affine()) : TransformWrapper<Affine>(node)
class ScaleWrapper(node: Scale = Scale()) : TransformWrapper<Scale>(node)

class ImmutableTransformWrapper(node: Transform) : TransformWrapper<Transform>(node) {
    companion object {
        val JFX_QNAME = JvmQualifiedClassName("javafx.scene.transform.Transform.ImmutableTransform")
    }

    init {
        requireEquals(node::class.jvmQualifiedClassName, JFX_QNAME)
    }
}