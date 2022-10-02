package matt.fx.graphics.wrapper.pane.stack

import javafx.beans.property.ObjectProperty
import javafx.geometry.Pos
import javafx.scene.layout.StackPane
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl

open class StackPaneWrapper<C: NodeWrapper>(node: StackPane = StackPane()): PaneWrapperImpl<StackPane, C>(node) {

  constructor(vararg nodes: C): this(StackPane(*nodes.map { it.node }.toTypedArray()))


  var alignment: Pos
	get() = node.alignment
	set(value) {
	  node.alignment = value
	}

  fun alignmentProperty(): ObjectProperty<Pos> = node.alignmentProperty()
}