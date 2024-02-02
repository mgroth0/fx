package matt.fx.graphics.wrapper.node.shape.ellipse

import javafx.scene.Parent
import javafx.scene.shape.Ellipse
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper

fun NodeWrapperImpl<Parent>.ellipse(
    centerX: Number = 0.0,
    centerY: Number = 0.0,
    radiusX: Number = 0.0,
    radiusY: Number = 0.0,
    op: EllipseWrapper.()->Unit = {}
) =
    EllipseWrapper(centerX.toDouble(), centerY.toDouble(), radiusX.toDouble(), radiusY.toDouble()).attachTo(this, op)

open class EllipseWrapper(
    node: Ellipse = Ellipse(),
): ShapeWrapper<Ellipse>(node) {

    constructor(
        centerX: Double, centerY: Double, radiusX: Double, radiusY: Double
    ): this(Ellipse(centerX, centerY, radiusX, radiusY))


    var radiusX
        get() = node.radiusX
        set(value) {
            node.radiusX = value
        }

    val radiusXProperty by lazy { node.radiusXProperty().toNonNullableProp().cast<Double>() }


    var radiusY
        get() = node.radiusY
        set(value) {
            node.radiusY = value
        }

    val radiusYProperty by lazy { node.radiusYProperty().toNonNullableProp().cast<Double>() }


}
