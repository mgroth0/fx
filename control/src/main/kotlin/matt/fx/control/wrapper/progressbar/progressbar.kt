package matt.fx.control.wrapper.progressbar

import javafx.beans.property.DoubleProperty
import javafx.scene.control.ProgressBar
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.obs.bind.smartBind
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.obs.prop.ObsVal


fun ET.progressbar(initialValue: Double? = null, op: ProgressBarWrapper.()->Unit = {}) =
  ProgressBarWrapper().attachTo(this, op) {
	if (initialValue != null) it.progress = initialValue
  }

fun ET.progressbar(property: ObsVal<Number>, op: ProgressBarWrapper.()->Unit = {}) = progressbar().apply {
  bind(property)
  op(this)
}

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

  val progressProperty by lazy { node.progressProperty().toNonNullableProp() }
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}

fun ProgressBarWrapper.bind(property: ObsVal<Number>, readonly: Boolean = false) =
  progressProperty.smartBind(property, readonly)