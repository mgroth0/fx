package matt.fx.graphics.wrapper.node.shape.circle

import javafx.scene.Parent
import javafx.scene.paint.Paint
import javafx.scene.shape.Circle
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper
import matt.obs.prop.ObsVal
import matt.obs.prop.writable.BindableProperty

fun NodeWrapperImpl<Parent>.circle(
    centerX: Number = 0.0,
    centerY: Number = 0.0,
    radius: Number = 0.0,
    op: CircleWrapper.() -> Unit = {}
) =
    CircleWrapper(centerX.toDouble(), centerY.toDouble(), radius.toDouble()).attachTo(this, op)

open class CircleWrapper(
    node: Circle = Circle()
): ShapeWrapper<Circle>(node) {

    constructor(
        centerX: Double,
        centerY: Double,
        radius: Double
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


    fun toPoint() = DynamicPoint(x = centerXProperty.cast(Double::class), y = centerYProperty.cast(Double::class))
}


class DynamicPoint(
    val x: ObsVal<Double> = BindableProperty(0.0),
    val y: ObsVal<Double> = BindableProperty(0.0)
)
