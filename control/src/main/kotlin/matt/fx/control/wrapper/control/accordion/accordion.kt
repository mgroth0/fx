package matt.fx.control.wrapper.control.accordion

import javafx.collections.ObservableList
import javafx.scene.control.Accordion
import javafx.scene.control.TitledPane
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.titled.TitledPaneWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl

class AccordionWrapper(node: Accordion = Accordion()): ControlWrapperImpl<Accordion>(node) {
  val panes: ObservableList<TitledPane> get() = node.panes

  fun <T: NodeWrapper> fold(
	title: String = "",
	node: T,
	expanded: Boolean = false,
	op: T.()->Unit = {}
  ): TitledPaneWrapper {
	val fold = TitledPaneWrapper().apply {
	  text = title
	  graphic = node
	}
	fold.isExpanded = expanded
	panes += fold.node
	op(node)
	return fold
  }

  @Deprecated(
	"Properties added to the container will be lost if you add only a single child Node",
	ReplaceWith("Accordion.fold(title, node, op)"),
	DeprecationLevel.WARNING
  )
  fun fold(title: String = "", op: PaneWrapper<NodeWrapper>.()->Unit = {}): TitledPaneWrapper {
	val vbox = VBoxWrapperImpl<NodeWrapper>().also(op)
	val fold =
	  TitledPaneWrapper().apply {
		text = title; graphic = if (vbox.children.size == 1) vbox.children[0] else vbox
	  }
	panes += fold.node
	return fold
  }

  override fun addChild(child: NodeWrapper, index: Int?) {
	require(index == null)
	node.panes.add(TitledPane("new titled pane", child.node))
  }


}
