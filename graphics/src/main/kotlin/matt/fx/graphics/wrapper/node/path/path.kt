package matt.fx.graphics.wrapper.node.path

import javafx.collections.ObservableList
import javafx.scene.Parent
import javafx.scene.shape.ArcTo
import javafx.scene.shape.ClosePath
import javafx.scene.shape.CubicCurveTo
import javafx.scene.shape.HLineTo
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.shape.PathElement
import javafx.scene.shape.QuadCurveTo
import javafx.scene.shape.VLineTo
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper

fun NodeWrapperImpl<Parent>.path(vararg elements: PathElement, op: PathWrapper.()->Unit = {}) =
  PathWrapper(*elements).attachTo(this, op)


open class PathWrapper(
   node: Path = Path(),
): ShapeWrapper<Path>(node) {
  constructor(
	vararg elements: PathElement
  ): this(Path(*elements))


  val elements: ObservableList<PathElement> get() = node.elements



  fun moveTo(x: Number = 0.0, y: Number = 0.0) = apply {
	elements.add(MoveTo(x.toDouble(), y.toDouble()))
  }

  fun hlineTo(x: Number) = apply { elements.add(HLineTo(x.toDouble())) }

  fun vlineTo(y: Number) = apply { elements.add(VLineTo(y.toDouble())) }

  fun quadcurveTo(
	controlX: Number = 0.0,
	controlY: Number = 0.0,
	x: Number = 0.0,
	y: Number = 0.0,
	op: QuadCurveTo.()->Unit = {}
  ) = apply {
	elements.add(QuadCurveTo(controlX.toDouble(), controlY.toDouble(), x.toDouble(), y.toDouble()).also(op))
  }

  fun cubiccurveTo(
	controlX1: Number = 0.0,
	controlY1: Number = 0.0,
	controlX2: Number = 0.0,
	controlY2: Number = 0.0,
	x: Number = 0.0,
	y: Number = 0.0,
	op: CubicCurveTo.()->Unit = {}
  ) = apply {
	elements.add(
	  CubicCurveTo(
		controlX1.toDouble(), controlY1.toDouble(), controlX2.toDouble(), controlY2.toDouble(), x.toDouble(), y.toDouble()
	  ).also(op)
	)
  }

  fun lineTo(x: Number = 0.0, y: Number = 0.0) = apply {
	elements.add(LineTo(x.toDouble(), y.toDouble()))
  }

  fun arcTo(
	radiusX: Number = 0.0, radiusY: Number = 0.0,
	xAxisRotation: Number = 0.0, x: Number = 0.0,
	y: Number = 0.0, largeArcFlag: Boolean = false,
	sweepFlag: Boolean = false, op: ArcTo.()->Unit = {}
  ) = apply {
	elements.add(
	  ArcTo(
		radiusX.toDouble(), radiusY.toDouble(), xAxisRotation.toDouble(), x.toDouble(), y.toDouble(), largeArcFlag,
		sweepFlag
	  ).also(op)
	)
  }

  fun closepath() = apply { elements.add(ClosePath()) }


}


