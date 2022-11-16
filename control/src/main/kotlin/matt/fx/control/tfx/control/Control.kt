@file:Suppress("UNCHECKED_CAST")

/*slightly modified code I stole from tornadofx*/

package matt.fx.control.tfx.control

import matt.collect.weak.WeakMap
import matt.fx.control.inter.select.SelectableValue
import matt.lang.weak.WeakRef
import matt.log.warn.warn
import matt.model.flowlogic.keypass.KeyPass
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
  val toggles = basicObservableSetOf<WeakRef<SelectableValue<V>>>()
  fun derefToggles(): Set<SelectableValue<V>> {
	val itr = toggles.iterator()
	val r = mutableSetOf<SelectableValue<V>>()
	while (itr.hasNext()) {
	  val n = itr.next()
	  val de = n.deref()
	  if (de == null) {
		itr.remove()
	  } else {
		r += de
	  }
	}
	return r
  }

  val selectedToggle = BindableProperty<SelectableValue<V>?>(null)
  val selectedValue = BindableProperty<V?>(null)

  fun selectToggle(toggle: SelectableValue<V>?) {
	selectedToggle.value = toggle
  }

  fun selectValue(value: V?) {
	selectedValue.value = value
  }

  init {
	val rBlocker = RecursionBlocker()
	selectedToggle.onChange {
	  require(it == null || it in derefToggles())
	  rBlocker.with {
		selectedValue.value = it?.value
	  }
	}
	selectedValue.onChange { newVal ->
	  rBlocker.with {
		selectedToggle.value = newVal?.let {
		  derefToggles().first { it.value == newVal }
		}
	  }
	}
  }

  private val listeners = WeakMap<SelectableValue<V>, Listener>()

  private var selecting = KeyPass()
  private fun didSelectToggle(toggle: SelectableValue<V>) = selecting.with {
	derefToggles().filter { it != toggle }.forEach {
	  it.isSelected = false
	}
	selectedToggle.value = toggle
  }

  private fun didUnSelectToggle(toggle: SelectableValue<V>) {
	if (selecting.isNotHeld) {
	  val theVal = selectedToggle.value
	  if (theVal != toggle) {
		warn("odd issue: expected selectedToggle.value to be $toggle but it was $theVal. I don't , but it still should be figured out.")
	  }
	  selectedToggle.value = null
	}
  }

  fun hasSelection() = selectedValue.value != null

  fun removeToggle(s: SelectableValue<V>) {
	toggles.removeAll { it.deref() == s }
  }
  fun addToggle(s: SelectableValue<V>) {
	toggles.add(WeakRef(s))
  }

  init {
	toggles.onChange { change ->
	  (change as? AdditionBase)?.addedElements?.forEach { toggle ->
		val togg = toggle.deref()!!
		listeners[togg] = togg.selectedProperty.onChangeWithAlreadyWeak(toggle) { tog: SelectableValue<V>, sel ->
		  /*println("selectedProperty of toggle ${toggle} changed to ${it}")
		  println("selectedToggle.value=${selectedToggle.value}")
		  println("selectedValue.value=${selectedValue.value}")*/

		  if (sel) didSelectToggle(tog)
		  else didUnSelectToggle(tog)
		}
		if (togg.isSelected) {
		  if (hasSelection()) togg.isSelected = false
		  else selectedToggle.value = togg
		}
	  }
	  (change as? RemovalBase)?.removedElements?.forEach { toggle ->
		if (selectedToggle.value == toggle) {
		  selectedToggle.value = null
		}
		listeners.remove(toggle.deref())?.removeListener()
	  }
	}
  }
}