package matt.fx.graphics.wrapper.subscene

import javafx.beans.property.ObjectProperty
import javafx.scene.Parent
import javafx.scene.SubScene
import javafx.scene.paint.Paint
import matt.fx.base.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.scenelike.SceneLikeWrapper
import kotlin.reflect.KClass
import kotlin.reflect.cast


class SubSceneWrapper<R: ParentWrapper<*>>(
    node: SubScene,
    val parentCls: KClass<R>
): NodeWrapperImpl<SubScene>(node),
    SceneLikeWrapper<SubScene, R> {



    companion object {
        inline operator fun <reified P: ParentWrapper<*>> invoke(
            root: P,
            userWidth: Double,
            userHeight: Double
        ) = SubSceneWrapper(
            SubScene(root.node, userWidth, userHeight), P::class
        )
    }

    override fun castRoot(nw: NodeWrapper): R = parentCls.cast(nw)

    override val rootProperty: ObjectProperty<Parent> get() = node.rootProperty()
    override val widthProperty get() = node.widthProperty().toNonNullableROProp().cast<Double>(Double::class)
    override val heightProperty get() = node.heightProperty().toNonNullableROProp().cast<Double>(Double::class)
    override val fillProperty: ObjectProperty<Paint> get() = node.fillProperty()
}
