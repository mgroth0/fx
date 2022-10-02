package matt.fx.control.wrapper.control.tree.like

import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.control.MultipleSelectionModel
import javafx.scene.control.TreeItem
import javafx.scene.layout.Region
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.hurricanefx.wrapper.selects.SelectingControl
import matt.fx.graphics.wrapper.sizeman.SizeManaged

interface TreeLikeWrapper<N: Region, T>: RegionWrapper<NodeWrapper>, SelectingControl<TreeItem<T>>, SizeManaged {
  var root: TreeItem<T>
  var isShowRoot: Boolean
  override val selectionModel: MultipleSelectionModel<TreeItem<T>>
  val selectedValue: T? get() = selectedItem?.value
  fun scrollTo(i: Int)
  fun getRow(ti: TreeItem<T>): Int
}



/**
 * Add matt.fx.control.layout.children to the given item by invoking the supplied childFactory function, which converts
 * a TreeItem&matt.hurricanefx.eye.prop.str.matt.lang.`when`.lt;T> to a List&matt.hurricanefx.eye.prop.str.matt.lang.`when`.lt;T>?.
 *
 * If the childFactory returns a non-empty list, each entry in the list is converted to a TreeItem&matt.hurricanefx.eye.prop.str.matt.lang.`when`.lt;T>
 * via the supplied itemProcessor function. The default itemProcessor from TreeTableView.populate and TreeTable.populate
 * simply wraps the given T in a TreeItem, but you can override it to add icons etc. Lastly, the populateTree
 * function is called for each of the generated child items.
 */
fun <T> populateTree(item: TreeItem<T>, itemFactory: (T)->TreeItem<T>, childFactory: (TreeItem<T>)->Iterable<T>?) {
  val children = childFactory.invoke(item)

  children?.map { itemFactory(it) }?.apply {
    item.children.setAll(this)
    forEach { populateTree(it, itemFactory, childFactory) }
  }

  (children as? ObservableList<T>)?.addListener(ListChangeListener { change ->
    while (change.next()) {
      if (change.wasPermutated()) {
        item.children.subList(change.from, change.to).clear()
        val permutated = change.list.subList(change.from, change.to).map { itemFactory(it) }
        item.children.addAll(change.from, permutated)
        permutated.forEach { populateTree(it, itemFactory, childFactory) }
      } else {
        if (change.wasRemoved()) {
          val removed = change.removed.flatMap { removed -> item.children.filter { it.value == removed } }
          item.children.removeAll(removed)
        }
        if (change.wasAdded()) {
          val added = change.addedSubList.map { itemFactory(it) }
          item.children.addAll(change.from, added)
          added.forEach { populateTree(it, itemFactory, childFactory) }
        }
      }
    }
  })
}