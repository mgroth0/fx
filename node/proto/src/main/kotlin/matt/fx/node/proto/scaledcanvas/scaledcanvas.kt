package matt.fx.node.proto.scaledcanvas

import javafx.scene.layout.Pane
import matt.hurricanefx.eye.lang.DProp
import matt.hurricanefx.eye.prop.math.div
import matt.hurricanefx.eye.prop.math.minus
import matt.hurricanefx.eye.prop.math.times
import matt.hurricanefx.eye.wrapper.obs.obsval.toNonNullableROProp
import matt.hurricanefx.wrapper.canvas.Canv
import matt.hurricanefx.wrapper.canvas.CanvasWrapper
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.hurricanefx.wrapper.node.attach
import matt.hurricanefx.wrapper.region.RegionWrapperImpl
import matt.hurricanefx.wrapper.target.EventTargetWrapper
import matt.lang.NEVER

fun EventTargetWrapper.scaledCanvas(
  width: Number,
  height: Number,
  scale: Number = 1.0,
  op: ScaledCanvas.()->Unit = {}
) =
  attach(
	ScaledCanvas(
	  width = width,
	  height = height,
	  initialScale = scale.toDouble()
	), op
  )

fun EventTargetWrapper.scaledCanvas(
  hw: Number,
  scale: Number = 1.0,
  op: ScaledCanvas.()->Unit = {}
) = scaledCanvas(height = hw, width = hw, scale = scale, op = op)

open class ScaledCanvas(
  canvas: CanvasWrapper = CanvasWrapper(),
  initialScale: Double = 1.0,
): RegionWrapperImpl<Pane, CanvasWrapper>(Pane()), Canv by canvas {

  constructor(
	height: Number,
	width: Number,
	initialScale: Double = 1.0,
  ): this(CanvasWrapper(width = width.toDouble(), height = height.toDouble()), initialScale)

  constructor(hw: Number, scale: Double): this(height = hw.toDouble(), width = hw.toDouble(), initialScale = scale)

  val scale = DProp(initialScale)

  init {
	canvas.apply {
	  layoutXProperty.bind((widthProperty*this@ScaledCanvas.scale - widthProperty)/2)
	  layoutYProperty.bind((heightProperty*this@ScaledCanvas.scale - heightProperty)/2)
	  scaleXProperty.bind(this@ScaledCanvas.scale)
	  scaleYProperty.bind(this@ScaledCanvas.scale)
	  this@ScaledCanvas.regionChildren.add(this)
	}
	exactHeightProperty.bind(actualHeight.toNonNullableROProp().cast())
	exactWidthProperty.bind(actualWidth.toNonNullableROProp().cast())
  }

  override fun addChild(child: NodeWrapper, index: Int?) = NEVER

  override val height: Double get() = actualHeight.value
  override val width: Double get() = actualWidth.value
}