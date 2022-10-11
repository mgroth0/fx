package matt.fx.graphics.fxthread

import com.sun.javafx.application.PlatformImpl
import javafx.application.Platform
import javafx.application.Platform.runLater
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.lang.function.MetaFunction
import matt.model.latch.SimpleLatch
import matt.service.scheduler.Scheduler
import kotlin.concurrent.thread
import kotlin.time.Duration

fun <T> runLaterReturn(op: ()->T): T {
  /*matt.log.todo.todo: can check if this is application thread and if so just run op in place*/
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
	Thread.sleep(d.inWholeMilliseconds)
	runLater {
	  op()
	}
  }
}


inline fun <T: Any, V> inRunLater(crossinline op: T.(V)->Unit): T.(V)->Unit {
  return {
	runLater {
	  op(it)
	}
  }
}







fun <N: NodeWrapper> N.runLater(op: N.()->Unit) = PlatformImpl.runLater { op() }




val runLaterOp: MetaFunction = {
  PlatformImpl.runLater(it)
}

object FXScheduler: Scheduler {
  override fun schedule(op: ()->Unit) {
	PlatformImpl.runLater(op)
  }
}