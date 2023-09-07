package matt.fx.node.tex.internal

import javafx.scene.paint.Color
import matt.caching.cache.LRUCache
import matt.collect.map.dmap.withStoringDefault
import matt.fig.model.EquationIr
import matt.fig.render.EquationRenderer
import matt.fx.graphics.style.intColorToFXColor
import matt.fx.graphics.wrapper.style.toFXColor
import matt.fx.node.tex.EquationNotReallyIrTex
import matt.log.warn.warn
import matt.model.code.tex.TexCode
import org.scilab.forge.jlatexmath.TeXFormula
import java.awt.image.PixelGrabber
import java.text.ParseException


internal val validTeXCache by lazy {
    LRUCache<TexCode, Boolean>(100).withStoringDefault {
        try {
            TeXFormula(it.code)
            true
        } catch (e: ParseException) {
            false
        }
    }
}

internal fun TexCode.isValidTeX() = validTeXCache[this]

class TexToPixelRenderer(
    private val texFontSize: Float,
    private val darkMode: Boolean = true
) : EquationRenderer<Array<Array<Color?>>?> {

    private val fg = if (darkMode) java.awt.Color.CYAN else java.awt.Color.BLACK
    private val bg = if (darkMode) java.awt.Color.BLACK else java.awt.Color.CYAN

    override fun render(equationData: EquationIr): Array<Array<Color?>>? {
        val code = (equationData as EquationNotReallyIrTex).code

        return try {

            val bgFX = bg.toFXColor()
            val tf = TeXFormula(code.code)
            val img = tf.createBufferedImage(
                TeXFormula.SERIF,
                texFontSize,
                fg,
                bg
            )

            val width = img.getWidth(null)
            val height = img.getHeight(null)
            val pix = IntArray(width * height) { 16711680 }
            PixelGrabber(img, 0, 0, width, height, pix, 0, width).grabPixels()
            val li = Array<Array<Color?>>(height) { Array(width) { null } }

            warn("if the file icon method works, this is unnecessary")

            (0..<width).forEach { j ->
                (0..<height).forEach { i ->
                    val index = if (i == 0) j else ((i * width) + j)
                    val rgb = pix[index]
                    li[i][j] = intColorToFXColor(rgb).takeIf { it != bgFX } ?: Color.TRANSPARENT
                }
            }
            li
        } catch (e: ParseException) {
            println(e.message)
            null
        }

    }
}