package matt.fx.control.chart.axis.value

import matt.fx.control.chart.axis.AxisWrapper
import matt.fx.control.chart.axis.value.moregenval.MoreGenericValueAxis
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp


abstract class ValueAxisWrapper<T: Any>(node: MoreGenericValueAxis<T>): AxisWrapper<T, MoreGenericValueAxis<T>>(node) {
  private val superNode = node
  val minorTickCountProperty by lazy { node.minorTickCountProperty().toNonNullableProp() }
  var minorTickCount by minorTickCountProperty
  val lowerBoundProperty get() = superNode.lowerBound
  var lowerBound by lowerBoundProperty
  val upperBoundProperty get() = superNode.upperBound
  var upperBound by upperBoundProperty
  val tickLabelFormatterProperty by lazy { node.tickLabelFormatterProperty().toNullableProp() }
  var tickLabelFormatter by tickLabelFormatterProperty
  val minorTickVisibleProperty by lazy { node.minorTickVisibleProperty().toNonNullableProp() }
  var isMinorTickVisible by minorTickVisibleProperty
}

/*abstract class OldValueAxisWrapper<T: Number>(node: ValueAxis<T>): AxisWrapper<T, Value<T>>(node) {
  val minorTickCountProperty by lazy { node.minorTickCountProperty().toNonNullableProp() }
  var minorTickCount by minorTickCountProperty
  *//*val lowerBoundProperty get() = node.lowerBound
  var lowerBound by lowerBoundProperty
  val upperBoundProperty get() = node.upperBound
  var upperBound by upperBoundProperty*//*
  val tickLabelFormatterProperty by lazy { node.tickLabelFormatterProperty().toNullableProp() }
  var tickLabelFormatter by tickLabelFormatterProperty
  val minorTickVisibleProperty by lazy { node.minorTickVisibleProperty().toNonNullableProp() }
  var isMinorTickVisible by minorTickVisibleProperty
}*/







