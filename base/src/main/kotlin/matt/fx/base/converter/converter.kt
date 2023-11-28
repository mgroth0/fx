package matt.fx.base.converter

import javafx.beans.value.ObservableValue
import javafx.util.Callback
import javafx.util.StringConverter
import matt.fx.base.mtofx.createROFXPropWrapper
import matt.fx.base.wrapper.obs.obsval.toNullableROProp
import matt.lang.convert.BiConverter
import matt.obs.prop.ObsVal

fun <T> BiConverter<T, String>.toFXConverter() = object : StringConverter<T>() {
    override fun toString(`object`: T) = convertToB(`object`)
    override fun fromString(string: String) = convertToA(string)
}

@JvmName("toFXConverter2")
fun <T> BiConverter<String, T>.toFXConverter() = object : StringConverter<T>() {
    override fun toString(`object`: T) = convertToA(`object`)
    override fun fromString(string: String) = convertToB(string)
}


fun <T> StringConverter<T>.toMConverter() = object : matt.prim.converters.StringConverter<T> {
    override fun toString(t: T): String {
        return this@toMConverter.toString(t)
    }

    override fun fromString(s: String): T {
        return this@toMConverter.fromString(s)
    }

}

class ConverterConverter<T>() : BiConverter<StringConverter<T>, matt.prim.converters.StringConverter<T>> {
    override fun convertToB(a: StringConverter<T>): matt.prim.converters.StringConverter<T> {
        return a.toMConverter()
    }

    override fun convertToA(b: matt.prim.converters.StringConverter<T>): StringConverter<T> {
        return b.toFXConverter()
    }

}

fun <I, O> callbackConverter() = object : BiConverter<Callback<I, ObservableValue<O>>, Callback<I, ObsVal<O>>> {


    override fun convertToB(a: Callback<I, ObservableValue<O>>): Callback<I, ObsVal<O>> {
        return Callback<I, ObsVal<O>> { param ->
            val o = a.call(param)!!


            val m = o.toNullableROProp().cast<O>()
            m
        }
    }

    override fun convertToA(b: Callback<I, ObsVal<O>>): Callback<I, ObservableValue<O>> {
        return Callback<I, ObservableValue<O>> { param -> b.call(param).createROFXPropWrapper() }
    }

}
