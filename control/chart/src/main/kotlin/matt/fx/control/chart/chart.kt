package matt.fx.control.chart

import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.NullableFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.control.chart.line.highperf.relinechart.xy.chart.ChartForPrivateProps
import matt.fx.graphics.wrapper.inter.titled.Titled
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.lang.NOT_IMPLEMENTED
import matt.model.code.idea.ChartIdea


open class ChartWrapper<N: ChartForPrivateProps>(node: N): RegionWrapperImpl<N, NodeWrapper>(node), Titled, ChartIdea {


  override val titleProperty: NullableFXBackedBindableProp<String> by lazy { node.titleProperty().toNullableProp() }

  override fun addChild(child: NodeWrapper, index: Int?) = NOT_IMPLEMENTED

  val animatedProperty: NonNullFXBackedBindableProp<Boolean> by lazy { node.animatedProperty().toNonNullableProp() }
  var animated: Boolean by animatedProperty

  val legendVisibleProperty: NonNullFXBackedBindableProp<Boolean> by lazy { node.legendVisibleProperty().toNonNullableProp() }
  var isLegendVisible by legendVisibleProperty

}