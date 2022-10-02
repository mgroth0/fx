package matt.fx.control.wrapper.progressbar

import javafx.beans.property.DoubleProperty
import javafx.beans.value.ObservableValue
import javafx.scene.control.ProgressBar
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.hurricanefx.eye.bind.smartBind

class ProgressBarWrapper(
   node: ProgressBar = ProgressBar(),
): ControlWrapperImpl<ProgressBar>(node) {
  companion object {
	fun ProgressBar.wrapped() = ProgressBarWrapper(this)
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

fun ProgressBarWrapper.bind(property: ObservableValue<Number>, readonly: Boolean = false) =
  progressProperty().smartBind(property, readonly)