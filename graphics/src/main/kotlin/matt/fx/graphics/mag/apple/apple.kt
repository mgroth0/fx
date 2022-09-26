@file:Suppress("DEPRECATION")

package matt.fx.graphics.mag.apple

import javafx.application.Platform.runLater
import javafx.geometry.Rectangle2D
import javafx.stage.Screen
import javafx.stage.Stage
import matt.auto.applescript.AppleScriptExpression
import matt.auto.applescript.applescript
import matt.auto.applescript.interactiveOsascript
import matt.auto.applescript.runAppleScript
import matt.auto.compileAndOrRunApplescript
import matt.auto.macapp.JavaMacApp
import matt.auto.macapp.MacApp.Companion.getFrontmostProcessFromKotlinNative
import matt.auto.macapp.SystemEvents
import matt.fx.graphics.mag.left
import matt.hurricanefx.wrapper.stage.StageWrapper
import matt.lang.err
import matt.log.profile.tic
import kotlin.concurrent.thread

fun moveFrontmostWindowByApplescript(x: Number, y: Number, width: Number, height: Number) {
  println(
	"MOVE:" + compileAndOrRunApplescript(
	  "moveFrontmostWindow",
	  x.toInt().toString(),
	  y.toInt().toString(),
	  width.toInt().toString(),
	  height.toInt().toString()
	)
  )
}

fun moveAppWindowByApplescript(app: String, x: Number, y: Number, width: Number, height: Number) {

  runAppleScript {
	tell(SystemEvents) {
	  scriptLines += "frontmostProcess to first application process whose name is \"$app\""
	  tell("frontmostProcess") {
		tell("(1st window whose value of attribute \"AXMain\" is true)") {
		  set("windowTitle", AppleScriptExpression("value of attribute \"AXTitle\""))
		  set("position", AppleScriptExpression("{${x.toInt()}, ${y.toInt()}}"))
		  set("size", AppleScriptExpression("{${width.toInt()}, ${height.toInt()}}"))
		}
	  }
	}
  }

}

fun getFrontmostWindowPositionAndSizeByApplescript(): Rectangle2D {
  var s = compileAndOrRunApplescript("getFrontmostWindowPositionAndSize")
  val x = s.substringBefore(",").trim().toInt()
  s = s.substringAfter(",")
  val y = s.substringBefore(",").trim().toInt()
  s = s.substringAfter(",")
  val width = s.substringBefore(",").trim().toInt()
  s = s.substringAfter(",")
  val height = s.trim().toInt().toString()
  return Rectangle2D(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
}

fun getAppWindowPositionAndSizeByApplescript(app: String): Rectangle2D {
  var s = runAppleScript {
	tell(SystemEvents) {
	  scriptLines += "frontmostProcess to first application process whose name is \"$app\""
	  tell("frontmostProcess") {
		tell("(1st window whose value of attribute \"AXMain\" is true)") {
		  `return`(AppleScriptExpression("{position, size}"))
		}
	  }
	}
  }
  val x = s.substringBefore(",").trim().toInt()
  s = s.substringAfter(",")
  val y = s.substringBefore(",").trim().toInt()
  s = s.substringAfter(",")
  val width = s.substringBefore(",").trim().toInt()
  s = s.substringAfter(",")
  val height = s.trim().toInt().toString()
  return Rectangle2D(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
}


fun sdtInTest() {
  val writerP = interactiveOsascript(
	"""
	 log "in script 1"
     set stdin to do shell script "cat 0<&3"
	 log "in script 2"
     return "hello, " & stdin
  """.trimIndent()
  )
  val p = writerP.second
  val reader = p.inputStream.bufferedReader()
  val readerE = p.errorStream.bufferedReader()
  val writer = writerP.first
  thread {
	reader.forEachLine {
	  println("READ:$it")
	}
  }
  thread {
	readerE.forEachLine {
	  println("ERROR:$it")
	}
  }
  writer.write("HELLO1")
  writer.write("HELLO2")
  writer.close()
  println("CODE=${p.waitFor()}")
}

/*https://stackoverflow.com/questions/70647124/how-to-reduce-overhead-and-run-applescripts-faster*/
@Suppress("UNREACHABLE_CODE") fun appleLeft() {
  err(
	"""
	its not worth it. Doing it through matt.auto.applescript.applescript is extremely slow and there is no workaround for that. The only other option is to do it through objective C, which is extremely complicated and not worth it. You can try again if you want, but trust me its insane and you have to go through annoying accessibility APIs. Even with kotlin native, it is not worth it. Keyboard maestro and magnet seemed to have done exactly this and developed the perfect native code for this. But guess what, they are closed source. Maybe I should just respect their work and use their software for now. Its not that bad...
  """.trimIndent()
  )
  /*https://stackoverflow.com/questions/70647124/how-to-reduce-overhead-and-run-applescripts-faster*/
  sdtInTest()
  tic()
  val app = getFrontmostProcessFromKotlinNative() // NOSONAR
  if (app !is JavaMacApp) {
	val bounds = getFrontmostWindowPositionAndSizeByApplescript()
	runLater {
	  val screen =
		Screen.getScreensForRectangle(bounds.minX, bounds.minY, bounds.width, bounds.height).firstOrNull()!!
	  moveFrontmostWindowByApplescript(
		screen.bounds.minX,
		screen.bounds.minY,
		screen.bounds.width/2,
		screen.bounds.height
	  )
	}
  }
  if (false) {
	runLater {
	  val fakeStage = StageWrapper(Stage())
	  fakeStage.left()
	}
  }
}