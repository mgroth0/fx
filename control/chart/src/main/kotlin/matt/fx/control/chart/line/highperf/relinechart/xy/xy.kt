package matt.fx.control.chart.line.highperf.relinechart.xy

import com.sun.javafx.charts.Legend
import com.sun.javafx.charts.Legend.LegendItem
import com.sun.javafx.collections.NonIterableChange
import com.sun.javafx.scene.control.skin.resources.ControlResources
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.beans.binding.StringBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ObjectPropertyBase
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectPropertyBase
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.StringProperty
import javafx.beans.property.StringPropertyBase
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ListChangeListener.Change
import javafx.collections.ObservableList
import javafx.css.CssMetaData
import javafx.css.Styleable
import javafx.css.StyleableBooleanProperty
import javafx.css.StyleableProperty
import javafx.geometry.Orientation.HORIZONTAL
import javafx.geometry.Orientation.VERTICAL
import javafx.geometry.Side.BOTTOM
import javafx.geometry.Side.LEFT
import javafx.geometry.Side.RIGHT
import javafx.geometry.Side.TOP
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.chart.XYChart
import javafx.scene.layout.Region
import javafx.scene.shape.ClosePath
import javafx.scene.shape.Line
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import matt.collect.weak.WeakMap
import matt.fig.modell.series.SeriesIdea
import matt.fx.base.rewrite.ReWrittenFxClass
import matt.fx.control.chart.axis.value.axis.AxisForPackagePrivateProps
import matt.fx.control.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps.StyleableProperties.classCssMetaData
import matt.fx.control.chart.line.highperf.relinechart.xy.chart.ChartForPrivateProps
import matt.fx.control.css.BooleanCssMetaData
import matt.fx.graphics.anim.interp.MyInterpolator
import matt.lang.anno.Open
import matt.model.data.xyz.Dim2D
import matt.obs.prop.BindableProperty
import java.text.MessageFormat
import java.util.BitSet
import java.util.Collections


/**
 * Chart base class for all 2 axis charts. It is responsible for drawing the two
 * axes and the plot content. It contains a list of all content in the plot and
 * implementations of XYChart can add nodes to this list that need to be rendered.
 *
 *
 * It is possible to install Tooltips on data items / symbols.
 * For example the following code snippet installs Tooltip on the 1st data item.
 *
 * <pre>`
 * XYChart.Data item = ( XYChart.Data)series.getData().get(0);
 * Tooltip.install(item.getNode(), new Tooltip("Symbol-0"));
`</pre> *
 *
 * @since JavaFX 2.0
 */
@ReWrittenFxClass(XYChart::class)
abstract class XYChartForPackagePrivateProps<X, Y>(

    // -------------- PUBLIC PROPERTIES --------------------------------------------------------------------------------


    internal val xAxis: AxisForPackagePrivateProps<X>,


    yAxis: AxisForPackagePrivateProps<Y>
) : ChartForPrivateProps() {


    private val xAxisProperty: ReadOnlyObjectProperty<AxisForPackagePrivateProps<X>> =
        object : ReadOnlyObjectPropertyBase<AxisForPackagePrivateProps<X>>() {
            override fun getBean(): Any = this

            override fun getName(): String = "xAxis"

            override fun get(): AxisForPackagePrivateProps<X> = xAxis
        }

//    private fun xAxisProperty(): ObservableValue<AxisForPackagePrivateProps<X>> {
//        return xAxisProperty
//    }


    // -------------- PRIVATE FIELDS -----------------------------------------------------------------------------------
    // to indicate which colors are being used for the series
    private val colorBits = BitSet(8)
    val seriesColorMap: MutableMap<Series<X, Y>?, Int> = HashMap()
    private var rangeValid = false
    private val verticalZeroLine = Line()
    private val horizontalZeroLine = Line()
    private val verticalGridLines = Path()
    private val horizontalGridLines = Path()
    private val horizontalRowFill = Path()
    private val verticalRowFill = Path()
    private val plotBackground = Region()
    val plotArea: Group = object : Group() {
        override fun requestLayout() {} // suppress layout requests
    }
    val plotContent = Group()
    private val plotAreaClip = Rectangle()
    private val displayedSeries: MutableList<Series<X, Y>> = ArrayList()

    private val legend = Legend()

    /** This is called when a series is added or removed from the chart  */
    private val seriesChanged = ListChangeListener { c: Change<out Series<X, Y>?> ->
        val series = c.list
        while (c.next()) {
            // RT-12069, linked list pointers should update when list is permuted.
            if (c.wasPermutated()) {
                displayedSeries.sortWith { o1: Series<X, Y>?, o2: Series<X, Y>? ->
                    series.indexOf(
                        o2
                    ) - series.indexOf(o1)
                }
            }
            if (c.removed.isNotEmpty()) updateLegend()
            val dupCheck = (HashSet(displayedSeries))
            dupCheck.removeAll(c.removed.toSet())


            c.addedSubList.forEach { sery ->

                if (!dupCheck.add(sery) && !sery!!.setToRemove) {
                    error(
                        """
                        Duplicate series added
                        	$sery
                        	displayedSize = ${displayedSeries.size}
                        	change.list.size = ${c.list.size}
                        	change.removed.size=${c.removed.size}
                        	change.added.size=${c.addedSubList.size}
                        	mattFixes.size={mattsBeingRemovedSet.size}
                        	index=${c.list.indexOf(sery)}
                        	setToRemove=${sery.setToRemove}
                        """.trimIndent()
                    )
                }
                /*
                      if (sery in dupCheck) {
                          mattsBeingRemovedSet.firstOrNull {
                              it.seriesBeingRemoved == sery
                          }?.go {
                              it.timeline!!.stop()
                              it.finalRemovalOp()
                          } ?:
                      }*/


            }


            for (s in c.removed) {
                s!!.setToRemove = true
                seriesRemoved(s)
            }
            var i = c.from
            while (i < c.to && !c.wasPermutated()) {
                val s = c.list[i]
//                 add new listener to data
                if (s!!.setToRemove) {
                    s.setToRemove = false
                    s.getChart()!!.seriesBeingRemovedIsAdded(s)
                }
                s.setChart(this@XYChartForPackagePrivateProps)
                // update linkedList Pointers for series
                displayedSeries.add(s)
                // update default color style class
                val nextClearBit = colorBits.nextClearBit(0)
                colorBits[nextClearBit] = true
                s.defaultColorStyleClass = DEFAULT_COLOR + nextClearBit % 8
                seriesColorMap[s] = nextClearBit % 8
                // inform sub-classes of series added
                seriesAdded(s, i)
                i++
            }
            if (c.from < c.to) updateLegend()
            seriesChanged(c)
        }
        // update axis ranges
        invalidateRange()
        // lay everything out
        requestChartLayout()
    }

    /**
     * Get the X axis, by default it is along the bottom of the plot
     * @return the X axis of the chart
     */
    fun getXAxis(): AxisForPackagePrivateProps<X>? = xAxis

    internal val yAxis: AxisForPackagePrivateProps<Y>


    private val yAxisProperty: ReadOnlyObjectProperty<AxisForPackagePrivateProps<Y>> =
        object : ReadOnlyObjectPropertyBase<AxisForPackagePrivateProps<Y>>() {
            override fun getBean(): Any = this

            override fun getName(): String = "yAxis"

            override fun get(): AxisForPackagePrivateProps<Y> = yAxis
        }
//
//    private fun yAxisProperty(): ObservableValue<AxisForPackagePrivateProps<Y>> {
//        return yAxisProperty
//    }


    /**
     * Get the Y axis, by default it is along the left of the plot
     * @return the Y axis of this chart
     */
    fun getYAxis(): AxisForPackagePrivateProps<Y>? = yAxis

    /** XYCharts data  */
    internal val data: ObjectProperty<ObservableList<Series<X, Y>>> =
        object : ObjectPropertyBase<ObservableList<Series<X, Y>>>() {
            private var old: ObservableList<Series<X, Y>>? = null
            override fun invalidated() {
                val current: ObservableList<Series<X, Y>>? = value
                if (current === old) return
                var saveAnimationState = -1
                // add remove listeners
                if (old != null) {
                    old!!.removeListener(seriesChanged)
                    // Set animated to false so we don't animate both remove and add
                    // at the same time. RT-14163
                    // RT-21295 - disable animated only when current is also not null.
                    if (current != null && old!!.size > 0) {
                        saveAnimationState = if (old!![0].getChart()!!.animatedProperty().value) 1 else 2
                        old!![0].getChart()!!.setAnimated(false)
                    }
                }
                current?.addListener(seriesChanged)
                // fire series change event if series are added or removed
                if (old != null || current != null) {
                    val removed = if (old != null) old!! else emptyList()
                    val toIndex = current?.size ?: 0
                    // let series listener know all old series have been removed and new that have been added
                    if (toIndex > 0 || !removed.isEmpty()) {
                        seriesChanged.onChanged(object : NonIterableChange<Series<X, Y>>(0, toIndex, current!!) {
                            override fun getRemoved(): List<Series<X, Y>> = removed

                            override fun getPermutation(): IntArray = IntArray(0)
                        })
                    }
                } else if (old?.let { it.size > 0 } ?: false) {
                    // let series listener know all old series have been removed
                    seriesChanged.onChanged(object : NonIterableChange<Series<X, Y>?>(0, 0, current) {
                        override fun getRemoved(): List<Series<X, Y>> = old!!

                        override fun getPermutation(): IntArray = IntArray(0)
                    })
                }
                // restore animated on chart.
                if (current != null && current.size > 0 && saveAnimationState != -1) {
                    current[0].getChart()!!.setAnimated(saveAnimationState == 1)
                }
                old = current
            }

            override fun getBean(): Any = this@XYChartForPackagePrivateProps

            override fun getName(): String = "data"
        }

    fun getData(): ObservableList<Series<X, Y>> = data.value

    fun setData(value: ObservableList<Series<X, Y>>) {
        data.value = value
    }

    fun dataProperty(): ObjectProperty<ObservableList<Series<X, Y>>> = data

    /** True if vertical grid lines should be drawn  */
    private val verticalGridLinesVisible: BooleanProperty = object : StyleableBooleanProperty(true) {
        override fun invalidated() {
            requestChartLayout()
        }

        override fun getBean(): Any = this@XYChartForPackagePrivateProps

        override fun getName(): String = "verticalGridLinesVisible"

        override fun getCssMetaData(): CssMetaData<XYChartForPackagePrivateProps<*, *>, Boolean> = StyleableProperties.VERTICAL_GRID_LINE_VISIBLE
    }

    /**
     * Indicates whether vertical grid lines are visible or not.
     *
     * @return true if verticalGridLines are visible else false.
     * @see .verticalGridLinesVisibleProperty
     */
    fun getVerticalGridLinesVisible(): Boolean = verticalGridLinesVisible.get()

    fun setVerticalGridLinesVisible(value: Boolean) {
        verticalGridLinesVisible.set(value)
    }

    fun verticalGridLinesVisibleProperty(): BooleanProperty = verticalGridLinesVisible

    /** True if horizontal grid lines should be drawn  */
    private val horizontalGridLinesVisible: BooleanProperty = object : StyleableBooleanProperty(true) {
        override fun invalidated() {
            requestChartLayout()
        }

        override fun getBean(): Any = this@XYChartForPackagePrivateProps

        override fun getName(): String = "horizontalGridLinesVisible"

        override fun getCssMetaData(): CssMetaData<XYChartForPackagePrivateProps<*, *>, Boolean> = StyleableProperties.HORIZONTAL_GRID_LINE_VISIBLE
    }

    fun isHorizontalGridLinesVisible(): Boolean = horizontalGridLinesVisible.get()

    fun setHorizontalGridLinesVisible(value: Boolean) {
        horizontalGridLinesVisible.set(value)
    }

    fun horizontalGridLinesVisibleProperty(): BooleanProperty = horizontalGridLinesVisible

    /** If true then alternative vertical columns will have fills  */
    private val alternativeColumnFillVisible: BooleanProperty = object : StyleableBooleanProperty(false) {
        override fun invalidated() {
            requestChartLayout()
        }

        override fun getBean(): Any = this@XYChartForPackagePrivateProps

        override fun getName(): String = "alternativeColumnFillVisible"

        override fun getCssMetaData(): CssMetaData<XYChartForPackagePrivateProps<*, *>, Boolean> = StyleableProperties.ALTERNATIVE_COLUMN_FILL_VISIBLE
    }

    fun isAlternativeColumnFillVisible(): Boolean = alternativeColumnFillVisible.value

    fun setAlternativeColumnFillVisible(value: Boolean) {
        alternativeColumnFillVisible.value = value
    }

    fun alternativeColumnFillVisibleProperty(): BooleanProperty = alternativeColumnFillVisible

    /** If true then alternative horizontal rows will have fills  */
    private val alternativeRowFillVisible: BooleanProperty = object : StyleableBooleanProperty(true) {
        override fun invalidated() {
            requestChartLayout()
        }

        override fun getBean(): Any = this@XYChartForPackagePrivateProps

        override fun getName(): String = "alternativeRowFillVisible"

        override fun getCssMetaData(): CssMetaData<XYChartForPackagePrivateProps<*, *>, Boolean> = StyleableProperties.ALTERNATIVE_ROW_FILL_VISIBLE
    }

    fun isAlternativeRowFillVisible(): Boolean = alternativeRowFillVisible.value

    fun setAlternativeRowFillVisible(value: Boolean) {
        alternativeRowFillVisible.value = value
    }

    fun alternativeRowFillVisibleProperty(): BooleanProperty = alternativeRowFillVisible

    /**
     * If this is true and the vertical axis has both positive and negative values then a additional axis line
     * will be drawn at the zero point
     *
     * @defaultValue true
     */
    private val verticalZeroLineVisible: BooleanProperty = object : StyleableBooleanProperty(true) {
        override fun invalidated() {
            requestChartLayout()
        }

        override fun getBean(): Any = this@XYChartForPackagePrivateProps

        override fun getName(): String = "verticalZeroLineVisible"

        override fun getCssMetaData(): CssMetaData<XYChartForPackagePrivateProps<*, *>, Boolean> = StyleableProperties.VERTICAL_ZERO_LINE_VISIBLE
    }

    fun isVerticalZeroLineVisible(): Boolean = verticalZeroLineVisible.get()

    fun setVerticalZeroLineVisible(value: Boolean) {
        verticalZeroLineVisible.set(value)
    }

    fun verticalZeroLineVisibleProperty(): BooleanProperty = verticalZeroLineVisible

    /**
     * If this is true and the horizontal axis has both positive and negative values then a additional axis line
     * will be drawn at the zero point
     *
     * @defaultValue true
     */
    private val horizontalZeroLineVisible: BooleanProperty = object : StyleableBooleanProperty(true) {
        override fun invalidated() {
            requestChartLayout()
        }

        override fun getBean(): Any = this@XYChartForPackagePrivateProps

        override fun getName(): String = "horizontalZeroLineVisible"

        override fun getCssMetaData(): CssMetaData<XYChartForPackagePrivateProps<*, *>, Boolean> = StyleableProperties.HORIZONTAL_ZERO_LINE_VISIBLE
    }

    fun isHorizontalZeroLineVisible(): Boolean = horizontalZeroLineVisible.get()

    fun setHorizontalZeroLineVisible(value: Boolean) {
        horizontalZeroLineVisible.set(value)
    }

    fun horizontalZeroLineVisibleProperty(): BooleanProperty = horizontalZeroLineVisible

    // -------------- PROTECTED PROPERTIES -----------------------------------------------------------------------------
    protected val plotChildren: ObservableList<Node>
        /**
         * Modifiable and observable list of all content in the plot. This is where implementations of XYChart should add
         * any nodes they use to draw their plot.
         *
         * @return Observable list of plot children
         */
        get() = plotContent.children
    // -------------- CONSTRUCTOR --------------------------------------------------------------------------------------
    /**
     * Constructs a XYChart given the two axes. The initial content for the chart
     * plot background and plot area that includes vertical and horizontal grid
     * lines and fills, are added.
     *
     * @param xAxis X Axis for this XY chart
     * @param yAxis Y Axis for this XY chart
     */
    init {
        //	xAxis = xAxis
        if (xAxis.side.value == null) xAxis.setSide(BOTTOM)
        xAxis.setEffectiveOrientation(HORIZONTAL)
        this.yAxis = yAxis
        if (yAxis.side.value == null) yAxis.setSide(LEFT)
        yAxis.setEffectiveOrientation(VERTICAL)
        // RT-23123 autoranging leads to charts incorrect appearance.
        xAxis.autoRangingProperty()
            .addListener { _: ObservableValue<out Boolean?>?, _: Boolean?, _: Boolean? -> updateAxisRange() }
        yAxis.autoRangingProperty()
            .addListener { _: ObservableValue<out Boolean?>?, _: Boolean?, _: Boolean? -> updateAxisRange() }
        // add initial content to chart content
        chartChildren.addAll(plotBackground, plotArea, xAxis, yAxis)
        // We don't want plotArea or plotContent to autoSize or do layout
        plotArea.isAutoSizeChildren = false
        plotContent.isAutoSizeChildren = false
        // setup clipping on plot area
        plotAreaClip.isSmooth = false
        plotArea.clip = plotAreaClip
        // add children to plot area
        plotArea.children.addAll(
            verticalRowFill, horizontalRowFill,
            verticalGridLines, horizontalGridLines,
            verticalZeroLine, horizontalZeroLine,
            plotContent
        )
        // setup css style classes
        plotContent.styleClass.setAll("plot-content")
        plotBackground.styleClass.setAll("chart-plot-background")
        verticalRowFill.styleClass.setAll("chart-alternative-column-fill")
        horizontalRowFill.styleClass.setAll("chart-alternative-row-fill")
        verticalGridLines.styleClass.setAll("chart-vertical-grid-lines")
        horizontalGridLines.styleClass.setAll("chart-horizontal-grid-lines")
        verticalZeroLine.styleClass.setAll("chart-vertical-zero-line")
        horizontalZeroLine.styleClass.setAll("chart-horizontal-zero-line")
        // mark plotContent as unmanaged as its preferred size changes do not effect our layout
        plotContent.isManaged = false
        plotArea.isManaged = false
        // matt.hurricanefx.eye.wrapper.obs.collect.list.listen to animation on/off and sync to axis
        animatedProperty().addListener { _: ObservableValue<out Boolean?>?, _: Boolean?, newValue: Boolean? ->
            if (getXAxis() != null) getXAxis()!!.setAnimated(newValue!!)
            if (getYAxis() != null) getYAxis()!!.setAnimated(newValue!!)
        }
        setLegend(legend)
    }

    // -------------- METHODS ------------------------------------------------------------------------------------------
    val dataSize: Int
        /**
         * Gets the size of the data returning 0 if the data is null
         *
         * @return The number of items in data, or null if data is null
         */
        get() {
            val data = getData()
            @Suppress("UNNECESSARY_SAFE_CALL", "USELESS_ELVIS")
            return data?.size ?: 0
        }

    /** Called when a series's name has changed  */
    private fun seriesNameChanged() {
        updateLegend()
        requestChartLayout()
    }

    private fun dataItemsChanged(
        series: Series<X, Y>,
        removed: List<Data<X, Y>>,
        addedFrom: Int,
        addedTo: Int
    ) {
        for (item in removed) {
            dataItemRemoved(item, series)
        }
        for (i in addedFrom until addedTo) {
            val item = series.getData()[i]
            dataItemAdded(series, i, item)
        }
        invalidateRange()
        requestChartLayout()
    }

    val dataItemChangedAnimDur = BindableProperty(Duration.millis(700.0))
    val dataItemChangedAnimInterp = BindableProperty(MyInterpolator.EASE_BOTH)

    private fun <T> dataValueChanged(
        item: Data<X, Y>,
        newValue: T,
        currentValueProperty: ObjectProperty<T>
    ) {
        if (currentValueProperty.get() !== newValue) invalidateRange()
        dataItemChanged(item)
        if (shouldAnimate()) {
            animate(
                KeyFrame(
                    Duration.ZERO,
                    KeyValue(currentValueProperty, currentValueProperty.get(), MyInterpolator.MY_DEFAULT_INTERPOLATOR)
                ),
                KeyFrame(
                    dataItemChangedAnimDur.value,
                    KeyValue(currentValueProperty, newValue, dataItemChangedAnimInterp.value)
                )
            )
        } else {
            currentValueProperty.set(newValue)
            requestChartLayout()
        }
    }

    /**
     * This is called whenever a series is added or removed and the legend needs to be updated
     */
    @Suppress("SENSELESS_COMPARISON")
    protected fun updateLegend() {
        val legendList: MutableList<LegendItem> = ArrayList()
        if (getData() != null) {
            for (seriesIndex in getData().indices) {
                val series = getData()[seriesIndex]
                legendList.add(createLegendItemForSeries(series, seriesIndex))
            }
        }
        legend.items.setAll(legendList)
        if (legendList.size > 0) {
            if (getLegend() == null) {
                setLegend(legend)
            }
        } else {
            setLegend(null)
        }
    }

    /**
     * Called by the updateLegend for each series in the chart in order to
     * create new legend item
     * @param series the series for this legend item
     * @param seriesIndex the index of the series
     * @return new legend item for this series
     */
    open fun createLegendItemForSeries(
        series: Series<X, Y>,
        seriesIndex: Int
    ): LegendItem = LegendItem(series.getName())

    /**
     * This method is called when there is an attempt to add series that was
     * set to be removed, and the removal might not have completed.
     * @param series
     */
    open fun seriesBeingRemovedIsAdded(series: Series<X, Y>) {}

    /**
     * This method is called when there is an attempt to add a Data item that was
     * set to be removed, and the removal might not have completed.
     * @param data
     */
    open fun dataBeingRemovedIsAdded(
        item: Data<X, Y>,
        series: Series<X, Y>
    ) {
    }

    /**
     * Called when a data item has been added to a series. This is where implementations of XYChart can create/add new
     * nodes to getPlotChildren to represent this data item. They also may animate that data add with a fade in or
     * similar if animated = true.
     *
     * @param series    The series the data item was added to
     * @param itemIndex The index of the new item within the series
     * @param item      The new data item that was added
     */
    protected abstract fun dataItemAdded(
        series: Series<X, Y>,
        itemIndex: Int,
        item: Data<X, Y>
    )

    /**
     * Called when a data item has been removed from data model but it is still visible on the chart. Its still visible
     * so that you can handle animation for removing it in this method. After you are done animating the data item you
     * must call removeDataItemFromDisplay() to remove the items node from being displayed on the chart.
     *
     * @param item   The item that has been removed from the series
     * @param series The series the item was removed from
     */
    protected abstract fun dataItemRemoved(
        item: Data<X, Y>,
        series: Series<X, Y>
    )

    /**
     * Called when a data item has changed, ie its xValue, yValue or extraValue has changed.
     *
     * @param item    The data item who was changed
     */
    protected abstract fun dataItemChanged(item: Data<X, Y>)

    /**
     * A series has been added to the charts data model. This is where implementations of XYChart can create/add new
     * nodes to getPlotChildren to represent this series. Also you have to handle adding any data items that are
     * already in the series. You may simply call dataItemAdded() for each one or provide some different animation for
     * a whole series being added.
     *
     * @param series      The series that has been added
     * @param seriesIndex The index of the new series
     */
    protected abstract fun seriesAdded(
        series: Series<X, Y>,
        seriesIndex: Int
    )

    /**
     * A series has been removed from the data model but it is still visible on the chart. Its still visible
     * so that you can handle animation for removing it in this method. After you are done animating the data item you
     * must call removeSeriesFromDisplay() to remove the series from the display list.
     *
     * @param series The series that has been removed
     */
    protected abstract fun seriesRemoved(series: Series<X, Y>)

    /**
     * Called when each atomic change is made to the list of series for this chart
     * @param c a Change instance representing the changes to the series
     */
    //  protected open fun seriesChanged(c: Change<out Series<*, *>>) {}


    protected open fun updateStyleClassOf(
        s: Series<X, Y>,
        i: Int
    ) {
    }


    private val lastSeriesIndices = WeakMap<Series<*, *>, Int>()
    protected open fun seriesChanged(c: Change<out Series<*, *>>) {
        // Update style classes for all series lines and symbols
        // Note: is there a more efficient way of doing this?
        /*Matt: Yes, there is.*/

        synchronized(lastSeriesIndices) {

            data.value.forEach {
                val previous = lastSeriesIndices[it]
                val i = c.list.indexOf(it)
                if (previous == null || previous != i) {
                    updateStyleClassOf(it as Series<X, Y>, i)
                    lastSeriesIndices[it] = i
                }
            }

        }
    }


    /**
     * This is called when a data change has happened that may cause the range to be invalid.
     */
    private fun invalidateRange() {
        rangeValid = false
    }

    /**
     * This is called when the range has been invalidated and we need to update it. If the axis are auto
     * ranging then we compile a list of all data that the given axis has to plot and call invalidateRange() on the
     * axis passing it that data.
     */
    protected open fun updateAxisRange() {
        val xa = getXAxis()
        val ya = getYAxis()
        var xData: MutableList<X>? = null
        var yData: MutableList<Y>? = null
        if (xa!!.isAutoRanging()) xData = ArrayList()
        if (ya!!.isAutoRanging()) yData = ArrayList()
        if (xData != null || yData != null) {
            for (series in getData()) {
                for (data in series.getData()) {
                    xData?.add(data!!.xValue)
                    yData?.add(data!!.yValue)
                }
            }
            if (xData != null) xa.invalidateRange(xData)
            if (yData != null) ya.invalidateRange(yData)
        }
    }

    /**
     * Called to update and layout the plot children. This should include all work to updates nodes representing
     * the plot on top of the axis and grid lines etc. The origin is the top left of the plot area, the plot area with
     * can be got by getting the width of the x axis and its height from the height of the y axis.
     */
    protected abstract fun layoutPlotChildren()

    @Suppress("NAME_SHADOWING") /** {@inheritDoc}  */
    final
    override fun layoutChartChildren(
        top: Double,
        left: Double,
        width: Double,
        height: Double
    ) {
        var top = top
        var left = left
        @Suppress("SENSELESS_COMPARISON")
        if (getData() == null) return
        if (!rangeValid) {
            rangeValid = true
            @Suppress("SENSELESS_COMPARISON")
            if (getData() != null) updateAxisRange()
        }
        // snap top and left to pixels
        top = snapPositionY(top)
        left = snapPositionX(left)
        // get starting stuff
        val xa = getXAxis()
        val xaTickMarks = xa!!.tickMarks
        val ya = getYAxis()
        val yaTickMarks = ya!!.tickMarks
        // check we have 2 axises and know their sides
        @Suppress("SENSELESS_COMPARISON")
        if (xa == null || ya == null) return
        // try and work out width and height of axises
        var xAxisWidth = 0.0
        var xAxisHeight = 30.0 // guess x axis height to start with
        var yAxisWidth = 0.0
        var yAxisHeight = 0.0
        for (count in 0..4) {
            yAxisHeight = snapSizeY(height - xAxisHeight)
            if (yAxisHeight < 0) {
                yAxisHeight = 0.0
            }
            yAxisWidth = ya.prefWidth(yAxisHeight)
            xAxisWidth = snapSizeX(width - yAxisWidth)
            if (xAxisWidth < 0) {
                xAxisWidth = 0.0
            }
            val newXAxisHeight = xa.prefHeight(xAxisWidth)
            if (newXAxisHeight == xAxisHeight) break
            xAxisHeight = newXAxisHeight
        }
        // round axis sizes up to whole integers to snap to pixel
        xAxisWidth = Math.ceil(xAxisWidth)
        xAxisHeight = Math.ceil(xAxisHeight)
        yAxisWidth = Math.ceil(yAxisWidth)
        yAxisHeight = Math.ceil(yAxisHeight)
        // calc xAxis height
        var xAxisY = 0.0
        when (xa.effectiveSide) {
            TOP    -> {
                xa.isVisible = true
                xAxisY = top + 1
                top += xAxisHeight
            }

            BOTTOM -> {
                xa.isVisible = true
                xAxisY = top + yAxisHeight
            }

            else   -> {}
        }

        // calc yAxis width
        var yAxisX = 0.0
        when (ya.effectiveSide) {
            LEFT  -> {
                ya.isVisible = true
                yAxisX = left + 1
                left += yAxisWidth
            }

            RIGHT -> {
                ya.isVisible = true
                yAxisX = left + xAxisWidth
            }

            else  -> {}
        }
        // resize axises
        xa.resizeRelocate(left, xAxisY, xAxisWidth, xAxisHeight)
        ya.resizeRelocate(yAxisX, top, yAxisWidth, yAxisHeight)
        // When the chart is resized, need to specifically call out the axises
        // to lay out as they are unmanaged.
        xa.requestAxisLayout()
        xa.layout()
        ya.requestAxisLayout()
        ya.layout()
        // layout plot content
        layoutPlotChildren()
        // get axis zero points
        val xAxisZero = xa.zeroPosition
        val yAxisZero = ya.zeroPosition
        // position vertical and horizontal zero lines
        if (java.lang.Double.isNaN(xAxisZero) || !isVerticalZeroLineVisible()) {
            verticalZeroLine.isVisible = false
        } else {
            verticalZeroLine.startX = left + xAxisZero + 0.5
            verticalZeroLine.startY = top
            verticalZeroLine.endX = left + xAxisZero + 0.5
            verticalZeroLine.endY = top + yAxisHeight
            verticalZeroLine.isVisible = true
        }
        if (java.lang.Double.isNaN(yAxisZero) || !isHorizontalZeroLineVisible()) {
            horizontalZeroLine.isVisible = false
        } else {
            horizontalZeroLine.startX = left
            horizontalZeroLine.startY = top + yAxisZero + 0.5
            horizontalZeroLine.endX = left + xAxisWidth
            horizontalZeroLine.endY = top + yAxisZero + 0.5
            horizontalZeroLine.isVisible = true
        }
        // layout plot background
        plotBackground.resizeRelocate(left, top, xAxisWidth, yAxisHeight)
        // update clip
        plotAreaClip.x = left
        plotAreaClip.y = top
        plotAreaClip.width = xAxisWidth + 1
        plotAreaClip.height = yAxisHeight + 1
        //        plotArea.setClip(new Rectangle(left, top, xAxisWidth, yAxisHeight));
        // position plot group, its origin is the bottom left corner of the plot area
        plotContent.layoutX = left
        plotContent.layoutY = top
        plotContent.requestLayout() // Note: not sure this is right, maybe plotContent should be resizeable
        // update vertical grid lines
        verticalGridLines.elements.clear()
        if (getVerticalGridLinesVisible()) {
            for (i in xaTickMarks.indices) {
                val tick = xaTickMarks[i]
                val x = xa.getDisplayPosition(tick.getValue())
                if ((x != xAxisZero || !isVerticalZeroLineVisible()) && x > 0 && x <= xAxisWidth) {
                    verticalGridLines.elements.add(MoveTo(left + x + 0.5, top))
                    verticalGridLines.elements.add(LineTo(left + x + 0.5, top + yAxisHeight))
                }
            }
        }
        // update horizontal grid lines
        horizontalGridLines.elements.clear()
        if (isHorizontalGridLinesVisible()) {
            for (i in yaTickMarks.indices) {
                val tick = yaTickMarks[i]
                val y = ya.getDisplayPosition(tick.getValue())
                if ((y != yAxisZero || !isHorizontalZeroLineVisible()) && y >= 0 && y < yAxisHeight) {
                    horizontalGridLines.elements.add(MoveTo(left, top + y + 0.5))
                    horizontalGridLines.elements.add(LineTo(left + xAxisWidth, top + y + 0.5))
                }
            }
        }
        // Note: is there a more efficient way to calculate horizontal and vertical row fills?
        // update vertical row fill
        verticalRowFill.elements.clear()
        if (isAlternativeColumnFillVisible()) {
            // tick marks are not sorted so get all the positions and sort them
            val tickPositionsPositive: MutableList<Double> = ArrayList()
            val tickPositionsNegative: MutableList<Double> = ArrayList()
            for (i in xaTickMarks.indices) {
                val pos = xa.getDisplayPosition(xaTickMarks[i].getValue())
                if (pos == xAxisZero) {
                    tickPositionsPositive.add(pos)
                    tickPositionsNegative.add(pos)
                } else if (pos < xAxisZero) {
                    tickPositionsPositive.add(pos)
                } else {
                    tickPositionsNegative.add(pos)
                }
            }
            Collections.sort(tickPositionsPositive)
            Collections.sort(tickPositionsNegative)
            // iterate over every pair of positive tick marks and create fill
            run {
                var i = 1
                while (i < tickPositionsPositive.size) {
                    if (i + 1 < tickPositionsPositive.size) {
                        val x1 = tickPositionsPositive[i]
                        val x2 = tickPositionsPositive[i + 1]
                        verticalRowFill.elements.addAll(
                            MoveTo(left + x1, top),
                            LineTo(left + x1, top + yAxisHeight),
                            LineTo(left + x2, top + yAxisHeight),
                            LineTo(left + x2, top),
                            ClosePath()
                        )
                    }
                    i += 2
                }
            }
            // iterate over every pair of positive tick marks and create fill
            var i = 0
            while (i < tickPositionsNegative.size) {
                if (i + 1 < tickPositionsNegative.size) {
                    val x1 = tickPositionsNegative[i]
                    val x2 = tickPositionsNegative[i + 1]
                    verticalRowFill.elements.addAll(
                        MoveTo(left + x1, top),
                        LineTo(left + x1, top + yAxisHeight),
                        LineTo(left + x2, top + yAxisHeight),
                        LineTo(left + x2, top),
                        ClosePath()
                    )
                }
                i += 2
            }
        }
        // update horizontal row fill
        horizontalRowFill.elements.clear()
        if (isAlternativeRowFillVisible()) {
            // tick marks are not sorted so get all the positions and sort them
            val tickPositionsPositive: MutableList<Double> = ArrayList()
            val tickPositionsNegative: MutableList<Double> = ArrayList()
            for (i in yaTickMarks.indices) {
                val pos = ya.getDisplayPosition(yaTickMarks[i].getValue())
                if (pos == yAxisZero) {
                    tickPositionsPositive.add(pos)
                    tickPositionsNegative.add(pos)
                } else if (pos < yAxisZero) {
                    tickPositionsPositive.add(pos)
                } else {
                    tickPositionsNegative.add(pos)
                }
            }
            Collections.sort(tickPositionsPositive)
            Collections.sort(tickPositionsNegative)
            // iterate over every pair of positive tick marks and create fill
            run {
                var i = 1
                while (i < tickPositionsPositive.size) {
                    if (i + 1 < tickPositionsPositive.size) {
                        val y1 = tickPositionsPositive[i]
                        val y2 = tickPositionsPositive[i + 1]
                        horizontalRowFill.elements.addAll(
                            MoveTo(left, top + y1),
                            LineTo(left + xAxisWidth, top + y1),
                            LineTo(left + xAxisWidth, top + y2),
                            LineTo(left, top + y2),
                            ClosePath()
                        )
                    }
                    i += 2
                }
            }
            // iterate over every pair of positive tick marks and create fill
            var i = 0
            while (i < tickPositionsNegative.size) {
                if (i + 1 < tickPositionsNegative.size) {
                    val y1 = tickPositionsNegative[i]
                    val y2 = tickPositionsNegative[i + 1]
                    horizontalRowFill.elements.addAll(
                        MoveTo(left, top + y1),
                        LineTo(left + xAxisWidth, top + y1),
                        LineTo(left + xAxisWidth, top + y2),
                        LineTo(left, top + y2),
                        ClosePath()
                    )
                }
                i += 2
            }
        }
        //
    }

    /**
     * Get the index of the series in the series linked list.
     *
     * @param series The series to find index for
     * @return index of the series in series list
     */
    fun getSeriesIndex(series: Series<X, Y>?): Int = displayedSeries.indexOf(series)

    val seriesSize: Int
        /**
         * Computes the size of series linked list
         * @return size of series linked list
         */
        get() = displayedSeries.size

    /**
     * This should be called from seriesRemoved() when you are finished with any animation for deleting the series from
     * the chart. It will remove the series from showing up in the Iterator returned by getDisplayedSeriesIterator().
     *
     * @param series The series to remove
     */
    protected fun removeSeriesFromDisplay(series: Series<X, Y>?) {
        if (series != null) series.setToRemove = false
        series!!.setChart(null)
        displayedSeries.remove(series)
        val idx = seriesColorMap.remove(series)!!
        colorBits.clear(idx)
    }

    protected val displayedSeriesIterator: Iterator<Series<X, Y>>
        /**
         * XYChart maintains a list of all series currently displayed this includes all current series + any series that
         * have recently been deleted that are in the process of being faded(animated) out. This creates and returns a
         * iterator over that list. This is what implementations of XYChart should use when plotting data.
         *
         * @return iterator over currently displayed series
         */
        get() = Collections.unmodifiableList(displayedSeries).iterator()


//    protected inner class MattFix(
//        val seriesBeingRemoved: Series<X, Y>,
//        val finalRemovalOp: Op
//    ) {
//
//        var timeline: Timeline? = null
//    }
//
//    protected val mattsBeingRemovedSet = mutableSetOf<MattFix>()


    /**
     * Creates an array of KeyFrames for fading out nodes representing a series
     *
     * @param series The series to remove
     * @param fadeOutTime Time to fade out, in milliseconds
     * @return array of two KeyFrames from zero to fadeOutTime
     */
    fun createSeriesRemoveTimeLine(
        series: Series<X, Y>,
        fadeOutTime: Long
    ): Array<KeyFrame> {
        val nodes: MutableList<Node?> = ArrayList()
        nodes.add(series.getNode())
        for (d in series.getData()) {
            if (d!!.node != null) {
                nodes.add(d.node)
            }
        }
        // fade out series node and symbols
        val startValues = arrayOfNulls<KeyValue>(nodes.size)
        val endValues = arrayOfNulls<KeyValue>(nodes.size)
        for (j in nodes.indices) {
            startValues[j] = KeyValue(nodes[j]!!.opacityProperty(), 1, MyInterpolator.MY_DEFAULT_INTERPOLATOR)
            endValues[j] = KeyValue(nodes[j]!!.opacityProperty(), 0, MyInterpolator.MY_DEFAULT_INTERPOLATOR)
        }

        return arrayOf(
            KeyFrame(Duration.ZERO, *startValues),
            KeyFrame(Duration.millis(fadeOutTime.toDouble()), {
                plotChildren.removeAll(nodes)
                removeSeriesFromDisplay(series)
            }, *endValues)
        )

//        val removeOp = {
//            plotChildren.removeAll(nodes)
//            removeSeriesFromDisplay(series)
//        }
//        val mattFix = MattFix(series, removeOp)
//        mattsBeingRemovedSet += mattFix
//        return arrayOf(
//            KeyFrame(Duration.ZERO, *startValues),
//            KeyFrame(Duration.millis(fadeOutTime.toDouble()), {
//                removeOp()
//                mattsBeingRemovedSet -= mattFix
//            }, *endValues)
//        )
    }

    /**
     * The current displayed data value plotted on the X axis. This may be the same as xValue or different. It is
     * used by XYChart to animate the xValue from the old value to the new value. This is what you should plot
     * in any custom XYChart implementations. Some XYChart chart implementations such as LineChart also use this
     * to animate when data is added or removed.
     * @param item The XYChart.Data item from which the current X axis data value is obtained
     * @return The current displayed X data value
     */
    protected fun getCurrentDisplayedXValue(item: Data<X, Y>): X = item.getCurrentX()

    /** Set the current displayed data value plotted on X axis.
     *
     * @param item The XYChart.Data item from which the current X axis data value is obtained.
     * @param value The X axis data value
     * @see .getCurrentDisplayedXValue
     */
    protected fun setCurrentDisplayedXValue(
        item: Data<X, Y>,
        value: X
    ) {
        item.setCurrentX(value)
    }

    /** The current displayed data value property that is plotted on X axis.
     *
     * @param item The XYChart.Data item from which the current X axis data value property object is obtained.
     * @return The current displayed X data value ObjectProperty.
     * @see .getCurrentDisplayedXValue
     */
    protected fun currentDisplayedXValueProperty(item: Data<X, Y>): ObjectProperty<X> = item.currentXProperty()

    /**
     * The current displayed data value plotted on the Y axis. This may be the same as yValue or different. It is
     * used by XYChart to animate the yValue from the old value to the new value. This is what you should plot
     * in any custom XYChart implementations. Some XYChart chart implementations such as LineChart also use this
     * to animate when data is added or removed.
     * @param item The XYChart.Data item from which the current Y axis data value is obtained
     * @return The current displayed Y data value
     */
    protected fun getCurrentDisplayedYValue(item: Data<X, Y>): Y = item.getCurrentY()

    /**
     * Set the current displayed data value plotted on Y axis.
     *
     * @param item The XYChart.Data item from which the current Y axis data value is obtained.
     * @param value The Y axis data value
     * @see .getCurrentDisplayedYValue
     */
    protected fun setCurrentDisplayedYValue(
        item: Data<X, Y>,
        value: Y
    ) {
        item.setCurrentY(value)
    }

    /** The current displayed data value property that is plotted on Y axis.
     *
     * @param item The XYChart.Data item from which the current Y axis data value property object is obtained.
     * @return The current displayed Y data value ObjectProperty.
     * @see .getCurrentDisplayedYValue
     */
    protected fun currentDisplayedYValueProperty(item: Data<X, Y>): ObjectProperty<Y> = item.currentYProperty()

    /**
     * The current displayed data extra value. This may be the same as extraValue or different. It is
     * used by XYChart to animate the extraValue from the old value to the new value. This is what you should plot
     * in any custom XYChart implementations.
     * @param item The XYChart.Data item from which the current extra value is obtained
     * @return The current extra value
     */
    protected fun getCurrentDisplayedExtraValue(item: Data<X, Y>): Any? = item.getCurrentExtraValue()

    /**
     * Set the current displayed data extra value.
     *
     * @param item The XYChart.Data item from which the current extra value is obtained.
     * @param value The extra value
     * @see .getCurrentDisplayedExtraValue
     */
    protected fun setCurrentDisplayedExtraValue(
        item: Data<X, Y>,
        value: Any?
    ) {
        item.setCurrentExtraValue(value)
    }

    /**
     * The current displayed extra value property.
     *
     * @param item The XYChart.Data item from which the current extra value property object is obtained.
     * @return ObjectProperty&lt;Object&gt; The current extra value ObjectProperty
     * @see .getCurrentDisplayedExtraValue
     */
    protected fun currentDisplayedExtraValueProperty(item: Data<X, Y>): ObjectProperty<Any?> = item.currentExtraValueProperty()

    /**
     * XYChart maintains a list of all items currently displayed this includes all current data + any data items
     * recently deleted that are in the process of being faded out. This creates and returns a iterator over
     * that list. This is what implementations of XYChart should use when plotting data.
     *
     * @param series The series to get displayed data for
     * @return iterator over currently displayed items from this series
     */
    internal fun getDisplayedDataIterator(series: Series<X, Y>): Iterator<Data<X, Y>> = Collections.unmodifiableList(series.displayedData).iterator()

    /**
     * This should be called from dataItemRemoved() when you are finished with any animation for deleting the item from the
     * chart. It will remove the data item from showing up in the Iterator returned by getDisplayedDataIterator().
     *
     * @param series The series to remove
     * @param item   The item to remove from series's display list
     */
    protected fun removeDataItemFromDisplay(
        series: Series<X, Y>,
        item: Data<X, Y>?
    ) {
        series.removeDataItemRef(item)
    }

    // -------------- STYLESHEET HANDLING ------------------------------------------------------------------------------
    private object StyleableProperties {
        val HORIZONTAL_GRID_LINE_VISIBLE: CssMetaData<XYChartForPackagePrivateProps<*, *>, Boolean> =
            object : BooleanCssMetaData<XYChartForPackagePrivateProps<*, *>>(
                "-fx-horizontal-grid-lines-visible",
                true
            ) {
                override fun isSettable(node: XYChartForPackagePrivateProps<*, *>): Boolean = node.horizontalGridLinesVisible.value == null ||
                    !node.horizontalGridLinesVisible.isBound

                override fun getStyleableProperty(node: XYChartForPackagePrivateProps<*, *>): StyleableProperty<Boolean?> {
                    @Suppress("UNCHECKED_CAST")
                    return node.horizontalGridLinesVisibleProperty() as StyleableProperty<Boolean?>
                }
            }
        val HORIZONTAL_ZERO_LINE_VISIBLE: BooleanCssMetaData<XYChartForPackagePrivateProps<*, *>> =
            object : BooleanCssMetaData<XYChartForPackagePrivateProps<*, *>>(
                "-fx-horizontal-zero-line-visible",
                true
            ) {
                override fun isSettable(node: XYChartForPackagePrivateProps<*, *>): Boolean = node.horizontalZeroLineVisible.value == null ||
                    !node.horizontalZeroLineVisible.isBound

                override fun getStyleableProperty(node: XYChartForPackagePrivateProps<*, *>): StyleableProperty<Boolean?> {
                    @Suppress("UNCHECKED_CAST")
                    return node.horizontalZeroLineVisibleProperty() as StyleableProperty<Boolean?>
                }
            }
        val ALTERNATIVE_ROW_FILL_VISIBLE: BooleanCssMetaData<XYChartForPackagePrivateProps<*, *>> =
            object : BooleanCssMetaData<XYChartForPackagePrivateProps<*, *>>(
                "-fx-alternative-row-fill-visible",
                true
            ) {
                override fun isSettable(node: XYChartForPackagePrivateProps<*, *>): Boolean = node.alternativeRowFillVisible.value == null ||
                    !node.alternativeRowFillVisible.isBound

                override fun getStyleableProperty(node: XYChartForPackagePrivateProps<*, *>): StyleableProperty<Boolean?> {
                    @Suppress("UNCHECKED_CAST")
                    return node.alternativeRowFillVisibleProperty() as StyleableProperty<Boolean?>
                }
            }
        val VERTICAL_GRID_LINE_VISIBLE: BooleanCssMetaData<XYChartForPackagePrivateProps<*, *>> =
            object : BooleanCssMetaData<XYChartForPackagePrivateProps<*, *>>(
                "-fx-vertical-grid-lines-visible",
                true
            ) {
                override fun isSettable(node: XYChartForPackagePrivateProps<*, *>): Boolean = node.verticalGridLinesVisible.value == null ||
                    !node.verticalGridLinesVisible.isBound

                override fun getStyleableProperty(node: XYChartForPackagePrivateProps<*, *>): StyleableProperty<Boolean?> {
                    @Suppress("UNCHECKED_CAST")
                    return node.verticalGridLinesVisibleProperty() as StyleableProperty<Boolean?>
                }
            }
        val VERTICAL_ZERO_LINE_VISIBLE: BooleanCssMetaData<XYChartForPackagePrivateProps<*, *>> =
            object : BooleanCssMetaData<XYChartForPackagePrivateProps<*, *>>(
                "-fx-vertical-zero-line-visible",
                true
            ) {
                override fun isSettable(node: XYChartForPackagePrivateProps<*, *>): Boolean = node.verticalZeroLineVisible.value == null ||
                    !node.verticalZeroLineVisible.isBound

                override fun getStyleableProperty(node: XYChartForPackagePrivateProps<*, *>): StyleableProperty<Boolean?> {
                    @Suppress("UNCHECKED_CAST")
                    return node.verticalZeroLineVisibleProperty() as StyleableProperty<Boolean?>
                }
            }
        val ALTERNATIVE_COLUMN_FILL_VISIBLE: BooleanCssMetaData<XYChartForPackagePrivateProps<*, *>> =
            object : BooleanCssMetaData<XYChartForPackagePrivateProps<*, *>>(
                "-fx-alternative-column-fill-visible",
                true
            ) {
                override fun isSettable(node: XYChartForPackagePrivateProps<*, *>): Boolean = node.alternativeColumnFillVisible.value == null ||
                    !node.alternativeColumnFillVisible.isBound

                override fun getStyleableProperty(node: XYChartForPackagePrivateProps<*, *>): StyleableProperty<Boolean?> {
                    @Suppress("UNCHECKED_CAST")
                    return node.alternativeColumnFillVisibleProperty() as StyleableProperty<Boolean?>
                }
            }
        val classCssMetaData: List<CssMetaData<out Styleable?, *>>? by lazy {
            val styleables: MutableList<CssMetaData<out Styleable?, *>> = ArrayList(getClassCssMetaData())
            styleables.add(HORIZONTAL_GRID_LINE_VISIBLE)
            styleables.add(HORIZONTAL_ZERO_LINE_VISIBLE)
            styleables.add(ALTERNATIVE_ROW_FILL_VISIBLE)
            styleables.add(VERTICAL_GRID_LINE_VISIBLE)
            styleables.add(VERTICAL_ZERO_LINE_VISIBLE)
            styleables.add(ALTERNATIVE_COLUMN_FILL_VISIBLE)
            Collections.unmodifiableList(styleables)
        }

    }

    @Open
    /**
     * {@inheritDoc}
     * @since JavaFX 8.0
     */
    override fun getCssMetaData(): List<CssMetaData<out Styleable?, *>>? = classCssMetaData
    // -------------- INNER CLASSES ------------------------------------------------------------------------------------
    /**
     * A single data item with data for 2 axis charts
     * @since JavaFX 2.0
     */
    class Data<X, Y> {
        // -------------- PUBLIC PROPERTIES ----------------------------------------
        var setToRemove = false

        /** The series this data belongs to  */
        private var series: Series<X, Y>? = null
        private val seriesProperty: ObjectProperty<Series<X, Y>> = SimpleObjectProperty()
        fun setSeries(series: Series<X, Y>?) {
            this.series = series
            this.seriesProperty.set(series);
        }

        /** The generic data value to be plotted on the X axis  */
        internal val xValueProp: ObjectProperty<X> = object : SimpleObjectProperty<X>(this@Data, "XValue") {
            override fun invalidated() {
                if (series != null) {
                    val chart = series!!.getChart()
                    chart?.dataValueChanged(this@Data, get(), currentXProperty())
                } else {
                    // data has not been added to series yet :
                    // so currentX and X should be the same
                    setCurrentX(get())
                }
            }
        }
        var xValue: X
            get() = xValueProp.get()
            set(value) {
                xValueProp.set(value)
                // handle the case where this is a init because the default constructor was used
                // and the case when series is not associated to a chart due to a remove series
                if (currentX.get() == null || series != null && series!!.getChart() == null) currentX.value = value
            }


        /**
         * The generic data value to be plotted on the X axis.
         * @return The XValue property
         */
        fun XValueProperty(): ObjectProperty<X> = xValueProp

        /** The generic data value to be plotted on the Y axis  */
        val yValueProp: ObjectProperty<Y> = object : SimpleObjectProperty<Y>(this@Data, "YValue") {
            override fun invalidated() {
                if (series != null) {
                    val chart = series!!.getChart()
                    chart?.dataValueChanged(this@Data, get(), currentYProperty())
                } else {
                    // data has not been added to series yet :
                    // so currentY and Y should be the same
                    setCurrentY(get())
                }
            }
        }

        var yValue: Y
            get() = yValueProp.get()
            set(value) {
                yValueProp.set(value)
                // handle the case where this is a init because the default constructor was used
                // and the case when series is not associated to a chart due to a remove series
                if (currentY.get() == null || series != null && series!!.getChart() == null) currentY.value = value
            }


        /**
         * The generic data value to be plotted on the Y axis.
         * @return the YValue property
         */
        fun YValueProperty(): ObjectProperty<Y> = yValueProp

        fun valueOfDim(dim: Dim2D): Any? = when (dim) {
            Dim2D.X -> xValue
            Dim2D.Y -> yValue
        }

        /**
         * The generic data value to be plotted in any way the chart needs. For example used as the radius
         * for BubbleChart.
         */
        internal val extraValue: ObjectProperty<Any> = object : SimpleObjectProperty<Any>(this@Data, "extraValue") {
            override fun invalidated() {
                if (series != null) {
                    val chart = series!!.getChart()
                    chart?.dataValueChanged(this@Data, get(), currentExtraValueProperty())
                }
            }
        }

        fun getExtraValue(): Any = extraValue.get()

        fun setExtraValue(value: Any) {
            extraValue.set(value)
        }

        fun extraValueProperty(): ObjectProperty<Any> = extraValue

        /**
         * The node to display for this data item. You can either create your own node and set it on the data item
         * before you add the item to the chart. Otherwise the chart will create a node for you that has the default
         * representation for the chart type. This node will be set as soon as the data is added to the chart. You can
         * then get it to add mouse listeners etc. Charts will do their best to position and size the node
         * appropriately, for example on a Line or Scatter chart this node will be positioned centered on the data
         * values position. For a bar chart this is positioned and resized as the bar for this data item.
         */
        val nodeProp: ObjectProperty<Node> = object : SimpleObjectProperty<Node>(this, "node") {
            override fun invalidated() {
                val node = get()
                if (node != null) {
                    node.accessibleTextProperty().unbind()


                    /*requires resource from FX 21+5*/
                    val seriesLabel: ObservableValue<String> = seriesProperty
                        .flatMap(Series<X, Y>::nameProperty)
                        .orElse("")
                    val xAxisLabel: ObservableValue<String> = seriesProperty
                        .flatMap(Series<X, Y>::chartProperty)
                        .flatMap(XYChartForPackagePrivateProps<X, Y>::xAxisProperty)
                        .flatMap(AxisForPackagePrivateProps<X>::labelProperty)
                        .orElse(ControlResources.getString("XYChart.series.xaxis"))
                    val yAxisLabel: ObservableValue<String> = seriesProperty
                        .flatMap(Series<X, Y>::chartProperty)
                        .flatMap(XYChartForPackagePrivateProps<X, Y>::yAxisProperty)
                        .flatMap(AxisForPackagePrivateProps<Y>::labelProperty)
                        .orElse(ControlResources.getString("XYChart.series.yaxis"))



                    node.accessibleTextProperty().bind(object : StringBinding() {
                        init {
                            bind(
                                currentXProperty(),
                                currentYProperty(),
                                seriesLabel,
                                xAxisLabel,
                                yAxisLabel
                            );
                        }

                        override fun computeValue(): String {

                            /*requires resources from FX 21+5*/
                            val seriesName = seriesLabel.value
                            val xAxisName = xAxisLabel.value
                            val yAxisName = yAxisLabel.value
                            val format = ControlResources.getString("XYChart.series.accessibleText")
                            val mf = MessageFormat(format)
                            val args = arrayOf(
                                seriesName,
                                xAxisName,
                                getCurrentX(),
                                yAxisName,
                                getCurrentY()
                            )
                            return mf.format(args)


//                            val seriesName = if (series != null) series!!.getName() else ""
//                            return seriesName + " X Axis is " + getCurrentX() + " Y Axis is " + getCurrentY()
                        }
                    })
                }
            }
        }

        var node: Node?
            get() = nodeProp.get()
            set(value) = nodeProp.set(value)


        /**
         * The current displayed data value plotted on the X axis. This may be the same as xValue or different. It is
         * used by XYChart to animate the xValue from the old value to the new value. This is what you should plot
         * in any custom XYChart implementations. Some XYChart chart implementations such as LineChart also use this
         * to animate when data is added or removed.
         */
        internal val currentX: ObjectProperty<X> = SimpleObjectProperty(this, "currentX")
        fun getCurrentX(): X = currentX.get()

        fun setCurrentX(value: X) {
            currentX.set(value)
        }

        fun currentXProperty(): ObjectProperty<X> = currentX

        /**
         * The current displayed data value plotted on the Y axis. This may be the same as yValue or different. It is
         * used by XYChart to animate the yValue from the old value to the new value. This is what you should plot
         * in any custom XYChart implementations. Some XYChart chart implementations such as LineChart also use this
         * to animate when data is added or removed.
         */
        internal val currentY: ObjectProperty<Y> = SimpleObjectProperty(this, "currentY")
        fun getCurrentY(): Y = currentY.get()

        fun setCurrentY(value: Y) {
            currentY.set(value)
        }

        fun currentYProperty(): ObjectProperty<Y> = currentY

        /**
         * The current displayed data extra value. This may be the same as extraValue or different. It is
         * used by XYChart to animate the extraValue from the old value to the new value. This is what you should plot
         * in any custom XYChart implementations.
         */
        private val currentExtraValue: ObjectProperty<Any?> = SimpleObjectProperty(this, "currentExtraValue")
        fun getCurrentExtraValue(): Any? = currentExtraValue.value

        fun setCurrentExtraValue(value: Any?) {
            currentExtraValue.value = value
        }

        fun currentExtraValueProperty(): ObjectProperty<Any?> = currentExtraValue
        // -------------- CONSTRUCTOR -------------------------------------------------
        /**
         * Creates an empty XYChart.Data object.
         */
        constructor()

        /**
         * Creates an instance of XYChart.Data object and initializes the X,Y
         * data values.
         *
         * @param xValue The X axis data value
         * @param yValue The Y axis data value
         */
        constructor(
            xValue: X,
            yValue: Y
        ) {
            this.xValue = xValue
            this.yValue = yValue
            setCurrentX(xValue)
            setCurrentY(yValue)
        }

        /**
         * Creates an instance of XYChart.Data object and initializes the X,Y
         * data values and extraValue.
         *
         * @param xValue The X axis data value.
         * @param yValue The Y axis data value.
         * @param extraValue Chart extra value.
         */
        constructor(
            xValue: X,
            yValue: Y,
            extraValue: Any
        ) {
            this.xValue = xValue
            this.yValue = yValue
            setExtraValue(extraValue)
            setCurrentX(xValue)
            setCurrentY(yValue)
            setCurrentExtraValue(extraValue)
        }
        // -------------- PUBLIC METHODS ----------------------------------------------
        /**
         * Returns a string representation of this `Data` object.
         * @return a string representation of this `Data` object.
         */
        override fun toString(): String = "Data[" + xValue + "," + yValue + "," + getExtraValue() + "]"
    }

    /**
     * A named series of data items
     * @since JavaFX 2.0
     */
    class Series<X, Y> @JvmOverloads constructor(data: ObservableList<Data<X, Y>> = FXCollections.observableArrayList()) :
        SeriesIdea {
            // -------------- PRIVATE PROPERTIES ----------------------------------------
            /** the style class for default color for this series  */
            var defaultColorStyleClass: String? = null
            var setToRemove = false
            val displayedData: MutableList<Data<X, Y>> = ArrayList()
            private val dataChangeListener: ListChangeListener<Data<X, Y>> =
                ListChangeListener { c ->
                    val data2 = c.list
                    val chart = getChart()
                    while (c.next()) {
                        if (chart != null) {
                            // RT-25187 Probably a sort happened, just reorder the pointers and return.
                            if (c.wasPermutated()) {
                                displayedData.sortWith { o1: Data<X, Y>?, o2: Data<X, Y>? ->
                                    data2.indexOf(
                                        o2
                                    ) - data2.indexOf(o1)
                                }
                                return@ListChangeListener
                            }
                            val dupCheck: MutableSet<Data<X, Y>?> = HashSet(displayedData)
                            dupCheck.removeAll(c.removed)
                            for (d in c.addedSubList) {
                                require(dupCheck.add(d)) { "Duplicate data added" }
                            }

                            // update data items reference to series
                            for (item in c.removed) {
                                item!!.setToRemove = true
                            }
                            if (c.addedSize > 0) {
                                for (itemPtr in c.addedSubList) {
                                    if (itemPtr!!.setToRemove) {
                                        @Suppress("SENSELESS_COMPARISON")
                                        if (chart != null) chart.dataBeingRemovedIsAdded(itemPtr, this@Series)
                                        itemPtr.setToRemove = false
                                    }
                                }
                                for (d in c.addedSubList) {
                                    d!!.setSeries(this@Series)
                                }
                                if (c.from == 0) {
                                    displayedData.addAll(0, c.addedSubList)
                                } else {
                                    displayedData.addAll(displayedData.indexOf(data2[c.from - 1]) + 1, c.addedSubList)
                                }
                            }
                            // inform chart
                            chart.dataItemsChanged(
                                this@Series,
                                c.removed as List<Data<X, Y>>, c.from, c.to
                            )
                        } else {
                            val dupCheck: MutableSet<Data<X, Y>?> = HashSet()
                            for (d in data2) {
                                require(dupCheck.add(d)) { "Duplicate data added" }
                            }
//                        val debugCL = classLoaderOf(c)
//                        println("debugCL1=${debugCL}")
                            for (d in c.addedSubList) {
                                d!!.setSeries(this@Series)
                            }
                        }
                    }
                }
            // -------------- PUBLIC PROPERTIES ----------------------------------------
            /** Reference to the chart this series belongs to  */
            private val chart: ReadOnlyObjectWrapper<XYChartForPackagePrivateProps<X, Y>> =
                object : ReadOnlyObjectWrapper<XYChartForPackagePrivateProps<X, Y>>(this, "chart") {
                    override fun invalidated() {
                        if (get() == null) {
                            displayedData.clear()
                        } else {
                            displayedData.addAll(getData())
                        }
                    }
                }

            fun getChart(): XYChartForPackagePrivateProps<X, Y>? = chart.get()

            fun setChart(value: XYChartForPackagePrivateProps<X, Y>?) {
                chart.set(value)
            }

            fun chartProperty(): ReadOnlyObjectProperty<XYChartForPackagePrivateProps<X, Y>> = chart.readOnlyProperty

            /** The user displayable name for this series  */
            internal val name: StringProperty = object : StringPropertyBase() {
                override fun invalidated() {
                    get() // make non-lazy
                    if (getChart() != null) getChart()!!.seriesNameChanged()
                }

                override fun getBean(): Any = this@Series

                override fun getName(): String = "name"
            }

            fun getName(): String? = name.get()

            fun setName(value: String) {
                name.set(value)
            }

            fun nameProperty(): StringProperty = name

            /**
             * The node to display for this series. This is created by the chart if it uses nodes to represent the whole
             * series. For example line chart uses this for the line but scatter chart does not use it. This node will be
             * set as soon as the series is added to the chart. You can then get it to add mouse listeners etc.
             */
            internal val node: ObjectProperty<Node> = SimpleObjectProperty(this, "node")
            fun getNode(): Node = node.get()

            fun setNode(value: Node) = node.set(value)

            fun nodeProperty(): ObjectProperty<Node> = node

            /** ObservableList of data items that make up this series  */

            @Suppress("UNNECESSARY_SAFE_CALL")
            internal val data: ObjectProperty<ObservableList<Data<X, Y>>> =
                object : ObjectPropertyBase<ObservableList<Data<X, Y>>>() {
                    private var old: ObservableList<Data<X, Y>>? = null
                    override fun invalidated() {
                        val current: ObservableList<Data<X, Y>> = getValue()
                        // add remove listeners
                        if (old != null) old!!.removeListener(dataChangeListener)
                        current.addListener(dataChangeListener)
                        // fire data change event if series are added or removed
                        @Suppress("KotlinConstantConditions", "SENSELESS_COMPARISON")
                        if (old != null || current != null) {
                            val removed = if (old != null) old!! else emptyList()

                            @Suppress("USELESS_ELVIS")

                            val toIndex = current?.size ?: 0
                            // let data listener know all old data have been removed and new data that has been added
                            if (toIndex > 0 || !removed.isEmpty()) {
                                dataChangeListener.onChanged(object : NonIterableChange<Data<X, Y>>(0, toIndex, current) {
                                    override fun getRemoved(): List<Data<X, Y>> = removed

                                    override fun getPermutation(): IntArray = IntArray(0)
                                })
                            }
                        } else if (old?.let { it.size > 0 } ?: false) {
                            // let series listener know all old series have been removed
                            dataChangeListener.onChanged(object : NonIterableChange<Data<X, Y>>(0, 0, current) {
                                override fun getRemoved(): List<Data<X, Y>> = old!!

                                override fun getPermutation(): IntArray = IntArray(0)
                            })
                        }
                        old = current
                    }

                    override fun getBean(): Any = this@Series

                    override fun getName(): String = "data"
                }

            fun getData(): ObservableList<Data<X, Y>> = data.value

            fun setData(value: ObservableList<Data<X, Y>>) {
                data.value = value
            }


            fun dataProperty(): ObjectProperty<ObservableList<Data<X, Y>>> = data
            /**
             * Constructs a Series and populates it with the given [ObservableList] data.
             *
             * @param data ObservableList of XYChart.Data
             */
            // -------------- CONSTRUCTORS ----------------------------------------------
            /**
             * Construct a empty series
             */
            init {
                setData(data)
                for (item in data) item!!.setSeries(this)
            }

            /**
             * Constructs a named Series and populates it with the given [ObservableList] data.
             *
             * @param name a name for the series
             * @param data ObservableList of XYChart.Data
             */
            constructor(
                name: String,
                data: ObservableList<Data<X, Y>>
            ) : this(data) {
                setName(name)
            }
            // -------------- PUBLIC METHODS ----------------------------------------------
            /**
             * Returns a string representation of this `Series` object.
             * @return a string representation of this `Series` object.
             */
            override fun toString(): String = "Series[" + getName() + "]"

            // -------------- PRIVATE/PROTECTED METHODS -----------------------------------
        /*
         * The following methods are for manipulating the pointers in the linked list
         * when data is deleted.
         */
            fun removeDataItemRef(item: Data<X, Y>?) {
                if (item != null) item.setToRemove = false
                displayedData.remove(item)
            }

            fun getItemIndex(item: Data<X, Y>?): Int = displayedData.indexOf(item)

            fun getItem(i: Int): Data<X, Y>? = displayedData[i]

            val dataSize: Int
                get() = displayedData.size
        }

    companion object {
        var DEFAULT_COLOR = "default-color"
    }
}
