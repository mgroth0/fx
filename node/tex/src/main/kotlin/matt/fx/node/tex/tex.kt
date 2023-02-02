@file:OptIn(ExperimentalStdlibApi::class)

package matt.fx.node.tex

import javafx.scene.paint.Color
import matt.caching.cache.LRUCache
import matt.collect.map.dmap.withStoringDefault
import matt.fx.graphics.style.intColorToFXColor
import matt.log.warn.warn
import org.scilab.forge.jlatexmath.TeXFormula
import java.awt.image.PixelGrabber
import java.text.ParseException

/*set all to 1 for a working default*/
private const val RESOLUTION_CONTROL = 2f
private const val SCALE_CONTROL = 1.5
private const val INLINE_SCALE_CONTROL = 0.5
/*private const val HEIGHT_CONTROL = 1
private const val WIDTH_CONTROL = 5*/

private const val TEX_FONT_SIZE = 50f*RESOLUTION_CONTROL
private const val TEX_SCALE_COEF = SCALE_CONTROL*0.05f/RESOLUTION_CONTROL
private const val TEX_SCALE_COEF_INLINE = INLINE_SCALE_CONTROL*0.05f/RESOLUTION_CONTROL


class TexNodeFactory


private val validTeXCache by lazy {
  LRUCache<String, Boolean>(100).withStoringDefault {
	try {
	  TeXFormula(it)
	  true
	} catch (e: ParseException) {
	  false
	}
  }
}

private fun String.isValidTeX() = validTeXCache[this]


private fun String.texToPixels(): Array<Array<Color?>>? {
  return try {
	val tf = TeXFormula(this)
	val img = tf.createBufferedImage(
	  TeXFormula.SERIF, TEX_FONT_SIZE,
	  java.awt.Color.CYAN,
	  java.awt.Color.BLACK
	)

	val width = img.getWidth(null)
	val height = img.getHeight(null)
	val pix = IntArray(width*height) { 16711680 }
	PixelGrabber(img, 0, 0, width, height, pix, 0, width).grabPixels()
	val li = Array<Array<Color?>>(height) { Array(width) { null } }

	warn("if the file icon method works, this is unnecessary")

	(0..<width).forEach { j ->
	  (0..<height).forEach { i ->
		val index = if (i == 0) j else ((i*width) + j)
		val rgb = pix[index]
		li[i][j] =
		  intColorToFXColor(rgb).takeIf { it != Color.BLACK }
		  ?: Color.TRANSPARENT
	  }
	}
	li
  } catch (e: ParseException) {
	println(e.message)
	null
  }
}