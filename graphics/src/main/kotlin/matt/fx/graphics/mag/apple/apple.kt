@file:Suppress("DEPRECATION")

package matt.fx.graphics.mag.apple

import javafx.geometry.Rectangle2D
import javafx.stage.Screen
import javafx.stage.Stage
import matt.async.date.tic
import matt.auto.applescript
import matt.auto.compileAndOrRunApplescript
import matt.auto.interactiveOsascript
import matt.fx.graphics.mag.left
import matt.hurricanefx.tornadofx.async.runLater
import matt.klib.log.warn
import matt.klib.todo
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
  applescript(
	"""
	tell application "System Events"
		set frontmostProcess to first application process whose name is "$app"
		tell frontmostProcess
			tell (1st window whose value of attribute "AXMain" is true)
				set windowTitle to value of attribute "AXTitle"
				set position to {${x.toInt()}, ${y.toInt()}}
				set size to {${width.toInt()}, ${height.toInt()}}
			end tell
		end tell
	end tell
  """.trimIndent()
  )
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
  var s = applescript(
	"""
	tell application "System Events"
		set frontmostProcess to first application process whose name is "$app"
		tell frontmostProcess
			tell (1st window whose value of attribute "AXMain" is true)
				return {position, size}
			end tell
		end tell
	end tell
  """.trimIndent()
  )
  val x = s.substringBefore(",").trim().toInt()
  s = s.substringAfter(",")
  val y = s.substringBefore(",").trim().toInt()
  s = s.substringAfter(",")
  val width = s.substringBefore(",").trim().toInt()
  s = s.substringAfter(",")
  val height = s.trim().toInt().toString()
  return Rectangle2D(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
}

@Deprecated("use kotlin native instead")
fun getNameOfFrontmostProcessFromApplescript(): String {
  return compileAndOrRunApplescript(
	"getNameOfFrontmostProcess",
  )
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
fun appleLeft() {
  /*https://stackoverflow.com/questions/70647124/how-to-reduce-overhead-and-run-applescripts-faster*/
  sdtInTest()
  tic()
  val appName = getNameOfFrontmostProcessFromApplescript() // NOSONAR
  if (appName != "java") {
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
	  val fakeStage = Stage()
	  fakeStage.left()
	}
  }
}