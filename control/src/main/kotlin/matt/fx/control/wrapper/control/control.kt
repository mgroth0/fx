package matt.fx.control.wrapper.control

import javafx.scene.control.Control
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl

interface ControlWrapper: RegionWrapper<NodeWrapper> {
    override val node: Control
  /*val tooltipProp: Var<TooltipWrapper?>
  var tooltip: tooltipWrapper?*/
}

abstract class ControlWrapperImpl<N: Control>(node: N):
    RegionWrapperImpl<N, NodeWrapper>(node, NodeWrapper::class),
    ControlWrapper {


    /*

    // Don't use this. It requires that I use the built in Tooltip, which I do not

          final override val tooltipProp by lazy {
	node.tooltipProperty().toNullableProp().proxy(wrapperConverter<Tooltip, tooltipWrapper>().nullable())
  }


  final override var tooltip by lazyVarDelegate { tooltipProp }



     */
}
