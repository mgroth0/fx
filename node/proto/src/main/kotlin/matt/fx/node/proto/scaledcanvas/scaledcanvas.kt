package matt.fx.node.proto.scaledcanvas

import javafx.scene.layout.Pane
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.hurricanefx.eye.lang.DProp
import matt.hurricanefx.eye.prop.math.div
import matt.hurricanefx.eye.prop.math.minus
import matt.hurricanefx.eye.prop.math.times
import matt.hurricanefx.eye.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.graphics.wrapper.canvas.Canv
import matt.fx.graphics.wrapper.canvas.CanvasWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.lang.NEVER
import matt.obs.bindings.math.div
import matt.obs.bindings.math.minus
import matt.obs.bindings.math.times
import matt.obs.prop.BindableProperty

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

  val scale = BindableProperty(initialScale)

  init {
	canvas.apply {
	  layoutXProperty.bind((widthProperty*this@ScaledCanvas.scale - widthProperty)/2)
	  layoutYProperty.bind((heightProperty*this@ScaledCanvas.scale - heightProperty)/2)
	  scaleXProperty.bind(this@ScaledCanvas.scale)
	  scaleYProperty.bind(this@ScaledCanvas.scale)
	  this@ScaledCanvas.regionChildren.add(this)
	}
	exactHeightProperty.bind(actualHeight)
	exactWidthProperty.bind(actualWidth)
  }

  override fun addChild(child: NodeWrapper, index: Int?) = NEVER

  override val height: Double get() = actualHeight.value
  override val width: Double get() = actualWidth.value
}