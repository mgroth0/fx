package matt.fx.graphics.wrapper.pane.vbox

import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.SimplePaneWrapper
import matt.fx.graphics.wrapper.pane.box.BoxWrapper
import matt.fx.graphics.wrapper.pane.box.BoxWrapperImpl

typealias VBoxW = VBoxWrapperImpl<NodeWrapper>

interface VBoxWrapper<C: NodeWrapper>: BoxWrapper<C>

open class VBoxWrapperImpl<C: NodeWrapper>(node: VBox = VBox()): BoxWrapperImpl<VBox, C>(node), VBoxWrapper<C> {
  constructor(vararg nodes: C): this(VBox(*nodes.map { it.node }.toTypedArray()))

}

fun VBoxWrapperImpl<PaneWrapper<*>>.spacer(prio: Priority = Priority.ALWAYS, op: PaneWrapperImpl<*, *>.()->Unit = {}) =
  attach(SimplePaneWrapper<NodeWrapper>().apply { vGrow = prio }, op)
