package matt.fx.control.wrapper.control.combo

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.scene.control.ComboBox
import javafx.scene.control.ComboBoxBase
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.SingleSelectionModel
import javafx.util.Callback
import javafx.util.StringConverter
import matt.fx.control.wrapper.cellfact.ListCellFactory
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.selects.SelectingControl
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.bind.smartBind
import matt.hurricanefx.eye.collect.asObservable
import matt.hurricanefx.eye.lib.onChange

fun <T> ComboBoxWrapper<T>.bindSelected(property: Property<T>) {
  selectionModel.selectedItemProperty().onChange {
	property.value = it
  }
}
fun <T> ET.combobox(
  property: Property<T>? = null,
  values: List<T>? = null,
  op: ComboBoxWrapper<T>.()->Unit = {}
) =
  ComboBoxWrapper<T>().attachTo(this, op) {
	if (values != null) it.items = values as? ObservableList<T> ?: values.asObservable()
	if (property != null) it.bind(property)
  }

class ComboBoxWrapper<E>(
  node: ComboBox<E> = ComboBox<E>(),
): ComboBoxBaseWrapper<E, ComboBox<E>>(node), SelectingControl<E>, ListCellFactory<ListView<E>, E> {
  companion object {
	fun <T> ComboBox<T>.wrapped() = ComboBoxWrapper(this)
  }

  constructor(items: ObservableList<E>): this(ComboBox(items))


  override val cellFactoryProperty: ObjectProperty<Callback<ListView<E>, ListCell<E>>> get() = node.cellFactoryProperty()


  var items: ObservableList<E>
	get() = node.items
	set(value) {
	  node.items = value
	}

  fun itemsProperty(): ObjectProperty<ObservableList<E>> = node.itemsProperty()

  var converter: StringConverter<E>
	get() = node.converter
	set(value) {
	  node.converter = value
	}

  fun converterProperty(): ObjectProperty<StringConverter<E>> = node.converterProperty()

  override val selectionModel: SingleSelectionModel<E> get() = node.selectionModel

}

open class ComboBoxBaseWrapper<T, N: ComboBoxBase<T>>(node: N): ControlWrapperImpl<N>(node) {

  fun editableProperty(): BooleanProperty = node.editableProperty()

  var value: T?
	get() = node.value
	set(theVal) {
	  node.value = theVal
	}

  fun valueProperty(): ObjectProperty<T> = node.valueProperty()

  var promptText: String?
	get() = node.promptText
	set(value) {
	  node.promptText = value
	}

  fun promptTextProperty(): StringProperty = node.promptTextProperty()
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }


}


fun ComboBoxBaseWrapper<*,*>.editableWhen(predicate: ObservableValue<Boolean>) = apply {
  editableProperty().bind(predicate)
}

fun <T> ComboBoxBaseWrapper<T,*>.bind(property: ObservableValue<T>, readonly: Boolean = false) =
  valueProperty().smartBind(property, readonly)