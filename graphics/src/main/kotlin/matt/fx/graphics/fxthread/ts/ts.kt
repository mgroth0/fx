package matt.fx.graphics.fxthread.ts

import javafx.application.Platform.runLater
import matt.fx.graphics.fxthread.ensureInFXThreadInPlace
import matt.fx.graphics.fxthread.ensureInFXThreadOrRunLater
import matt.lang.function.Op
import matt.obs.bind.MyBinding
import matt.obs.col.change.mirror
import matt.obs.col.olist.BasicObservableListImpl
import matt.obs.col.olist.MutableObsList
import matt.obs.prop.BindableProperty
import matt.obs.prop.ObsVal
import matt.obs.watch.PropertyWatcher

private class BlockingFXWatcher<T>(source: ObsVal<T>): MyBinding<T>(calcArg = {
  source.value
}) {
  init {
	source.onChange {
	  ensureInFXThreadInPlace {
		markInvalid()
	  }
	}
  }
}

private class NonBlockingFXWatcher<T>(source: ObsVal<T>): MyBinding<T>(calcArg = {
  source.value
}) {
  init {
	source.onChange {
	  ensureInFXThreadOrRunLater {
		markInvalid()
	  }
	}
  }
}


fun <T> ObsVal<T>.nonBlockingFXWatcher(): ObsVal<T> {
  return (this as? NonBlockingFXWatcher<T>) ?: NonBlockingFXWatcher(this)
}

fun <T> ObsVal<T>.blockingFXWatcher(): ObsVal<T> {

  return (this as? BlockingFXWatcher<T>) ?: BlockingFXWatcher(this)

}


private class NonBlockingFXListWatcher<E>(source: MutableObsList<E>): BasicObservableListImpl<E>(source) {
  init {
	source.onChange {
	  ensureInFXThreadOrRunLater {
		mirror(it)
	  }
	}
  }
}

private class BlockingFXListWatcher<E>(source: MutableObsList<E>): BasicObservableListImpl<E>(source) {
  init {
	source.onChange {
	  ensureInFXThreadInPlace {
		mirror(it)
	  }
	}
  }
}


fun <E> MutableObsList<E>.nonBlockingFXWatcher(): MutableObsList<E> {

  return (this as? NonBlockingFXListWatcher<E>) ?: NonBlockingFXListWatcher(this)

}

fun <E> MutableObsList<E>.blockingFXWatcher(): MutableObsList<E> {


  return (this as? BlockingFXListWatcher<E>) ?: BlockingFXListWatcher(this)

}


class FXThreadSafeProp<T>(value: T): BindableProperty<T>(value) {
  override var value: T
	get() = super.value
	set(value) {
	  runLater {
		super.value = value
	  }
	}
}

fun <T: Any> ObsVal<T>.periodicFXUpdates() = GlobalFXWatcher.watch(this)

object GlobalFXWatcher: PropertyWatcher() {
  override fun runOps(ops: List<Op>) {
	runLater {
	  ops.forEach { it() }
	}
  }
}