package matt.fx.graphics.style

import com.jthemedetecor.OsThemeDetector
import javafx.application.Platform.runLater
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.paint.Color
import matt.color.IntColor
import matt.css.MyStyleDsl
import matt.css.props.ColorLikeCssConverter
import matt.fx.graphics.wrapper.FXNodeWrapperDSL
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.fx.graphics.wrapper.style.FXColor
import matt.fx.graphics.wrapper.style.toFXColor
import matt.fx.graphics.wrapper.style.toMColor
import matt.lang.require.requireEquals
import matt.log.warn.dumpStack
import matt.log.warn.warn
import matt.model.op.convert.StringConverter
import matt.obs.prop.BindableProperty
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

    val darkModeProp = BindableProperty(getIsDarkSafe() ?: true)

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


val MODENA_CSS = ClassLoader.getSystemResource("modena/modena.css").toString()
val DARK_MODENA_CSS = ClassLoader.getSystemResource("modena/darkModena.css").toString()
val CUSTOM_CSS = ClassLoader.getSystemResource("modena/custom.css").toString()


fun SceneWrapper<*>.reloadStyle(darkMode: Boolean) {
    stylesheets.clear()
    fill = Color.WHITE /*FX DEFAULT*/
    if (darkMode) {
        stylesheets.add(DARK_MODENA_CSS)
        stylesheets.add(CUSTOM_CSS)

        //     ensure that even while the screen is loading it is black. So not white flashes or flickering while refreshing
        fill =
            Color.BLACK    //        maybe also possible by styleing .root in css, but if I remember correctly that also affects other nodes
    }
}


private class StyleClass {
    operator fun getValue(
        styleClassDSL: StyleClassDSL,
        property: KProperty<*>
    ): Any? {
        styleClassDSL.s.styleClass += property.name
        return null
    }
}

@FXNodeWrapperDSL
class StyleClassDSL(val s: Node) : MyStyleDsl() {
    val yellowText by StyleClass()
    val blueText by StyleClass()
    val darkGreyText by StyleClass()
    val redText by StyleClass()
    val whiteText by StyleClass()
    val greenBackground by StyleClass()
    val nodeTextField by StyleClass()
    val presentationMode by StyleClass()
    val flowablePresentationMode by StyleClass()
    val eDatetag by StyleClass()
    val rDatetag by StyleClass()
    val datetag by StyleClass()
    val flowTypeTag by StyleClass()
    val specialTag by StyleClass()

    private fun styleMap() =
        s.style.split(";").filter { it.isNotBlank() }.map { it.split(":") }.associate { it[0] to it[1] }.toMutableMap()

    private fun updateFromMap(map: Map<String, String>) {
        s.style = map.entries.joinToString(separator = ";") { "${it.key}: ${it.value}" }
    }

    override fun set(
        key: String,
        value: Any
    ) {
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

    var fxTextFill: Color? by custom(FXColorStringConverter)
    var fxStroke: Color? by custom(FXColorStringConverter)
    var fxFill: Color? by custom(FXColorStringConverter)
    var fxBackgroundColor: Color? by custom(FXColorStringConverter)
}

object FXColorStringConverter : StringConverter<FXColor> {
    override fun toString(t: FXColor): String {
        return ColorLikeCssConverter.toString(
            t.toMColor()
        )
    }

    override fun fromString(s: String): FXColor {
        return ((ColorLikeCssConverter.fromString(s) as IntColor).toFXColor())
    }

}


fun NodeWrapper.sty(op: StyleClassDSL.() -> Unit) {
    StyleClassDSL(this.node).apply(op)
}


fun intColorToFXColor(i: Int): Color {
    return Color.rgb(i shr 16 and 0xFF, i shr 8 and 0xFF, i and 0xFF)
}


/*part of this file was taken from tornadofx*/



fun insets(all: Number) = Insets(all.toDouble(), all.toDouble(), all.toDouble(), all.toDouble())
fun insets(
    horizontal: Number? = null,
    vertical: Number? = null
) = Insets(
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

val Insets.horizontal: Double
    get() {
        requireEquals(left, right)
        return left
    }
val Insets.vertical: Double
    get() {
        if (top != bottom) {
            warn("top($top) != bottom($bottom)")
            dumpStack()
        }
        return top
    }
val Insets.all: Double
    get() {
        requireEquals(left, right)
        requireEquals(left, top)
        requireEquals(left, bottom)
        return left
    }