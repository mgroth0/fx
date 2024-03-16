package matt.fx.base.prop

import javafx.beans.Observable
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import matt.prim.str.decap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KFunction
import kotlin.reflect.KFunction2
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

fun <T> property(value: T? = null): FXPropertyDelegate<T> = FXPropertyDelegate(SimpleObjectProperty<T>(value))
fun <T> property(block: () -> Property<T>) = FXPropertyDelegate(block())

class FXPropertyDelegate<T>(val fxProperty: Property<T>) : ReadWriteProperty<Any, T?> {

    override fun getValue(
        thisRef: Any,
        property: KProperty<*>
    ): T? = fxProperty.value

    override fun setValue(
        thisRef: Any,
        property: KProperty<*>,
        value: T?
    ) {
        fxProperty.value = value
    }
}

/**
 * Convert an owner instance and a corresponding property reference into an observable
 */
fun <S, T> S.observable(prop: KMutableProperty1<S, T>) = observable(this, prop)



/**
 * Convert an owner instance and a corresponding property reference into a readonly observable
 */
fun <S, T> observable(
    owner: S,
    prop: KProperty1<S, T>
): ReadOnlyObjectProperty<T> =
    object : ReadOnlyObjectWrapper<T>(owner, prop.name) {
        override fun get() = prop.get(owner)
    }

/**
 * Convert an bean instance and a corresponding getter/setter reference into a writable observable.
 *
 * Example: val observableName = observable(myPojo, MyPojo::getName, MyPojo::setName)
 */
fun <S : Any, T> observable(
    bean: S,
    getter: KFunction<T>,
    setter: KFunction2<S, T, Unit>
): PojoProperty<T> {
    val propName = getter.name.substring(3).decap()

    return object : PojoProperty<T>(bean, propName) {
        override fun get() = getter.call(bean)
        override fun set(newValue: T) {
            setter.invoke(bean, newValue)
        }
    }
}

open class PojoProperty<T>(
    bean: Any,
    propName: String
) : SimpleObjectProperty<T>(bean, propName) {
    fun refresh() {
        fireValueChangedEvent()
    }
}

fun <T> ObservableValue<T>.stringBinding(
    vararg dependencies: Observable,
    op: (T?) -> String
): StringBinding =
    Bindings.createStringBinding({ op(value) }, this, *dependencies)
