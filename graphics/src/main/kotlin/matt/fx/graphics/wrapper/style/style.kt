package matt.fx.graphics.wrapper.style

import javafx.css.CssMetaData
import javafx.css.PseudoClass
import javafx.css.Styleable
import javafx.scene.Node
import javafx.scene.paint.Color
import matt.color.AwtColor
import matt.color.ContrastAlgorithm
import matt.color.IntColor
import matt.color.calculateContrastingColor
import matt.color.name.findName
import matt.color.toAwtColor
import matt.color.toMColor
import matt.fx.base.wrapper.obs.collect.list.createMutableWrapper
import matt.fx.base.wrapper.obs.collect.set.createImmutableWrapper
import matt.fx.graphics.wrapper.style.FXStyle.fill
import matt.fx.graphics.wrapper.style.FXStyle.`text-fill`
import matt.lang.NOT_IMPLEMENTED
import matt.lang.anno.Open
import matt.lang.err
import matt.obs.col.olist.MutableObsList
import matt.obs.col.oset.ObsSet
import matt.prim.str.LineAppender

typealias FXColor = Color

fun IntColor.toFXColor() = toAwtColor().toFXColor()
fun FXColor.toMColor() = toAwtColor().toMColor()
fun FXColor.toAwtColor() = AwtColor(red.toFloat(), green.toFloat(), blue.toFloat(), opacity.toFloat())
fun FXColor.findName() = toMColor().findName()
fun FXColor.hex() = toMColor().hex()
fun AwtColor.toFXColor() = FXColor(red / 255.0, green / 255.0, blue / 255.0, alpha / 255.0)
fun FXColor.mostContrasting(alg: ContrastAlgorithm) = toMColor().calculateContrastingColor(alg).toFXColor()

abstract class StyleableWrapperImpl(private val node: Styleable) : StyleableWrapper {
    final override val typeSelector: String get() = node.typeSelector
    final override val id: String? get() = node.id
    final override val cssMetaData: MutableList<CssMetaData<out Styleable, *>> get() = node.cssMetaData
    final override val styleableParent: Styleable? get() = node.styleableParent
    final override val pseudoClassStates by lazy {
        node.pseudoClassStates.createImmutableWrapper()
    }
    final override val styleClass: MutableObsList<String> by lazy { node.styleClass.createMutableWrapper() }
    final override fun getTheStyle(): String? = node.style
}

class StyleableWrapperImpl2(private val node: Node) : StyleableWrapperImpl(node) {
    override fun setTheStyle(value: String) {
        node.style = value
    }
}


interface StyleableWrapper {
    val typeSelector: String
    val id: String?

    val cssMetaData: List<CssMetaData<out Styleable, *>>
    val styleableParent: Styleable?
    val pseudoClassStates: ObsSet<PseudoClass>

    val styleClass: MutableObsList<String>


    @Open
    var style
        get() = getTheStyle()
        set(value) {
            setTheStyle(value!!)
        }

    fun setTheStyle(value: String)
    fun getTheStyle(): String?


    @Open    var fillStyle: Color
        get() = NOT_IMPLEMENTED
        set(value) {
            style += "${fill.key}: ${value.toMColor().hex()};"
        }
    @Open   var textFillStyle: Color
        get() = NOT_IMPLEMENTED
        set(value) {
            style += "${`text-fill`.key}: ${value.toMColor().hex()};"
        }


    @Open    fun styleInfo(): String {
        val r = LineAppender()
        r += ("${this::class}->$typeSelector")

        r += ("\tstyleclasses")
        styleClass.forEach { sc ->
            r += ("\t\t$sc")
        }

        r += ("\tpseudo")
        pseudoClassStates.forEach { pc ->
            r += ("\t\t$pc->${pc.pseudoClassName}")
        }
        r += ("\tsample")
        r += ("\t\t$style")
        if (false) {    // string too big!
            r += ("\tmeta (${cssMetaData.size})")
            cssMetaData.forEach {
                r += ("\t\t$it")
            }
        }
        return r.toString()
    }
}

@Suppress("EnumEntryName")
enum class FXStyle {
    fill, `text-fill`, `font-size`, `font-family`, `font-weight`, `font-style`;

    companion object {
        fun of(vararg pairs: Pair<FXStyle, String>): String = pairs.joinToString("; ") { "${it.first.key}: ${it.second}" }
    }

    val key = "-fx-$name"
}

fun String.parseFXStyle() =
    split(";").map { it.substringBefore(":") to it.substringAfter(":") }.associate { (key, value) ->
        (FXStyle.entries.firstOrNull { it.key == key.trim() } ?: err(
            """
            unknown fx style key: $key in "${this@parseFXStyle}"
            """.trimIndent()
        )) to value
    }

typealias FXStyleMap = Map<FXStyle, String>

fun FXStyleMap.toStyleString() = entries.joinToString(";") { "${it.key.key}: ${it.value}" }
