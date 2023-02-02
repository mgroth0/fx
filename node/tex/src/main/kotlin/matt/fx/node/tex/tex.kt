package matt.fx.node.tex

import javafx.scene.paint.Color
import matt.caching.cache.LRUCache
import matt.collect.map.dmap.withStoringDefault
import matt.fx.graphics.wrapper.pane.vbox.VBoxW
import matt.fx.node.proto.scaledcanvas.ScaledCanvas
import matt.fx.node.proto.scaledcanvas.toCanvas
import matt.fx.node.tex.internal.isValidTeX
import matt.fx.node.tex.internal.texToPixels
import matt.obs.math.double.op.times
import matt.obs.prop.BindableProperty

private const val RESOLUTION_CONTROL = 2f
private const val TEX_FONT_SIZE = 50f*RESOLUTION_CONTROL

class TexNodeFactory(

  val scale: Double,
  //  val scale: BindableProperty<Double>

  /*set all to 1 for a working default*/

  //  private const val SCALE_CONTROL = 1.5
  //	  private const val INLINE_SCALE_CONTROL = 0.5


  /*private const val HEIGHT_CONTROL = 1
	private const val WIDTH_CONTROL = 5*/


) {


  private val scaleCoef = scale*0.05f/RESOLUTION_CONTROL


  fun isValidTex(code: String) = code.isValidTeX()

  /*private fun toPixels(code: String) = code.texToPixels(texFontSize = TEX_FONT_SIZE)*/

  fun toCanvas(code: String) = texPixels[code]?.toCanvas()?.let {
	TexCanvas(it, scaleCoef)
	/*scale.bind(
	  (fontSizeProp*scaleCoef)
	)*/
  }
  //  code.texToPixels(
  //	texFontSize = TEX_FONT_SIZE
  //  )

  private val texPixels by lazy {
	LRUCache<String, Array<Array<Color?>>?>(100)
	  .withStoringDefault {
		it.texToPixels(
		  texFontSize = TEX_FONT_SIZE
		)
	  }
  }
}

class TexCanvas(
  innerCanv: ScaledCanvas,
  someScaleThing: Double
): VBoxW() {
  val fontSizeSortOf = BindableProperty(12.0)

  init {
	innerCanv.scale.bind(
	  fontSizeSortOf*someScaleThing
	)
	+innerCanv
  }
}