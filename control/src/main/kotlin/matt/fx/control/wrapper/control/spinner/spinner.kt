package matt.fx.control.wrapper.control.spinner

import javafx.beans.property.BooleanProperty
import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.bind.smartBind
import matt.hurricanefx.eye.mtofx.createWritableFXPropWrapper
import matt.lang.err
import matt.obs.prop.BindableProperty


/**
 * Create a spinner for an arbitrary type. This spinner requires you to configure a value factory, or it will throw an exception.
 */
fun <T> ET.spinner(
  editable: Boolean = false,
  property: Property<T>? = null,
  enableScroll: Boolean = false,
  op: SpinnerWrapper<T>.()->Unit = {}
) = SpinnerWrapper<T>().also {
  it.isEditable = editable
  it.attachTo(this, op)

  if (property != null) requireNotNull(it.valueFactory) {
	"You must configure the value factory or use the Number based spinner builder " +
		"which configures a default value factory along with min, max and initialValue!"
  }.valueProperty().apply {
	bindBidirectional(property)
  }

  if (enableScroll) it.setOnScroll { event ->
	if (event.deltaY > 0) it.increment()
	if (event.deltaY < 0) it.decrement()
  }

  if (editable) it.focusedProperty().addListener { _, _, newValue: Boolean? ->
	if (newValue == null) err("here it is")
	if (!newValue) it.increment(0)
  }
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
  val spinner: SpinnerWrapper<T>


  /*property is IntegerProperty && property !is DoubleProperty && property !is FloatProperty) ||*/
  val isInt = min is Int || max is Int || initialValue is Int ||
	  T::class == kotlin.Int::class || T::class == java.lang.Integer::class || T::class.javaPrimitiveType == java.lang.Integer::class.java
  if (isInt) {
	spinner = SpinnerWrapper(
	  min?.toInt() ?: 0,
	  max?.toInt() ?: 100,
	  initialValue?.toInt() ?: 0,
	  amountToStepBy?.toInt() ?: 1
	)
  } else {
	spinner = SpinnerWrapper(
	  min?.toDouble() ?: 0.0, max?.toDouble() ?: 100.0, initialValue?.toDouble()
		?: 0.0, amountToStepBy?.toDouble() ?: 1.0
	)
  }
  if (property != null) {
	spinner.valueFactory!!.valueProperty().bindBidirectional(property.createWritableFXPropWrapper())
  }
  spinner.isEditable = editable

  if (enableScroll) {
	spinner.setOnScroll { event ->
	  if (event.deltaY > 0) spinner.increment()
	  if (event.deltaY < 0) spinner.decrement()
	}
  }

  if (editable) {
	spinner.focusedProperty().addListener { _, _, newValue: Boolean? ->
	  if (newValue == null) err("here it is")
	  if (!newValue) {
		spinner.increment(0)
	  }
	}
  }

  return spinner.attachTo(this, op)
}

fun <T> ET.spinner(
  items: ObservableList<T>,
  editable: Boolean = false,
  property: Property<T>? = null,
  enableScroll: Boolean = false,
  op: SpinnerWrapper<T>.()->Unit = {}
) = SpinnerWrapper<T>(items).attachTo(this, op) {
  if (property != null) it.valueFactory!!.valueProperty().apply {
	bindBidirectional(property)
  }

  it.isEditable = editable

  if (enableScroll) it.setOnScroll { event ->
	if (event.deltaY > 0) it.increment()
	if (event.deltaY < 0) it.decrement()
  }

  if (editable) it.focusedProperty().addListener { _, _, newValue: Boolean? ->
	if (newValue == null) err("here it is")
	if (!newValue) it.increment(0)
  }
}

fun <T> ET.spinner(
  valueFactory: SpinnerValueFactory<T>,
  editable: Boolean = false,
  property: Property<T>? = null,
  enableScroll: Boolean = false,
  op: SpinnerWrapper<T>.()->Unit = {}
) = SpinnerWrapper<T>(valueFactory).attachTo(this, op) {
  if (property != null) it.valueFactory!!.valueProperty().apply {
	bindBidirectional(property)
  }

  it.isEditable = editable

  if (enableScroll) it.setOnScroll { event ->
	if (event.deltaY > 0) it.increment()
	if (event.deltaY < 0) it.decrement()
  }

  if (editable) it.focusedProperty().addListener { _, _, newValue: Boolean? ->
	if (newValue == null) err("here it is")
	if (!newValue) it.increment(0)
  }
}

class SpinnerWrapper<T>(
   node: Spinner<T> = Spinner<T>(),
): ControlWrapperImpl<Spinner<T>>(node) {
  constructor(min: Int, max: Int, initial: Int, step: Int): this(Spinner(min, max, initial, step))
  constructor(min: Double, max: Double, initialVal: Double, step: Double): this(Spinner(min, max, initialVal, step))
  constructor(min: Double, max: Double, initialVal: Double): this(Spinner(min, max, initialVal))
  constructor(items: ObservableList<T>): this(Spinner(items))
  constructor(valueFactory: SpinnerValueFactory<T>): this(Spinner(valueFactory))


  val value: T? get() = node.value
  fun valueProperty(): ReadOnlyObjectProperty<T> = node.valueProperty()

  var valueFactory: SpinnerValueFactory<T>?
	get() = node.valueFactory
	set(value) {
	  node.valueFactory = value
	}


  var isEditable
	get() = node.isEditable
	set(value) {
	  node.isEditable = value
	}


  fun editableProperty(): BooleanProperty = node.editableProperty()

  fun increment() = node.increment()
  fun decrement() = node.decrement()
  fun increment(steps: Int) = node.increment(steps)
  fun decrement(steps: Int) = node.decrement(steps)
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}

fun <T> SpinnerWrapper<T>.bind(property: ObservableValue<T>, readonly: Boolean = false) =
  valueFactory!!.valueProperty().smartBind(property, readonly)