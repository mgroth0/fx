package matt.fx.control.wrapper.control.treetable

import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.TreeTableView
import javafx.scene.control.TreeTableView.ResizeFeatures
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
import matt.fx.graphics.fxWidth
import matt.fx.graphics.service.nullableWrapperConverter
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.lang.common.go
import matt.obs.prop.ObsVal
import matt.obs.prop.writable.VarProp
import matt.obs.prop.writable.toVarProp
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

fun <T : Any> TreeTableViewWrapper<T>.items(): Sequence<TreeItemWrapper<T>> = root!!.recurse { it.children.map { TreeItemWrapper(it) } }

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

/* this one is different! it will apply a special width for the first coolColumn (which it assumes is for arrows) */
fun <T : Any> TreeTableViewWrapper<T>.autoResizeColumns() {
    val roo = root ?: return
    columnResizePolicy = TreeTableView.UNCONSTRAINED_RESIZE_POLICY

    columns.forEachIndexed { index, column ->
        if (index == 0) {
            column.prefWidth =
                roo.recursionDepth { it.children.map { TreeItemWrapper(it) } } * 15.0 /* guess. works with depth=2. maybe can be smaller. */
        } else {
            column.setPrefWidth(
                (
                    (
                        roo.recurseToFlat({ it.children.map { TreeItemWrapper(it) } }).map {
                            column.getCellData(it.node)
                        }.map {
                            "$it".fxWidth
                        }.toMutableList() +
                            listOf(
                                column.text.fxWidth
                            )
                    ).maxOrNull() ?: 0.0
                ) + 10.0
            )
        }
    }
}


fun <T> ET.treetableview(
    root: TreeItemWrapper<T & Any>? = null,
    op: TreeTableViewWrapper<T & Any>.() -> Unit = {}
) =
    TreeTableViewWrapper<T & Any>().attachTo(this, op) {
        if (root != null) it.root = root
    }

class TreeTableViewWrapper<E : Any>(
    node: TreeTableView<E> = TreeTableView()
) : ControlWrapperImpl<TreeTableView<E>>(node),
    TreeLikeWrapper<TreeTableView<E>, E>,
    TableLikeWrapper<TreeItem<E>> {
    override fun isInsideRow() = true

    val sortOrder: ObservableList<TreeTableColumn<E, *>> get() = node.sortOrder

    final override val rootProperty by lazy {
        node.rootProperty().toNullableProp().proxy(
            nullableWrapperConverter<TreeItem<E>, TreeItemWrapper<E>>()
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

    fun sort() = node.sort()

    inline fun <reified P: Any> column(
        title: String,
        prop: KMutableProperty1<E, P>,
        noinline op: TreeTableColumnWrapper<E, P>.() -> Unit = {}
    ): TreeTableColumnWrapper<E, P> {
        val column = TreeTableColumnWrapper.invoke2<E, P>(title)
        column.cellValueFactory =
            Callback {
                prop.call(it.value.value).toVarProp()
            } /*Matt: added null safety here way later because I ran into a NPE here... thought I went years without this null safety first so maybe the null was my fault?*/
        addColumnInternal(column)
        return column.also(op)
    }


    inline fun <reified P: Any> column(
        title: String,
        prop: KProperty1<E, P>,
        noinline op: TreeTableColumnWrapper<E, P>.() -> Unit = {}
    ): TreeTableColumnWrapper<E, P> {
        val column = TreeTableColumnWrapper.invoke2<E, P>(title)
        column.cellValueFactory =
            Callback {
                prop.call(it.value.value).toVarProp()
            } /*Matt: added null safety here way later because I ran into a NPE here... thought I went years without this null safety first so maybe the null was my fault?*/
        addColumnInternal(column)
        return column.also(op)
    }

    @Suppress("ForbiddenAnnotation")
    @JvmName(name = "columnForObservableProperty")
    inline fun <reified P: Any> column(
        title: String,
        prop: KProperty1<E, ObsVal<P>>
    ): TreeTableColumnWrapper<E, P> {
        val column = TreeTableColumnWrapper.invoke2<E, P>(title)
        column.cellValueFactory = Callback { prop.call(it.value.value) }
        addColumnInternal(column)
        return column
    }


    inline fun <reified P: Any> column(
        title: String,
        observableFn: KFunction<ObsVal<P>>
    ): TreeTableColumnWrapper<E, P> {
        val column = TreeTableColumnWrapper.invoke2<E, P>(title)
        column.cellValueFactory = Callback { observableFn.call(it.value) }
        addColumnInternal(column)
        return column
    }


    /**
     * Create a matt.hurricanefx.tableview.coolColumn with a value factory that extracts the value from the given callback.
     */
    inline fun <reified P: Any> column(
        title: String,
        crossinline valueProvider: (TreeTableColumn.CellDataFeatures<E, P>) -> ObsVal<P>
    ): TreeTableColumnWrapper<E, P> {
        val column = TreeTableColumnWrapper.invoke2<E, P>(title)
        column.cellValueFactory = Callback { valueProvider(it) }
        addColumnInternal(column)
        return column
    }





    fun <P: Any> addColumnInternal(
        column: TreeTableColumnWrapper<E, P>,
        index: Int? = null
    ) {

        val columnTarget =  columns


        if (index == null) columnTarget.add(column.node) else columnTarget.add(index, column.node)
    }


    /**
     * Matt was here!
     */
    @Suppress("ForbiddenAnnotation")
    @JvmName("coolColumn")
    inline fun <reified P: Any> column(
        getter: KFunction<P>,
        op: TreeTableColumnWrapper<E, P>.() -> Unit = {}
    ): TreeTableColumnWrapper<E, P> =
        column(getter.name) {
            VarProp(getter.call(it.value.value))
        }.apply(op)


    /**
     * Matt was here!
     */
    @Suppress("ForbiddenAnnotation")
    @JvmName("coolColumn2")
    inline fun <reified P: Any> column(
        getter: KProperty1<E, P>,
        op: TreeTableColumnWrapper<E, P>.() -> Unit = {}
    ): TreeTableColumnWrapper<E, P> =
        column(getter.name) {
            VarProp(getter.call(it.value.value))
        }.apply(op)
}





fun <T : Any> TreeTableViewWrapper<T>.populate(
    itemFactory: (T) -> TreeItemWrapper<T> = { TreeItemWrapper(it) },
    childFactory: (TreeItemWrapper<T>) -> Iterable<T>?
) = root?.go {
    populateTree(it.node, { itemFactory(it).node }, { childFactory(TreeItemWrapper(it)) })
}
