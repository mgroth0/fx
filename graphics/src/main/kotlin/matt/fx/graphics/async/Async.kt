package matt.fx.graphics.async

/*slightly modified code I stole from tornadofx*/

import javafx.application.Platform
import matt.async.date.Duration
import matt.fx.graphics.async.ThreadPoolType.Daemon
import matt.fx.graphics.async.ThreadPoolType.NoDaemon
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread

private enum class ThreadPoolType { NoDaemon, Daemon }

private val threadPools = mutableMapOf<ThreadPoolType, ExecutorService>()

internal val tfxThreadPool: ExecutorService
  get() = threadPools.getOrPut(NoDaemon) {
	Executors.newCachedThreadPool(TFXThreadFactory(daemon = false))
  }

internal val tfxDaemonThreadPool: ExecutorService
  get() = threadPools.getOrPut(Daemon) {
	Executors.newCachedThreadPool(TFXThreadFactory(daemon = true))
  }


internal fun shutdownThreadPools() {
  threadPools.values.forEach { it.shutdown() }
  threadPools.clear()
}

private class TFXThreadFactory(val daemon: Boolean): ThreadFactory {
  private val threadCounter = AtomicLong(0L)
  override fun newThread(runnable: Runnable?) = Thread(runnable, threadName()).apply {
	isDaemon = daemon
  }

  private fun threadName() = "tornadofx-thread-${threadCounter.incrementAndGet()}" + if (daemon) "-daemon" else ""
}


/**
 * Run the specified Runnable on the JavaFX Application Thread at some
 * unspecified time in the future.
 */
fun runLater(op: ()->Unit) = Platform.runLater(op)

private val runLaterTimer: Timer by lazy { Timer(true) }


/*MATT'S STUFF BELOW*/

fun <T> runLaterReturn(op: ()->T): T {
  /*todo: can check if this is application thread and if so just run op in place*/
  var r: T? = null
  val sem = Semaphore(0)
  try {
	runLater {
	  r = op()
	  sem.release()
	}
  } catch (e: Exception) {
	sem.release()
	e.printStackTrace()
  }
  sem.acquire()
  return r!!
}

fun <T> ensureInFXThreadInPlace(op: ()->T): T {
  return if (Platform.isFxApplicationThread()) op()
  else runLaterReturn { op() }
}


fun runMuchLater(d: Duration, op: ()->Unit) {
  thread {
	Thread.sleep(d.inMilliseconds.toLong())
	runLater {
	  op()
	}
  }
}

fun runMuchLaterReturn(d: Duration, op: ()->Unit) {
  Thread.sleep(d.inMilliseconds.toLong())
  runLaterReturn {
	op()
  }
}