package matt.fx.base.wrapper.obs.obsval.prop

import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import matt.fx.base.wrapper.obs.obsval.FXBackedProp
import matt.fx.base.wrapper.obs.obsval.NonNullFXBackedReadOnlyBindableProp
import matt.fx.base.wrapper.obs.obsval.NullableFXBackedReadOnlyBindableProp
import matt.lang.convert.BiConverter
import matt.obs.bindhelp.BindableValue
import matt.obs.bindhelp.BindableValueHelper
import matt.obs.listen.OldAndNewListener
import matt.obs.listen.update.ValueChange
import matt.obs.prop.MObservableVal
import matt.obs.prop.MWritableValNewAndOld
import matt.obs.prop.Var
import matt.obs.prop.WritableMObservableVal

//fun <T> Property<T>.bindBidirectional(other: NullableFXBackedBindableProp<T>) =
//  other.getBoundedBidirectionallyFrom(this)

//fun <T: Any> Property<T>.bindBidirectional(other: NonNullFXBackedBindableProp<T>) =
//  other.getBoundedBidirectionallyFrom(this)

fun <T> Property<T>.toNullableProp() = NullableFXBackedBindableProp(this)
fun <T: Any> Property<T>.toNonNullableProp() = NonNullFXBackedBindableProp(this)

interface WritableFXBackedProp<FX_T>: FXBackedProp<FX_T>,
									  WritableMObservableVal<FX_T, ValueChange<FX_T>, OldAndNewListener<FX_T,ValueChange<FX_T>,out ValueChange<FX_T>>> {
  /*fun getBoundedBidirectionallyFrom(p: Property<FX_T>)
  fun bind(p: ObservableValue<FX_T>)
  fun unbind()
  fun bindBidirectional(p: Property<FX_T>)*/


  override val bindManager: BindableValue<FX_T>
  override fun bindBidirectional(source: Var<FX_T>, checkEquality: Boolean, clean: Boolean , debug: Boolean, weak: Boolean) =
	bindManager.bindBidirectional(source, checkEquality = checkEquality, clean=clean,debug = debug, weak = weak)

  override fun <S> bindBidirectional(source: Var<S>, converter: BiConverter<FX_T, S>) =
	bindManager.bindBidirectional(source, converter)

  override fun bind(source: MObservableVal<out FX_T, *, *>) = bindManager.bind(source)
  override fun bindWeakly(source: MObservableVal<out FX_T, *, *>) = bindManager.bindWeakly(source)


  override fun unbind() = bindManager.unbind()


}

open class NullableFXBackedBindableProp<T>(private val o: Property<T>): NullableFXBackedReadOnlyBindableProp<T?>(o),
																		WritableFXBackedProp<T?>,
																		MWritableValNewAndOld<T?> {

  //  override var boundTo: MObservableROPropBase<out T?>? = null

  final override val bindManager = BindableValueHelper(this)
  override var theBind by bindManager::theBind

  //  override val isBound get() = o.isBound

  override var value: T?
	get() = super.value
	set(v) {

	  o.value = v
	}


  override val isFXBound get() = o.isBound

  /*override fun getBoundedBidirectionallyFrom(p: Property<T>) {
	Bindings.bindBidirectional(p, o)
  }

  override fun bind(p: ObservableValue<T>) = o.bind(p)
  override fun unbind() = o.unbind()
  override fun bindBidirectional(p: Property<T>) = o.bindBidirectional(p)*/
}

open class NonNullFXBackedBindableProp<T: Any>(private val o: Property<T>): NonNullFXBackedReadOnlyBindableProp<T>(o),
																			WritableFXBackedProp<T>,
																			MWritableValNewAndOld<T>{


  final override val bindManager = BindableValueHelper(this)
  override var theBind by bindManager::theBind

  override val isFXBound get() = o.isBound


  override var value: T
	get() = super.value
	set(v) {
	  o.value = v
	}

  private fun watchForNulls(p: ObservableValue<T>) {
	requireNotNull(p.value) {
	  "null leaked into non-null ${p::class.simpleName}"
	}
	p.addListener { _, _, newValue ->
		requireNotNull(newValue) {
		"null leaked into non-null ${p::class.simpleName}"
	  }
	}
  }

}