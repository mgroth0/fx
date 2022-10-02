package matt.fx.control.wrapper.control.spinner

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import matt.hurricanefx.eye.bind.smartBind
import matt.hurricanefx.wrapper.control.ControlWrapperImpl
import matt.hurricanefx.wrapper.node.NodeWrapper

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