package matt.fx.control.fxapp

import com.sun.javafx.application.LauncherImpl
import com.sun.javafx.util.Logging
import javafx.application.Application
import javafx.application.Platform
import javafx.application.Preloader
import javafx.application.Preloader.StateChangeNotification.Type.BEFORE_START
import javafx.event.Event
import javafx.geometry.Pos.CENTER
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.control.TextArea
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.Stage
import javafx.stage.Window
import javafx.stage.WindowEvent
import matt.async.thread.namedThread
import matt.collect.itr.recurse.chain.chain
import matt.fx.control.wrapper.wrapped.WrapperServiceImpl
import matt.fx.graphics.fxthread.FXAppStateWatcher
import matt.fx.graphics.service.WrapperServiceHub
import matt.fx.graphics.style.insets
import matt.lang.anno.Fixes
import matt.lang.function.Op
import matt.lang.go
import matt.lang.require.requireNot
import matt.log.logger.Logger
import matt.log.report.BugReport
import matt.log.reporter.TracksTime
import matt.model.code.report.Reporter

private val monitor = {}
private var didRunFXApp = false

const val DEFAULT_THROW_ON_APP_THREAD_THROWABLE = false

fun runFXAppBlocking(
    usePreloaderApp: Boolean = false,
    reporter: Reporter? = null,
    throwOnApplicationThreadThrowable: Boolean = DEFAULT_THROW_ON_APP_THREAD_THROWABLE,
    fxOp: () -> Unit,
) {
    synchronized(monitor) {
        requireNot(didRunFXApp) {
            "If I want to run multiple FX Apps in one JVM Runtime, I need to look at everything here and make sure it will be compatible with that. Though, not sure if JavaFX itself will even support that."
        }
        didRunFXApp = true
    }
    exitAndReThrowOnAppThrowable = throwOnApplicationThreadThrowable
    WrapperServiceHub.install(WrapperServiceImpl)
    (reporter as? TracksTime)?.toc("running FX App")
    fxBlock = fxOp
    namedThread(isDaemon = true,name="disable FX Logger Thread") {
        Logging.getJavaFXLogger().disableLogging()
        /* dodge "Unsupported JavaFX configuration..." part 1 */
    }
    (reporter as? TracksTime)?.toc("started disabling FX logging")
    fxStopwatch = (reporter as? TracksTime)
    if (usePreloaderApp) {
        (reporter as? TracksTime)?.toc("launching preloader")
        LauncherImpl.launchApplication(MinimalFXApp::class.java, FirstPreloader::class.java, arrayOf())
    } else {
        (reporter as? TracksTime)?.toc("launching app")
        Application.launch(MinimalFXApp::class.java)
    }
    applicationThreadIssue?.go {
        throw Exception("${it::class.qualifiedName} was thrown from the FX Application Thread", it)
    }
    (reporter as? Logger)?.info("main thread has exited from Application.launch")
}

private var fxStopwatch: TracksTime? = null
private lateinit var fxBlock: () -> Unit
private var applicationThreadIssue: Throwable? = null
private var exitAndReThrowOnAppThrowable: Boolean? = null


class FirstPreloader : Preloader() {
    private var bar: ProgressBar? = null
    var stage: Stage? = null
    private fun createPreloaderScene(): Scene {
        bar = ProgressBar()
        val p = BorderPane()
        p.center = bar
        return Scene(p, 300.0, 150.0)
    }

    override fun start(stage: Stage) {

        if (exitAndReThrowOnAppThrowable!!) {
            Thread.currentThread().setUncaughtExceptionHandler { _, e ->
                applicationThreadIssue = e
                Platform.exit()
                Platform.runLater {
                    Platform.exit()
                }
            }
        }

        fxStopwatch?.toc("starting preloader app")
        this.stage = stage
        stage.scene = createPreloaderScene()
        stage.show()
        fxStopwatch?.toc("finished starting preloader app")
    }

    override fun handleProgressNotification(pn: ProgressNotification) {
        bar!!.progress = pn.progress
    }

    override fun handleStateChangeNotification(evt: StateChangeNotification) {
        if (evt.type == BEFORE_START) {
            stage!!.hide()
        }
    }
}


val ERROR_POP_UP_TEXT = """
  The application has encountered an unexpected error. Please submit a bug report. To help further, leave a comment in the bug report listing the steps you took that caused the error.
  """.trim()


/*This is taken straight from JavaFX Source Code*/
@Fixes("Big NullPointerException Bug")
private class DefaultGlassAppEventHandler(
    private val buildInHandler: com.sun.glass.ui.Application.EventHandler
) : com.sun.glass.ui.Application.EventHandler() {
    override fun handleQuitAction(
        app: com.sun.glass.ui.Application?,
        time: Long
    ) {

        /*Emulate:

              com.sun.javafx.tk.quantum.GlassStage.requestClosingAllWindows

              with the NullPointerException Fix

              */

        /*if (Platform.isNestedLoopRunning()) {
            Toolkit.getToolkit().exitAllNestedEventLoops()
        }*/




        @Fixes("Big NullPointerException Bug")
        tryToEscapeNestedEventLoopsAndThenFinally {
            @Fixes("Big NullPointerException Bug")
            buildInHandler.handleQuitAction(app, time)
        }


    }

    override fun handleThemeChanged(themeName: String?): Boolean {
        return buildInHandler.handleThemeChanged(themeName)
        /*val highContrastSchemeName: String =
            com.sun.glass.ui.Application.GetApplication().getHighContrastScheme(themeName)
        return PlatformImpl.setAccessibilityTheme(highContrastSchemeName)*/
    }


    /*After thinking a lot about this, I think the only sane solution is to first ensure tht all currently running events are completed if in a nestedLoop. This seems like the only way to gracefully exit without a NullPointerException.*/
    @Fixes("Big NullPointerException Bug")
    private fun tryToEscapeNestedEventLoopsAndThenFinally(
        depth: Int = 0,
        op: Op
    ) {
        if (depth < 10 && Platform.isNestedLoopRunning()) {
            tryToCloseDeepestDialog()
            Platform.runLater {
                tryToEscapeNestedEventLoopsAndThenFinally(depth = depth + 1, op)
            }
        } else op()
    }

}
//
//@Fixes("Big NullPointerException Bug")
//private class MyGlassApEventHandler : DefaultGlassAppEventHandler() {
//    override fun handleQuitAction(app: com.sun.glass.ui.Application?, time: Long) {
//
//
//    }
//
//}

@Fixes("Big NullPointerException Bug")
private fun tryToCloseDeepestDialog() {

    Window.getWindows().filterIsInstance<Stage>().filter {
        Stage::class.java.getDeclaredField("inNestedEventLoop").run {
            isAccessible = true
            get(it) as Boolean
        }
    }.sortedByDescending {
        (it as Window).chain { (it as? Stage)?.owner }.count()
    }.firstOrNull()?.go {
        Event.fireEvent(
            it,
            WindowEvent(
                it,
                WindowEvent.WINDOW_CLOSE_REQUEST
            )
        )
    }
}

/*
@Fixes("Big NullPointerException Bug")
private fun quitJavaFX() {


    Window.getWindows().asSequence().filterIsInstance<Stage>()
        .firstOrNull { it.isFullScreen }?.isFullScreen = false

    Window.getWindows().toList().sortedByDescending {
        *//*ATTEMPT TO FIX THE BIG NullPointerException Bug Part 1*//*
        var score = 0

        if (it is Stage && it.modality == Modality.APPLICATION_MODAL) score++
        if (it is PopupWindow) score++
        if (it is Stage && Stage::class.java.getDeclaredField("inNestedEventLoop").run {
                isAccessible = true
                get(it) as Boolean
            }) {
            score = 2
        } else if (it is Stage && it.modality == Modality.APPLICATION_MODAL) {
            score++
        } else if (it is PopupWindow) score++

        score

    }.forEach {
        *//*In case of child windows some of them could already be closed
        so check if list still contains an object*//*
        if (it in Window.getWindows() && it.isShowing) {
            *//*it.hide()*//*
            Event.fireEvent(
                it,
                WindowEvent(
                    it,
                    WindowEvent.WINDOW_CLOSE_REQUEST
                )
            )
        }
    }
}*/


//val TEMP_DEBUG_LOG_FILE = TEMP_DIR["deephys"]["temp_debug.log"].apply {
//    mkparents()
//    text = ""
//}


class MinimalFXApp : Application() {


    //  companion object {
    //	var fxStop: (() -> Unit)? = null
    //  }
    override fun start(primaryStage: Stage?) {
        FXAppStateWatcher.markAsStarted()
//        Thread.setDefaultUncaughtExceptionHandler { t, e ->
//            TEMP_DEBUG_LOG_FILE.appendln("UNCAUGHT EXCEPTION: ${e}, $t, ${e.stackTraceToString()}")
//        }
//        TEMP_DEBUG_LOG_FILE.appendln("here1")
        if (exitAndReThrowOnAppThrowable!!) {
//            TEMP_DEBUG_LOG_FILE.appendln("here2: ${Thread.currentThread().name},${Thread.currentThread().id}")
            Thread.currentThread().setUncaughtExceptionHandler { _, e ->
//                TEMP_DEBUG_LOG_FILE.appendln("here3")
                applicationThreadIssue = e
                Platform.exit()
//                TEMP_DEBUG_LOG_FILE.appendln("here4")
                Platform.runLater {
//                    TEMP_DEBUG_LOG_FILE.appendln("here5")
                    Platform.exit()
//                    TEMP_DEBUG_LOG_FILE.appendln("here6")
                }
//                TEMP_DEBUG_LOG_FILE.appendln("here7")
            }
//            TEMP_DEBUG_LOG_FILE.appendln("here8")
        }
//        TEMP_DEBUG_LOG_FILE.appendln("here9")


        /*GOAL: TO PREVENT THE BIG NullPointerException BUG THAT I JUST EMAILED THE OPENJFX LISTSERV ABOUT*/
        com.sun.glass.ui.Application.GetApplication().eventHandler =
            DefaultGlassAppEventHandler(com.sun.glass.ui.Application.GetApplication().eventHandler)

        fxStopwatch?.toc("starting main app")


        /* dodge "Unsupported JavaFX configuration..." part 2 */
        Logging.getJavaFXLogger().enableLogging()
        if (exitAndReThrowOnAppThrowable!!) {
            fxBlock()
        } else {
            try {
                fxBlock()
            } catch (e: Exception) {
                val bugText = BugReport(t = Thread.currentThread(), e = e).text
                println("\n\n$bugText\n\n")
                val debugScene = Scene(
                    VBox(
                        Label(ERROR_POP_UP_TEXT).apply {
                            isWrapText = true
                            font = Font.font(18.0)
                            insets(25.0)
                        },
                        VBox().apply {
                            minHeight = 50.0
                        },
                        TextArea(bugText).apply {
                            insets(25.0)
                        }
                    ).apply {
                        alignment = CENTER
                    }
                )
                primaryStage!!.apply {
                    title = "JavaFX Application Failed To Start"
                    scene = debugScene
                    width = 800.0
                    height = 500.0
                    centerOnScreen()
                }
                primaryStage.show()

            }
        }


        fxStopwatch?.toc("finished starting main app")
    }

    /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
    override fun stop() {


        FXAppStateWatcher.markAsStopped(cause = applicationThreadIssue)
        /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/    /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/    /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/    /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/    /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/    /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
    }/*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
}


