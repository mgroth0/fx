package matt.fx.control.wrapper.progressindicator

import javafx.beans.property.DoubleProperty
import javafx.beans.value.ObservableValue
import javafx.scene.control.ProgressIndicator
import matt.hurricanefx.eye.bind.smartBind
import matt.hurricanefx.wrapper.control.ControlWrapperImpl
import matt.hurricanefx.wrapper.node.NodeWrapper

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