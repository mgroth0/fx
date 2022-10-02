package matt.fx.control.wrapper.chart.axis.value.number

import javafx.scene.chart.NumberAxis
import matt.fx.control.wrapper.chart.axis.value.ValueAxisWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp

fun minimalNumberAxis() = NumberAxisWrapper().apply {
  minorTickCount = 0
  isAutoRanging = false
  isTickMarkVisible = false
  isTickLabelsVisible = false
}

class NumberAxisWrapper(node: NumberAxis = NumberAxis()): ValueAxisWrapper<Number>(node) {
  val tickUnitProperty by lazy { node.tickUnitProperty().toNonNullableProp() }
  var tickUnit by tickUnitProperty

  val minorTickVisibleProperty by lazy { node.minorTickVisibleProperty().toNonNullableProp() }
  var isMinorTickVisible by minorTickVisibleProperty


}