package matt.fx.control.wrapper.treeitem

import javafx.collections.ObservableMap
import javafx.scene.control.CheckBoxTreeItem
import javafx.scene.control.TreeItem
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper

open class TreeItemWrapper<T>(node: TreeItem<T>): SingularEventTargetWrapper<TreeItem<T>>(node) {
  override val properties: ObservableMap<Any, Any?>
	get() = TODO("Not yet implemented")

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }

  override fun removeFromParent() {
	TODO("Not yet implemented")
  }

  override fun isInsideRow(): Boolean {
	TODO("Not yet implemented")
  }

  var isExpanded get() = node.isExpanded
	set(value) {
	  node.isExpanded = value
	}
  val children by node::children



  // -- TreeItem helpers
  /**
   * Expand this [TreeItem] and matt.fx.control.layout.children down to `depth`.
   */
  fun expandTo(depth: Int) {
	if (depth > 0) {
	  this.isExpanded = true
	  this.children.forEach { it.wrapped().expandTo(depth - 1) }
	}
  }

  /**
   * Expand this `[TreeItem] and all it's matt.fx.control.layout.children.
   */
  fun expandAll() = expandTo(Int.MAX_VALUE)

  /**
   * Collapse this [TreeItem] and all it's matt.fx.control.layout.children.
   */

  fun collapseAll() {
	this.isExpanded = false
	this.children.forEach { it.wrapped().collapseAll() }
  }
}

class CheckBoxTreeItemWrapper<T>(node: CheckBoxTreeItem<T>): TreeItemWrapper<T>(node)


