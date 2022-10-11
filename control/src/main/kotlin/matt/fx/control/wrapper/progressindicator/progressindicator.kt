package matt.fx.control.wrapper.progressindicator

import javafx.scene.control.ProgressIndicator
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.obs.bind.smartBind
import matt.obs.prop.ObsVal
import matt.obs.prop.Var

fun ET.progressindicator(op: ProgressIndicatorWrapper.()->Unit = {}) = ProgressIndicatorWrapper().attachTo(this, op)

fun ET.progressindicator(property: Var<Double>, op: ProgressIndicatorWrapper.()->Unit = {}) =
  progressindicator().apply {
	bind(property)
	op(this)
  }


class ProgressIndicatorWrapper(
  node: ProgressIndicator = ProgressIndicator(),
): ControlWrapperImpl<ProgressIndicator>(node) {


  var progress
	get() = node.progress
	set(value) {
	  node.progress = value
	}

  val progressProperty by lazy { node.progressProperty().toNonNullableProp().cast<Double>() }
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}

fun ProgressIndicatorWrapper.bind(property: ObsVal<Double>, readonly: Boolean = false) =
  progressProperty.smartBind(property, readonly)