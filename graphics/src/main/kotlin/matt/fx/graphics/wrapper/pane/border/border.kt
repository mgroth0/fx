package matt.fx.graphics.wrapper.pane.border

import javafx.scene.Node
import javafx.scene.layout.BorderPane
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl

open class BorderPaneWrapper<C: NodeWrapper>(node: BorderPane = BorderPane()): PaneWrapperImpl<BorderPane, C>(node) {

  var center: Node?
	get() = node.center
	set(value) {
	  node.center = value
	}
  var top: Node?
	get() = node.top
	set(value) {
	  node.top = value
	}
  var left: Node?
	get() = node.left
	set(value) {
	  node.left = value
	}
  var right: Node?
	get() = node.right
	set(value) {
	  node.right = value
	}
  var bottom: Node?
	get() = node.bottom
	set(value) {
	  node.bottom = value
	}


  @Deprecated("Use top = node {} instead")
  fun <T: NodeWrapperImpl<*>> top(topNode: T, op: T.()->Unit = {}): T {
	top = topNode.node
	return attach(topNode, op)
  }


  @Deprecated("Use bottom = node {} instead")
  fun <T: NodeWrapperImpl<*>> bottom(bottomNode: T, op: T.()->Unit = {}): T {
	bottom = bottomNode.node
	return attach(bottomNode, op)
  }

  @Deprecated("Use left = node {} instead")
  fun <T: NodeWrapperImpl<*>> left(leftNode: T, op: T.()->Unit = {}): T {
	left = leftNode.node
	return attach(leftNode, op)
  }

  @Deprecated("Use right = node {} instead")
  fun <T: NodeWrapperImpl<*>> right(rightNode: T, op: T.()->Unit = {}): T {
	right = rightNode.node
	return attach(rightNode, op)
  }

  @Deprecated("Use center = node {} instead")
  fun <T: NodeWrapperImpl<*>> center(centerNode: T, op: T.()->Unit = {}): T {
	center = centerNode.node
	return attach(centerNode, op)
  }

  override fun addChild(child: NodeWrapper, index: Int?) {
	require(index == null)
	center = child.node
  }

}