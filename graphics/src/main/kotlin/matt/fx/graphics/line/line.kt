package matt.fx.graphics.line

import javafx.scene.shape.Line
import matt.hurricanefx.wrapper.line.LineWrapper
import matt.hurricanefx.wrapper.node.line
import matt.hurricanefx.wrapper.pane.PaneWrapperImpl
import matt.hurricanefx.wrapper.style.FXColor
import matt.math.BasicPoint
import matt.math.Point
import matt.math.unaryMinus


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


  fun to(x: Number? = null, y: Number? = null) {
	return to(BasicPoint(x = x ?: current.xDouble, y = y ?: current.yDouble))
  }

  fun move(x: Number, y: Number) {
	return to(BasicPoint(x = current.xDouble + x.toDouble(), y = current.yDouble + y.toDouble()))
  }

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


