package matt.fx.control.wrapper.chart.axis.value

import javafx.scene.chart.ValueAxis
import matt.fx.control.wrapper.chart.axis.AxisWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp


abstract class ValueAxisWrapper<T: Number>(node: ValueAxis<T>): AxisWrapper<T, ValueAxis<T>>(node) {
  val minorTickCountProperty by lazy { node.minorTickCountProperty().toNonNullableProp() }
  var minorTickCount by minorTickCountProperty
  val lowerBoundProperty by lazy { node.lowerBoundProperty().toNonNullableProp().cast<Double>() }
  var lowerBound by lowerBoundProperty
  val upperBoundProperty by lazy { node.upperBoundProperty().toNonNullableProp().cast<Double>() }
  var upperBound by upperBoundProperty
  val tickLabelFormatterProperty by lazy { node.tickLabelFormatterProperty().toNullableProp() }
  var tickLabelFormatter by tickLabelFormatterProperty
}