package matt.fx.graphics.wrapper.pane.stack

import javafx.beans.property.ObjectProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.StackPane
import matt.fx.graphics.tfx.nodes.MarginableConstraints
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl

inline fun <C: NodeWrapper> ET.stackpane(initialChildren: Iterable<C>? = null, op: StackPaneWrapper<C>.()->Unit = {}) = attach(
  StackPaneWrapper<C>().apply { if (initialChildren != null) children.addAll(initialChildren) }, op
)

open class StackPaneWrapper<C: NodeWrapper>(node: StackPane = StackPane()): PaneWrapperImpl<StackPane, C>(node) {

  constructor(vararg nodes: C): this(StackPane(*nodes.map { it.node }.toTypedArray()))


  var alignment: Pos
	get() = node.alignment
	set(value) {
	  node.alignment = value
	}

  fun alignmentProperty(): ObjectProperty<Pos> = node.alignmentProperty()
}


class StackpaneConstraint(
  node: Node,
  override var margin: Insets? = StackPane.getMargin(node),
  var alignment: Pos? = null

): MarginableConstraints() {
  fun <T: Node> applyToNode(node: T): T {
	margin?.let { StackPane.setMargin(node, it) }
	alignment?.let { StackPane.setAlignment(node, it) }
	return node
  }
}




inline fun <T: Node> T.stackpaneConstraints(op: (StackpaneConstraint.()->Unit)): T {
  val c = StackpaneConstraint(this)
  c.op()
  return c.applyToNode(this)
}

