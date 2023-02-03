package matt.fx.control.wrapper.control

import javafx.scene.control.Control
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl

interface ControlWrapper: RegionWrapper<NodeWrapper> {
  override val node: Control
  /*val tooltipProp: Var<matt.fx.control.wrapper.tooltip.fixed.TooltipWrapper?>
  var matt.fx.control.wrapper.tooltip.fixed.tooltip: matt.fx.control.wrapper.tooltip.fixed.TooltipWrapper?*/
}

abstract class ControlWrapperImpl<N: Control>(node: N): RegionWrapperImpl<N, NodeWrapper>(node),
														ControlWrapper {


  /*Don't use this. It requires that I use the built in Tooltip, which I do not*/
/*  final override val tooltipProp by lazy {
	node.tooltipProperty().toNullableProp().proxy(uncheckedWrapperConverter<Tooltip, matt.fx.control.wrapper.tooltip.fixed.TooltipWrapper>().nullable())
  }
  final override var matt.fx.control.wrapper.tooltip.fixed.tooltip by lazyVarDelegate { tooltipProp }*/

}