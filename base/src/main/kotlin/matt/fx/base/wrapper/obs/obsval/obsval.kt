package matt.fx.base.wrapper.obs.obsval

import javafx.beans.Observable
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.DoubleBinding
import javafx.beans.binding.FloatBinding
import javafx.beans.binding.IntegerBinding
import javafx.beans.binding.LongBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.binding.StringBinding
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import matt.lang.ktversion.ifPastInitialK2
import matt.log.warn.warn
import matt.obs.listen.OldAndNewListener
import matt.obs.listen.update.ValueChange
import matt.obs.prop.FXBackedPropBase
import matt.obs.prop.MObservableROValBase
import matt.obs.prop.MObservableValNewAndOld

fun <T> ObservableValue<T>.toNullableROProp() = NullableFXBackedReadOnlyBindableProp(this)
fun <T : Any> ObservableValue<T>.toNonNullableROProp() = NonNullFXBackedReadOnlyBindableProp(this)


interface FXBackedProp<FX_T> : ReadableFXBackedPropBinder<FX_T>, FXBackedPropBase

interface ReadableFXBackedPropBinder<FX_T> {
    fun booleanBinding(
        vararg dependencies: Observable,
        op: (FX_T?) -> Boolean
    ): BooleanBinding

    fun integerBinding(
        vararg dependencies: Observable,
        op: (FX_T?) -> Int
    ): IntegerBinding

    fun longBinding(
        vararg dependencies: Observable,
        op: (FX_T?) -> Long
    ): LongBinding

    fun doubleBinding(
        vararg dependencies: Observable,
        op: (FX_T?) -> Double
    ): DoubleBinding

    fun floatBinding(
        vararg dependencies: Observable,
        op: (FX_T?) -> Float
    ): FloatBinding

    fun <T> objectBinding(
        vararg dependencies: Observable,
        op: (FX_T?) -> T
    ): ObjectBinding<T>

    fun stringBinding(
        vararg dependencies: Observable,
        op: (FX_T?) -> String
    ): StringBinding
}

private class ReadingBinder<FX_T>(private val o: ObservableValue<FX_T>) : ReadableFXBackedPropBinder<FX_T> {

    override fun booleanBinding(
        vararg dependencies: Observable,
        op: (FX_T?) -> Boolean
    ): BooleanBinding =
        Bindings.createBooleanBinding({ op(o.value) }, o, *dependencies)

    override fun integerBinding(
        vararg dependencies: Observable,
        op: (FX_T?) -> Int
    ): IntegerBinding =
        Bindings.createIntegerBinding({ op(o.value) }, o, *dependencies)

    override fun longBinding(
        vararg dependencies: Observable,
        op: (FX_T?) -> Long
    ): LongBinding =
        Bindings.createLongBinding({ op(o.value) }, o, *dependencies)

    override fun doubleBinding(
        vararg dependencies: Observable,
        op: (FX_T?) -> Double
    ): DoubleBinding =
        Bindings.createDoubleBinding({ op(o.value) }, o, *dependencies)

    override fun floatBinding(
        vararg dependencies: Observable,
        op: (FX_T?) -> Float
    ): FloatBinding =
        Bindings.createFloatBinding({ op(o.value) }, o, *dependencies)

    override fun <T> objectBinding(
        vararg dependencies: Observable,
        op: (FX_T?) -> T
    ): ObjectBinding<T> =
        Bindings.createObjectBinding({ op(o.value) }, o, *dependencies)

    override fun stringBinding(
        vararg dependencies: Observable,
        op: (FX_T?) -> String
    ): StringBinding =
        Bindings.createStringBinding({ op(o.value) }, o, *dependencies)


}


open class NullableFXBackedReadOnlyBindableProp<T>(private val o: ObservableValue<T>) :
    MObservableROValBase<T?, ValueChange<T?>, OldAndNewListener<T?, ValueChange<T?>, out ValueChange<T?>>>(),
    FXBackedProp<T>,
    MObservableValNewAndOld<T?>,
    ReadableFXBackedPropBinder<T> {


    private val readingBinder = ReadingBinder(o)
    override fun booleanBinding(
        vararg dependencies: Observable,
        op: (T?) -> Boolean
    ): BooleanBinding {
        return readingBinder.booleanBinding(*dependencies, op = op)
    }

    override fun doubleBinding(
        vararg dependencies: Observable,
        op: (T?) -> Double
    ): DoubleBinding {
        return readingBinder.doubleBinding(*dependencies, op = op)
    }

    override fun floatBinding(
        vararg dependencies: Observable,
        op: (T?) -> Float
    ): FloatBinding {
        return readingBinder.floatBinding(*dependencies, op = op)
    }

    override fun integerBinding(
        vararg dependencies: Observable,
        op: (T?) -> Int
    ): IntegerBinding {
        return readingBinder.integerBinding(*dependencies, op = op)
    }

    override fun longBinding(
        vararg dependencies: Observable,
        op: (T?) -> Long
    ): LongBinding {
        return readingBinder.longBinding(*dependencies, op = op)
    }


    override fun stringBinding(
        vararg dependencies: Observable,
        op: (T?) -> String
    ): StringBinding {
        return readingBinder.stringBinding(*dependencies, op = op)
    }

    override fun <TT> objectBinding(
        vararg dependencies: Observable,
        op: (T?) -> TT
    ): ObjectBinding<TT> {
        return readingBinder.objectBinding(*dependencies, op = op)
    }


    init {
        ifPastInitialK2 {
            warn("used to delegate ReadableFXBackedPropBinder<T> by ReadingBinder(o) before bug in kotlin 2.0.0-Beta1")
        }
    }

    override val isFXBound get() = (o as? Property<*>)?.isBound ?: false

    override val value: T?
        get() {
            return o.value
        }


    init {
        o.addListener { _, old, new ->
            notifyListeners(ValueChange(old = old, new = new))
        }
    }

}

open class NonNullFXBackedReadOnlyBindableProp<T : Any>(private val o: ObservableValue<T>) :
    MObservableROValBase<T, ValueChange<T>, OldAndNewListener<T, ValueChange<T>, out ValueChange<T>>>(),
    FXBackedProp<T>,
    MObservableValNewAndOld<T>,
    ReadableFXBackedPropBinder<T> {


    override val isFXBound get() = (o as? Property<*>)?.isBound ?: false
    override val value: T
        get() {
            return o.value!!
        }

    init {
        o.addListener { _, old, new ->
            notifyListeners(ValueChange(old = old, new = new))
        }
    }

    override fun booleanBinding(
        vararg dependencies: Observable,
        op: (T?) -> Boolean
    ): BooleanBinding {
        TODO("delegated MutableSet<E> by ReadingBinder(o) until kotlin 2.0.0Beta1 <:[")
    }

    override fun integerBinding(
        vararg dependencies: Observable,
        op: (T?) -> Int
    ): IntegerBinding {
        TODO("delegated MutableSet<E> by ReadingBinder(o) until kotlin 2.0.0Beta1 <:[")
    }

    override fun longBinding(
        vararg dependencies: Observable,
        op: (T?) -> Long
    ): LongBinding {
        TODO("delegated MutableSet<E> by ReadingBinder(o) until kotlin 2.0.0Beta1 <:[")
    }

    override fun doubleBinding(
        vararg dependencies: Observable,
        op: (T?) -> Double
    ): DoubleBinding {
        TODO("delegated MutableSet<E> by ReadingBinder(o) until kotlin 2.0.0Beta1 <:[")
    }

    override fun floatBinding(
        vararg dependencies: Observable,
        op: (T?) -> Float
    ): FloatBinding {
        TODO("delegated MutableSet<E> by ReadingBinder(o) until kotlin 2.0.0Beta1 <:[")
    }

    override fun <TT> objectBinding(
        vararg dependencies: Observable,
        op: (T?) -> TT
    ): ObjectBinding<TT> {
        TODO("delegated MutableSet<E> by ReadingBinder(o) until kotlin 2.0.0Beta1 <:[")
    }

    override fun stringBinding(
        vararg dependencies: Observable,
        op: (T?) -> String
    ): StringBinding {
        TODO("delegated MutableSet<E> by ReadingBinder(o) until kotlin 2.0.0Beta1 <:[")
    }
}