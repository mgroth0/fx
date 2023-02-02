package matt.fx.node.tex.internal

import javafx.scene.paint.Color
import matt.caching.cache.LRUCache
import matt.collect.map.dmap.withStoringDefault
import matt.fx.graphics.style.intColorToFXColor
import matt.log.warn.warn
import org.scilab.forge.jlatexmath.TeXFormula
import java.awt.image.PixelGrabber
import java.text.ParseException







internal val validTeXCache by lazy {
  LRUCache<String, Boolean>(100).withStoringDefault {
	try {
	  TeXFormula(it)
	  true
	} catch (e: ParseException) {
	  false
	}
  }
}

internal fun String.isValidTeX() = validTeXCache[this]


internal fun String.texToPixels(
  texFontSize: Float
): Array<Array<Color?>>? {
  return try {
	val tf = TeXFormula(this)
	val img = tf.createBufferedImage(
	  TeXFormula.SERIF, texFontSize,
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