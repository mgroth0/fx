package matt.fx.control.wrapper.control.list

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.MultipleSelectionModel
import javafx.util.Callback
import matt.hurricanefx.wrapper.cellfact.ListCellFactory
import matt.hurricanefx.wrapper.control.ControlWrapperImpl
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.hurricanefx.wrapper.selects.SelectingControl


open class ListViewWrapper<E>(
   node: ListView<E> = ListView<E>(),
): ControlWrapperImpl<ListView<E>>(node), SelectingControl<E>, ListCellFactory<ListView<E>, E> {

  constructor(items: ObservableList<E>): this(ListView(items))

  fun scrollTo(i: Int) = node.scrollTo(i)
  fun scrollTo(e: E) = node.scrollTo(e)

  override val cellFactoryProperty: ObjectProperty<Callback<ListView<E>, ListCell<E>>> get() = node.cellFactoryProperty()

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

  override val selectionModel: MultipleSelectionModel<E> get() = node.selectionModel
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }


}


fun <T> ListViewWrapper<T>.selectWhere(scrollTo: Boolean = true, condition: (T)->Boolean) {
  items.asSequence().filter(condition).forEach {
	selectionModel.select(it)
	if (scrollTo) scrollTo(it)
  }
}


fun ListViewWrapper<*>.editableWhen(predicate: ObservableValue<Boolean>) = apply {
  editableProperty().bind(predicate)
}