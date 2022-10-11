package matt.fx.control.wrapper.control.combo

import javafx.beans.property.ObjectProperty
import javafx.beans.property.StringProperty
import javafx.collections.ObservableList
import javafx.scene.control.ComboBox
import javafx.scene.control.ComboBoxBase
import javafx.scene.control.ListView
import javafx.util.StringConverter
import matt.fx.control.wrapper.cellfact.ListCellFactory
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.selects.SelectingControl
import matt.fx.control.wrapper.selects.wrap
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.collect.asObservable
import matt.hurricanefx.eye.wrapper.obs.collect.createFXWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp
import matt.obs.bind.smartBind
import matt.obs.col.olist.ObsList
import matt.obs.prop.ObsVal
import matt.obs.prop.ValProp
import matt.obs.prop.VarProp

fun <T: Any> ComboBoxWrapper<T>.bindSelected(property: VarProp<T?>) {
  selectionModel.selectedItemProperty.onChange {
	property.value = it
  }
}

fun <T: Any> ET.combobox(
  property: VarProp<T?>? = null,
  values: List<T>? = null,
  op: ComboBoxWrapper<T>.()->Unit = {}
) =
  ComboBoxWrapper<T>().attachTo(this, op) {
	if (values != null) it.items = values as? ObservableList<T> ?: values.asObservable()
	if (property != null) it.bind(property)
  }

class ComboBoxWrapper<E: Any>(
  node: ComboBox<E> = ComboBox<E>(),
): ComboBoxBaseWrapper<E, ComboBox<E>>(node), SelectingControl<E>, ListCellFactory<ListView<E>, E> {

  constructor(items: ObsList<E>): this(ComboBox<E>(items.createFXWrapper()))


  override val cellFactoryProperty by lazy { node.cellFactoryProperty().toNonNullableProp() }


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

  override val selectionModel by lazy { node.selectionModel.wrap() }

}

open class ComboBoxBaseWrapper<T: Any, N: ComboBoxBase<T>>(node: N): ControlWrapperImpl<N>(node) {

  val editableProperty by lazy { node.editableProperty().toNonNullableProp() }

  var value: T?
	get() = node.value
	set(theVal) {
	  node.value = theVal
	}

  val valueProperty by lazy { node.valueProperty().toNullableProp() }

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


fun ComboBoxBaseWrapper<*, *>.editableWhen(predicate: ObsVal<Boolean>) = apply {
  editableProperty.bind(predicate)
}

fun <T: Any> ComboBoxBaseWrapper<T, *>.bind(property: ValProp<T?>, readonly: Boolean = false) =
  valueProperty.smartBind(property, readonly)