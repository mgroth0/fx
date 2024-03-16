package matt.fx.control.wrapper.control.table

import javafx.application.Platform.runLater
import javafx.collections.ObservableList
import javafx.scene.control.TableColumn
import javafx.scene.control.TablePosition
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.control.TableView.ResizeFeatures
import javafx.scene.control.TableView.TableViewFocusModel
import javafx.util.Callback
import matt.async.thread.namedThread
import matt.fx.base.wrapper.obs.collect.list.mfxMutableListConverter
import matt.fx.base.wrapper.obs.obsval.NullableFXBackedReadOnlyBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.base.wrapper.obs.obsval.toNullableROProp
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.table.cols.ColumnsDSL
import matt.fx.control.wrapper.control.table.cols.ColumnsDSLImpl
import matt.fx.control.wrapper.control.tablelike.TableLikeWrapper
import matt.fx.control.wrapper.selects.wrap
import matt.fx.graphics.fxWidth
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.lang.common.go
import matt.lang.function.Op
import matt.lang.setall.setAll
import matt.obs.bind.binding
import matt.obs.col.olist.ImmutableObsList
import matt.obs.col.olist.MutableObsList
import matt.obs.col.olist.toMutableObsList
import matt.obs.prop.ObsVal
import matt.time.dur.sleep
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun <T : Any> ET.tableview(
    items: ImmutableObsList<T>? = null,
    op: TableViewWrapper<T>.() -> Unit = {}
) =
    TableViewWrapper<T>().attachTo(this, op) {
        if (items != null) {
            if (items is MutableObsList<T>) {
                it.items = items
            } else {
                it.items =
                    items.toMutableObsList().apply {
                        items.onChange {
                            setAll(items)
                        }
                    }
            }
        }
    }

fun <T : Any> ET.tableview(
    items: ObsVal<out MutableObsList<T>>,
    op: TableViewWrapper<T>.() -> Unit = {}
) =
    TableViewWrapper<T>().attachTo(this, op) {
        it.itemsProperty.bind(items.binding { it })
    }

open class TableViewWrapper<E : Any>(
    node: TableView<E> = TableView<E>()
    /*private val elementClass: KClass<E>*/
) : ControlWrapperImpl<TableView<E>>(node), TableLikeWrapper<E>, ColumnsDSL<E> by ColumnsDSLImpl<E>(node.columns) {

    companion object {
        /*inline operator fun <reified E: Any> invoke(items: ObservableList<E>) = TableViewWrapper(TableView(items)


        ,E::class




        )*/
        operator fun <E: Any> invoke(items: ObservableList<E>) = TableViewWrapper(TableView(items)/*,E::class*/)
    }


    fun setRowFactory(value: Callback<TableView<E>, TableRow<E>>) = node.setRowFactory(value)


    final override fun isInsideRow() = true

    val editableProperty: NonNullFXBackedBindableProp<Boolean> by lazy { node.editableProperty().toNonNullableProp() }

    var isEditable by editableProperty

    fun refresh() = node.refresh()


/*
    val sortOrder by lazy {
        node.sortOrder.createMutableWrapper().toSyncedList(
            wrapperConverter(
                TableColumn::class,
                TableColumnWrapper::class
            )
        )
    }
*/

    val columnResizePolicyProperty: NonNullFXBackedBindableProp<Callback<ResizeFeatures<Any>, Boolean>> by lazy {
        node.columnResizePolicyProperty().toNonNullableProp()
    }
    var columnResizePolicy by columnResizePolicyProperty


    val itemsProperty by lazy { node.itemsProperty().toNullableProp().proxy(mfxMutableListConverter<E>().nullable()) }
    var items by itemsProperty

    val comparatorProperty by lazy { node.comparatorProperty().toNullableROProp() }
    val comparator by comparatorProperty

    final override val columns: ObservableList<TableColumn<E, *>> get() = node.columns

    final override val selectionModel by lazy { node.selectionModel.wrap() }
    fun scrollTo(i: Int) = node.scrollTo(i)
    fun scrollToWithWeirdDirtyFix(
        i: Int,
        recursionLevel: Int = 5,
        sleepTime: Duration = 100.milliseconds,
        callback: Op = {}
    ) {
        scrollTo(i) /*scrollTo is not working well... hope this helps*/
        if (recursionLevel > 0) {
            namedThread(isDaemon = true, name = "scrollToWithWeirdDirtyFix Thread") {
                sleep(sleepTime)
                runLater {
                    scrollToWithWeirdDirtyFix(
                        i,
                        recursionLevel = recursionLevel - 1,
                        sleepTime = sleepTime,
                        callback = callback
                    )
                }
            }
        } else {
            callback()
        }
    }

    fun scrollTo(e: E) = node.scrollTo(e)
    val focusModel: TableViewFocusModel<E> get() = node.focusModel
    fun sort() = node.sort()
    val editingCellProperty: NullableFXBackedReadOnlyBindableProp<TablePosition<E, *>> by lazy { node.editingCellProperty().toNullableROProp() }
    fun edit(
        row: Int,
        col: TableColumn<E, *>
    ) = node.edit(row, col)


    final override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }





    /* call the method after inserting the data into table */
    fun autoResizeColumns() {
        columnResizePolicy = TableView.UNCONSTRAINED_RESIZE_POLICY
        columns.associateWith { column ->

            val dataList =
                (0..<items!!.size).map {
                    column.getCellData(it)
                }
            if (dataList.any { it is NW }) {
                null /*prevent resizing of nodeColumn which is managed separately. Trying to resize those here leads to issues because getCellData() returns a different node then the one being displayed*/
            } else {
                val textWidths =
                    dataList.mapNotNull {
                        it?.toString()?.fxWidth ?: 0.0
                    }.toTypedArray()

                val widths =
                    listOf(
                        *textWidths,
                        column.text.fxWidth
                    )
                val bareMinW = widths.maxOrNull() ?: 0.0
                bareMinW + 10.0
            }
        }.forEach { (column, w) ->
            w?.go(column::setPrefWidth)
        }
    }






    /*val selectedCell: TablePosition<E, *>?
        get() = selectionModel.selectedCells.firstOrNull() as TablePosition<E, *>?*/
}



