package matt.fx.control.wrapper.chart.axis

import javafx.scene.chart.Axis
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.graphics.wrapper.region.RegionWrapperImpl

typealias MAxis<T> = AxisWrapper<T, out Axis<T>>

abstract class AxisWrapper<T, N: Axis<T>>(node: N): RegionWrapperImpl<N, NodeWrapper>(node) {
  val autoRangingProperty by lazy { node.autoRangingProperty().toNonNullableProp() }
  var isAutoRanging by autoRangingProperty
  val tickMarkVisibleProperty by lazy { node.tickMarkVisibleProperty().toNonNullableProp() }
  var isTickMarkVisible by tickMarkVisibleProperty

  val tickLabelsVisibleProperty by lazy { node.tickLabelsVisibleProperty().toNonNullableProp() }
  var isTickLabelsVisible by tickLabelsVisibleProperty
  val animatedProperty by lazy { node.animatedProperty().toNonNullableProp() }
  var animated by animatedProperty
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}