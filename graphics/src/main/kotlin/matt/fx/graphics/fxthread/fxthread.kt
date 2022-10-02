package matt.fx.graphics.fxthread

import javafx.application.Platform
import javafx.application.Platform.runLater
import matt.model.latch.SimpleLatch
import matt.time.dur.Duration
import kotlin.concurrent.thread

fun <T> runLaterReturn(op: ()->T): T {
  /*todo: can check if this is application thread and if so just run op in place*/
  var r: Any? = object {}
  val latch = SimpleLatch()
  try {
	runLater {
	  r = op()
	  latch.open()
	}
  } catch (e: Exception) {
	latch.open()
	e.printStackTrace()
  }
  latch.await()
  @Suppress("UNCHECKED_CAST")
  return (r as T)
}

inline fun <reified T> ensureInFXThreadInPlace(crossinline op: ()->T): T {
  return if (Platform.isFxApplicationThread()) op()
  else runLaterReturn { op() }
}

inline fun ensureInFXThreadOrRunLater(crossinline op: ()->Unit) {
  return if (Platform.isFxApplicationThread()) op()
  else runLater { op() }
}


fun runMuchLater(d: Duration, op: ()->Unit) {
  thread {
	Thread.sleep(d.inMilliseconds.toLong())
	runLater {
	  op()
	}
  }
}