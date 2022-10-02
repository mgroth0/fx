package matt.fx.control.wrapper.control

import javafx.beans.property.ObjectProperty
import javafx.scene.control.Control
import javafx.scene.control.Tooltip
import matt.fx.control.control.dsl.ControlDSL
import matt.fx.control.control.nodedsl.NodeControlDSL
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl

interface ControlWrapper: RegionWrapper<NodeWrapper> {
  override val node: Control
  val tooltipProp: ObjectProperty<Tooltip> get() = node.tooltipProperty()
  var tooltip: Tooltip?
	get() = tooltipProp.get()
	set(value) = tooltipProp.setValue(value)
}

abstract class ControlWrapperImpl<N: Control>(node: N): RegionWrapperImpl<N, NodeWrapper>(node),
														ControlWrapper,
														NodeControlDSL