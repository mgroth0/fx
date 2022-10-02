package matt.fx.control.wrapper.chart

import javafx.beans.property.BooleanProperty
import javafx.beans.property.StringProperty
import javafx.scene.chart.Chart
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.hurricanefx.eye.prop.getValue
import matt.hurricanefx.eye.prop.setValue
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.lang.NOT_IMPLEMENTED

open class ChartWrapper<N: Chart>(node: N): RegionWrapperImpl<N, NodeWrapper>(node) {


  var title: String?
	get() = node.title
	set(value) {
	  node.title = value
	}

  fun titleProperty(): StringProperty = node.titleProperty()
  override fun addChild(child: NodeWrapper, index: Int?) = NOT_IMPLEMENTED

  val animatedProperty: BooleanProperty get() = node.animatedProperty()
  var animated by animatedProperty

  val legendVisibleProperty by lazy { node.legendVisibleProperty().toNonNullableProp() }
  var isLegendVisible by legendVisibleProperty

}