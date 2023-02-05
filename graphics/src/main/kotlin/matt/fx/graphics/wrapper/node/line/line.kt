package matt.fx.graphics.wrapper.node.line

import javafx.scene.shape.Line
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp

fun ET.line(
  startX: Number = 0.0,
  startY: Number = 0.0,
  endX: Number = 0.0,
  endY: Number = 0.0,
  op: LineWrapper.()->Unit = {}
) =
  LineWrapper(startX.toDouble(), startY.toDouble(), endX.toDouble(), endY.toDouble()).attachTo(this, op)


open class LineWrapper(
  node: Line = Line(),
): ShapeWrapper<Line>(node) {

  constructor(
	startX: Double, startY: Double, endX: Double, endY: Double
  ): this(Line(startX, startY, endX, endY))


  var bothXs: Double
	get() = run {
	  require(startX == endX)
	  startX
	}
	set(value) {
	  startX = value
	  endX = value
	}

  var startX
	get() = node.startX
	set(value) {
	  node.startX = value
	}

  val startXProperty by lazy { node.startXProperty().toNonNullableProp() }
  var startY
	get() = node.startY
	set(value) {
	  node.startY = value
	}

  val startYProperty by lazy { node.startYProperty().toNonNullableProp() }
  var endX
	get() = node.endX
	set(value) {
	  node.endX = value
	}

  val endXProperty by lazy { node.endXProperty().toNonNullableProp() }
  var endY
	get() = node.endY
	set(value) {
	  node.endY = value
	}

  val endYProperty by lazy { node.endYProperty().toNonNullableProp() }


}