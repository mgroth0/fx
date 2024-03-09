package matt.fx.graphics.wrapper.node.line.arc

import javafx.scene.Parent
import javafx.scene.shape.Arc
import javafx.scene.shape.ArcType
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper

fun NodeWrapperImpl<Parent>.arc(
    type: ArcType,
    centerX: Number = 0.0,
    centerY: Number = 0.0,
    radiusX: Number = 0.0,
    radiusY: Number = 0.0,
    startAngle: Number = 0.0,
    length: Number = 0.0,
    op: ArcWrapper.() -> Unit = {}
) =
    ArcWrapper(
        centerX.toDouble(), centerY.toDouble(), radiusX.toDouble(), radiusY.toDouble(), startAngle.toDouble(),
        length.toDouble(), type = type
    ).attachTo(this, op)


open class ArcWrapper(
    node: Arc = Arc()
) : ShapeWrapper<Arc>(node) {
    constructor(
        centerX: Double,
        centerY: Double,
        radiusX: Double,
        radiusY: Double,
        startAngle: Double,
        length: Double,
        type: ArcType
    ) : this(Arc(centerX, centerY, radiusX, radiusY, startAngle, length).apply { typeProperty().set(type) })

    val centerXProp: NonNullFXBackedBindableProp<Number> = node.centerXProperty().toNonNullableProp()
    val centerYProp: NonNullFXBackedBindableProp<Number> = node.centerYProperty().toNonNullableProp()
    var centerX by centerXProp
    var centerY by centerYProp

    val radiusXProp: NonNullFXBackedBindableProp<Number> = node.radiusXProperty().toNonNullableProp()
    val radiusYProp: NonNullFXBackedBindableProp<Number> = node.radiusYProperty().toNonNullableProp()

    var radiusX by node::radiusX
    var radiusY by node::radiusY
    var startAngle by node::startAngle
    var length by node::length
    var type: ArcType by node::type
}
