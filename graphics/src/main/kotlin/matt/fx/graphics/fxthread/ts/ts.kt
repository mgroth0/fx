package matt.fx.graphics.fxthread.ts

import javafx.application.Platform.runLater
import matt.fx.graphics.fxthread.ensureInFXThreadInPlace
import matt.fx.graphics.fxthread.ensureInFXThreadOrRunLater
import matt.obs.bind.MyBinding
import matt.obs.col.change.mirror
import matt.obs.col.olist.BasicObservableListImpl
import matt.obs.col.olist.ObsList
import matt.obs.prop.BindableProperty
import matt.obs.prop.ObsVal

private class BlockingFXWatcher<T>(source: ObsVal<T>): MyBinding<T>(calc = {
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

private class NonBlockingFXWatcher<T>(source: ObsVal<T>): MyBinding<T>(calc = {
  source.value
}) {
  init {
	source.onChange {
	  println("NonBlockingFXWatcher 1")
	  ensureInFXThreadOrRunLater {
		println("NonBlockingFXWatcher 2")
		markInvalid()
		println("NonBlockingFXWatcher 3")
	  }
	  println("NonBlockingFXWatcher 4")
	}
  }
}


fun <T> ObsVal<T>.nonBlockingFXWatcher(): ObsVal<T> {
  return (this as? NonBlockingFXWatcher<T>) ?: NonBlockingFXWatcher(this)
}

fun <T> ObsVal<T>.blockingFXWatcher(): ObsVal<T> {

  return (this as? BlockingFXWatcher<T>) ?: BlockingFXWatcher(this)

}


private class NonBlockingFXListWatcher<E>(source: ObsList<E>): BasicObservableListImpl<E>(source) {
  init {
	source.onChange {
	  ensureInFXThreadOrRunLater {
		mirror(it)
	  }
	}
  }
}

private class BlockingFXListWatcher<E>(source: ObsList<E>): BasicObservableListImpl<E>(source) {
  init {
	source.onChange {
	  ensureInFXThreadInPlace {
		mirror(it)
	  }
	}
  }
}


fun <E> ObsList<E>.nonBlockingFXWatcher(): ObsList<E> {

  return (this as? NonBlockingFXListWatcher<E>) ?: NonBlockingFXListWatcher(this)

}

fun <E> ObsList<E>.blockingFXWatcher(): ObsList<E> {


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