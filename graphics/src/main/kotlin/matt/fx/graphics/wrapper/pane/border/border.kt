package matt.fx.graphics.wrapper.pane.border

import javafx.scene.layout.BorderPane
import matt.fx.graphics.service.wrapped
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl

fun <C: NodeWrapper> ET.borderpane(op: BorderPaneWrapper<C>.()->Unit = {}) = attach(BorderPaneWrapper(), op)
open class BorderPaneWrapper<C: NodeWrapper>(node: BorderPane = BorderPane()): PaneWrapperImpl<BorderPane, C>(node) {

  var center: NW?
	get() = node.center?.wrapped()
	set(value) {
	  value?.let {
		it.removeFromParent()
		node.center = it.node
	  }
	}
  var top: NW?
	get() = node.top?.wrapped()
	set(value) {
	  value?.let {
		it.removeFromParent()
		node.top = it.node
	  }
	}
  var left: NW?
	get() = node.left?.wrapped()
	set(value) {

	  value?.let {
		it.removeFromParent()
		node.left = it.node
	  }
	}
  var right: NW?
	get() = node.right?.wrapped()
	set(value) {
	  value?.let {
		it.removeFromParent()
		node.right = it.node
	  }
	}
  var bottom: NW?
	get() = node.bottom?.wrapped()
	set(value) {
	  value?.let {
		it.removeFromParent()
		node.bottom = it.node
	  }
	}


  override fun addChild(child: NodeWrapper, index: Int?) {
	require(index == null)
	/*center = child*/
	/*this needs to do nothing, or else behavior is undefined.*/
	/*children of border pane should always be set like:

	borderpane {
		left = text("left")
		right = text("right")
	}

	I don't know why having a default here like `center = child` breaks things. I really don't get it, but I've proven that this breaks things. The child ends up not being visible anywhere.

	* */
  }

}