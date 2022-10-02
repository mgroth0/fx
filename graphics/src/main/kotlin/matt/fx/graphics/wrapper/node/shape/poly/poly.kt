package matt.fx.graphics.wrapper.node.shape.poly

import javafx.scene.shape.Polygon
import matt.hurricanefx.eye.wrapper.obs.collect.createMutableWrapper
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper

open class PolygonWrapper(
  node: Polygon = Polygon(),
): ShapeWrapper<Polygon>(node) {
  companion object {
	fun Polygon.wrapped() = PolygonWrapper(this)
  }

  constructor(
	vararg points: Double
  ): this(Polygon(*points))

  val points by lazy { node.points.createMutableWrapper() }

}