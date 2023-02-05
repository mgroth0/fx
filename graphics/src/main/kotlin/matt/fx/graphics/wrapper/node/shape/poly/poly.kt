package matt.fx.graphics.wrapper.node.shape.poly

import javafx.scene.shape.Polygon
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper
import matt.fx.base.wrapper.obs.collect.list.createMutableWrapper

open class PolygonWrapper(
  node: Polygon = Polygon(),
): ShapeWrapper<Polygon>(node) {
  constructor(
	vararg points: Double
  ): this(Polygon(*points))

  val points by lazy { node.points.createMutableWrapper() }

}