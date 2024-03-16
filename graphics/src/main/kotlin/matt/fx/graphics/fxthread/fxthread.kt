package matt.fx.graphics.fxthread

import com.sun.javafx.application.PlatformImpl
import javafx.application.Platform
import javafx.application.Platform.runLater
import matt.async.thread.namedThread
import matt.async.thread.runner.ThreadRunner
import matt.fx.graphics.fxthread.FXAppState.DID_NOT_START_YET
import matt.fx.graphics.fxthread.FXAppState.STARTED
import matt.fx.graphics.fxthread.FXAppState.STOPPED
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.lang.assertions.require.requireEquals
import matt.lang.exec.Exec
import matt.lang.function.Produce
import matt.lang.model.value.Value
import matt.model.flowlogic.runner.Run
import matt.model.flowlogic.runner.Runner
import matt.obs.subscribe.j.LatchManager
import matt.service.scheduler.Scheduler
import kotlin.time.Duration

enum class FXAppState {
    DID_NOT_START_YET,
    STARTED,
    STOPPED
}

object FXAppStateWatcher {
    private var state = DID_NOT_START_YET
    fun getState() = state

    @Synchronized
    fun markAsStarted() {
        requireEquals(state, DID_NOT_START_YET)
        state = STARTED
    }

    @Synchronized
    fun markAsStopped(cause: Throwable?) {
        requireEquals(state, STARTED)
        RunLaterReturnLatchManager.cancel(cause)
        state = STOPPED
    }
}


val RunLaterReturnLatchManager by lazy { LatchManager() }

fun <T> runLaterReturn(op: () -> T): T {
    var r: Value<T>? = null
    val latch = RunLaterReturnLatchManager.getLatch()
    try {
        runLater {
            r = Value(op())
            latch.open()
        }
    } catch (e: Exception) {
        latch.open()
        e.printStackTrace()
    }
    latch.await()
    return r!!.value
}

inline fun <T> ensureInFXThreadInPlace(crossinline op: () -> T): T =
    if (Platform.isFxApplicationThread()) op()
    else runLaterReturn { op() }

inline fun ensureInFXThreadOrRunLater(crossinline op: () -> Unit) =
    if (Platform.isFxApplicationThread()) op()
    else runLater { op() }


fun runMuchLater(
    d: Duration,
    op: () -> Unit
) {
    namedThread(name = "runMuchLater Thread") {
        Thread.sleep(d.inWholeMilliseconds)
        runLater {
            op()
        }
    }
}


inline fun <T : Any, V> inRunLater(crossinline op: T.(V) -> Unit): T.(V) -> Unit = {
    runLater {
        op(it)
    }
}


fun <N : NodeWrapper> N.runLater(op: N.() -> Unit) = PlatformImpl.runLater { op() }


val runLaterOp: Exec = {
    PlatformImpl.runLater(it)
}

object FXScheduler : Scheduler {
    override fun schedule(op: () -> Unit) {
        PlatformImpl.runLater(op)
    }
}

object FXRunner : Runner {
    override fun <R> run(op: Produce<R>): Run<R> {
        val run =
            ThreadRunner.run {
                runLaterReturn(op)
            }
        return run
    }
}
