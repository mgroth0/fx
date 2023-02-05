package matt.fx.graphics.wrapper.node.parent

import javafx.scene.Parent
import matt.fx.graphics.service.wrapped
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.line.poly.PolylineWrapper
import matt.fx.graphics.wrapper.node.line.quad.QuadCurveWrapper
import matt.fx.graphics.wrapper.node.shape.poly.PolygonWrapper
import matt.hurricanefx.eye.wrapper.obs.collect.list.createImmutableWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.toNullableROProp
import matt.obs.bind.binding
import matt.obs.col.olist.mappedlist.toMappedList

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



  fun <CC: C> addr(child: CC, op: (CC.()->Unit)? = null): CC {
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

fun NodeWrapper.parentProperty() = node.parentProperty().toNullableROProp().binding {
  it?.wrapped()
}


//@Suppress("UNCHECKED_CAST", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
//private fun Parent.getChildrenReflectively(): MutableList<Node>? {
//    val getter = this.javaClass.findMethodByName("getChildren")
//    if (getter != null && java.util.List::class.java.isAssignableFrom(getter.returnType)) {
//        getter.isAccessible = true
//        return getter.invoke(this) as MutableList<Node>
//    }
//    return null
//}


