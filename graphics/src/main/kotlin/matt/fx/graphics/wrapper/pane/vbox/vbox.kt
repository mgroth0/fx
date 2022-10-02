package matt.fx.graphics.wrapper.pane.vbox

import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.SimplePaneWrapper
import matt.fx.graphics.wrapper.pane.box.BoxWrapper

typealias VBoxW = VBoxWrapper<NodeWrapper>

open class VBoxWrapper<C: NodeWrapper>(node: VBox = VBox()): BoxWrapper<VBox, C>(node) {
  constructor(vararg nodes: C): this(VBox(*nodes.map { it.node }.toTypedArray()))

}

fun VBoxWrapper<PaneWrapper<*>>.spacer(prio: Priority = Priority.ALWAYS, op: PaneWrapperImpl<*, *>.()->Unit = {}) =
  attach(SimplePaneWrapper<NodeWrapper>().apply { vGrow = prio }, op)
