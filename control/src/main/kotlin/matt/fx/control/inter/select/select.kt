package matt.fx.control.inter.select

import matt.fx.control.toggle.mech.ToggleMechanism
import matt.fx.control.wrapper.control.value.constval.HasConstValue
import matt.lang.B
import matt.obs.prop.GoodVar
import matt.obs.prop.Var

interface Selectable {
  val selectedProperty: GoodVar<B>
  var isSelected
	get() = selectedProperty.value
	set(value) {
	  selectedProperty.value = value
	}
}

interface SelectableValue<V: Any>: Selectable, HasConstValue<V> {
  val toggleMechanism: Var<ToggleMechanism<V>?>
}