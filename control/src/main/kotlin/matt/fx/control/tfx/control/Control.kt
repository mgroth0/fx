@file:Suppress("UNCHECKED_CAST")

/*slightly modified code I stole from tornadofx*/

package matt.fx.control.tfx.control

import matt.fx.control.inter.select.SelectableValue
import matt.model.flowlogic.recursionblocker.RecursionBlocker
import matt.obs.col.change.AdditionBase
import matt.obs.col.change.RemovalBase
import matt.obs.col.oset.basicObservableSetOf
import matt.obs.listen.Listener
import matt.obs.prop.BindableProperty
import matt.obs.prop.Var


/**
 * Bind the selectedValueProperty of this toggle group to the given property. Passing in a writeable value
 * will result in a bidirectional matt.klib.matt.hurricanefx.eye.collect.collectbind.bind.binding, while passing in a read only value will result in a unidirectional matt.klib.matt.hurricanefx.eye.collect.collectbind.bind.binding.
 *
 * If the toggles are configured with the value parameter (@see #togglebutton and #radiogroup), the corresponding
 * button will be selected when the value is changed. Likewise, if the selected toggle is changed,
 * the property value will be updated if it is writeable.
 */
/*fun <T> ToggleGroup.bind(property: ObservableValue<T>) = selectedValueProperty<T>().apply {
  (property as? Property<T>)?.also { bindBidirectional(it) } ?: bind(property)
}*/

/**
 * Generates a writable property that represents the selected value for this toggele group.
 * If the toggles are configured with a value (@see #togglebutton and #radiogroup) the corresponding
 * toggle will be selected when this value is changed. Likewise, if the toggle is changed by clicking
 * it, the value for the toggle will be written to this property.
 *
 * To matt.hurricanefx.eye.collect.collectbind.bind to this property, use the #ToggleGroup.matt.hurricanefx.eye.collect.collectbind.bind() function.
 */

private object SEL_VAL_PROP

/*fun <T> ToggleGroup.selectedValueProperty(): ObjectProperty<T> =
  properties.getOrPut(SEL_VAL_PROP) {
	SimpleObjectProperty<T>(((selectedToggleProperty().value as? Node)?.wrapped() as? HasConstValue<T>)?.value).apply {
	  selectedToggleProperty().toNullableROProp().onChange {
		value = ((it as? Node)?.wrapped() as? HasConstValue<T>)?.value
	  }
	  toNullableProp().onChange { selectedValue ->
		selectToggle(toggles.find {
		  ((it as Node).wrapped() as HasConstValue<T>).value == selectedValue
		})
	  }
	}
  } as ObjectProperty<T>*/


/**
 * Listen to changes and update the value of the property if the given mutator results in a different value
 */
fun <T> Var<T>.mutateOnChange(mutator: (T)->T) = onChange {
  val changed = mutator(value)
  if (changed != value) value = changed
}

/*ToggleGroup was a better name, but that is taken... */
class ToggleMechanism<V: Any>() {
  val toggles = basicObservableSetOf<SelectableValue<V>>()

  val selectedToggle = BindableProperty<SelectableValue<V>?>(null)
  val selectedValue = BindableProperty<V?>(null)

  init {
	val rBlocker = RecursionBlocker()
	selectedToggle.onChange {
	  rBlocker.with {
		selectedValue.value = it?.value
	  }
	}
	selectedValue.onChange { newVal ->
	  rBlocker.with {
		selectedToggle.value = newVal?.let {
		  toggles.first { it.value == newVal }
		}
	  }
	}
  }

  private val listeners = mutableMapOf<SelectableValue<V>, Listener>()

  init {
	toggles.onChange {
	  (it as? AdditionBase)?.addedElements?.forEach { toggle ->
		if (toggle.isSelected) {
		  if (selectedValue.value != null) {
			toggle.isSelected = false
		  } else {
			selectedToggle.value = toggle
		  }
		}
		listeners[toggle] = toggle.selectedProperty.onChange {
		  if (it) {
			toggles.forEach {
			  it.isSelected = false
			}
			if (selectedValue.value != null) {
			  toggle.isSelected = false
			} else {
			  selectedToggle.value = toggle
			}
		  }
		}
	  }
	  (it as? RemovalBase)?.removedElements?.forEach { toggle ->
		if (selectedToggle.value == toggle) {
		  selectedToggle.value = null
		}
		listeners.remove(toggle)!!.removeListener()
	  }
	}
  }
}