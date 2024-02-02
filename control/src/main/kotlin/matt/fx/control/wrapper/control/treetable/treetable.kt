package matt.fx.control.wrapper.control.treetable

import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.TreeTablePosition
import javafx.scene.control.TreeTableRow
import javafx.scene.control.TreeTableView
import javafx.scene.control.TreeTableView.ResizeFeatures
import javafx.scene.input.InputEvent
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.util.Callback
import matt.collect.itr.recurse.depth.recursionDepth
import matt.collect.itr.recurse.recurse
import matt.collect.itr.recurse.recurseToFlat
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.tablelike.TableLikeWrapper
import matt.fx.control.wrapper.control.tree.like.TreeLikeWrapper
import matt.fx.control.wrapper.control.tree.like.populateTree
import matt.fx.control.wrapper.control.treecol.TreeTableColumnWrapper
import matt.fx.control.wrapper.selects.wrap
import matt.fx.control.wrapper.treeitem.TreeItemWrapper
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.fxWidth
import matt.fx.graphics.service.uncheckedNullableWrapperConverter
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.lang.go
import matt.obs.prop.ObsVal
import matt.obs.prop.VarProp
import matt.obs.prop.toVarProp
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

fun <T : Any> TreeTableViewWrapper<T>.items(): Sequence<TreeItemWrapper<T>> = root!!.recurse { it.children }

fun <T : Any> TreeTableViewWrapper<T>.select(o: T?) {
    Platform.runLater {
        when {
            o != null -> {
                selectionModel.select(items().firstOrNull { it == o }?.node)
            }

            else      -> selectionModel.clearSelection()
        }
    }
}

// this one is different! it will apply a special width for the first coolColumn (which it assumes is for arrows)
fun <T : Any> TreeTableViewWrapper<T>.autoResizeColumns() {
    val roo = root ?: return
    columnResizePolicy = TreeTableView.UNCONSTRAINED_RESIZE_POLICY

    columns.forEachIndexed { index, column ->
        if (index == 0) {
            column.prefWidth =
                roo.recursionDepth { it.children } * 15.0 // guess. works with depth=2. maybe can be smaller.
        } else {
            column.setPrefWidth(
                ((roo.recurseToFlat({ it.children }).map {
                    column.getCellData(it.node)
                }.map {
                    "$it".fxWidth
                }.toMutableList() + listOf(
                    column.text.fxWidth
                )).maxOrNull() ?: 0.0) + 10.0
            )
        }
    }
}

/*TreeTableViewWrapper<T & Any> before K2*/
val <T: Any> TreeTableViewWrapper<T>.selectedCell: TreeTablePosition<T, *>?
    get() = selectionModel.selectedCells.firstOrNull()


/*TreeTableViewWrapper<T & Any> before K2*/
val <T: Any> TreeTableViewWrapper<T>.selectedColumn: TreeTableColumn<T, *>?
    get() = selectedCell?.tableColumn

//val <T> TreeTableViewWrapper<T>.selectedValue: Any?
//  get() = selectedColumn?.getCellObservableValue(selectionModel.selectedItem)?.value

fun <T> ET.treetableview(
    root: TreeItemWrapper<T & Any>? = null,
    op: TreeTableViewWrapper<T & Any>.() -> Unit = {}
) =
    TreeTableViewWrapper<T & Any>().attachTo(this, op) {
        if (root != null) it.root = root
    }

class TreeTableViewWrapper<E : Any>(
    node: TreeTableView<E> = TreeTableView(),
) : ControlWrapperImpl<TreeTableView<E>>(node),
    TreeLikeWrapper<TreeTableView<E>, E>,
    TableLikeWrapper<TreeItem<E>> {
    override fun isInsideRow() = true

    fun editableProperty(): BooleanProperty = node.editableProperty()

    val sortOrder: ObservableList<TreeTableColumn<E, *>> get() = node.sortOrder

    final override val rootProperty by lazy {
        node.rootProperty().toNullableProp().proxy(
            uncheckedNullableWrapperConverter<TreeItem<E>, TreeItemWrapper<E>>()
        )
    }

    override var isShowRoot: Boolean
        get() = node.isShowRoot
        set(value) {
            node.isShowRoot = value
        }

    var columnResizePolicy: Callback<ResizeFeatures<*>, Boolean>
        get() = node.columnResizePolicy
        set(value) {
            node.columnResizePolicy = value
        }


    override val columns: ObservableList<TreeTableColumn<E, *>> get() = node.columns


    override fun getRow(ti: TreeItem<E>) = node.getRow(ti)

    override val selectionModel by lazy { node.selectionModel.wrap() }
    override fun scrollTo(i: Int) = node.scrollTo(i)
    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }

    fun setRowFactory(value: Callback<TreeTableView<E>, TreeTableRow<E>>) = node.setRowFactory(value)

    fun sort() = node.sort()

    inline fun <reified P> column(
        title: String,
        prop: KMutableProperty1<E, P>,
        noinline op: TreeTableColumnWrapper<E, P>.() -> Unit = {}
    ): TreeTableColumnWrapper<E, P> {
        val column = TreeTableColumnWrapper<E, P>(title)
        column.cellValueFactory = Callback {
            prop.call(it.value.value).toVarProp()
            //	  it.value.value?.let {
            //		observable(
            //		  it, prop
            //		)
            //	  }
        } /*Matt: added null safety here way later because I ran into a NPE here... thought I went years without this null safety first so maybe the null was my fault?*/
        addColumnInternal(column)
        return column.also(op)
    }


    inline fun <reified P> column(
        title: String,
        prop: KProperty1<E, P>,
        noinline op: TreeTableColumnWrapper<E, P>.() -> Unit = {}
    ): TreeTableColumnWrapper<E, P> {
        val column = TreeTableColumnWrapper<E, P>(title)
        column.cellValueFactory = Callback {
            prop.call(it.value.value).toVarProp()
            //	  it.value.value?.let {
            //		observable(
            //		  it, prop
            //		)
            //	  }
        } /*Matt: added null safety here way later because I ran into a NPE here... thought I went years without this null safety first so maybe the null was my fault?*/
        addColumnInternal(column)
        return column.also(op)
    }


    @JvmName(name = "columnForObservableProperty")
    fun <P> column(
        title: String,
        prop: KProperty1<E, ObsVal<P>>
    ): TreeTableColumnWrapper<E, P> {
        val column = TreeTableColumnWrapper<E, P>(title)
        column.cellValueFactory = Callback { prop.call(it.value.value) }
        addColumnInternal(column)
        return column
    }


    inline fun <reified P> column(
        title: String,
        observableFn: KFunction<ObsVal<P>>
    ): TreeTableColumnWrapper<E, P> {
        val column = TreeTableColumnWrapper<E, P>(title)
        column.cellValueFactory = Callback { observableFn.call(it.value) }
        addColumnInternal(column)
        return column
    }


    /**
     * Create a matt.hurricanefx.tableview.coolColumn with a value factory that extracts the value from the given callback.
     */
    fun <P> column(
        title: String,
        valueProvider: (TreeTableColumn.CellDataFeatures<E, P>) -> ObsVal<P>
    ): TreeTableColumnWrapper<E, P> {
        val column = TreeTableColumnWrapper<E, P>(title)
        column.cellValueFactory = Callback { valueProvider(it) }
        addColumnInternal(column)
        return column
    }


    /**
     * Create a matt.hurricanefx.tableview.coolColumn holding matt.fx.control.layout.children columns
     */
    @Suppress("UNCHECKED_CAST")
    fun nestedColumn(
        title: String,
        op: TreeTableViewWrapper<E>.() -> Unit = {}
    ): TreeTableColumnWrapper<E, Any?> {
        val column = TreeTableColumnWrapper<E, Any?>(title)
        addColumnInternal(column)
        val previousColumnTarget = node.properties["tornadofx.columnTarget"] as? ObservableList<TableColumn<E, *>>
        node.properties["tornadofx.columnTarget"] = column.columns
        op(this)
        node.properties["tornadofx.columnTarget"] = previousColumnTarget
        return column
    }


    //  /**
    //   * Create a matt.hurricanefx.tableview.coolColumn using the propertyName of the attribute you want shown.
    //   */
    //  fun <P> column(
    //	title: String,
    //	propertyName: String,
    //	op: TreeTableColumnWrapper<E, P>.()->Unit = {}
    //  ): TreeTableColumnWrapper<E, P> {
    //	val column = TreeTableColumnWrapper<E, P>(title)
    //	column.cellValueFactory = TreeItemPropertyValueFactory<E, P>(propertyName)
    //	addColumnInternal(column)
    //	return column.also(op)
    //  }


    //  /**
    //   * Create a matt.hurricanefx.tableview.coolColumn using the getter of the attribute you want shown.
    //   */
    //  @JvmName("pojoColumn")
    //  fun <P> column(title: String, getter: KFunction<P>): TreeTableColumnWrapper<E, P> {
    //	val startIndex = if (getter.name.startsWith("is") && getter.name[2].isUpperCase()) 2 else 3
    //	val propName = getter.name.substring(startIndex).decap()
    //	return this.column(title, propName)
    //  }


    fun <P> addColumnInternal(
        column: TreeTableColumnWrapper<E, P>,
        index: Int? = null
    ) {
        @Suppress("UNCHECKED_CAST")
        val columnTarget =
            node.properties["tornadofx.columnTarget"] as? ObservableList<TreeTableColumn<E, *>> ?: columns
        if (index == null) columnTarget.add(column.node) else columnTarget.add(index, column.node)
    }


    /**
     * Matt was here!
     */
    @JvmName("coolColumn")
    inline fun <P> column(
        getter: KFunction<P>,
        op: TreeTableColumnWrapper<E, P>.() -> Unit = {}
    ): TreeTableColumnWrapper<E, P> = column(getter.name) {
        VarProp(getter.call(it.value.value))
    }.apply(op)


    /**
     * Matt was here!
     */
    @JvmName("coolColumn2")
    inline fun <P> column(
        getter: KProperty1<E, P>,
        op: TreeTableColumnWrapper<E, P>.() -> Unit = {}
    ): TreeTableColumnWrapper<E, P> = column(getter.name) {
        VarProp(getter.call(it.value.value))
    }.apply(op)

}

//fun <T> TreeTableViewWrapper<T & Any>.selectFirst() = selectionModel.selectFirst()


fun <T> TreeTableViewWrapper<T & Any>.bindSelected(property: Property<T>) {
    selectionModel.selectedItemProperty.onChange {
        property.value = it?.value
    }
}


/**
 * Execute action when the enter key is pressed or the mouse is clicked

 * @param clickCount The number of mouse clicks to trigger the action
 * *
 * @param action The action to execute on select
 */
fun <T> TreeTableViewWrapper<T & Any>.onUserSelect(
    clickCount: Int = 2,
    action: (T) -> Unit
) {
    val isSelected = { event: InputEvent ->
        event.target.wrapped().isInsideRow() && !selectionModel.selectionIsEmpty()
    }

    addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
        if (event.clickCount == clickCount && isSelected(event))
            action(selectedItem!!.value)
    }

    addEventFilter(KeyEvent.KEY_PRESSED) { event ->
        if (event.code == KeyCode.ENTER && !event.isMetaDown && isSelected(event))
            action(selectedItem!!.value)
    }
}


fun <T : Any> TreeTableViewWrapper<T>.populate(
    itemFactory: (T) -> TreeItemWrapper<T> = { TreeItemWrapper(it) },
    childFactory: (TreeItemWrapper<T>) -> Iterable<T>?
) = root?.go {
    populateTree(it, itemFactory, childFactory)
}


fun TreeTableViewWrapper<*>.editableWhen(predicate: ObservableValue<Boolean>) = apply {
    editableProperty().bind(predicate)
}

fun <T : Any> TreeTableViewWrapper<T>.multiSelect(enable: Boolean = true) {
    selectionModel.selectionMode = if (enable) SelectionMode.MULTIPLE else SelectionMode.SINGLE
}
