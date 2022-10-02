package matt.fx.control.wrapper.progressindicator

import javafx.beans.property.DoubleProperty
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.scene.control.ProgressIndicator
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.bind.smartBind
import matt.fx.graphics.wrapper.ET

fun ET.progressindicator(op: ProgressIndicatorWrapper.()->Unit = {}) = ProgressIndicatorWrapper().attachTo(this, op)

fun ET.progressindicator(property: Property<Number>, op: ProgressIndicatorWrapper.()->Unit = {}) =
  progressindicator().apply {
	bind(property)
	op(this)
  }


class ProgressIndicatorWrapper(
  node: ProgressIndicator = ProgressIndicator(),
): ControlWrapperImpl<ProgressIndicator>(node) {
  companion object {
	fun ProgressIndicator.wrapped() = ProgressIndicatorWrapper(this)
  }


  var progress
	get() = node.progress
	set(value) {
	  node.progress = value
	}

  fun progressProperty(): DoubleProperty = node.progressProperty()
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}

fun ProgressIndicatorWrapper.bind(property: ObservableValue<Number>, readonly: Boolean = false) =
  progressProperty().smartBind(property, readonly)