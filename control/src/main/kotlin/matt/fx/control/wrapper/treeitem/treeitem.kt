package matt.fx.control.wrapper.treeitem

import javafx.collections.ObservableMap
import javafx.scene.control.CheckBoxTreeItem
import javafx.scene.control.TreeItem
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.service.uncheckedWrapperConverter
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.hurricanefx.eye.wrapper.obs.collect.createMutableWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.obs.col.olist.mappedlist.toSyncedList

open class TreeItemWrapper<T: Any>(node: TreeItem<T> = TreeItem()): SingularEventTargetWrapper<TreeItem<T>>(node) {
  constructor(item: T): this(TreeItem(item))

  val value by node::value

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

  val expandedProperty by lazy { node.expandedProperty().toNonNullableProp() }
  var isExpanded by expandedProperty
  val parent get() = node.parent?.wrapped()
  val children by lazy {
	node.children.createMutableWrapper().toSyncedList(uncheckedWrapperConverter<TreeItem<T>, TreeItemWrapper<T>>())
  }


  // -- TreeItem helpers
  /**
   * Expand this [TreeItem] and matt.fx.control.layout.children down to `depth`.
   */
  fun expandTo(depth: Int) {
	if (depth > 0) {
	  this.isExpanded = true
	  this.children.forEach { it.expandTo(depth - 1) }
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
	this.children.forEach { it.collapseAll() }
  }
}

class CheckBoxTreeItemWrapper<T: Any>(node: CheckBoxTreeItem<T>): TreeItemWrapper<T>(node)


