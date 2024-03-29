package matt.fx.graphics.wrapper.node.shape.rect

import javafx.scene.Parent
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper
import matt.fx.graphics.wrapper.sizeman.SizeControlled
import matt.lang.anno.Open
import matt.lang.delegation.lazyVarDelegate

fun NodeWrapperImpl<Parent>.rectangle(
    x: Number = 0.0,
    y: Number = 0.0,
    width: Number = 0.0,
    height: Number = 0.0,
    op: RectangleWrapper.() -> Unit = {}
) =
    RectangleWrapper(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble()).attachTo(this, op)


open class RectangleWrapper(
    node: Rectangle = Rectangle()
): ShapeWrapper<Rectangle>(node), SizeControlled {
    constructor(x: Double, y: Double, width: Double, height: Double): this(Rectangle(x, y, width, height))
    constructor(width: Double, height: Double, fill: Paint): this(Rectangle(width, height, fill))
    constructor(width: Double, height: Double): this(Rectangle(width, height))

    final override val widthProperty by lazy {
        node.widthProperty().toNonNullableProp().cast<Double>(Double::class)
    }

    final override val heightProperty by lazy { node.heightProperty().toNonNullableProp().cast<Double>(Double::class) }


    val xProperty by lazy {
        node.xProperty().toNonNullableProp().cast<Double>(Double::class)
    }
    var x: Double by lazyVarDelegate { xProperty }
    val yProperty by lazy {
        node.yProperty().toNonNullableProp().cast<Double>(Double::class)
    }
    var y: Double by lazyVarDelegate { yProperty }


    @Open
    override var height
        get() = node.height
        set(value) {
            node.height = value
        }

    final override var width
        get() = node.width
        set(value) {
            node.width = value
        }
}
