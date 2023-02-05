package matt.fx.base.converter

import javafx.beans.value.ObservableValue
import javafx.util.Callback
import javafx.util.StringConverter
import matt.fx.base.mtofx.createROFXPropWrapper
import matt.model.op.convert.Converter
import matt.obs.prop.ObsVal


fun <T> Converter<T, String>.toFXConverter() = object: StringConverter<T>() {
  override fun toString(`object`: T) = convertToB(`object`)
  override fun fromString(string: String) = convertToA(string)
}

@JvmName("toFXConverter2")
fun <T> Converter<String, T>.toFXConverter() = object: StringConverter<T>() {
  override fun toString(`object`: T) = convertToA(`object`)
  override fun fromString(string: String) = convertToB(string)
}


fun <T> StringConverter<T>.toMConverter() = object: matt.model.op.convert.StringConverter<T> {
  override fun toString(t: T): String {
	return this@toMConverter.toString(t)
  }

  override fun fromString(s: String): T {
	return this@toMConverter.fromString(s)
  }

}

class ConverterConverter<T>(): Converter<StringConverter<T>, matt.model.op.convert.StringConverter<T>> {
  override fun convertToB(a: StringConverter<T>): matt.model.op.convert.StringConverter<T> {
	return a.toMConverter()
  }

  override fun convertToA(b: matt.model.op.convert.StringConverter<T>): StringConverter<T> {
	return b.toFXConverter()
  }

}

fun <I, O> callbackConverter() = object:
  Converter<Callback<I, ObservableValue<O>>, Callback<I, ObsVal<O>>> {
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
