@file:OptIn(ExperimentalStdlibApi::class)

package matt.fx.node.proto.scaledcanvas

import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import matt.async.thread.schedule.SchedulingDaemon
import matt.fx.control.wrapper.progressindicator.PerformantProgressIndicator
import matt.fx.graphics.fxthread.runLater
import matt.fx.graphics.style.intColorToFXColor
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.fx.graphics.wrapper.canvas.Canv
import matt.fx.graphics.wrapper.canvas.CanvasWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.lang.common.NEVER
import matt.lang.common.go
import matt.obs.math.double.op.div
import matt.obs.math.double.op.minus
import matt.obs.math.double.op.times
import matt.obs.prop.writable.BindableProperty
import matt.time.UnixTime
import java.awt.image.BufferedImage
import java.lang.ref.WeakReference
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun Array<Array<Color?>>.toCanvas() =
    ScaledCanvas(
        width = this[0].size.toDouble(), height = size.toDouble(), initialScale = 1.0
    ).apply {
        this@toCanvas.forEachIndexed { x, row ->
            row.forEachIndexed { y, c ->
                this[y, x]/*dont understand why this is reversed*/ = c!!
            }
        }
    }

fun EventTargetWrapper.scaledCanvas(
    width: Number,
    height: Number,
    scale: Number = 1.0,
    op: ScaledCanvas.() -> Unit = {}
) = attach(
    ScaledCanvas(
        width = width, height = height, initialScale = scale.toDouble()
    ),
    op
)

fun EventTargetWrapper.scaledCanvas(
    hw: Number,
    scale: Number = 1.0,
    op: ScaledCanvas.() -> Unit = {}
) = scaledCanvas(height = hw, width = hw, scale = scale, op = op)


@OptIn(ExperimentalStdlibApi::class)
fun BufferedImage.toScaledCanvas(): ScaledCanvas {
    val canv = ScaledCanvas(width = width, height = height)
    (0..<width).forEach { x ->
        (0..<height).forEach { y ->
            canv[x, y] = intColorToFXColor(getRGB(x, y))
        }
    }
    return canv
}


open class ScaledCanvas(
    private val canvas: CanvasWrapper = CanvasWrapper(),
    initialScale: Double = 1.0,
    initializeInLoadingMode: Boolean = false,
    progressIndicatorWidthAndHeight: Double? = null,
    delayLoadingIndicatorBy: Duration? = null
) : RegionWrapperImpl<Pane, CanvasWrapper>(Pane()), Canv by canvas {

    companion object {
        private val worker = SchedulingDaemon(500.milliseconds, "ScaledCanvas Worker")
    }

    constructor(
        height: Number,
        width: Number,
        initialScale: Double = 1.0
    ) : this(CanvasWrapper(width = width.toDouble(), height = height.toDouble()), initialScale)

    constructor(
        hw: Number,
        scale: Double
    ) : this(height = hw.toDouble(), width = hw.toDouble(), initialScale = scale)

    val scale = BindableProperty(initialScale)


    private val loadingIndicator =
        lazy {

            /*val prog = ProgressIndicatorWrapper()*/
            val prog = PerformantProgressIndicator()

            prog.apply {

                if (delayLoadingIndicatorBy != null) {
                    isVisible = false
                    val weakThis = WeakReference(this)
                    worker.schedule(UnixTime() + delayLoadingIndicatorBy) {
                        weakThis.get()?.go {
                            runLater {
                                it.isVisible = true
                            }
                        }
                    }
                }

                progressIndicatorWidthAndHeight?.go {
                    exactWidth = it
                    exactHeight = it
                }
            }.node
        }

    private val paneChildren = this@ScaledCanvas.node.children

    fun showAsLoading() {
        paneChildren.remove(canvas.node)
        paneChildren.add(loadingIndicator.value)
    }

    fun showCanvas() {
        if (loadingIndicator.isInitialized()) paneChildren.remove(loadingIndicator.value)
        paneChildren.add(canvas.node)
        exactHeightProperty.bindWeakly(actualHeight)
        exactWidthProperty.bindWeakly(actualWidth)
    }

    init {
        canvas.apply {
            layoutXProperty.bindWeakly((widthProperty * this@ScaledCanvas.scale - widthProperty) / 2.0)
            layoutYProperty.bindWeakly((heightProperty * this@ScaledCanvas.scale - heightProperty) / 2.0)
            scaleXProperty.bindWeakly(this@ScaledCanvas.scale)
            scaleYProperty.bindWeakly(this@ScaledCanvas.scale)
        }
        if (initializeInLoadingMode) showAsLoading()
        else showCanvas()
    }

    final override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) = NEVER

    final override val height: Double get() = actualHeight.value
    final override val width: Double get() = actualWidth.value
}


