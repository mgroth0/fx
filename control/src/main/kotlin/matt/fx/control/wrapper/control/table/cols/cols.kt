package matt.fx.control.wrapper.control.table.cols

import javafx.collections.ObservableList
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.Callback
import matt.fx.control.wrapper.control.column.TableColumnWrapper
import matt.fx.graphics.wrapper.FXNodeWrapperDSL
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.obs.prop.ObsVal
import matt.obs.prop.writable.BindableProperty
import matt.obs.prop.writable.toVarProp
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


inline fun <E: Any, reified P: Any> ColumnsDSL<E>.column(
    title: String,
    prop: KMutableProperty1<E, P>,
    noinline op: TableColumnWrapper<E, P>.() -> Unit = {}
): TableColumnWrapper<E, P> = column(title = title, prop = prop, op = op, pClass = P::class)

inline fun <E: Any, reified P: Any> ColumnsDSL<E>.column(
    title: String? = DEFAULT_TITLE,
    prop: KProperty1<E, ObsVal<P>>,
    noinline op: TableColumnWrapper<E, P>.() -> Unit = {}
): TableColumnWrapper<E, P> = column(title = title, prop = prop, op = op, pClass = P::class)


inline fun <E: Any, reified P: Any> ColumnsDSL<E>.propCol(
    prop: KProperty1<E, ObsVal<P>>,
    noinline op: TableColumnWrapper<E, P>.() -> Unit = {}
): TableColumnWrapper<E, P> = column(prop = prop, op = op, pClass = P::class)

inline fun <E: Any, reified P: Any> ColumnsDSL<E>.column(
    title: String,
    prefWidth: Double? = DEFAULT_PREF_WIDTH,
    noinline valueProvider: (TableColumn.CellDataFeatures<E, P>) -> ObsVal<P>
): TableColumnWrapper<E, P> = column(title = title, prefWidth = prefWidth, valueProvider = valueProvider, pClass = P::class)




inline fun <E: Any, reified P: Any> ColumnsDSL<E>.column(
    title: String,
    observableFn: KFunction<ObsVal<P>>,
    noinline op: TableColumnWrapper<E, P>.() -> Unit
): TableColumnWrapper<E, P> = column(title = title, observableFn = observableFn, op = op, pClass = P::class)

inline fun <E: Any, reified P: Any> ColumnsDSL<E>.column(
    getter: KFunction<P>,
    noinline op: TableColumnWrapper<E, P>.() -> Unit = {}
): TableColumnWrapper<E, P> = column(getter = getter, op = op, pClass = P::class)

inline fun <E: Any, reified P: Any> ColumnsDSL<E>.column(
    getter: KProperty1<E, P>,
    noinline op: TableColumnWrapper<E, P>.() -> Unit = {}
): TableColumnWrapper<E, P> = column(getter = getter, op = op, pClass = P::class)

@PublishedApi
internal val DEFAULT_TITLE: String? = null
@PublishedApi
internal val DEFAULT_PREF_WIDTH: Double? = null

@FXNodeWrapperDSL
interface ColumnsDSL<E : Any> {
    fun <P: Any> column(
        pClass: KClass<P>,
        title: String,
        prop: KMutableProperty1<E, P>,
        op: TableColumnWrapper<E, P>.() -> Unit = {}
    ): TableColumnWrapper<E, P>

    fun <P: Any> column(
        pClass: KClass<P>,
        title: String? = DEFAULT_TITLE,
        prop: KProperty1<E, ObsVal<P>>,
        op: TableColumnWrapper<E, P>.() -> Unit = {}
    ): TableColumnWrapper<E, P>


    fun <P: Any> propCol(
        pClass: KClass<P>,
        prop: KProperty1<E, ObsVal<P>>,
        op: TableColumnWrapper<E, P>.() -> Unit = {}
    ): TableColumnWrapper<E, P>

    fun <P: Any> column(
        pClass: KClass<P>,
        title: String,
        prefWidth: Double? = DEFAULT_PREF_WIDTH,
        valueProvider: (TableColumn.CellDataFeatures<E, P>) -> ObsVal<P>
    ): TableColumnWrapper<E, P>

 /*   fun <P> columnDebug1(
        title: String,
        prefWidth: Double? = DEFAULT_PREF_WIDTH,
        valueProvider: (TableColumn.CellDataFeatures<E, P>) -> ObsVal<P>,
    ) = column(
        title = title,
        prefWidth = prefWidth,
        valueProvider = valueProvider
    )*/

    fun nodeColumn(
        title: String,
        prefWidth: Double? = DEFAULT_PREF_WIDTH,
        nodeProvider: (E) -> NodeWrapper
    ): TableColumnWrapper<E, NodeWrapper>

    fun <P: Any> column(
        pClass: KClass<P>,
        title: String,
        observableFn: KFunction<ObsVal<P>>,
        op: TableColumnWrapper<E, P>.() -> Unit
    ): TableColumnWrapper<E, P>

    fun <P: Any> column(
        pClass: KClass<P>,
        getter: KFunction<P>,
        op: TableColumnWrapper<E, P>.() -> Unit = {}
    ): TableColumnWrapper<E, P>

    fun <P: Any> column(
        pClass: KClass<P>,
        getter: KProperty1<E, P>,
        op: TableColumnWrapper<E, P>.() -> Unit = {}
    ): TableColumnWrapper<E, P>
}

class ColumnsDSLImpl<E : Any>(private val columns: ObservableList<TableColumn<E, *>>) : ColumnsDSL<E> {


    override fun <P: Any> column(
        pClass: KClass<P>,
        title: String,
        prop: KMutableProperty1<E, P>,
        op: TableColumnWrapper<E, P>.() -> Unit
    ) = column(pClass, title) {
        prop.call(it.value).toVarProp()
    }.also(op)

    override fun <P: Any> column(
        pClass: KClass<P>,
        title: String,
        observableFn: KFunction<ObsVal<P>>,
        op: TableColumnWrapper<E, P>.() -> Unit
    ) = column(pClass, title) {
        observableFn.call(it.value)
    }.also(op)

    override fun <P: Any> column(
        pClass: KClass<P>,
        getter: KFunction<P>,
        op: TableColumnWrapper<E, P>.() -> Unit
    ) = column(pClass, getter.name) {
        getter.call(it.value).toVarProp()
    }.apply(op)


    override fun <P: Any> column(
        pClass: KClass<P>,
        title: String?,
        prop: KProperty1<E, ObsVal<P>>,
        op: TableColumnWrapper<E, P>.() -> Unit
    ) = column(pClass, title ?: prop.name) {
        prop.call(it.value)
    }.also(op)

    override fun <P: Any> propCol(
        pClass: KClass<P>,
        prop: KProperty1<E, ObsVal<P>>,
        op: TableColumnWrapper<E, P>.() -> Unit
    ) = column(pClass, prop.name) {
        prop.call(it.value)
    }.also(op)

    override fun nodeColumn(
        title: String,
        prefWidth: Double?,
        nodeProvider: (E) -> NodeWrapper
    ) = column(NodeWrapper::class, title, prefWidth = prefWidth) {
        BindableProperty(nodeProvider(it.value))
    }.apply {
        simpleCellFactory {
            "" to it
        }
    }

    override fun <P: Any> column(
        pClass: KClass<P>,
        getter: KProperty1<E, P>,
        op: TableColumnWrapper<E, P>.() -> Unit
    ) = column(pClass, getter.name) {
        getter.call(it.value).toVarProp()
    }.apply(op)

    override fun <P: Any> column(
        pClass: KClass<P>,
        title: String,
        prefWidth: Double?,
        valueProvider: (TableColumn.CellDataFeatures<E, P>) -> ObsVal<P>
    ): TableColumnWrapper<E, P> {
        val column = TableColumnWrapper<E, P>(title, pClass)
        column.cellValueFactory =
            Callback {
                valueProvider(it)
            }
        prefWidth?.let { column.prefWidth = it }
        columns.add(column.node)
        return column
    }
}





fun <E : Any> TableColumnWrapper<E, String>.makeEditable(): TableColumnWrapper<E, String> =
    apply {
        tableView!!.isEditable = true
        cellFactory = TextFieldTableCell.forTableColumn()
    }
