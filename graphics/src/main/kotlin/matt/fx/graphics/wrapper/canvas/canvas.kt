package matt.fx.graphics.wrapper.canvas

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.PixelWriter
import javafx.scene.paint.Color
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.fx.graphics.wrapper.sizeman.HasHeight
import matt.fx.graphics.wrapper.sizeman.HasWidth
import matt.lang.anno.Open
import matt.lang.common.NOT_IMPLEMENTED
import matt.lang.delegation.lazyVarDelegate
import matt.model.data.rect.DoubleRectSize
import matt.obs.math.double.ObsD
import matt.obs.math.double.op.times

fun ET.canvas(
    width: Double = 0.0,
    height: Double = 0.0,
    op: CanvasWrapper.() -> Unit = {}
) =
    attach(CanvasWrapper(width, height), op)

interface Canv {

    /*does not consider scale*/
    var pixelWidth: Double
    var pixelHeight: Double

    /*considers scale*/
    val actualWidth: ObsD
    val actualHeight: ObsD

    val graphicsContext: GraphicsContext
    val pixelWriter: PixelWriter
    @Open
    operator fun set(
        x: Int,
        y: Int,
        c: Color
    ) = pixelWriter.setColor(x, y, c)

    @Open fun drawBorder() {
        graphicsContext.apply {
            stroke = Color.YELLOW /*BLUE*/
            fill = Color.YELLOW /*BLUE*/
            rect(0.0, 0.0, pixelWidth, 2.0)
            fill()
            rect(pixelWidth - 2.0, 0.0, 2.0, pixelHeight)
            fill()
            rect(0.0, pixelHeight - 2.0, pixelWidth, 2.0)
            fill()
            rect(0.0, 0.0, 2.0, pixelHeight)
            fill()
        }
    }
}

open class CanvasWrapper(node: Canvas = Canvas()) : NodeWrapperImpl<Canvas>(node), Canv, HasWidth, HasHeight {
    constructor(
        width: Double,
        height: Double
    ) : this(Canvas(width, height))

    constructor(
        size: DoubleRectSize
    ) : this(width = size.width, height = size.height)


    final override val widthProperty = node.widthProperty().toNonNullableProp().cast(Double::class)
    final override var pixelWidth by lazyVarDelegate { widthProperty }
    final override val heightProperty = node.heightProperty().toNonNullableProp().cast(Double::class)
    final override var pixelHeight by lazyVarDelegate { heightProperty }

    final override val actualWidth by lazy { widthProperty * scaleXProperty }
    final override val actualHeight by lazy { heightProperty * scaleYProperty }

    final override val graphicsContext: GraphicsContext get() = node.graphicsContext2D
    final override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) = NOT_IMPLEMENTED


    final override val pixelWriter: PixelWriter by lazy { graphicsContext.pixelWriter }
}

