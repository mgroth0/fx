package matt.fx.control.wrapper.control.tree

import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.Property
import javafx.scene.control.SelectionMode
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import matt.collect.itr.recurse.recurse
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.control.wrapper.cellfact.TreeCellFactory
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.tree.like.TreeLikeWrapper
import matt.fx.control.wrapper.control.tree.like.populateTree
import matt.fx.control.wrapper.selects.wrap
import matt.fx.control.wrapper.treeitem.TreeItemWrapper
import matt.fx.graphics.service.uncheckedNullableWrapperConverter
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo

fun <T: Any> TreeViewWrapper<T>.items(): Sequence<TreeItemWrapper<T>> = root!!.recurse { it.children }


fun <T: Any> TreeViewWrapper<T>.select(o: T?) {
    Platform.runLater {
        when {
            o != null -> {
                selectionModel.select(items().firstOrNull { it == o }?.node)
            }

            else      -> selectionModel.clearSelection()
        }
    }
}


fun <T: Any> ET.treeview(root: TreeItemWrapper<T>? = null, op: TreeViewWrapper<T>.() -> Unit = {}) =
    TreeViewWrapper<T>().attachTo(this, op) {
        if (root != null) it.root = root
    }


class TreeViewWrapper<T: Any>(node: TreeView<T> = TreeView(), op: TreeViewWrapper<T>.() -> Unit = {}):
    ControlWrapperImpl<TreeView<T>>(node),
    TreeLikeWrapper<TreeView<T>, T>,
    TreeCellFactory<TreeView<T>, T> {
    init {
        op()
    }


    fun editableProperty(): BooleanProperty = node.editableProperty()


    override val cellFactoryProperty by lazy { node.cellFactoryProperty().toNullableProp() }


    override val rootProperty by lazy {
        node.rootProperty().toNullableProp().proxy(uncheckedNullableWrapperConverter<TreeItem<T>, TreeItemWrapper<T>>())
    }

    override var isShowRoot: Boolean
        get() = node.isShowRoot
        set(value) {
            node.isShowRoot = value
        }

    override val selectionModel by lazy { node.selectionModel.wrap() }
    override fun scrollTo(i: Int) = node.scrollTo(i)
    override fun addChild(child: NodeWrapper, index: Int?) {
        TODO()
    }

    override fun getRow(ti: TreeItem<T>) = node.getRow(ti)


    fun populate(
        itemFactory: (T) -> TreeItemWrapper<T> = { TreeItemWrapper(it) },
        childFactory: (TreeItemWrapper<T>) -> Iterable<T>?
    ) = populateTree(root!!, itemFactory, childFactory)
}


fun <T: Any> TreeViewWrapper<T>.bindSelected(property: Property<T>) {

    selectedItemProperty.onChange { property.value = it?.value }
}

fun <T: Any> TreeViewWrapper<T>.multiSelect(enable: Boolean = true) {
    selectionModel.selectionMode = if (enable) SelectionMode.MULTIPLE else SelectionMode.SINGLE
}
