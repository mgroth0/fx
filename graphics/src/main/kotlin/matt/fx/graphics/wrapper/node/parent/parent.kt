package matt.fx.graphics.wrapper.node.parent

import javafx.scene.Parent
import matt.fx.base.wrapper.obs.collect.list.createImmutableWrapper
import matt.fx.base.wrapper.obs.obsval.toNullableROProp
import matt.fx.graphics.service.wrapped
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.line.poly.PolylineWrapper
import matt.fx.graphics.wrapper.node.line.quad.QuadCurveWrapper
import matt.fx.graphics.wrapper.node.shape.poly.PolygonWrapper
import matt.fx.graphics.wrapper.node.tri.Triangle
import matt.lang.anno.Open
import matt.obs.bind.binding
import matt.obs.col.olist.mappedlist.toMappedList
import kotlin.reflect.KClass
import kotlin.reflect.cast

interface ParentWrapper<C : NodeWrapper> : NodeWrapper {
    override val node: Parent

    fun castChild(a: Any): C

    @Open
    fun polygon(vararg points: Number, op: PolygonWrapper.() -> Unit = {}) =
        PolygonWrapper(*points.map(Number::toDouble).toDoubleArray()).attachTo(this, op)

    @Open fun triangle(op: Triangle.() -> Unit = {}) =
        Triangle().attachTo(this, op)

    @Open fun polyline(vararg points: Number, op: PolylineWrapper.() -> Unit = {}) =
        PolylineWrapper(*points.map(Number::toDouble).toDoubleArray()).attachTo(this, op)

    @Open fun quadcurve(
        startX: Number = 0.0,
        startY: Number = 0.0,
        controlX: Number = 0.0,
        controlY: Number = 0.0,
        endX: Number = 0.0,
        endY: Number = 0.0,
        op: QuadCurveWrapper.() -> Unit = {}
    ) =
        QuadCurveWrapper(
            startX.toDouble(),
            startY.toDouble(),
            controlX.toDouble(),
            controlY.toDouble(),
            endX.toDouble(),
            endY.toDouble()
        ).attachTo(this, op)


    @Open fun <CC : C> addr(child: CC, op: (CC.() -> Unit)? = null): CC {
        op?.invoke(child)
        add(child)
        return child
    }
}

abstract class ParentWrapperImpl<out N : Parent, C : NodeWrapper>(
    node: N,
    val childClass: KClass<C>
) :
    NodeWrapperImpl<N>(node),
        ParentWrapper<C> {


    final override fun castChild(a: Any): C = childClass.cast(a)



    val childrenUnmodifiable by lazy {
        node.childrenUnmodifiable.createImmutableWrapper().toMappedList {
            childClass.cast(it.wrapped())
        }
    }

    fun requestLayout() = node.requestLayout()
}

val NodeWrapper.parent get() : ParentWrapper<*>? = node.parent?.wrapped() as ParentWrapper<*>?

fun NodeWrapper.parentProperty() =
    node.parentProperty().toNullableROProp().binding {
        it?.wrapped()
    }




