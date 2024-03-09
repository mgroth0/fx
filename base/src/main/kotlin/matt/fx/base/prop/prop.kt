package matt.fx.base.prop

import javafx.beans.Observable
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanExpression
import javafx.beans.binding.ObjectBinding
import javafx.beans.binding.StringBinding
import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder
import javafx.beans.value.ObservableValue
import matt.fx.base.prop.SingleAssignThreadSafetyMode.SYNCHRONIZED
import matt.lang.anno.Open
import matt.obs.col.olist.MutableObsList
import matt.prim.str.decap
import java.lang.reflect.Field
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KFunction2
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaMethod

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

fun Class<*>.findFieldByName(name: String): Field? {
    val field = (declaredFields + fields).find { it.name == name }
    if (field != null) return field
    if (superclass == java.lang.Object::class.java) return null
    return superclass.findFieldByName(name)
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



/**
 * Convert a pojo bean instance into a writable observable.
 *
 * Example: val observableName = myPojo.observable(MyPojo::getName, MyPojo::setName)
 *            or
 *          val observableName = myPojo.observable(MyPojo::getName)
 *            or
 *          val observableName = myPojo.observable("name")
 */
fun <S : Any, T : Any> S.observable(
    getter: KFunction<T>? = null,
    setter: KFunction2<S, T, Unit>? = null,
    propertyName: String? = null,
    @Suppress("UNUSED_PARAMETER") propertyType: KClass<T>? = null
): ObjectProperty<T> {
    if (getter == null && propertyName == null) throw AssertionError("Either getter or propertyName must be provided")
    val propName =
        propertyName
            ?: getter?.name?.substring(3)?.decap()

    val r =
        JavaBeanObjectPropertyBuilder.create().apply {
            bean(this@observable)
            name(propName)
            if (getter != null) this.getter(getter.javaMethod)
            if (setter != null) this.setter(setter.javaMethod)
        }.build()


    @Suppress("UNCHECKED_CAST")
    return r as ObjectProperty<T>
}

enum class SingleAssignThreadSafetyMode {
    SYNCHRONIZED,
    NONE
}

fun <T> singleAssign(threadSafetyMode: SingleAssignThreadSafetyMode = SYNCHRONIZED): SingleAssign<T> =
    if (threadSafetyMode == SYNCHRONIZED) SynchronizedSingleAssign() else UnSynchronizedSingleAssign()

interface SingleAssign<T> {
    fun isInitialized(): Boolean
    operator fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): T

    operator fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: T
    )
}

private class SynchronizedSingleAssign<T> : UnSynchronizedSingleAssign<T>() {

    @Volatile
    override var _value: Any? = UnInitializedValue

    override operator fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: T
    ) = synchronized(this) {
        super.setValue(thisRef, property, value)
    }
}

private open class UnSynchronizedSingleAssign<T> : SingleAssign<T> {

    protected object UnInitializedValue

    protected open var _value: Any? = UnInitializedValue

    final override operator fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): T {
        if (!isInitialized()) throw UninitializedPropertyAccessException("Value has not been assigned yet!")
        @Suppress("UNCHECKED_CAST")
        return _value as T
    }

    @Open
    override operator fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: T
    ) {
        if (isInitialized()) throw Exception("Value has already been assigned!")
        _value = value
    }

    final override fun isInitialized() = _value != UnInitializedValue
}

/**
 * Binds this property to an observable, automatically unbinding it before if already bound.
 */
fun <T> Property<T>.cleanBind(observable: ObservableValue<T>) {
    unbind()
    bind(observable)
}

/**
 * A Boolean matt.klib.matt.hurricanefx.eye.collect.collectbind.bind.binding that tracks all items in an observable list and create an observable boolean
 * value by anding together an observable boolean representing each element in the observable list.
 * Whenever the list changes, the matt.klib.matt.hurricanefx.eye.collect.collectbind.bind.binding is updated as well
 */
fun <T : Any> booleanListBinding(
    list: MutableObsList<T>,
    defaultValue: Boolean = false,
    itemToBooleanExpr: T.() -> BooleanExpression
): BooleanExpression {
    val facade = SimpleBooleanProperty()
    fun rebind() {
        if (list.isEmpty()) {
            facade.unbind()
            facade.value = defaultValue
        } else {
            facade.cleanBind(list.map(itemToBooleanExpr).reduce { a, b -> a.and(b) })
        }
    }
    list.onChange { rebind() }
    rebind()
    return facade
}


fun <T> ObservableValue<T>.stringBinding(
    vararg dependencies: Observable,
    op: (T?) -> String
): StringBinding =
    Bindings.createStringBinding({ op(value) }, this, *dependencies)

fun <T, R> ObservableValue<T>.objectBindingN(
    vararg dependencies: Observable,
    op: (T?) -> R?
): ObjectBinding<R?> =
    Bindings.createObjectBinding({ op(value) }, this, *dependencies)


