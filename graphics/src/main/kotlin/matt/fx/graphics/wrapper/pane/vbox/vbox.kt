package matt.fx.graphics.wrapper.pane.vbox

import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.SimplePaneWrapper
import matt.fx.graphics.wrapper.pane.box.BoxWrapper
import matt.fx.graphics.wrapper.pane.box.BoxWrapperImpl


fun <C: NodeWrapper> ET.vbox(
  spacing: Number? = null,
  alignment: Pos? = null,
  op: VBoxWrapper<C>.()->Unit = {}
): VBoxWrapper<C> {
  val vbox = VBoxWrapperImpl<C>(VBox())
  if (alignment != null) vbox.alignment = alignment
  if (spacing != null) vbox.spacing = spacing.toDouble()
  return attach(vbox, op)
}

typealias VBoxW = VBoxWrapperImpl<NodeWrapper>

interface VBoxWrapper<C: NodeWrapper>: BoxWrapper<C>

open class VBoxWrapperImpl<C: NodeWrapper>(node: VBox = VBox()): BoxWrapperImpl<VBox, C>(node), VBoxWrapper<C> {
  constructor(vararg nodes: C): this(VBox(*nodes.map { it.node }.toTypedArray()))

}

fun VBoxWrapperImpl<PaneWrapper<*>>.spacer(prio: Priority = Priority.ALWAYS, op: PaneWrapperImpl<*, *>.()->Unit = {}) =
  attach(SimplePaneWrapper<NodeWrapper>().apply { vGrow = prio }, op)
