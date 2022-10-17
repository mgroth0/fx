package matt.fx.control.wrapper.chart

import javafx.scene.chart.Chart
import matt.fx.graphics.wrapper.inter.titled.Titled
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp
import matt.lang.NOT_IMPLEMENTED

open class ChartWrapper<N: Chart>(node: N): RegionWrapperImpl<N, NodeWrapper>(node), Titled {


  override val titleProperty by lazy { node.titleProperty().toNullableProp() }
  override fun addChild(child: NodeWrapper, index: Int?) = NOT_IMPLEMENTED

  val animatedProperty by lazy { node.animatedProperty().toNonNullableProp() }
  var animated by animatedProperty

  val legendVisibleProperty by lazy { node.legendVisibleProperty().toNonNullableProp() }
  var isLegendVisible by legendVisibleProperty

}