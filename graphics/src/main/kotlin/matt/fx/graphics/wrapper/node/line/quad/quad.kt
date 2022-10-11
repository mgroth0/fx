package matt.fx.graphics.wrapper.node.line.quad

import javafx.scene.shape.QuadCurve
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper

open class QuadCurveWrapper(
  node: QuadCurve = QuadCurve(),
): ShapeWrapper<QuadCurve>(node) {

  constructor(
	startX: Double, startY: Double, controlX: Double, controlY: Double, endX: Double, endY: Double
  ): this(QuadCurve(startX, startY, controlX, controlY, endX, endY))


}

