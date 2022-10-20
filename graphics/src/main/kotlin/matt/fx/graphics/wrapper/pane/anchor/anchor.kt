package matt.fx.graphics.wrapper.pane.anchor

import javafx.scene.layout.AnchorPane
import matt.collect.itr.mapToArray
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.lang.NOT_IMPLEMENTED
import matt.log.warn.warnOnce

fun <C: NodeWrapper> ET.anchorpane(
  vararg nodes: C,
  op: AnchorPaneWrapperImpl<C>.()->Unit = {}
): AnchorPaneWrapperImpl<C> {
  val anchorpane = AnchorPaneWrapperImpl<C>()
  if (nodes.isNotEmpty()) anchorpane.children.addAll(nodes)
  attach(anchorpane, op)
  return anchorpane
}

interface AnchorPaneWrapper<C: NodeWrapper>: PaneWrapper<C> {
  override val node: AnchorPane
  var left: NodeWrapper
	get() = NOT_IMPLEMENTED
	set(value) {
	  if (value.node !in node.children) add(value)
	  value.setAsLeftAnchor(0.0)
	}
  var right: NodeWrapper
	get() = NOT_IMPLEMENTED
	set(value) {
	  if (value.node !in node.children) add(value)
	  value.setAsRightAnchor(0.0)
	}
  var bottom: NodeWrapper
	get() = NOT_IMPLEMENTED
	set(value) {
	  if (value.node !in node.children) add(value)
	  value.setAsBottomAnchor(0.0)
	}
  var top: NodeWrapper
	get() = NOT_IMPLEMENTED
	set(value) {
	  if (value.node !in node.children) add(value)
	  value.setAsTopAnchor(0.0)
	}
  var allSides: NodeWrapper
	get() = NOT_IMPLEMENTED
	set(value) {
	  warnOnce("`value.node !in node.children` is a temp fix. There must be some wrapper issue here.")
	  left = value
	  right = value
	  bottom = value
	  top = value
	}
}

open class AnchorPaneWrapperImpl<C: NodeWrapper>(node: AnchorPane = AnchorPane()): PaneWrapperImpl<AnchorPane, C>(node),
																				   AnchorPaneWrapper<C> {

  constructor (vararg children: NodeWrapper): this(AnchorPane(*children.mapToArray { it.node }))


}