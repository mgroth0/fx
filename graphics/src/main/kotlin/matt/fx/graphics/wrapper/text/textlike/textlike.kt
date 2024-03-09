package matt.fx.graphics.wrapper.text.textlike

import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import matt.fx.graphics.font.fixed
import matt.fx.graphics.style.DarkModeController
import matt.fx.graphics.style.sty
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.onHover
import matt.fx.graphics.wrapper.style.FXColor
import matt.lang.anno.Open
import matt.obs.prop.writable.Var

val MONO_FONT: Font by lazy { Font.font("monospaced") }
val CONSOLAS_FONT: Font by lazy { Font.font("Consolas") }

interface TextLike : EventTargetWrapper {
    val textProperty: Var<String?>
    @Open
    var text: String
        get() = textProperty.value ?: ""
        set(value) {
            textProperty.value = value
        }

    val fontProperty: Var<Font>
    @Open var font: Font
        get() = fontProperty.value
        set(value) {
            fontProperty v value
        }

    @Open fun monospace() {
        font = MONO_FONT
    }
}

interface ColoredText : TextLike, NodeWrapper {

    val textFillProperty: Var<Paint?>
    @Open var textFill: Paint?
        get() = textFillProperty.value
        set(value) {
            textFillProperty v value
        }
}

class DarkLightFXColor(
    val darkColor: FXColor,
    val lightColor: FXColor
)

private val DEFAULT_DARK_LIGHT_HIGHLIGHT_COLOR by lazy {
    DarkLightFXColor(
        darkColor = FXColor.YELLOW, lightColor = FXColor.BLUE
    )
}

fun ColoredText.highlightOnHover(
    hoverColor: DarkLightFXColor = DEFAULT_DARK_LIGHT_HIGHLIGHT_COLOR,
    nonHoverColor: Color? = null
) {

    onHover {
        val dark = DarkModeController.darkModeProp.value

        (this as NodeWrapperImpl<*>).sty {
            if (it) {
                val color = if (dark) hoverColor.darkColor else hoverColor.lightColor
                fxFill = color
                fxTextFill = color
            } else {
                fxFill = nonHoverColor
                fxTextFill = nonHoverColor
            }
        }
    }
}

fun <T : ColoredText> T.applyConsoleStyle(
    size: Double? = null,
    color: Color? = null
): T {
    font = CONSOLAS_FONT
    if (size != null) {
        font = font.fixed().copy(size = size).fx()
    }
    if (color != null) {
        textFill = color
    }
    return this
}
