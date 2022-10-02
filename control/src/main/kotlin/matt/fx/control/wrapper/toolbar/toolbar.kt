package matt.fx.control.wrapper.toolbar

import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.ToolBar
import javafx.scene.layout.Priority
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.button.ButtonWrapper
import matt.fx.control.wrapper.sep.SeparatorWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.SimplePaneWrapper
import matt.obs.prop.ValProp

class ToolBarWrapper(
  node: ToolBar = ToolBar(),
): ControlWrapperImpl<ToolBar>(node) {
  companion object {
	fun ToolBar.wrapped() = ToolBarWrapper(this)
  }

  override val childList get() = items


  val items: ObservableList<Node> get() = node.items


  fun spacer(prio: Priority = Priority.ALWAYS, op: PaneWrapperImpl<*, *>.()->Unit = {}): PaneWrapperImpl<*, *> {
	val pane = SimplePaneWrapper<NodeWrapper>().apply {
	  hgrow = prio
	}
	op(pane)
	add(pane)
	return pane
  }

  override fun addChild(child: NodeWrapper, index: Int?) {
	if (index != null) {
	  items.add(index, child.node)
	} else {
	  items.add(child.node)
	}
  }

}