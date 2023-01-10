package matt.fx.control.wrapper.control.spinner

import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.spinner.fact.int.MyIntegerSpinnerValueFactory
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.converter.ConverterConverter
import matt.hurricanefx.eye.wrapper.obs.collect.list.createFXWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNonNullableROProp
import matt.lang.err
import matt.model.op.convert.Converter
import matt.obs.bind.smartBind
import matt.obs.col.olist.ObsList
import matt.obs.prop.BindableProperty
import matt.obs.prop.ObsVal
import matt.obs.prop.Var

/**
 * Create a spinner for an arbitrary type. This spinner requires you to configure a value factory, or it will throw an exception.
 */
fun <T: Any> ET.spinner(
  editable: Boolean = false,
  property: Var<T>? = null,
  enableScroll: Boolean = false,
  op: SpinnerWrapper<T>.()->Unit = {}
) = SpinnerWrapper<T>().also {
  it.attachTo(this, op)

  if (property != null) requireNotNull(it.valueFactory) {
	"You must configure the value factory or use the Number based spinner builder " +
	"which configures a default value factory along with min, max and initialValue!"
  }.valueProperty.apply {
	bindBidirectional(property)
  }
  it.initialConfig(
	editable = editable,
	enableScroll = enableScroll
  )
}


inline fun <reified T: Number> ET.spinner(
  min: T? = null,
  max: T? = null,
  initialValue: T? = null,
  amountToStepBy: T? = null,
  editable: Boolean = false,
  property: BindableProperty<T>? = null,
  enableScroll: Boolean = false,
  noinline op: SpinnerWrapper<T>.()->Unit = {}
): SpinnerWrapper<T> {
  /*property is IntegerProperty && property !is DoubleProperty && property !is FloatProperty) ||*/
  val isInt = min is Int || max is Int || initialValue is Int ||
			  T::class == Int::class || T::class == java.lang.Integer::class || T::class.javaPrimitiveType == java.lang.Integer::class.java
  val spinner = if (isInt) {
	SpinnerWrapper<T>(
	  min?.toInt() ?: 0,
	  max?.toInt() ?: 100,
	  initialValue?.toInt() ?: 0,
	  amountToStepBy?.toInt() ?: 1
	)
  } else {
	SpinnerWrapper(
	  min?.toDouble() ?: 0.0, max?.toDouble() ?: 100.0, initialValue?.toDouble()
														?: 0.0, amountToStepBy?.toDouble() ?: 1.0
	)
  }
  if (property != null) {
	spinner.valueFactory.valueProperty.bindBidirectional(property)
  }
  spinner.initialConfig(
	editable = editable,
	enableScroll = enableScroll
  )

  return spinner.attachTo(this, op)
}

fun <T: Any> ET.spinner(
  items: ObsList<T>,
  editable: Boolean = false,
  property: Var<T>? = null,
  enableScroll: Boolean = false,
  op: SpinnerWrapper<T>.()->Unit = {}
) = SpinnerWrapper(items).attachTo(this, op) {
  if (property != null) it.valueFactory.valueProperty.apply {
	bindBidirectional(property)
  }
  it.initialConfig(
	editable = editable,
	enableScroll = enableScroll
  )
}


fun <T: Any> ET.spinner(
  valueFactory: SpinnerValueFactory<T>,
  editable: Boolean = false,
  property: Var<T>? = null,
  enableScroll: Boolean = false,
  op: SpinnerWrapper<T>.()->Unit = {}
) = SpinnerWrapper(valueFactory).attachTo(this, op) {
  if (property != null) it.valueFactory.valueProperty.apply {
	bindBidirectional(property)
  }
  it.initialConfig(
	editable = editable,
	enableScroll = enableScroll
  )
}

@PublishedApi
internal fun SpinnerWrapper<*>.initialConfig(
  editable: Boolean = false,
  enableScroll: Boolean = false,
) {
  isEditable = editable

  if (enableScroll) listenToScrolls()

  if (editable) tfxWeirdEditableThing()
}

class SpinnerWrapper<T: Any>(
  node: Spinner<T> = Spinner<T>(),
): ControlWrapperImpl<Spinner<T>>(node) {
  constructor(
	min: Int,
	max: Int,
	initial: Int,
	step: Int
  ): this(Spinner(min, max, initial, step))

  constructor(
	min: Double,
	max: Double,
	initialVal: Double,
	step: Double
  ): this(Spinner(min, max, initialVal, step))

  constructor(
	min: Double,
	max: Double,
	initialVal: Double
  ): this(Spinner(min, max, initialVal))

  constructor(items: ObsList<T>): this(Spinner(items.createFXWrapper()))
  constructor(valueFactory: SpinnerValueFactory<T>): this(Spinner(valueFactory))


  val editor by lazy { node.editor.wrapped() }

  val value: T get() = node.value
  val valueProperty by lazy { node.valueProperty().toNonNullableROProp() }

  val valueFactoryProperty by lazy {
	node.valueFactoryProperty().toNonNullableProp().proxy(
	  object: Converter<SpinnerValueFactory<T>, SpinnerValueFactoryWrapper<T>> {
		override fun convertToB(a: SpinnerValueFactory<T>): SpinnerValueFactoryWrapper<T> {
		  return a.wrap()
		}

		override fun convertToA(b: SpinnerValueFactoryWrapper<T>): SpinnerValueFactory<T> {
		  return b.svf
		}

	  }
	)
  }

  var valueFactory by valueFactoryProperty

  init {
	val svf = valueFactory.svf
	if (svf is IntegerSpinnerValueFactory) {

	  val newSVF = MyIntegerSpinnerValueFactory(
		min = svf.min,
		max = svf.max,
		initialValue = svf.value,
		amountToStepBy = svf.amountToStepBy
	  )

	  @Suppress("UNCHECKED_CAST")
	  valueFactory = SpinnerValueFactoryWrapper(newSVF as SpinnerValueFactory<T>)
	}
  }


  var isEditable
	get() = node.isEditable
	set(value) {
	  node.isEditable = value
	}

  fun listenToScrolls() {
	setOnScroll { event ->
	  if (event.deltaY > 0) increment()
	  if (event.deltaY < 0) decrement()
	}
  }

  val editableProperty by lazy { node.editableProperty().toNonNullableProp() }

  fun increment() = node.increment()
  fun decrement() = node.decrement()
  fun increment(steps: Int) = node.increment(steps)
  fun decrement(steps: Int) = node.decrement(steps)
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }

  fun tfxWeirdEditableThing() {
	focusedProperty.onChange { newValue: Boolean? ->
	  if (newValue == null) err("here it is")
	  if (!newValue) increment(0)
	}
  }


}

fun <T: Any> SpinnerWrapper<T>.bind(property: ObsVal<T>, readonly: Boolean = false) =
  valueFactory.valueProperty.smartBind(property, readonly)


fun <T: Any> SpinnerValueFactory<T>.wrap() = SpinnerValueFactoryWrapper(this)
class SpinnerValueFactoryWrapper<T: Any>(internal val svf: SpinnerValueFactory<T>) {
  fun increment(steps: Int) = svf.increment(steps)
  fun decrement(steps: Int) = svf.decrement(steps)
  val valueProperty by lazy { svf.valueProperty().toNonNullableProp() }
  var value by valueProperty
  val converterProperty by lazy { svf.converterProperty().toNonNullableProp().proxy(ConverterConverter()) }
  var converter by converterProperty
}