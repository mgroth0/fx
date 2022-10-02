package matt.fx.graphics.wrapper.node.parent

import javafx.beans.property.ReadOnlyObjectProperty
import javafx.scene.Parent
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.wrapper.obs.collect.createImmutableWrapper
import matt.fx.graphics.wrapper.node.line.poly.PolylineWrapper
import matt.fx.graphics.wrapper.node.line.quad.QuadCurveWrapper
import matt.fx.graphics.wrapper.node.shape.poly.PolygonWrapper
import matt.obs.col.olist.mappedlist.toMappedList
import matt.fx.graphics.service.wrapped

interface ParentWrapper<C: NodeWrapper>: NodeWrapper {
  override val node: Parent

  fun polygon(vararg points: Number, op: PolygonWrapper.()->Unit = {}) =
	PolygonWrapper(*points.map(Number::toDouble).toDoubleArray()).attachTo(this, op)

  fun polyline(vararg points: Number, op: PolylineWrapper.()->Unit = {}) =
	PolylineWrapper(*points.map(Number::toDouble).toDoubleArray()).attachTo(this, op)

  fun quadcurve(
	startX: Number = 0.0,
	startY: Number = 0.0,
	controlX: Number = 0.0,
	controlY: Number = 0.0,
	endX: Number = 0.0,
	endY: Number = 0.0,
	op: QuadCurveWrapper.()->Unit = {}
  ) =
	QuadCurveWrapper(
	  startX.toDouble(), startY.toDouble(), controlX.toDouble(), controlY.toDouble(), endX.toDouble(), endY.toDouble()
	).attachTo(this, op)



  fun addr(child: C, op: (C.()->Unit)? = null): C {
	op?.invoke(child)
	add(child)
	return child
  }

}

abstract class ParentWrapperImpl<out N: Parent, C: NodeWrapper>(node: N): NodeWrapperImpl<N>(node), ParentWrapper<C> {


  val childrenUnmodifiable by lazy {
	@Suppress("UNCHECKED_CAST")
	node.childrenUnmodifiable.createImmutableWrapper().toMappedList { it.wrapped() as C }
  }

  fun requestLayout() = node.requestLayout()

}

val NodeWrapper.parent get() : ParentWrapper<*>? = node.parent?.wrapped() as ParentWrapper<*>?

fun NodeWrapper.parentProperty(): ReadOnlyObjectProperty<Parent> = node.parentProperty()


//@Suppress("UNCHECKED_CAST", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
//private fun Parent.getChildrenReflectively(): MutableList<Node>? {
//    val getter = this.javaClass.findMethodByName("getChildren")
//    if (getter != null && java.util.List::class.java.isAssignableFrom(getter.returnType)) {
//        getter.isAccessible = true
//        return getter.invoke(this) as MutableList<Node>
//    }
//    return null
//}

