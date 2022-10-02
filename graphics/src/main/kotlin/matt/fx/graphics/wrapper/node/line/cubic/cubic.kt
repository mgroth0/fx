package matt.fx.graphics.wrapper.node.line.cubic

import javafx.scene.shape.CubicCurve
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper

open class CubicCurveWrapper(
   node: CubicCurve = CubicCurve(),
): ShapeWrapper<CubicCurve>(node) {
  companion object {
	fun CubicCurve.wrapped() = CubicCurveWrapper(this)
  }

  constructor(
	startX: Double,
	startY: Double,
	controlX1: Double,
	controlY1: Double,
	controlX2: Double,
	controlY2: Double,
	endX: Double,
	endY: Double
  ): this(CubicCurve(startX, startY, controlX1, controlY1, controlX2, controlY2, endX, endY))

}