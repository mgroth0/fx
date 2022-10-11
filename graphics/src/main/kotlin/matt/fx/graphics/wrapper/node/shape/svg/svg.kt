package matt.fx.graphics.wrapper.node.shape.svg

import javafx.scene.Parent
import javafx.scene.shape.FillRule
import javafx.scene.shape.SVGPath
import matt.fx.graphics.wrapper.node.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper
fun NodeWrapperImpl<Parent>.svgpath(
  content: String? = null,
  fillRule: FillRule? = null,
  op: SVGPathWrapper.()->Unit = {}
) = SVGPathWrapper().attachTo(this, op) {
  if (content != null) it.content = content
  if (fillRule != null) it.fillRule = fillRule
}

open class SVGPathWrapper(
  node: SVGPath = SVGPath(),
): ShapeWrapper<SVGPath>(node) {


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