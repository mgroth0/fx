package matt.fx.graphics.wrapper.node.shape.circle

import javafx.scene.paint.Paint
import javafx.scene.shape.Circle
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper
import matt.obs.prop.BindableProperty
import matt.obs.prop.ObsVal

open class CircleWrapper(
  node: Circle = Circle(),
): ShapeWrapper<Circle>(node) {

  constructor(
	centerX: Double,
	centerY: Double,
	radius: Double,
  ): this(Circle(centerX, centerY, radius))

  constructor(radius: Double, fill: Paint): this(Circle(radius, fill))
  constructor(radius: Double): this(Circle(radius))


  var radius
	get() = node.radius
	set(value) {
	  node.radius = value
	}
  val radiusProperty by lazy { node.radiusProperty().toNonNullableProp() }
  val centerXProperty by lazy { node.centerXProperty().toNonNullableProp() }
  val centerYProperty by lazy { node.centerYProperty().toNonNullableProp() }


  fun toPoint() = DynamicPoint(x = centerXProperty.cast(), y = centerYProperty.cast())

}


class DynamicPoint(
  val x: ObsVal<Double> = BindableProperty(0.0),
  val y: ObsVal<Double> = BindableProperty(0.0)
)