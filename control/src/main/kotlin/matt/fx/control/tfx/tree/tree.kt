package matt.fx.control.tfx.tree

/*slightly modified code I stole from tornadofx*/

import javafx.beans.property.Property
import javafx.scene.control.SelectionMode
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableView
import javafx.scene.control.TreeView
import matt.hurricanefx.eye.lib.onChange


fun <T> TreeView<T>.bindSelected(property: Property<T>) {
    selectionModel.selectedItemProperty().onChange { property.value = it?.value }
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
fun <T> TreeView<T>.selectFirst() = selectionModel.selectFirst()

fun <T> TreeView<T>.populate(
    itemFactory: (T) -> TreeItem<T> = { TreeItem(it) },
    childFactory: (TreeItem<T>) -> Iterable<T>?
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
val <T> TreeView<T>.selectedValue: T?
    get() = this.selectionModel.selectedItem?.value

fun <T> TreeView<T>.multiSelect(enable: Boolean = true) {
    selectionModel.selectionMode = if (enable) SelectionMode.MULTIPLE else SelectionMode.SINGLE
}

fun <T> TreeTableView<T>.multiSelect(enable: Boolean = true) {
    selectionModel.selectionMode = if (enable) SelectionMode.MULTIPLE else SelectionMode.SINGLE
}

// -- TreeItem helpers
/**
 * Expand this [TreeItem] and matt.fx.control.layout.children down to `depth`.
 */
fun <T> TreeItem<T>.expandTo(depth: Int) {
    if (depth > 0) {
        this.isExpanded = true
        this.children.forEach { it.expandTo(depth - 1) }
    }
}

/**
 * Expand this `[TreeItem] and all it's matt.fx.control.layout.children.
 */
fun <T> TreeItem<T>.expandAll() = expandTo(Int.MAX_VALUE)

/**
 * Collapse this [TreeItem] and all it's matt.fx.control.layout.children.
 */

fun <T> TreeItem<T>.collapseAll() {
    this.isExpanded = false
    this.children.forEach { it.collapseAll() }
}