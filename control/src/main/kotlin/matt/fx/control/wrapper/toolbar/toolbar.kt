package matt.fx.control.wrapper.toolbar

import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.ToolBar
import javafx.scene.layout.Priority
import matt.hurricanefx.wrapper.control.ControlWrapperImpl
import matt.hurricanefx.wrapper.control.button.ButtonWrapper
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.SimplePaneWrapper
import matt.hurricanefx.wrapper.sep.SeparatorWrapper
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
	TODO("Not yet implemented")
  }


  override fun separator(
	orientation: Orientation,
	op: SeparatorWrapper.()->Unit
  ): SeparatorWrapper {
	val separator = SeparatorWrapper(orientation).also(op)
	add(separator)
	return separator
  }


  override fun button(text: String, graphic: NodeWrapper?, op: ButtonWrapper.()->Unit) =
	ButtonWrapper().apply { this.text = text }.also {
	  if (graphic != null) it.graphic = graphic
	  this.items += it.node
	  op(it)
	}

  override fun button(text: ValProp<String>, graphic: NodeWrapper?, op: ButtonWrapper.()->Unit) =
	ButtonWrapper().also {
	  it.textProperty.bind(text)
	  if (graphic != null) it.graphic = graphic
	  this.items += it.node
	  op(it)
	}


}