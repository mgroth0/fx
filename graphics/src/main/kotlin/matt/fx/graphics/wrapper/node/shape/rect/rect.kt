package matt.fx.graphics.wrapper.node.shape.rect

import javafx.scene.Parent
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import matt.fx.graphics.wrapper.node.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper
import matt.fx.graphics.wrapper.sizeman.SizeControlled
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp

fun NodeWrapperImpl<Parent>.rectangle(
  x: Number = 0.0,
  y: Number = 0.0,
  width: Number = 0.0,
  height: Number = 0.0,
  op: RectangleWrapper.()->Unit = {}
) =
  RectangleWrapper(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble()).attachTo(this, op)


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
  override val widthProperty by lazy {
	node.widthProperty().toNonNullableProp().cast<Double>()
  }

  //  var height
  //	get() = node.height
  //	set(value) {
  //	  node.height = value
  //	}
  override val heightProperty by lazy { node.heightProperty().toNonNullableProp().cast<Double>() }


  val xProperty by lazy {
	node.xProperty().toNonNullableProp().cast<Double>()
  }
  var x by xProperty
  val yProperty by lazy {
	node.yProperty().toNonNullableProp().cast<Double>()
  }
  var y by yProperty


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