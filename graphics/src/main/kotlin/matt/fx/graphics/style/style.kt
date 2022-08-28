package matt.fx.graphics.style

import com.jthemedetecor.OsThemeDetector
import javafx.application.Platform.runLater
import javafx.scene.Node
import javafx.scene.paint.Color
import matt.color.hex
import matt.css.MyStyleDsl
import matt.hurricanefx.wrapper.node.NodeWrapperImpl
import matt.hurricanefx.wrapper.style.toAwtColor
import matt.klib.log.warn
import matt.klib.prop.BasicProperty
import java.util.logging.Level
import kotlin.reflect.KProperty

object DarkModeController {

  private val detector = OsThemeDetector.getDetector()

  init {
	//	LoggerFactory.getLogger(MacOSThemeDetector::class.java)
	/*warn("asked for help on github...")*/
	java.util.logging.LogManager.getLogManager().loggerNames.asIterator().forEach {
	  /*println("logger:${it}")*/
	}
	java.util.logging.Logger.getLogger("com.jthemedetecor.MacOSThemeDetector").level = Level.OFF
	java.util.logging.Logger.getLogger("com.jthemedetecor").level = Level.OFF
  }


  private fun getIsDarkSafe(): Boolean? {
	/*warn("this is pointless. The error is caught and annoying logged inside the library")*/
	return try {
	  detector.isDark
	} catch (e: java.lang.NullPointerException) {
	  /*https://github.com/Dansoftowner/jSystemThemeDetector/issues/25*/
	  warn("caught the null jSystemThemeDetector bug again")
	  null
	}
  }

  val darkModeProp = BasicProperty(getIsDarkSafe() ?: true)

  init {
	detector.registerListener { isDark ->
	  if (isDark != null) {
		runLater {
		  darkModeProp.value = isDark
		}
	  } else {
		warn("isDark was null. Guess that thing is still happening")
	  }
	}
  }
}


val MODENA_CSS = ClassLoader.getSystemResource("modena.css").toString()
val DARK_MODENA_CSS = ClassLoader.getSystemResource("darkModena.css").toString()
val CUSTOM_CSS = ClassLoader.getSystemResource("custom.css").toString()


private class StyleClass {
  operator fun getValue(styleClassDSL: StyleClassDSL, property: KProperty<*>): Any? {
	styleClassDSL.s.styleClass += property.name
	return null
  }
}


class StyleClassDSL(val s: Node): MyStyleDsl() {
  val yellowText by StyleClass()
  val blueText by StyleClass()
  val darkGreyText by StyleClass()
  val redText by StyleClass()
  val whiteText by StyleClass()
  val greenBackground by StyleClass()
  val nodeTextField by StyleClass()
  val presentationMode by StyleClass()
  val flowablePresentationMode by StyleClass()

  private fun styleMap() =
	s.style.split(";").filter { it.isNotBlank() }.map { it.split(":") }.associate { it[0] to it[1] }.toMutableMap()

  private fun updateFromMap(map: Map<String, String>) {
	s.style = map.entries.joinToString(separator = ";") { "${it.key}: ${it.value}" }
  }

  override fun set(key: String, value: Any) {
	val map = styleMap()
	map["-$key"] = value.toString()
	updateFromMap(map)
  }

  override fun get(key: String) = styleMap()["-$key"]!!

  override fun remove(key: String) {
	val map = styleMap()
	map.remove("-$key")
	updateFromMap(map)
  }

  override fun clear() {
	s.style = ""
  }

  var fxTextFill: Color? by custom({ Color.valueOf(this) }, { toAwtColor().hex() })

}

fun NodeWrapperImpl<*>.sty(op: StyleClassDSL.()->Unit) {
  StyleClassDSL(this.node).apply(op)
}




