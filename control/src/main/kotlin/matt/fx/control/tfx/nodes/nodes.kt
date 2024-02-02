package matt.fx.control.tfx.nodes

import javafx.scene.Node
import javafx.scene.control.SplitPane
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TreeTableCell
import javafx.scene.control.TreeTableColumn
import javafx.util.Callback
import matt.fx.graphics.wrapper.node.NodeWrapper
inline fun <T: NodeWrapper> T.splitpaneConstraints(op: SplitPaneConstraint.()->Unit): T {
    val c = SplitPaneConstraint()
    c.op()
    return c.applyToNode(this)
}


class TableColumnCellCache<T>(private val cacheProvider: (T)->Node) {
    private val store = mutableMapOf<T, Node>()
    fun getOrCreateNode(value: T) = store.getOrPut(value) { cacheProvider(value) }
}

fun <S, T> TableColumn<S, T>.cellDecorator(decorator: TableCell<S, T>.(T)->Unit) {
    val originalFactory = cellFactory

    cellFactory = Callback { column: TableColumn<S, T> ->
        val cell = originalFactory.call(column)
        cell.itemProperty().addListener { _, _, newValue ->
            if (newValue != null) decorator(cell, newValue)
        }
        cell
    }
}

fun <S, T> TreeTableColumn<S, T>.cellFormat(formatter: (TreeTableCell<S, T>.(T)->Unit)) {
    cellFactory = Callback { _: TreeTableColumn<S, T> ->
        object: TreeTableCell<S, T>() {
            private val defaultStyle = style

            // technically defined as TreeTableCell.DEFAULT_STYLE_CLASS = "tree-table-cell", but this is private
            private val defaultStyleClass = listOf(*styleClass.toTypedArray())

            override fun updateItem(item: T, empty: Boolean) {
                super.updateItem(item, empty)

                if (item == null || empty) {
                    text = null
                    graphic = null
                    style = defaultStyle
                    styleClass.setAll(defaultStyleClass)
                } else {
                    formatter(this, item)
                }
            }
        }
    }
}

enum class EditEventType(val editing: Boolean) {
    StartEdit(true), CommitEdit(false), CancelEdit(false)
}


val <S, T> TableCell<S, T>.rowItem: S get() = tableView.items[index]
val <S, T> TreeTableCell<S, T>.rowItem: S get() = treeTableView.getTreeItem(index).value



class SplitPaneConstraint(
    var isResizableWithParent: Boolean? = null
) {
    fun <T: NodeWrapper> applyToNode(node: T): T {
        isResizableWithParent?.let { SplitPane.setResizableWithParent(node.node, it) }
        return node
    }
}
