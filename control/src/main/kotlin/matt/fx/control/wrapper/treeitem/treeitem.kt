package matt.fx.control.wrapper.treeitem

import javafx.collections.ObservableMap
import javafx.scene.control.CheckBoxTreeItem
import javafx.scene.control.TreeItem
import matt.fx.base.wrapper.obs.collect.list.createMutableWrapper
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.service.uncheckedWrapperConverter
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.obs.col.olist.sync.toSyncedList

open class TreeItemWrapper<T : Any>(node: TreeItem<T> = TreeItem()) : SingularEventTargetWrapper<TreeItem<T>>(node) {
    constructor(item: T) : this(TreeItem(item))


    val value: T by node::value

    final override val properties: ObservableMap<Any, Any?>
        get() = TODO()

    final override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }

    final override fun removeFromParent() {
        TODO()
    }

    final override fun isInsideRow(): Boolean {
        TODO()
    }

    val expandedProperty: NonNullFXBackedBindableProp<Boolean> by lazy { node.expandedProperty().toNonNullableProp() }
    var isExpanded by expandedProperty
    val parent get() = node.parent?.wrapped()
    val children by lazy {
        node.children.createMutableWrapper().toSyncedList(uncheckedWrapperConverter<TreeItem<T>, TreeItemWrapper<T>>())
    }


    /**
     * Expand this [TreeItem] and matt.fx.control.layout.children down to `depth`.
     */
    fun expandTo(depth: Int) {
        if (depth > 0) {
            isExpanded = true
            children.forEach { it.expandTo(depth - 1) }
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
        isExpanded = false
        children.forEach { it.collapseAll() }
    }
}

class CheckBoxTreeItemWrapper<T : Any>(node: CheckBoxTreeItem<T>) : TreeItemWrapper<T>(node)


