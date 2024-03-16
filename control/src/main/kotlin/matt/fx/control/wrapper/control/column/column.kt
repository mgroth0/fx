package matt.fx.control.wrapper.control.column

import javafx.collections.ObservableList
import javafx.scene.control.TableColumn
import javafx.scene.control.TableColumn.CellDataFeatures
import javafx.util.Callback
import matt.fx.base.converter.callbackConverter
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.control.wrapper.cellfact.TableCellFactory
import matt.fx.control.wrapper.cellfact.cellvalfact.CellValueFactory
import matt.fx.control.wrapper.control.colbase.TableColumnBaseWrapper
import matt.fx.control.wrapper.control.hascols.HasCols
import matt.fx.control.wrapper.control.table.TableViewWrapper
import matt.fx.control.wrapper.control.table.cols.ColumnsDSL
import matt.fx.control.wrapper.control.table.cols.ColumnsDSLImpl
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.obs.prop.ObsVal
import matt.obs.prop.writable.BindableProperty
import kotlin.reflect.KClass

class TableColumnWrapper<E : Any, P: Any>(
    node: TableColumn<E, P>,
    private val pClass: KClass<P>
) : TableColumnBaseWrapper<E, P, TableColumn<E, P>>(node),
    TableCellFactory<TableColumn<E, P>, E, P>,
    CellValueFactory<CellDataFeatures<E, P>, P>,
    HasCols<E>,
    ColumnsDSL<E> by ColumnsDSLImpl(node.columns) {


    companion object {
        inline operator fun <E: Any, reified P: Any> invoke(name: String) = TableColumnWrapper(TableColumn<E, P>(name), P::class)
        operator fun <E: Any, P: Any> invoke(name: String, pClass: KClass<P>) = TableColumnWrapper(TableColumn<E, P>(name), pClass)
    }



    override val tableView: TableViewWrapper<E>? get() = node.tableView?.wrapped()
    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }

    override fun removeFromParent() {
        node.tableView.columns.remove(node)
    }


    override val columns: ObservableList<TableColumn<E, *>> = node.columns

    override val cellFactoryProperty by lazy { node.cellFactoryProperty().toNonNullableProp() }


    val cellValueFactoryProperty by lazy {
        node.cellValueFactoryProperty().toNullableProp().proxy(
            callbackConverter<CellDataFeatures<E, P>, P>(pClass).nullable()
        )
    }


    override var cellValueFactory by cellValueFactoryProperty

    infix fun value(cvf: (CellDataFeatures<E, P>) -> P) {
        cellValueFactory =
            Callback { it: CellDataFeatures<E, P> ->
                val createdValue = cvf(it)
                BindableProperty(createdValue)
            }
    }

    @Suppress("ForbiddenAnnotation")
    @JvmName("value1")
    infix fun value(cvf: (CellDataFeatures<E, P>) -> ObsVal<P>) {
        cellValueFactory =
            Callback { it: CellDataFeatures<E, P> ->
                cvf(it)
            }
    }


    /**
     * Get the value from the property representing this TableColumn.
     */
    fun getValue(item: E): P? = getTableColumnProperty(item).value

    /**
     * Get the property representing this TableColumn for the given item.
     */
    fun getTableColumnProperty(item: E): ObsVal<P> {
        val param = CellDataFeatures(node.tableView, node, item)
        val property = cellValueFactory!!.call(param)
        return property!!
    }
}
