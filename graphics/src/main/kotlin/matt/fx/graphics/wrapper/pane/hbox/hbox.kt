package matt.fx.graphics.wrapper.pane.hbox

import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.SimplePaneWrapper
import matt.fx.graphics.wrapper.pane.box.BoxWrapper

open class HBoxWrapper<C: NodeWrapper>(node: HBox = HBox()): BoxWrapper<HBox, C>(node) {
  constructor(vararg nodes: NodeWrapper): this(HBox(*nodes.map { it.node }.toTypedArray()))


}

fun HBoxWrapper<NodeWrapper>.spacer(prio: Priority = Priority.ALWAYS, op: PaneWrapperImpl<*, *>.()->Unit = {}) =
  attach(SimplePaneWrapper<NodeWrapper>().apply { hGrow = prio }, op)