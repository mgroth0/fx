package matt.fx.control.wrapper.control.treecol

import javafx.collections.ObservableList
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.TreeTableColumn.CellDataFeatures
import matt.fx.base.converter.callbackConverter
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.control.wrapper.cellfact.TreeTableCellFactory
import matt.fx.control.wrapper.cellfact.cellvalfact.CellValueFactory
import matt.fx.control.wrapper.control.colbase.TableColumnBaseWrapper
import matt.fx.control.wrapper.control.hascols.HasCols
import matt.fx.control.wrapper.control.treetable.TreeTableViewWrapper
import matt.fx.control.wrapper.wrapped.wrapped
import kotlin.reflect.KClass


class TreeTableColumnWrapper<E: Any, P: Any>(
    node: TreeTableColumn<E, P>,
    private val pClass: KClass<P>
): TableColumnBaseWrapper<TreeItem<E>, P, TreeTableColumn<E, P>>(node),
    TreeTableCellFactory<TreeTableColumn<E, P>, E, P>,
    CellValueFactory<CellDataFeatures<E, P>, P>,
    HasCols<TreeItem<E>> {

    companion object {
        inline operator fun <reified P: Any> invoke(name: String) = TreeTableColumnWrapper(TreeTableColumn(name), P::class)
        inline fun <E: Any, reified P: Any> invoke2(name: String) = TreeTableColumnWrapper<E, P>(TreeTableColumn(name), P::class)
    }


    override val cellFactoryProperty by lazy { node.cellFactoryProperty().toNonNullableProp() }


    val cellValueFactoryProperty by lazy {
        node.cellValueFactoryProperty().toNullableProp().proxy(
            callbackConverter<CellDataFeatures<E, P>, P>(pClass).nullable()
        )
    }
    override var cellValueFactory by cellValueFactoryProperty

    override val columns: ObservableList<TreeTableColumn<E, *>> = node.columns

    override val tableView: TreeTableViewWrapper<E>? get() = node.treeTableView?.wrapped()
    override fun removeFromParent() {
        node.treeTableView.columns.remove(node)
    }
}
