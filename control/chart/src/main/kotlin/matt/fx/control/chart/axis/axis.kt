package matt.fx.control.chart.axis

import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.control.chart.axis.value.axis.AxisForPackagePrivateProps
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl

typealias MAxis<T> = AxisWrapper<T, out AxisForPackagePrivateProps<T>>

abstract class AxisWrapper<T, N : AxisForPackagePrivateProps<T>>(node: N) : RegionWrapperImpl<N, NodeWrapper>(node, childClass = NodeWrapper::class) {
    val autoRangingProperty: NonNullFXBackedBindableProp<Boolean> by lazy { node.autoRangingProperty().toNonNullableProp() }
    var isAutoRanging by autoRangingProperty
    val tickMarkVisibleProperty: NonNullFXBackedBindableProp<Boolean> by lazy { node.tickMarkVisibleProperty().toNonNullableProp() }
    var isTickMarkVisible by tickMarkVisibleProperty

    val tickLabelsVisibleProperty: NonNullFXBackedBindableProp<Boolean> by lazy { node.tickLabelsVisibleProperty().toNonNullableProp() }
    var isTickLabelsVisible by tickLabelsVisibleProperty
    val animatedProperty: NonNullFXBackedBindableProp<Boolean> by lazy { node.animatedProperty().toNonNullableProp() }
    var animated by animatedProperty
    final override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }

    val axisLabelProp = node.labelProperty().toNullableProp()
    var axisLabel by axisLabelProp
}
