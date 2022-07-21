package matt.fx.graphics

import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.text.Font
import matt.async.MyTimerTask
import matt.async.date.Duration
import matt.async.every
import matt.color.AColor
import matt.color.mostContrastingForMe
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.tornadofx.shapes.line
import matt.hurricanefx.wrapper.LineWrapper
import matt.hurricanefx.wrapper.NodeWrapper
import matt.hurricanefx.wrapper.PaneWrapper
import matt.klib.math.BasicPoint
import matt.klib.math.Point
import matt.klib.math.unaryMinus

fun <T : Node> T.refreshWhileInSceneEvery(
  refresh_rate: Duration,
  op: MyTimerTask.(T) -> Unit
) {
  val thisNode: T = this
  sceneProperty().onChange {
	if (it != null) {
	  every(refresh_rate, ownTimer = true) {
		if (thisNode.scene == null) {
		  cancel()
		}
		op(thisNode)
	  }
	}
  }
}
fun <T: Node> NodeWrapper<T>.refreshWhileInSceneEvery(
  refresh_rate: Duration,
  op: MyTimerTask.(T) -> Unit
) = node.refreshWhileInSceneEvery(refresh_rate,op)

val fontFamilies: List<String> by lazy { Font.getFamilies() }

interface Inspectable {
  fun inspect(): PaneWrapper
}


typealias FXColor = Color

fun FXColor.toAwtColor() = AColor(red.toFloat(), green.toFloat(), blue.toFloat(), opacity.toFloat())
fun AColor.toFXColor() = FXColor(red/255.0, green/255.0, blue/255.0, alpha/255.0)
fun FXColor.mostContrastingForMe() = toAwtColor().mostContrastingForMe().toFXColor()


@Suppress("unused") class LineDrawDSL(private val parent: PaneWrapper, start: Point) {
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

fun PaneWrapper.drawLine(
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
