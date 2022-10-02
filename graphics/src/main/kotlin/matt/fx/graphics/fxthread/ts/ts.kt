package matt.fx.graphics.fxthread.ts

import javafx.application.Platform.runLater
import matt.fx.graphics.fxthread.ensureInFXThreadInPlace
import matt.fx.graphics.fxthread.ensureInFXThreadOrRunLater
import matt.obs.prop.BindableProperty
import matt.obs.prop.ObsVal

fun <T> ObsVal<T>.nonBlockingFXWatcher(): ObsVal<T> {
  val newProp = BindableProperty(value)
  onChange {
	ensureInFXThreadOrRunLater {
	  newProp.value = it
	}
  }
  return newProp
}

fun <T> ObsVal<T>.blockingFXWatcher(): ObsVal<T> {
  val newProp = BindableProperty(value)
  onChange {
	ensureInFXThreadInPlace {
	  newProp.value = it
	}
  }
  return newProp
}

class ThreadSafeProp<T>(value: T): BindableProperty<T>(value) {
  override var value: T
	get() = super.value
	set(value) {
	  runLater {
		super.value = value
	  }
	}
}
