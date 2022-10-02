package matt.fx.graphics.wrapper.node.shape.svg

import javafx.scene.shape.FillRule
import javafx.scene.shape.SVGPath
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper

open class SVGPathWrapper(
  node: SVGPath = SVGPath(),
): ShapeWrapper<SVGPath>(node) {
  companion object {
	fun SVGPath.wrapped() = SVGPathWrapper(this)
  }


  var content: String
	get() = node.content
	set(value) {
	  node.content = value
	}
  var fillRule: FillRule
	get() = node.fillRule
	set(value) {
	  node.fillRule = value
	}


}