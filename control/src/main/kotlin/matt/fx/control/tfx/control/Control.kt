@file:Suppress("UNCHECKED_CAST")

/*slightly modified code I stole from tornadofx*/

package matt.fx.control.tfx.control

import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.ToggleGroup
import matt.fx.control.wrapper.control.value.constval.HasConstValue
import matt.fx.control.wrapper.wrapped.wrapped
import matt.hurricanefx.eye.lib.onChange
import matt.obs.prop.Var


/**
 * Bind the selectedValueProperty of this toggle group to the given property. Passing in a writeable value
 * will result in a bidirectional matt.klib.matt.hurricanefx.eye.collect.collectbind.bind.binding, while passing in a read only value will result in a unidirectional matt.klib.matt.hurricanefx.eye.collect.collectbind.bind.binding.
 *
 * If the toggles are configured with the value parameter (@see #togglebutton and #radiogroup), the corresponding
 * button will be selected when the value is changed. Likewise, if the selected toggle is changed,
 * the property value will be updated if it is writeable.
 */
fun <T> ToggleGroup.bind(property: ObservableValue<T>) = selectedValueProperty<T>().apply {
  (property as? Property<T>)?.also { bindBidirectional(it) } ?: bind(property)
}

/**
 * Generates a writable property that represents the selected value for this toggele group.
 * If the toggles are configured with a value (@see #togglebutton and #radiogroup) the corresponding
 * toggle will be selected when this value is changed. Likewise, if the toggle is changed by clicking
 * it, the value for the toggle will be written to this property.
 *
 * To matt.hurricanefx.eye.collect.collectbind.bind to this property, use the #ToggleGroup.matt.hurricanefx.eye.collect.collectbind.bind() function.
 */

private object SEL_VAL_PROP

fun <T> ToggleGroup.selectedValueProperty(): ObjectProperty<T> =
  properties.getOrPut(SEL_VAL_PROP) {
	SimpleObjectProperty<T>(((selectedToggleProperty().value as? Node)?.wrapped() as? HasConstValue<T>)?.value).apply {
	  selectedToggleProperty().onChange {
		value = ((it as? Node)?.wrapped() as? HasConstValue<T>)?.value
	  }
	  onChange { selectedValue ->
		selectToggle(toggles.find {
		  ((it as Node).wrapped() as HasConstValue<T>).value == selectedValue
		})
	  }
	}
  } as ObjectProperty<T>


/**
 * Listen to changes and update the value of the property if the given mutator results in a different value
 */
fun <T> Var<T>.mutateOnChange(mutator: (T)->T) = onChange {
  val changed = mutator(value)
  if (changed != value) value = changed
}


