package matt.fx.control.wrapper.control.value

import matt.hurricanefx.wrapper.control.value.constval.HasConstValue
import matt.obs.prop.Var

interface HasWritableValue<V>: HasConstValue<V> {
  val valueProperty: Var<V>
  override var value: V
	get() = valueProperty.value
	set(value) {
	  valueProperty.value = value
	}
}