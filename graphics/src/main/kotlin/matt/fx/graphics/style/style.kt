package matt.fx.graphics.style

import com.jthemedetecor.OsThemeDetector
import javafx.application.Platform.runLater
import javafx.css.Styleable
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.layout.Border
import javafx.scene.layout.BorderStroke
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import matt.color.hex
import matt.fx.graphics.toAwtColor
import matt.klib.commons.RootProjects.flow
import matt.klib.commons.get
import matt.kjlib.prop.BasicBooleanProperty
import matt.klib.css.ColorLike
import matt.klib.css.LinearGradient
import matt.klib.css.MyStyleDsl
import matt.klib.str.LineAppender
import matt.klib.log.warn
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

  val darkModeProp = BasicBooleanProperty(getIsDarkSafe() ?: true)

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

val MODENA_CSS = flow.folder["style"]["modena.css"].toURI().toURL().toString()
val DARK_MODENA_CSS = flow.folder["style"]["darkModena.css"].toURI().toURL().toString()
val CUSTOM_CSS = flow.folder["style"]["custom.css"].toURI().toURL().toString()


fun Styleable.styleInfo(): String {
  val r = LineAppender()
  r += ("${this::class}->${typeSelector}")

  r += ("\tstyleclasses")
  styleClass.forEach { sc ->
	r += ("\t\t$sc")
  }

  r += ("\tpseudo")
  pseudoClassStates.forEach { pc ->
	r += ("\t\t$pc->${pc.pseudoClassName}")
  }
  r += ("\tsample")
  r += ("\t\t${style}")
  if (false) {
	// string too big!
	r += ("\tmeta (${cssMetaData.size})")
	cssMetaData.forEach {
	  r += ("\t\t${it}")
	}
  }
  return r.toString()
}

var Region.borderFill: Paint?
  get() = border?.strokes?.firstOrNull()?.topStroke
  set(value) {
	border = if (value == null) null
	else {
	  Border(BorderStroke(value, BorderStrokeStyle.SOLID, null, null))
	}

  }

var Region.borderDashFill: Paint?
  get() = border?.strokes?.firstOrNull { it.topStyle == BorderStrokeStyle.DASHED }?.topStroke
  set(value) {
	border = if (value == null) null
	else {
	  Border(BorderStroke(value, BorderStrokeStyle.DASHED, null, null))
	}

  }

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

fun Node.sty(op: StyleClassDSL.()->Unit) {
  StyleClassDSL(this).apply(op)
}


fun Region.yellow() {
  borderDashFill = Color.YELLOW
}

fun Region.blue() {
  borderDashFill = Color.BLUE
}

fun Region.purple() {
  borderDashFill = Color.PURPLE
}

fun Region.green() {
  borderDashFill = Color.GREEN
}

fun Region.red() {
  borderDashFill = Color.RED
}

fun Region.orange() {
  borderDashFill = Color.ORANGE
}


/*part of this file was taken from tornadofx*/



fun insets(all: Number) = Insets(all.toDouble(), all.toDouble(), all.toDouble(), all.toDouble())
fun insets(horizontal: Number? = null, vertical: Number? = null) = Insets(
  vertical?.toDouble() ?: 0.0,
  horizontal?.toDouble() ?: 0.0,
  vertical?.toDouble() ?: 0.0,
  horizontal?.toDouble() ?: 0.0
)

fun insets(
  top: Number? = null,
  right: Number? = null,
  bottom: Number? = null,
  left: Number? = null
) = Insets(
  top?.toDouble() ?: 0.0,
  right?.toDouble() ?: 0.0,
  bottom?.toDouble() ?: 0.0,
  left?.toDouble() ?: 0.0
)

fun Insets.copy(
  top: Number? = null,
  right: Number? = null,
  bottom: Number? = null,
  left: Number? = null
) = Insets(
  top?.toDouble() ?: this.top,
  right?.toDouble() ?: this.right,
  bottom?.toDouble() ?: this.bottom,
  left?.toDouble() ?: this.left
)


fun Insets.copy(
  horizontal: Number? = null,
  vertical: Number? = null
) = Insets(
  vertical?.toDouble() ?: this.top,
  horizontal?.toDouble() ?: this.right,
  vertical?.toDouble() ?: this.bottom,
  horizontal?.toDouble() ?: this.left
)

val Insets.horizontal get() = (left + right)/2
val Insets.vertical get() = (top + bottom)/2
val Insets.all get() = (left + right + top + bottom)/4