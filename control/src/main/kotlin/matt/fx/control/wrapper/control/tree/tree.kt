package matt.fx.control.wrapper.control.tree

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.scene.control.MultipleSelectionModel
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.util.Callback
import matt.fx.control.wrapper.cellfact.TreeCellFactory
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.tree.like.TreeLikeWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo


fun <T> ET.treeview(root: TreeItem<T>? = null, op: TreeViewWrapper<T>.()->Unit = {}) =
  TreeViewWrapper<T>().attachTo(this, op) {
	if (root != null) it.root = root
  }


class TreeViewWrapper<T>( node: TreeView<T> = TreeView(), op: TreeViewWrapper<T>.()->Unit = {}):
  ControlWrapperImpl<TreeView<T>>(node),
  TreeLikeWrapper<TreeView<T>, T>,
  TreeCellFactory<TreeView<T>, T> {
  init {
	op()
  }


  fun editableProperty(): BooleanProperty = node.editableProperty()


  override val cellFactoryProperty: ObjectProperty<Callback<TreeView<T>, TreeCell<T>>> = node.cellFactoryProperty()


  override var root: TreeItem<T>
	get() = node.root
	set(value) {
	  node.root = value
	}
  override var isShowRoot: Boolean
	get() = node.isShowRoot
	set(value) {
	  node.isShowRoot = value
	}

  override val selectionModel: MultipleSelectionModel<TreeItem<T>> get() = node.selectionModel
  override fun scrollTo(i: Int) = node.scrollTo(i)
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }

  override fun getRow(ti: TreeItem<T>) = node.getRow(ti)


}
