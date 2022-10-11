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
}

class CheckBoxTreeItemWrapper<T>(node: CheckBoxTreeItem<T>): TreeItemWrapper<T>(node)