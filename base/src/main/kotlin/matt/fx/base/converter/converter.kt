package matt.fx.base.converter

import javafx.beans.value.ObservableValue
import javafx.util.Callback
import javafx.util.StringConverter
import matt.fx.base.mtofx.createROFXPropWrapper
import matt.fx.base.wrapper.obs.obsval.toNullableROProp
import matt.lang.cast.castFun
import matt.lang.convert.BiConverter
import matt.obs.prop.ObsVal
import kotlin.reflect.KClass

@Suppress("ForbiddenAnnotation")
@JvmName("toFXConverter2")
fun <T> BiConverter<String, T>.toFXConverter() =
    object : StringConverter<T>() {
        override fun toString(`object`: T) = convertToA(`object`)
        override fun fromString(string: String) = convertToB(string)
    }


fun <T> StringConverter<T>.toMConverter() =
    object : matt.prim.converters.StringConverter<T> {
        override fun toString(t: T): String = this@toMConverter.toString(t)

        override fun fromString(s: String): T = this@toMConverter.fromString(s)
    }

class ConverterConverter<T>() : BiConverter<StringConverter<T>, matt.prim.converters.StringConverter<T>> {
    override fun convertToB(a: StringConverter<T>): matt.prim.converters.StringConverter<T> = a.toMConverter()

    override fun convertToA(b: matt.prim.converters.StringConverter<T>): StringConverter<T> = b.toFXConverter()
}

inline fun <I, reified O: Any> callbackConverter() = callbackConverter<I, O>(O::class)
fun <I, O: Any> callbackConverter(oClass: KClass<O>) =
    object : BiConverter<Callback<I, ObservableValue<O>>, Callback<I, ObsVal<O>>> {


        override fun convertToB(a: Callback<I, ObservableValue<O>>): Callback<I, ObsVal<O>> =
            Callback<I, ObsVal<O>> { param ->
                val o = a.call(param)!!


                val m = o.toNullableROProp().cast(oClass.castFun())
                m
            }

        override fun convertToA(b: Callback<I, ObsVal<O>>): Callback<I, ObservableValue<O>> =
            Callback<I, ObservableValue<O>> { param ->
                b.call(param).createROFXPropWrapper()
            }
    }
