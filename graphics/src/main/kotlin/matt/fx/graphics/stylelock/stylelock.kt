package matt.fx.graphics.stylelock

import matt.fx.graphics.stylelock.NonNullFXBackedUserStyleProp.Companion.originProp
import javafx.beans.property.Property
import javafx.css.StyleOrigin
import javafx.css.StyleableObjectProperty
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.NullableFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.WritableFXBackedProp
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

fun <T: Any> Property<T>.toNullableStyleProp() = NullableFXBackedUserStyleProp(this as StyleableObjectProperty<T>)
fun <T: Any> Property<T>.toNonNullableStyleProp() = NonNullFXBackedUserStyleProp(this as StyleableObjectProperty<T>)

class NullableFXBackedUserStyleProp<T>(private val o: StyleableObjectProperty<T>): NullableFXBackedBindableProp<T?>(o),
																				   WritableFXBackedProp<T?> {


  override var value: T?
	get() = super.value
	set(v) {


	  o.set(v)

	  originProp.javaField!!.set(o, StyleOrigin.USER_AGENT)

	  o.value = v
	}


}

class NonNullFXBackedUserStyleProp<T: Any>(private val o: StyleableObjectProperty<T>):
	NonNullFXBackedBindableProp<T>(o),
	WritableFXBackedProp<T> {

  companion object {
	val originProp = StyleableObjectProperty::class.declaredMemberProperties.first {
	  it.name == "origin"
	}.also {

	  it.isAccessible = true
	}
  }

  override var value: T
	get() = super.value
	set(v) {

	  o.set(v)
	  /*doing it this way instead of `o.value = v` causes origin to be StyleOrigin.USER, which will prevent CSS-passes from pseudoclass transitions to affect this property!*/

	  /*USER_AGENT puts an even stricter lock on I think... needed sometimes when psuedos are changing or something*/
	  /*originProp.call(StyleOrigin.USER_AGENT)*/

	  originProp.javaField!!.set(o, StyleOrigin.USER_AGENT)

	}


}