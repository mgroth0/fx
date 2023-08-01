package matt.fx.control.wrapper.control.list

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.scene.control.ListView
import javafx.scene.control.cell.TextFieldListCell
import matt.fx.base.wrapper.obs.collect.list.createFXWrapper
import matt.fx.base.wrapper.obs.collect.list.createMutableFXWrapper
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.control.wrapper.cellfact.ListCellFactory
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.selects.SelectingControl
import matt.fx.control.wrapper.selects.wrap
import matt.fx.graphics.hotkey.hotkeys
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.obs.col.olist.ImmutableObsList
import matt.obs.col.olist.MutableObsList


fun <T : Any> ET.listview(
    values: ImmutableObsList<T>? = null,
    op: ListViewWrapper<T>.() -> Unit = {}
) =
    ListViewWrapper<T>().attachTo(this, op) {
        if (values != null) {
            it.items = (values as? MutableObsList)?.createMutableFXWrapper()  ?: values.createFXWrapper()
        }
    }

//fun <T> ET.listview(values: ReadOnlyListProperty<T>, op: ListViewWrapper<T>.()->Unit = {}) =
//  listview(values as ObservableValue<ObservableList<T>>, op)

fun <T : Any> ET.listview(
    values: ObservableValue<ObservableList<T>>,
    op: ListViewWrapper<T>.() -> Unit = {}
) = ListViewWrapper<T>().attachTo(this, op) {
    it.itemsProperty().bind(values)
}

open class ListViewWrapper<E : Any>(
    node: ListView<E> = ListView<E>(),
) : ControlWrapperImpl<ListView<E>>(node), SelectingControl<E>, ListCellFactory<ListView<E>, E> {

    constructor(items: ObservableList<E>) : this(ListView(items))
//    constructor(items: MutableObsList<E>) : this(items.createMutableFXWrapper())
    constructor(items: ImmutableObsList<E>) : this((items as? MutableObsList)?.createMutableFXWrapper()  ?: items.createFXWrapper())

    fun scrollTo(i: Int) = node.scrollTo(i)
    fun scrollTo(e: E) = node.scrollTo(e)

    override val cellFactoryProperty by lazy { node.cellFactoryProperty().toNullableProp() }

    var isEditable
        get() = node.isEditable
        set(value) {
            node.isEditable = value
        }

    fun editableProperty(): BooleanProperty = node.editableProperty()

    var items: ObservableList<E>
        get() = node.items
        set(value) {
            node.items = value
        }

    fun itemsProperty(): ObjectProperty<ObservableList<E>> = node.itemsProperty()

    override val selectionModel by lazy { node.selectionModel.wrap() }
    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO("Not yet implemented")
    }


}


fun <T : Any> ListViewWrapper<T>.selectWhere(
    scrollTo: Boolean = true,
    condition: (T) -> Boolean
) {
    items.asSequence().filter(condition).forEach {
        selectionModel.select(it)
        if (scrollTo) scrollTo(it)
    }
}


fun ListViewWrapper<*>.editableWhen(predicate: ObservableValue<Boolean>) = apply {
    editableProperty().bind(predicate)
}


fun ListViewWrapper<String>.setupForModifying(
    defaultNew: String = "new item"
) {
    isEditable = true
    cellFactory = TextFieldListCell.forListView()
    hotkeys {
        N.meta {
            items.add(defaultNew)
        }
        BACK_SPACE.meta {
            val i = selectionModel.selectedIndex
            if (i != null) {
                items.removeAt(i)
            }
        }
    }
}