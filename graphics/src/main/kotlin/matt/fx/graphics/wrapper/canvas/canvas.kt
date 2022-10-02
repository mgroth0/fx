package matt.fx.graphics.wrapper.canvas

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.PixelWriter
import javafx.scene.paint.Color
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.hurricanefx.wrapper.node.NodeWrapperImpl
import matt.fx.graphics.wrapper.sizeman.HasHeight
import matt.fx.graphics.wrapper.sizeman.HasWidth
import matt.lang.NOT_IMPLEMENTED
import matt.obs.bindings.math.ObsD
import matt.obs.bindings.math.times

interface Canv {

  /*does not consider scale*/
  var pixelWidth: Double
  var pixelHeight: Double

  /*considers scale*/
  val actualWidth: ObsD
  val actualHeight: ObsD

  val graphicsContext: GraphicsContext
  val pixelWriter: PixelWriter
  operator fun set(x: Int, y: Int, c: Color) = pixelWriter.setColor(x, y, c)

  fun drawBorder() {
	graphicsContext.apply {
	  stroke = Color.YELLOW /*BLUE*/
	  fill = Color.YELLOW /*BLUE*/
	  rect(0.0, 0.0, pixelWidth, 2.0)
	  fill()
	  rect(pixelWidth - 2.0, 0.0, 2.0, pixelHeight)
	  fill()
	  rect(0.0, pixelHeight - 2.0, pixelWidth, 2.0)
	  fill()
	  rect(0.0, 0.0, 2.0, pixelHeight)
	  fill()
	}
  }
}

open class CanvasWrapper(node: Canvas = Canvas()): NodeWrapperImpl<Canvas>(node), Canv, HasWidth, HasHeight {
  constructor(
	width: Double,
	height: Double
  ): this(Canvas(width, height))


  final override val widthProperty = node.widthProperty().toNonNullableProp().cast<Double>()
  override var pixelWidth by widthProperty
  final override val heightProperty = node.heightProperty().toNonNullableProp().cast<Double>()
  override var pixelHeight by heightProperty

  override val actualWidth by lazy { widthProperty*scaleXProperty }
  override val actualHeight by lazy { heightProperty*scaleYProperty }

  override val graphicsContext: GraphicsContext get() = node.graphicsContext2D
  override fun addChild(child: NodeWrapper, index: Int?) = NOT_IMPLEMENTED


  override val pixelWriter: PixelWriter by lazy { graphicsContext.pixelWriter }


}

