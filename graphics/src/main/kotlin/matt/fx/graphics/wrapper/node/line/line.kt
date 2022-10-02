package matt.fx.graphics.wrapper.node.line

import javafx.scene.Parent
import javafx.scene.shape.Line
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper

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
  companion object {
	fun Line.wrapped() = LineWrapper(this)
  }


  constructor(
	startX: Double, startY: Double, endX: Double, endY: Double
  ): this(Line(startX, startY, endX, endY))


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