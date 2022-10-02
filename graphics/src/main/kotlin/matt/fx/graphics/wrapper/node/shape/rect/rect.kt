package matt.fx.graphics.wrapper.node.shape.rect

import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper
import matt.fx.graphics.wrapper.sizeman.SizeControlled

open class RectangleWrapper(
  node: Rectangle = Rectangle(),
): ShapeWrapper<Rectangle>(node), SizeControlled {
  constructor(x: Double, y: Double, width: Double, height: Double): this(Rectangle(x, y, width, height))
  constructor(width: Double, height: Double, fill: Paint): this(Rectangle(width, height, fill))
  constructor(width: Double, height: Double): this(Rectangle(width, height))

  //  var width
  //	get() = node.width
  //	set(value) {
  //	  node.width = value
  //	}
  override val widthProperty get() = node.widthProperty().toNonNullableProp().cast<Double>()

  //  var height
  //	get() = node.height
  //	set(value) {
  //	  node.height = value
  //	}
  override val heightProperty get() = node.heightProperty().toNonNullableProp().cast<Double>()


  override var height
	get() = node.height
	set(value) {
	  node.height = value
	}

  override var width
	get() = node.width
	set(value) {
	  node.width = value
	}


}