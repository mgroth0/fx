package matt.fx.graphics.wrapper.pane.hbox

import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.SimplePaneWrapper
import matt.fx.graphics.wrapper.pane.box.BoxWrapper
import matt.fx.graphics.wrapper.pane.box.BoxWrapperImpl

fun <C: NodeWrapper> ET.hbox(
  spacing: Number? = null,
  alignment: Pos? = null,
  op: HBoxWrapper<C>.()->Unit = {}
): HBoxWrapper<C> {
  val hbox = HBoxWrapperImpl<C>(HBox())
  if (alignment != null) hbox.alignment = alignment
  if (spacing != null) hbox.spacing = spacing.toDouble()
  return attach(hbox, op)
}


interface HBoxWrapper<C: NodeWrapper>: BoxWrapper<C>

open class HBoxWrapperImpl<C: NodeWrapper>(node: HBox = HBox()): BoxWrapperImpl<HBox, C>(node), HBoxWrapper<C> {
  constructor(vararg nodes: NodeWrapper): this(HBox(*nodes.map { it.node }.toTypedArray()))


}

fun HBoxWrapperImpl<NodeWrapper>.spacer(prio: Priority = Priority.ALWAYS, op: PaneWrapperImpl<*, *>.()->Unit = {}) =
  attach(SimplePaneWrapper<NodeWrapper>().apply { hGrow = prio }, op)