package matt.fx.control.wrapper.progressindicator

import javafx.geometry.Pos.CENTER_LEFT
import javafx.scene.control.ProgressIndicator
import javafx.scene.text.TextAlignment
import matt.async.schedule.every
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.graphics.font.fixed
import matt.fx.graphics.fxthread.runLaterReturn
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.parent.parentProperty
import matt.fx.graphics.wrapper.pane.hbox.h
import matt.fx.graphics.wrapper.pane.vbox.VBoxW
import matt.fx.graphics.wrapper.text.TextWrapper
import matt.fx.graphics.wrapper.text.text
import matt.fx.graphics.wrapper.text.textlike.MONO_FONT
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.lang.NEVER
import matt.obs.bind.smartBind
import matt.obs.math.double.op.times
import matt.obs.prop.ObsVal
import matt.obs.prop.Var
import kotlin.time.Duration.Companion.milliseconds

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


class PerformantProgressIndicator(): VBoxW() {

  companion object {
	private var instances = mutableSetOf<TextWrapper>()

	init {
	  var next = ".."
	  every(300.milliseconds) {
		val toChange = synchronized(PerformantProgressIndicator) {
		  instances.toSet()
		}
		runLaterReturn {
		  toChange.forEach {
			it.text = next
		  }
		}
		next = when (next) {
		  "."   -> ".."
		  ".."  -> "..."
		  "..." -> "."
		  else  -> NEVER
		}
	  }
	}
	private val myFont by lazy {
	  MONO_FONT.fixed().copy(size = 18.0).fx()
	}
  }

  init {

	alignment = CENTER_LEFT

	h {
	  exactWidthProperty.bindWeakly(widthProperty*0.25)
	}

	val t = text(".") {
	  textAlignment = TextAlignment.LEFT
	  font = myFont
	}

	parentProperty().onChange {
	  synchronized(PerformantProgressIndicator) {
		if (it != null) {
		  instances += t
		} else {
		  instances -= t
		  if (instances.isEmpty()) {
			instances = mutableSetOf() /*reduce memory consumption*/
		  }
		}
	  }
	}
  }
}