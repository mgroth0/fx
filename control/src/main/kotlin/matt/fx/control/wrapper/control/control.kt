package matt.fx.control.wrapper.control

import javafx.scene.control.Control
import javafx.scene.control.Tooltip
import matt.fx.control.wrapper.tooltip.TooltipWrapper
import matt.fx.graphics.service.uncheckedWrapperConverter
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp
import matt.obs.prop.Var

interface ControlWrapper: RegionWrapper<NodeWrapper> {
  override val node: Control
  val tooltipProp: Var<TooltipWrapper?>
  var tooltip: TooltipWrapper?
}

abstract class ControlWrapperImpl<N: Control>(node: N): RegionWrapperImpl<N, NodeWrapper>(node),
														ControlWrapper {

  final override val tooltipProp by lazy {
	node.tooltipProperty().toNullableProp().proxy(uncheckedWrapperConverter<Tooltip, TooltipWrapper>().nullable())
  }
  final override var tooltip by tooltipProp

}