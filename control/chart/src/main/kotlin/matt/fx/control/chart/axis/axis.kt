package matt.fx.control.chart.axis

import matt.fx.control.chart.axis.value.axis.AxisForPackagePrivateProps
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp

typealias MAxis<T> = AxisWrapper<T, out AxisForPackagePrivateProps<T>>

abstract class AxisWrapper<T, N: AxisForPackagePrivateProps<T>>(node: N): RegionWrapperImpl<N, NodeWrapper>(node) {
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