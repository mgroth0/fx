package matt.fx.graphics.wrapper.node.line.arc

import javafx.scene.shape.Arc
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper

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