package matt.fx.graphics.studio

import javafx.scene.SnapshotParameters
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import javafx.scene.transform.Transform
import matt.fx.graphics.fxthread.ensureInFXThreadInPlace
import matt.fx.graphics.style.reloadStyle
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.pane.anchor.AnchorPaneWrapperImpl
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.model.data.rect.IntRectSize
import matt.time.dur.sleep
import kotlin.time.Duration.Companion.milliseconds

private val DEFAULT_SNAP_SIZE = IntRectSize(
    width = 1000,
    height = 500
)
private const val DEFAULT_RESOLUTION_SCALE = 2

abstract class Studio(
    protected val snapshotSize: IntRectSize = DEFAULT_SNAP_SIZE,
    private val resolutionScale: Int = DEFAULT_RESOLUTION_SCALE
) {

    protected val snapshotWidth get() = snapshotSize.width
    protected val snapshotHeight get() = snapshotSize.height

    init {
        require(resolutionScale >= 1)
    }

    abstract fun prepareRegion(region: RegionWrapperImpl<*, *>)

    @Synchronized
    fun shoot(
        region: RegionWrapperImpl<*, *>
    ): Image {
        prepareRegion(region)
        sleep(100.milliseconds)

        val img = ensureInFXThreadInPlace {
            region.snapshot(snapshotParams, imageBuffer)
        }

        return img

    }


    private val snapshotParams = SnapshotParameters().apply {
        fill = Color.BLACK
        if (resolutionScale > 1) {
            transform = Transform.scale(resolutionScale.toDouble(), resolutionScale.toDouble())
        }
    }


    /*private var imageBuffer = WritableImage(max(fftChartM.width.toInt(), 1), max(fftChartM.height.toInt(), 1))*/
    private var imageBuffer = WritableImage(snapshotWidth * resolutionScale, snapshotHeight * resolutionScale)
    protected val snapshotSceneRoot = AnchorPaneWrapperImpl<NodeWrapper>().apply {
        exactWidth = snapshotWidth
        exactHeight = snapshotHeight
    }
    protected val snapshotScene = SceneWrapper<ParentWrapper<*>>(
        snapshotSceneRoot, userWidth = snapshotWidth.toDouble(), userHeight = snapshotHeight.toDouble()
    ).apply {
        reloadStyle(darkMode = true)
    }


}


class DefaultStudio(
    snapshotSize: IntRectSize = DEFAULT_SNAP_SIZE,
    resolutionScale: Int = DEFAULT_RESOLUTION_SCALE
) : Studio(snapshotSize = snapshotSize, resolutionScale = resolutionScale) {
    override fun prepareRegion(region: RegionWrapperImpl<*, *>) {
        snapshotSceneRoot.clear()
        snapshotSceneRoot.allSides = region
        region.exactWidth = snapshotWidth
        region.exactHeight = snapshotHeight
    }
}
