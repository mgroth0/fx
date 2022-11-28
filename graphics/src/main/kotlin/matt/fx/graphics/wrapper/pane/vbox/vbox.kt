package matt.fx.graphics.wrapper.pane.vbox

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import matt.fx.graphics.style.inset.MarginableConstraints
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.SimplePaneWrapper
import matt.fx.graphics.wrapper.pane.box.BoxWrapper
import matt.fx.graphics.wrapper.pane.box.BoxWrapperImpl
import matt.fx.graphics.wrapper.pane.hbox.HBoxWrapper
import matt.fx.graphics.wrapper.pane.hbox.hbox
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.lang.B
import matt.obs.prop.Var

fun ET.v(
  spacing: Number? = null,
  alignment: Pos? = null,
  op: VBoxWrapper<NW>.()->Unit = {}
) = vbox(spacing, alignment, op)

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

interface VBoxWrapper<C: NodeWrapper>: BoxWrapper<C> {
  val fillWidthProperty: Var<B>
  var isFillWidth: B
}

open class VBoxWrapperImpl<C: NodeWrapper>(node: VBox = VBox()): BoxWrapperImpl<VBox, C>(node), VBoxWrapper<C> {
  constructor(vararg nodes: C): this(VBox(*nodes.map { it.node }.toTypedArray()))

  final override val fillWidthProperty by lazy {
	node.fillWidthProperty().toNonNullableProp()
  }
  override var isFillWidth by fillWidthProperty
}

fun VBoxWrapperImpl<PaneWrapper<*>>.spacer(prio: Priority = Priority.ALWAYS, op: PaneWrapperImpl<*, *>.()->Unit = {}) =
  attach(SimplePaneWrapper<NodeWrapper>().apply { vGrow = prio }, op)




class VBoxConstraint(
  node: Node,
  override var margin: Insets? = VBox.getMargin(node),
  var vGrow: Priority? = null

): MarginableConstraints() {
  fun <T: Node> applyToNode(node: T): T {
    margin?.let { VBox.setMargin(node, it) }
    vGrow?.let { VBox.setVgrow(node, it) }
    return node
  }
}



inline fun <T: Node> T.vboxConstraints(op: (VBoxConstraint.()->Unit)): T {
  val c = VBoxConstraint(this)
  c.op()
  return c.applyToNode(this)
}

