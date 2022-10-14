package matt.fx.control.wrapper.control.tree

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.scene.control.SelectionMode
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.util.Callback
import matt.fx.control.wrapper.cellfact.TreeCellFactory
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.tree.like.TreeLikeWrapper
import matt.fx.control.wrapper.control.tree.like.populateTree
import matt.fx.control.wrapper.selects.wrap
import matt.fx.control.wrapper.treeitem.TreeItemWrapper
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.service.uncheckedNullableWrapperConverter
import matt.fx.graphics.service.uncheckedWrapperConverter
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp


fun <T> ET.treeview(root: TreeItem<T>? = null, op: TreeViewWrapper<T>.()->Unit = {}) =
  TreeViewWrapper<T>().attachTo(this, op) {
	if (root != null) it.root = root
  }


class TreeViewWrapper<T>(node: TreeView<T> = TreeView(), op: TreeViewWrapper<T>.()->Unit = {}):
  ControlWrapperImpl<TreeView<T>>(node),
  TreeLikeWrapper<TreeView<T>, T>,
  TreeCellFactory<TreeView<T>, T> {
  init {
	op()
  }


  fun editableProperty(): BooleanProperty = node.editableProperty()


  override val cellFactoryProperty by lazy { node.cellFactoryProperty().toNonNullableProp() }


  override val rootProperty by lazy {node.rootProperty().toNullableProp().proxy(uncheckedNullableWrapperConverter<TreeItem<T>,TreeItemWrapper<T>>())}
  override var isShowRoot: Boolean
	get() = node.isShowRoot
	set(value) {
	  node.isShowRoot = value
	}

  override val selectionModel by lazy { node.selectionModel.wrap() }
  override fun scrollTo(i: Int) = node.scrollTo(i)
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }

  override fun getRow(ti: TreeItem<T>) = node.getRow(ti)


}


fun <T> TreeViewWrapper<T>.bindSelected(property: Property<T>) {

  selectedItemProperty.onChange { property.value = it?.value }
}

/**
 * <p>This method will attempt to select the first index in the control.
 * If clearSelection is not called first, this method
 * will have the result of selecting the first index, whilst retaining
 * the selection of any other currently selected indices.</p>
 *
 * <p>If the first index is already selected, calling this method will have
 * no result, and no selection event will take place.</p>
 *
 * This functions is the same as calling.
 * ```
 * selectionModel.selectFirst()
 *
 * ```
 */
fun <T> TreeViewWrapper<T>.selectFirst() = selectionModel.selectFirst()

fun <T> TreeViewWrapper<T>.populate(
  itemFactory: (T)->TreeItemWrapper<T> = { TreeItemWrapper(it) },
  childFactory: (TreeItemWrapper<T>)->Iterable<T>?
) =
  populateTree(root, itemFactory, childFactory)


// -- Properties

/**
 * Returns the currently selected value of type [T] (which is currently the
 * selected value represented by the current selection model). If there
 * are multiple values selected, it will return the most recently selected
 * value.
 *
 * <p>Note that the returned value is a snapshot in time.
 */
val <T> TreeViewWrapper<T>.selectedValue: T?
  get() = this.selectionModel.selectedItem?.value

fun <T> TreeViewWrapper<T>.multiSelect(enable: Boolean = true) {
  selectionModel.selectionMode = if (enable) SelectionMode.MULTIPLE else SelectionMode.SINGLE
}
