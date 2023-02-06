package matt.fx.control.fxapp

import matt.log.report.BugReport
import com.sun.javafx.application.LauncherImpl
import com.sun.javafx.util.Logging
import javafx.application.Application
import javafx.application.Preloader
import javafx.application.Preloader.StateChangeNotification.Type.BEFORE_START
import javafx.geometry.Pos.CENTER
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.control.TextArea
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.Stage
import matt.fx.control.wrapper.wrapped.WrapperServiceImpl
import matt.fx.graphics.service.WrapperServiceHub
import matt.fx.graphics.style.insets
import matt.log.logger.Logger
import matt.log.reporter.TracksTime
import matt.model.code.report.Reporter
import kotlin.concurrent.thread


fun runFXAppBlocking(
  args: Array<String>,
  usePreloaderApp: Boolean = false,
  reporter: Reporter? = null,
  fxOp: (List<String>)->Unit,
) {
  WrapperServiceHub.install(WrapperServiceImpl)
  (reporter as? TracksTime)?.toc("running FX App")
  fxBlock = fxOp
  thread(isDaemon = true) {

	Logging.getJavaFXLogger().disableLogging() /* dodge "Unsupported JavaFX configuration..." part 1 */
  }
  (reporter as? TracksTime)?.toc("started disabling FX logging")
  fxStopwatch = (reporter as? TracksTime)
  if (usePreloaderApp) {
	(reporter as? TracksTime)?.toc("launching preloader")
	LauncherImpl.launchApplication(MinimalFXApp::class.java, FirstPreloader::class.java, args)
  } else {
	(reporter as? TracksTime)?.toc("launching app")
	Application.launch(MinimalFXApp::class.java, *args)
  }
  (reporter as? Logger)?.info("main thread has exited from Application.launch")
}

private var fxStopwatch: TracksTime? = null
private lateinit var fxBlock: (List<String>)->Unit


class FirstPreloader: Preloader() {
  private var bar: ProgressBar? = null
  var stage: Stage? = null
  private fun createPreloaderScene(): Scene {
	bar = ProgressBar()
	val p = BorderPane()
	p.center = bar
	return Scene(p, 300.0, 150.0)
  }

  override fun start(stage: Stage) {
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
  This JavaFX Application encountered an error. Please contact the developer via the bug tracker for this application or email (mgroth49@gmail.com) and include the following information:
  1. Copy and paste the text below
  2. Provide a list of steps, to the best of your ability, that lead you to this error. This way we can reproduce the issue on our end and have an easier time getting to the bottom of it.
  """.trimIndent()

class MinimalFXApp: Application() {


  //  companion object {
  //	var fxStop: (() -> Unit)? = null
  //  }
  override fun start(primaryStage: Stage?) {

	fxStopwatch?.toc("starting main app")

	/* dodge "Unsupported JavaFX configuration..." part 2 */
	Logging.getJavaFXLogger().enableLogging()
	try {
	  fxBlock(parameters.raw)
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

	fxStopwatch?.toc("finished starting main app")
  }

  /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
  override fun stop() {    /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/    /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/    /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/    /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/    /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/    /*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
  }/*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*//*DO_NOT_SHUTDOWN_WITH_FX_THREAD*/
}

