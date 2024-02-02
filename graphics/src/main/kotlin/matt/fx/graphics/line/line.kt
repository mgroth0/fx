package matt.fx.graphics.line

import javafx.scene.shape.Line
import matt.fx.graphics.wrapper.node.line.LineWrapper
import matt.fx.graphics.wrapper.node.line.line
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.style.FXColor
import matt.math.langg.arithmetic.neg.unaryMinus
import matt.model.data.point.BasicPoint
import matt.model.data.point.Point


@Suppress("unused") class LineDrawDSL(private val parent: PaneWrapperImpl<*, *>, start: Point) {
    private var current = start
    private val linesM = mutableListOf<LineWrapper>()
    val lines: List<LineWrapper> = linesM
    fun to(point: Point) {
        linesM += parent.line {
            startX = current.xDouble
            startY = current.yDouble
            endX = point.xDouble
            endY = point.yDouble
            current = BasicPoint(x = endX, y = endY)
        }
    }

    @Suppress("unused") val x get() = current.xDouble
    val y get() = current.yDouble


    fun to(x: Number? = null, y: Number? = null) = to(BasicPoint(x = x ?: current.xDouble, y = y ?: current.yDouble))

    fun move(x: Number, y: Number) = to(BasicPoint(x = current.xDouble + x.toDouble(), y = current.yDouble + y.toDouble()))

    fun right(x: Number) = move(x = x, y = 0.0)
    fun left(x: Number) = move(x = -x, y = 0.0)
    fun up(y: Number) = move(x = 0.0, y = -y)
    fun down(y: Number) = move(x = 0.0, y = y)
}

fun PaneWrapperImpl<*, *>.drawLine(
    color: FXColor, start: Point, op: LineDrawDSL.()->Unit
) {
    LineDrawDSL(this, start).apply {
        op()
        lines.forEach {
            it.stroke = color
            it.fill = color
        }
    }
}


@Suppress("unused") fun Line.startFrom(line: Line) {
    startX = line.endX
    startY = line.endY
}


