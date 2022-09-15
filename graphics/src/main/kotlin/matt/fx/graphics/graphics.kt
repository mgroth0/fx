package matt.fx.graphics

import javafx.scene.effect.Blend
import javafx.scene.effect.BlendMode.DIFFERENCE
import javafx.scene.effect.ColorInput
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.text.Font
import matt.async.schedule.MyTimerTask
import matt.async.schedule.every
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.wrapper.line.LineWrapper
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.hurricanefx.wrapper.node.line
import matt.hurricanefx.wrapper.pane.PaneWrapperImpl
import matt.hurricanefx.wrapper.style.FXColor
import matt.math.BasicPoint
import matt.math.Point
import matt.math.unaryMinus
import matt.time.dur.Duration


val INVERSION_EFFECT by lazy {
  val color = ColorInput()
  color.paint = Color.WHITE
  color.width = Double.MAX_VALUE
  color.height = Double.MAX_VALUE
  val blend = Blend(DIFFERENCE)
  blend.bottomInput = color
  blend
}

fun <T: NodeWrapper> T.refreshWhileInSceneEvery(
  refreshRate: Duration,
  op: MyTimerTask.(T)->Unit
) {
  val thisNode: T = this
  sceneProperty().onChange {
	if (it != null) {
	  every(refreshRate, ownTimer = true) {
		if (thisNode.node.scene == null) {
		  cancel()
		}
		op(thisNode)
	  }
	}
  }
}

val fontFamilies: List<String> by lazy { Font.getFamilies() }

interface Inspectable<N: PaneWrapperImpl<*,*>> {
  fun inspect(): N
}





@Suppress("unused") class LineDrawDSL(private val parent: PaneWrapperImpl<*,*>, start: Point) {
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

fun PaneWrapperImpl<*,*>.drawLine(
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


