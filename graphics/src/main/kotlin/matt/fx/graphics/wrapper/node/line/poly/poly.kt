package matt.fx.graphics.wrapper.node.line.poly

import javafx.scene.shape.Polyline
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper

open class PolylineWrapper(
   node: Polyline = Polyline(),
): ShapeWrapper<Polyline>(node) {
  companion object {
	fun Polyline.wrapped() = PolylineWrapper(this)
  }

  constructor(
	vararg points: Double
  ): this(Polyline(*points))


}