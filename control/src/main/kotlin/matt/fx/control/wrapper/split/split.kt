package matt.fx.control.wrapper.split

import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.SplitPane
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.graphics.wrapper.node.NodeWrapper

open class SplitPaneWrapper( node: SplitPane = SplitPane()): ControlWrapperImpl<SplitPane>(node) {
  var orientation: Orientation
	get() = node.orientation
	set(value) {
	  node.orientation = value
	}
  val items: ObservableList<Node> get() = node.items

  override val childList get() = items


  @Deprecated(
	"No need to wrap splitpane items in items{} anymore. Remove the matt.fx.control.wrapper.wrapped.getWrapper and all builder items will still be added as before.",
	ReplaceWith("no items{} matt.fx.control.wrapper.wrapped.getWrapper"),
	DeprecationLevel.WARNING
  )
  fun items(op: (SplitPaneWrapper.()->Unit)) = op(this)
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}