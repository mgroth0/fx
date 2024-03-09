package matt.fx.control.wrapper.control.row

import javafx.scene.control.Cell
import javafx.scene.control.IndexedCell
import javafx.scene.control.ListCell
import javafx.scene.control.TableCell
import javafx.scene.control.TableRow
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableRow
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.base.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.control.wrapper.labeled.LabeledWrapper

open class CellWrapper<E, N : Cell<E>>(
    node: N
) : LabeledWrapper<N>(node)

open class IndexedCellWrapper<E, N : IndexedCell<E>>(
    node: N
) : CellWrapper<E, N>(node)


class TreeTableRowWrapper<E>(
    node: TreeTableRow<E> = TreeTableRow()
) : IndexedCellWrapper<E, TreeTableRow<E>>(node) {
    val item: E get() = node.item
    val treeItem: TreeItem<E>? get() = node.treeItem
    override fun isInsideRow() = true
}

class TableRowWrapper<E>(
    node: TableRow<E> = TableRow()
) : IndexedCellWrapper<E, TableRow<E>>(node) {
    val itemProperty by lazy { node.itemProperty().toNullableProp() }
    val item: E? get() = node.item
    override fun isInsideRow() = true

    val selectedProperty by lazy { node.selectedProperty().toNonNullableROProp() }
}

class TreeCellOverride<T>(var updateItemOv: (item: T, empty: Boolean) -> Unit = { _, _ -> Unit }) : TreeCell<T>() {
    override fun updateItem(
        item: T,
        empty: Boolean
    ) {
        super.updateItem(item, empty)
        updateItemOv(item, empty)
    }
}

open class TreeCellWrapper<E>(
    node: TreeCellOverride<E> = TreeCellOverride()
) : IndexedCellWrapper<E, TreeCellOverride<E>>(node) {
    val item: E? get() = node.item
    val treeItem: TreeItem<E>? get() = node.treeItem
    var updateItemOv by node::updateItemOv
}

class ListCellOverride<T>(var updateItemOv: (item: T, empty: Boolean) -> Unit = { _, _ -> Unit }) : ListCell<T>() {
    override fun updateItem(
        item: T,
        empty: Boolean
    ) {
        super.updateItem(item, empty)
        updateItemOv(item, empty)
    }
}

class ListCellWrapper<E>(
    node: ListCellOverride<E> = ListCellOverride()
) : IndexedCellWrapper<E, ListCellOverride<E>>(node) {
    val item: E? get() = node.item
    var updateItemOv by node::updateItemOv
    override fun isInsideRow() = true
}

class TableCellOverride<E, P>(var updateItemOv: (item: P, empty: Boolean) -> Unit = { _, _ -> Unit }) :
    TableCell<E, P>() {
    override fun updateItem(
        item: P,
        empty: Boolean
    ) {
        super.updateItem(item, empty)
        updateItemOv(item, empty)
    }
}

class TableCellWrapper<E, P>(
    node: TableCellOverride<E, P> = TableCellOverride()
) : IndexedCellWrapper<P, TableCellOverride<E, P>>(node) {
    val item: P? get() = node.item
    var updateItemOv by node::updateItemOv
}

class TreeTableCellOverride<E, P>(var updateItemOv: (item: P, empty: Boolean) -> Unit = { _, _ -> Unit }) :
    TableCell<E, P>() {
    override fun updateItem(
        item: P,
        empty: Boolean
    ) {
        super.updateItem(item, empty)
        updateItemOv(item, empty)
    }
}

class TreeTableCellWrapper<E, P>(
    node: TreeTableCellOverride<E, P> = TreeTableCellOverride()
) : IndexedCellWrapper<P, TreeTableCellOverride<E, P>>(node) {
    val item: P? get() = node.item
    var updateItemOv by node::updateItemOv
}

