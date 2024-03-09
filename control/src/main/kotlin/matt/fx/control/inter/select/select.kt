package matt.fx.control.inter.select

import matt.fx.control.toggle.mech.ToggleMechanism
import matt.fx.control.wrapper.control.value.constval.HasConstValue
import matt.lang.anno.Open
import matt.lang.common.B
import matt.obs.prop.writable.GoodVar
import matt.obs.prop.writable.Var

interface Selectable {
    val selectedProperty: GoodVar<B>
    @Open
    var isSelected
        get() = selectedProperty.value
        set(value) {
            selectedProperty.value = value
        }
}

interface SelectableValue<V: Any>: Selectable, HasConstValue<V> {
    val toggleMechanism: Var<ToggleMechanism<V>?>
}
