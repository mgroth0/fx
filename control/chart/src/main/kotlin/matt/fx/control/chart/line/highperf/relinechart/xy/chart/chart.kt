package matt.fx.control.chart.line.highperf.relinechart.xy.chart

import com.sun.javafx.charts.ChartLayoutAnimator
import com.sun.javafx.charts.Legend
import com.sun.javafx.scene.NodeHelper
import com.sun.javafx.scene.control.skin.Utils
import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ObjectPropertyBase
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.StringProperty
import javafx.beans.property.StringPropertyBase
import javafx.collections.ObservableList
import javafx.css.CssMetaData
import javafx.css.Styleable
import javafx.css.StyleableBooleanProperty
import javafx.css.StyleableObjectProperty
import javafx.css.StyleableProperty
import javafx.css.converter.EnumConverter
import javafx.geometry.Pos.CENTER
import javafx.geometry.Side
import javafx.geometry.Side.BOTTOM
import javafx.geometry.Side.LEFT
import javafx.geometry.Side.RIGHT
import javafx.geometry.Side.TOP
import javafx.scene.Node
import javafx.scene.chart.Chart
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import matt.fx.base.rewrite.ReWrittenFxClass
import matt.fx.control.chart.line.highperf.relinechart.xy.chart.ChartForPrivateProps.StyleableProperties.classCssMetaData
import matt.fx.control.css.BooleanCssMetaData
import matt.lang.anno.Open
import java.util.Collections

/**
 * Base class for all charts. It has 3 parts the title, legend and chartContent. The chart content is populated by the
 * specific subclass of Chart.
 *
 * @since JavaFX 2.0
 */
@ReWrittenFxClass(Chart::class)
abstract class ChartForPrivateProps : Region() {
    /** Title Label  */
    private val titleLabel = Label()

    /**
     * This is the Pane that Chart subclasses use to contain the chart content,
     * It is sized to be inside the chart area leaving space for the title and legend.
     */
    val chartContent: Pane =
        object : Pane() {
            override fun layoutChildren() {
                val top = snappedTopInset()
                val left = snappedLeftInset()
                val bottom = snappedBottomInset()
                val right = snappedRightInset()
                val width = width
                val height = height
                val contentWidth = snapSizeX(width - (left + right))
                val contentHeight = snapSizeY(height - (top + bottom))
                layoutChartChildren(snapPositionY(top), snapPositionX(left), contentWidth, contentHeight)
            }

            override fun usesMirroring(): Boolean = useChartContentMirroring
        }

    /* Determines if chart content should be mirrored if node orientation is right-to-left. */
    var useChartContentMirroring = true

    /** Animator for animating stuff on the chart  */
    private val animator = ChartLayoutAnimator(chartContent)
    /** The chart title  */
    private val title: StringProperty =
        object : StringPropertyBase() {
            override fun invalidated() {
                titleLabel.text = get()
            }

            override fun getBean(): Any = this@ChartForPrivateProps

            override fun getName(): String = "title"
        }

    fun getTitle(): String? = title.get()

    fun setTitle(value: String) {
        title.set(value)
    }

    fun titleProperty(): StringProperty = title

    /**
     * The side of the chart where the title is displayed
     * @defaultValue Side.TOP
     */
    private val titleSide: StyleableObjectProperty<Side> =
        object : StyleableObjectProperty<Side>(TOP) {
            override fun invalidated() {
                requestLayout()
            }

            override fun getCssMetaData(): CssMetaData<out Styleable, Side> = StyleableProperties.TITLE_SIDE

            override fun getBean(): Any = this@ChartForPrivateProps

            override fun getName(): String = "titleSide"
        }

    fun getTitleSide(): Side = titleSide.get()

    fun setTitleSide(value: Side) {
        titleSide.set(value)
    }

    fun titleSideProperty(): StyleableObjectProperty<Side> = titleSide

    /**
     * The node to display as the Legend. Subclasses can set a node here to be displayed on a side as the legend. If
     * no legend is wanted then this can be set to null
     */
    private val legend: ObjectProperty<Node> =
        object : ObjectPropertyBase<Node>() {
            private var old: Node? = null
            override fun invalidated() {
                val newLegend = get()
                if (old != null) children.remove(old)
                if (newLegend != null) {
                    children.add(newLegend)
                    newLegend.isVisible = isLegendVisible()
                }
                old = newLegend
            }

            override fun getBean(): Any = this@ChartForPrivateProps

            override fun getName(): String = "legend"
        }

    protected fun getLegend(): Node? = legend.value

    protected fun setLegend(value: Node?) {
        legend.value = value
    }

    protected fun legendProperty(): ObjectProperty<Node> = legend

    /**
     * When true the chart will display a legend if the chart implementation supports a legend.
     */
    private val legendVisible: StyleableBooleanProperty =
        object : StyleableBooleanProperty(true) {
            override fun invalidated() {
                requestLayout()
            }

            override fun getCssMetaData(): CssMetaData<ChartForPrivateProps, Boolean> = StyleableProperties.LEGEND_VISIBLE

            override fun getBean(): Any = this@ChartForPrivateProps

            override fun getName(): String = "legendVisible"
        }

    fun isLegendVisible(): Boolean = legendVisible.value

    fun setLegendVisible(value: Boolean) {
        legendVisible.value = value
    }

    fun legendVisibleProperty(): StyleableBooleanProperty = legendVisible

    /**
     * The side of the chart where the legend should be displayed
     *
     * @defaultValue Side.BOTTOM
     */
    internal val legendSide: StyleableObjectProperty<Side> =
        object : StyleableObjectProperty<Side>(BOTTOM) {
            override fun invalidated() {
                val legendSide = get()
                val legend = getLegend()
                if (legend is Legend) legend.isVertical = LEFT == legendSide || RIGHT == legendSide
                requestLayout()
            }

            override fun getCssMetaData(): CssMetaData<out Styleable, Side> = StyleableProperties.LEGEND_SIDE

            override fun getBean(): Any = this@ChartForPrivateProps

            override fun getName(): String = "legendSide"
        }

    fun getLegendSide(): Side = legendSide.get()

    fun setLegendSide(value: Side) {
        legendSide.set(value)
    }

    fun legendSideProperty(): StyleableObjectProperty<Side> = legendSide

    /** When true any data changes will be animated.  */
    internal val animated: BooleanProperty = SimpleBooleanProperty(this, "animated", true)

    /**
     * Indicates whether data changes will be animated or not.
     *
     * @return true if data changes will be animated and false otherwise.
     */
    fun getAnimated(): Boolean = animated.get()

    fun setAnimated(value: Boolean) {
        animated.set(value)
    }

    fun animatedProperty(): BooleanProperty = animated

    protected val chartChildren: ObservableList<Node>
        /**
         * Modifiable and observable list of all content in the chart. This is where implementations of Chart should add
         * any nodes they use to draw their chart. This excludes the legend and title which are looked after by this class.
         *
         * @return Observable list of plot children
         */
        get() = chartContent.children
    /**
     * Creates a new default Chart instance.
     */
    init {
        titleLabel.alignment = CENTER
        titleLabel.focusTraversableProperty().bind(Platform.accessibilityActiveProperty())
        children.addAll(titleLabel, chartContent)
        styleClass.add("chart")
        titleLabel.styleClass.add("chart-title")
        chartContent.styleClass.add("chart-content")
        /* mark chartContent as unmanaged because any changes to its preferred size shouldn't cause a relayout */
        chartContent.isManaged = false
    }
    /**
     * Play a animation involving the given keyframes. On every frame of the animation the chart will be relayed out
     *
     * @param keyFrames Array of KeyFrames to play
     */
    fun animate(vararg keyFrames: KeyFrame?) {
        animator.animate(*keyFrames)
    }

    /**
     * Play the given animation on every frame of the animation the chart will be relayed out until the animation
     * finishes. So to add a animation to a chart, create a animation on data model, during layoutChartContent() map
     * data model to nodes then call this method with the animation.
     *
     * @param animation The animation to play
     */
    protected fun animate(animation: Animation?) {
        animator.animate(animation)
    }

    /** Call this when you know something has changed that needs the chart to be relayed out.  */
    protected fun requestChartLayout() {
        chartContent.requestLayout()
    }

    /**
     * This is used to check if any given animation should run. It returns true if animation is enabled and the node
     * is visible and in a scene.
     * @return true if animation is enabled and the node is visible and in a scene
     */
    protected fun shouldAnimate(): Boolean = getAnimated() && NodeHelper.isTreeShowing(this)

    /**
     * Called to update and layout the chart children available from getChartChildren()
     *
     * @param top The top offset from the origin to account for any padding on the chart content
     * @param left The left offset from the origin to account for any padding on the chart content
     * @param width The width of the area to layout the chart within
     * @param height The height of the area to layout the chart within
     */
    protected abstract fun layoutChartChildren(
        top: Double,
        left: Double,
        width: Double,
        height: Double
    )

    /**
     * Invoked during the layout pass to layout this chart and all its content.
     */
    final override fun layoutChildren() {
        var top = snappedTopInset()
        var left = snappedLeftInset()
        var bottom = snappedBottomInset()
        var right = snappedRightInset()
        val width = width
        val height = height
        /* layout title */
        if (getTitle() != null) {
            titleLabel.isVisible = true
            if (getTitleSide() == TOP) {
                val titleHeight = snapSizeY(titleLabel.prefHeight(width - left - right))
                titleLabel.resizeRelocate(left, top, width - left - right, titleHeight)
                top += titleHeight
            } else if (getTitleSide() == BOTTOM) {
                val titleHeight = snapSizeY(titleLabel.prefHeight(width - left - right))
                titleLabel.resizeRelocate(left, height - bottom - titleHeight, width - left - right, titleHeight)
                bottom += titleHeight
            } else if (getTitleSide() == LEFT) {
                val titleWidth = snapSizeX(titleLabel.prefWidth(height - top - bottom))
                titleLabel.resizeRelocate(left, top, titleWidth, height - top - bottom)
                left += titleWidth
            } else if (getTitleSide() == RIGHT) {
                val titleWidth = snapSizeX(titleLabel.prefWidth(height - top - bottom))
                titleLabel.resizeRelocate(width - right - titleWidth, top, titleWidth, height - top - bottom)
                right += titleWidth
            }
        } else {
            titleLabel.isVisible = false
        }
        /* layout legend */
        val legend = getLegend()
        /*@Suppress("SENSELESS_COMPARISON")*/
        if (legend != null) {
            var shouldShowLegend = isLegendVisible()
            if (shouldShowLegend) {
                if (getLegendSide() == TOP) {
                    val legendHeight = snapSizeY(legend.prefHeight(width - left - right))
                    val legendWidth =
                        Utils.boundedSize(snapSizeX(legend.prefWidth(legendHeight)), 0.0, width - left - right)
                    legend.resizeRelocate(
                        left + (width - left - right - legendWidth) / 2,
                        top,
                        legendWidth,
                        legendHeight
                    )
                    if (height - bottom - top - legendHeight < MIN_HEIGHT_TO_LEAVE_FOR_CHART_CONTENT) {
                        shouldShowLegend = false
                    } else {
                        top += legendHeight
                    }
                } else if (getLegendSide() == BOTTOM) {
                    val legendHeight = snapSizeY(legend.prefHeight(width - left - right))
                    val legendWidth =
                        Utils.boundedSize(snapSizeX(legend.prefWidth(legendHeight)), 0.0, width - left - right)
                    legend.resizeRelocate(
                        left + (width - left - right - legendWidth) / 2,
                        height - bottom - legendHeight,
                        legendWidth,
                        legendHeight
                    )
                    if (height - bottom - top - legendHeight < MIN_HEIGHT_TO_LEAVE_FOR_CHART_CONTENT) {
                        shouldShowLegend = false
                    } else {
                        bottom += legendHeight
                    }
                } else if (getLegendSide() == LEFT) {
                    val legendWidth = snapSizeX(legend.prefWidth(height - top - bottom))
                    val legendHeight =
                        Utils.boundedSize(snapSizeY(legend.prefHeight(legendWidth)), 0.0, height - top - bottom)
                    legend.resizeRelocate(
                        left,
                        top + (height - top - bottom - legendHeight) / 2,
                        legendWidth,
                        legendHeight
                    )
                    if (width - left - right - legendWidth < MIN_WIDTH_TO_LEAVE_FOR_CHART_CONTENT) {
                        shouldShowLegend = false
                    } else {
                        left += legendWidth
                    }
                } else if (getLegendSide() == RIGHT) {
                    val legendWidth = snapSizeX(legend.prefWidth(height - top - bottom))
                    val legendHeight =
                        Utils.boundedSize(snapSizeY(legend.prefHeight(legendWidth)), 0.0, height - top - bottom)
                    legend.resizeRelocate(
                        width - right - legendWidth,
                        top + (height - top - bottom - legendHeight) / 2,
                        legendWidth,
                        legendHeight
                    )
                    if (width - left - right - legendWidth < MIN_WIDTH_TO_LEAVE_FOR_CHART_CONTENT) {
                        shouldShowLegend = false
                    } else {
                        right += legendWidth
                    }
                }
            }
            legend.isVisible = shouldShowLegend
        }
        /* whats left is for the chart content */
        chartContent.resizeRelocate(left, top, width - left - right, height - top - bottom)
    }

    /**
     * Charts are sized outside in, user tells chart how much space it has and chart draws inside that. So minimum
     * height is a constant 150.
     */
    final override fun computeMinHeight(width: Double): Double = 150.0

    /**
     * Charts are sized outside in, user tells chart how much space it has and chart draws inside that. So minimum
     * width is a constant 200.
     */
    final override fun computeMinWidth(height: Double): Double = 200.0

    /**
     * Charts are sized outside in, user tells chart how much space it has and chart draws inside that. So preferred
     * width is a constant 500.
     */
    final override fun computePrefWidth(height: Double): Double = 500.0

    /**
     * Charts are sized outside in, user tells chart how much space it has and chart draws inside that. So preferred
     * height is a constant 400.
     */
    final override fun computePrefHeight(width: Double): Double = 400.0

    private object StyleableProperties {
        val TITLE_SIDE: CssMetaData<ChartForPrivateProps, Side> =
            object : CssMetaData<ChartForPrivateProps, Side>(
                "-fx-title-side",
                EnumConverter(Side::class.java),
                TOP
            ) {
                override fun isSettable(node: ChartForPrivateProps): Boolean = node.titleSide.value == null || !node.titleSide.isBound

                override fun getStyleableProperty(node: ChartForPrivateProps): StyleableProperty<Side> = node.titleSideProperty()
            }
        val LEGEND_SIDE: CssMetaData<ChartForPrivateProps, Side> =
            object : CssMetaData<ChartForPrivateProps, Side>(
                "-fx-legend-side",
                EnumConverter(Side::class.java),
                BOTTOM
            ) {
                override fun isSettable(node: ChartForPrivateProps): Boolean = node.legendSide.value == null || !node.legendSide.isBound

                override fun getStyleableProperty(node: ChartForPrivateProps): StyleableProperty<Side> = node.legendSideProperty()
            }
        val LEGEND_VISIBLE: BooleanCssMetaData<ChartForPrivateProps> =
            object : BooleanCssMetaData<ChartForPrivateProps>(
                "-fx-legend-visible",
                true
            ) {
                override fun isSettable(
                    node: ChartForPrivateProps
                ): Boolean = node.legendVisible.value == null || !node.legendVisible.isBound

                override fun getStyleableProperty(node: ChartForPrivateProps): StyleableProperty<Boolean?> = node.legendVisibleProperty()
            }
        val classCssMetaData: List<CssMetaData<out Styleable?, *>>? by lazy {
            val styleables: MutableList<CssMetaData<out Styleable?, *>> = ArrayList(getClassCssMetaData())
            styleables.add(TITLE_SIDE)
            styleables.add(LEGEND_VISIBLE)
            styleables.add(LEGEND_SIDE)
            Collections.unmodifiableList(styleables)
        }
    }

    @Open
    /**
     * {@inheritDoc}
     * @since JavaFX 8.0
     */
    override fun getCssMetaData(): List<CssMetaData<out Styleable?, *>>? = classCssMetaData

    companion object {
        private const val MIN_WIDTH_TO_LEAVE_FOR_CHART_CONTENT = 200
        private const val MIN_HEIGHT_TO_LEAVE_FOR_CHART_CONTENT = 150
    }
}
