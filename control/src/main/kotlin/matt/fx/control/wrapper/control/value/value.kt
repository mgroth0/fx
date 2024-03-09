package matt.fx.control.wrapper.control.value

import matt.fx.control.wrapper.control.value.constval.HasConstValue
import matt.lang.anno.Open
import matt.obs.prop.writable.Var

interface HasWritableValue<V>: HasConstValue<V> {
    val valueProperty: Var<V>
    @Open
    override var value: V
        get() = valueProperty.value
        set(value) {
            valueProperty.value = value
        }
}
