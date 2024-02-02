package matt.fx.graphics.wrapper.node.line.cubic

import javafx.scene.Parent
import javafx.scene.shape.CubicCurve
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper

fun NodeWrapperImpl<Parent>.cubiccurve(
    startX: Number = 0.0,
    startY: Number = 0.0,
    controlX1: Number = 0.0,
    controlY1: Number = 0.0,
    controlX2: Number = 0.0,
    controlY2: Number = 0.0,
    endX: Number = 0.0,
    endY: Number = 0.0,
    op: CubicCurveWrapper.()->Unit = {}
) =
    CubicCurveWrapper(
        startX.toDouble(), startY.toDouble(), controlX1.toDouble(), controlY1.toDouble(), controlX2.toDouble(),
        controlY2.toDouble(), endX.toDouble(), endY.toDouble()
    ).attachTo(this, op)

open class CubicCurveWrapper(
    node: CubicCurve = CubicCurve(),
): ShapeWrapper<CubicCurve>(node) {

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
