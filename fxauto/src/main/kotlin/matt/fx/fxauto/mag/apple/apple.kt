package matt.fx.fxauto.mag.apple

import javafx.application.Platform.runLater
import javafx.geometry.Rectangle2D
import javafx.stage.Screen
import javafx.stage.Stage
import matt.async.thread.namedThread
import matt.auto.ascript.AppleScriptExpressionImpl
import matt.auto.ascript.AppleScriptString
import matt.auto.ascript.DoShellScript
import matt.auto.ascript.interactiveOsascript
import matt.auto.ascript.runAppleScript
import matt.auto.compileAndOrRunApplescript
import matt.auto.macapp.MacApp.Companion.getFrontmostProcessFromKotlinNative
import matt.auto.macapp.java.JavaMacApp
import matt.auto.macapp.sysevents.SystemEvents
import matt.fx.graphics.mag.left
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.lang.common.err
import matt.lang.common.substringAfterSingular
import matt.lang.common.substringBeforeSingular
import matt.log.profile.stopwatch.tic
import matt.shell.commonj.context.ReapingShellExecutionContext

context(ReapingShellExecutionContext)
fun moveFrontmostWindowByApplescript(
    x: Number,
    y: Number,
    width: Number,
    height: Number
) {
    println(
        "MOVE:" +
            compileAndOrRunApplescript(
                "moveFrontmostWindow", x.toInt().toString(), y.toInt().toString(), width.toInt().toString(),
                height.toInt().toString()
            )
    )
}

context(ReapingShellExecutionContext)
fun moveAppWindowByApplescript(
    app: String,
    x: Number,
    y: Number,
    width: Number,
    height: Number
) {

    runAppleScript {
        tell(SystemEvents) {
            val frontmostProcess by AppleScriptExpressionImpl("get first application process whose name is \"$app\"")
            tell("frontmostProcess") {
                tell("(1st window whose value of attribute \"AXMain\" is true)") {
                    set("windowTitle", AppleScriptExpressionImpl("value of attribute \"AXTitle\""))
                    set("position", AppleScriptExpressionImpl("{${x.toInt()}, ${y.toInt()}}"))
                    set("size", AppleScriptExpressionImpl("{${width.toInt()}, ${height.toInt()}}"))
                }
            }
        }
    }
}

context(ReapingShellExecutionContext)
fun getFrontmostWindowPositionAndSizeByApplescript(): Rectangle2D {
    var s = compileAndOrRunApplescript("getFrontmostWindowPositionAndSize")
    val x = s.substringBeforeSingular(",").trim().toInt()
    s = s.substringAfterSingular(",")
    val y = s.substringBeforeSingular(",").trim().toInt()
    s = s.substringAfterSingular(",")
    val width = s.substringBeforeSingular(",").trim().toInt()
    s = s.substringAfterSingular(",")
    val height = s.trim().toInt().toString()
    return Rectangle2D(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
}

context(ReapingShellExecutionContext)
fun getAppWindowPositionAndSizeByApplescript(app: String): Rectangle2D {
    var s =
        runAppleScript {
            tell(SystemEvents) {
                val frontmostProcess by AppleScriptExpressionImpl("get first application process whose name is \"$app\"")
                tell("frontmostProcess") {
                    tell("(1st window whose value of attribute \"AXMain\" is true)") {
                        `return`(AppleScriptExpressionImpl("{position, size}"))
                    }
                }
            }
        }
    val x = s.substringBeforeSingular(",").trim().toInt()
    s = s.substringAfterSingular(",")
    val y = s.substringBeforeSingular(",").trim().toInt()
    s = s.substringAfterSingular(",")
    val width = s.substringBeforeSingular(",").trim().toInt()
    s = s.substringAfterSingular(",")
    val height = s.trim().toInt().toString()
    return Rectangle2D(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
}


context(ReapingShellExecutionContext)
fun sdtInTest() {


    val writerP =
        interactiveOsascript {
            log(AppleScriptString("in script 1"))
            set("stdin", DoShellScript("cat", "0<&3"))
            log(AppleScriptString("in script 2"))
            `return`(AppleScriptString("hello, ").concat(AppleScriptExpressionImpl("stdin")))
        }
    val p = writerP.second
    val reader = p.inputStream.bufferedReader()
    val readerE = p.errorStream.bufferedReader()
    val writer = writerP.first
    namedThread("stdInTest Thread 1") {
        reader.forEachLine {
            println("READ:$it")
        }
    }
    namedThread("stdInTest Thread 2") {
        readerE.forEachLine {
            println("ERROR:$it")
        }
    }
    writer.write("HELLO1")
    writer.write("HELLO2")
    writer.close()
    println("CODE=${p.waitFor()}")
}

context(ReapingShellExecutionContext)
/*https://stackoverflow.com/questions/70647124/how-to-reduce-overhead-and-run-applescripts-faster*/
@Suppress("UNREACHABLE_CODE")
fun appleLeft() {
    err(
        """
        its not worth it. Doing it through matt.auto.applescript.applescript is extremely slow and there is no workaround for that. The only other option is to do it through objective C, which is extremely complicated and not worth it. You can try again if you want, but trust me its insane and you have to go through annoying accessibility APIs. Even with kotlin native, it is not worth it. Keyboard maestro and magnet seemed to have done exactly this and developed the perfect native code for this. But guess what, they are closed source. Maybe I should just respect their work and use their software for now. Its not that bad...
        """.trimIndent()
    )/*https://stackoverflow.com/questions/70647124/how-to-reduce-overhead-and-run-applescripts-faster*/
    sdtInTest()
    tic()
    val app = getFrontmostProcessFromKotlinNative()
    if (app !is JavaMacApp) {
        val bounds = getFrontmostWindowPositionAndSizeByApplescript()
        runLater {
            val screen =
                Screen.getScreensForRectangle(bounds.minX, bounds.minY, bounds.width, bounds.height).firstOrNull()!!
            moveFrontmostWindowByApplescript(
                screen.bounds.minX, screen.bounds.minY, screen.bounds.width / 2, screen.bounds.height
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
