package matt.fx.graphics.wrapper.node.line.arc

import javafx.scene.Parent
import javafx.scene.shape.Arc
import matt.fx.graphics.wrapper.node.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper

fun NodeWrapperImpl<Parent>.arc(
  centerX: Number = 0.0,
  centerY: Number = 0.0,
  radiusX: Number = 0.0,
  radiusY: Number = 0.0,
  startAngle: Number = 0.0,
  length: Number = 0.0,
  op: ArcWrapper.()->Unit = {}
) =
  ArcWrapper(
	centerX.toDouble(), centerY.toDouble(), radiusX.toDouble(), radiusY.toDouble(), startAngle.toDouble(),
	length.toDouble()
  ).attachTo(this, op)


open class ArcWrapper(
  node: Arc = Arc(),
): ShapeWrapper<Arc>(node) {
  constructor(
	centerX: Double,
	centerY: Double,
	radiusX: Double,
	radiusY: Double,
	startAngle: Double,
	length: Double
  ): this(Arc(centerX, centerY, radiusX, radiusY, startAngle, length))

  var centerX by node::centerX
  var centerY by node::centerY
  var radiusX by node::radiusX
  var radiusY by node::radiusY
  var startAngle by node::startAngle
  var length by node::length


}