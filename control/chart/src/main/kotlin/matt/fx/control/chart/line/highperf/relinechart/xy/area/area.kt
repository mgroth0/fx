package matt.fx.control.chart.line.highperf.relinechart.xy.area

import javafx.animation.FadeTransition
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.beans.NamedArg
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.chart.AreaChart
import javafx.scene.shape.ClosePath
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.shape.StrokeLineJoin.BEVEL
import javafx.util.Duration
import matt.fx.base.rewrite.ReWrittenFxClass
import matt.fx.control.chart.axis.value.axis.AxisForPackagePrivateProps
import matt.fx.control.chart.line.highperf.relinechart.MorePerfOptionsLineChart
import matt.fx.control.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps
import matt.fx.control.chart.linelike.LineLikeChartNodeWithOptionalSymbols
import matt.fx.graphics.anim.interp.MyInterpolator

@ReWrittenFxClass(AreaChart::class)
class AreaChartForPrivateProps<X, Y> @JvmOverloads constructor(
    @NamedArg("xAxis") xAxis: AxisForPackagePrivateProps<X>,
    @NamedArg("yAxis") yAxis: AxisForPackagePrivateProps<Y>,
    @NamedArg("data")
    data: ObservableList<Series<X, Y>> = FXCollections.observableArrayList()
) : LineLikeChartNodeWithOptionalSymbols<X, Y>(xAxis, yAxis) {
    /** A multiplier for the Y values that we store for each series, it is used to animate in a new series  */
    private val seriesYMultiplierMap: MutableMap<Series<X, Y>, DoubleProperty> = HashMap()
    private var timeline: Timeline? = null


    /**
     * Construct a new Area Chart with the given axis and data
     *
     * @param xAxis The x axis to use
     * @param yAxis The y axis to use
     * @param data The data to use, this is the actual list used so any changes to it will be reflected in the chart

     * Construct a new Area Chart with the given axis
     *
     * @param xAxis The x axis to use
     * @param yAxis The y axis to use
     */
    init {
        setData(data)
    }

    /** {@inheritDoc}  */
    override fun updateAxisRange() {
        val xa = xAxis
        val ya = yAxis
        var xData: MutableList<X>? = null
        var yData: MutableList<Y>? = null
        if (xa.isAutoRanging()) xData = ArrayList()
        if (ya.isAutoRanging()) yData = ArrayList()
        if (xData != null || yData != null) {
            for (series in data.value) {
                for (data in series.data.value) {
                    xData?.add(data.xValueProp.value)
                    yData?.add(data.yValueProp.value)
                }
            }
            if (xData != null && !(xData.size == 1 && xAxis.toNumericValue(xData[0]) == 0.0)) {
                xa.invalidateRange(xData)
            }
            if (yData != null && !(yData.size == 1 && yAxis.toNumericValue(yData[0]) == 0.0)) {
                ya.invalidateRange(yData)
            }
        }
    }

    override fun dataItemAdded(
        series: Series<X, Y>,
        itemIndex: Int,
        item: Data<X, Y>
    ) {
        val symbol = createSymbol(series, data.value.indexOf(series), item, itemIndex)
        if (shouldAnimate()) {
            var animate = false
            if (itemIndex > 0 && itemIndex < series.data.value.size - 1) {
                animate = true
                val p1 = series.data.value[itemIndex - 1]
                val p2 = series.data.value[itemIndex + 1]
                val x1 = xAxis.toNumericValue(p1.xValueProp.value)
                val y1 = yAxis.toNumericValue(p1.yValueProp.value)
                val x3 = xAxis.toNumericValue(p2.xValueProp.value)
                val y3 = yAxis.toNumericValue(p2.yValueProp.value)
                val x2 = xAxis.toNumericValue(item.xValueProp.value)
                @Suppress("UNUSED_VARIABLE") val y2 = yAxis.toNumericValue(item.yValueProp.value)

                /* //1. y intercept of the line : y = ((y3-y1)/(x3-x1)) * x2 + (x3y1 - y3x1)/(x3 -x1) */
                val y = (y3 - y1) / (x3 - x1) * x2 + (x3 * y1 - y3 * x1) / (x3 - x1)
                item.currentY.value = yAxis.toRealValue(y)
                item.setCurrentX(xAxis.toRealValue(x2)!!)
                /*
                2. we can simply use the midpoint on the line as well..
                double x = (x3 + x1)/2;
                double y = (y3 + y1)/2;
                item.setCurrentX(x);
                item.setCurrentY(y);
                 */
            } else if (itemIndex == 0 && series.data.value.size > 1) {
                animate = true
                item.currentX.value = series.data.value[1].xValueProp.value
                item.setCurrentY(series.data.value[1].yValueProp.value)
            } else if (itemIndex == series.data.value.size - 1 && series.data.value.size > 1) {
                animate = true
                val last = series.data.value.size - 2
                item.currentX.value = series.data.value[last].xValueProp.value
                item.currentY.value = series.data.value[last].yValueProp.value
            }
            if (symbol != null) {
                /* fade in new symbol */
                symbol.opacity = 0.0
                plotChildren.add(symbol)
                val ft = FadeTransition(Duration.millis(500.0), symbol)
                ft.toValue = 1.0
                ft.play()
            }
            if (animate) {
                animate(
                    KeyFrame(
                        Duration.ZERO,
                        {
                            if (symbol != null && !plotChildren.contains(symbol)) {
                                plotChildren.add(symbol)
                            }
                        },
                        KeyValue(
                            item.currentYProperty(),
                            item.currentY.value,
                            MyInterpolator.MY_DEFAULT_INTERPOLATOR
                        ),
                        KeyValue(
                            item.currentXProperty(),
                            item.currentX.value,
                            MyInterpolator.MY_DEFAULT_INTERPOLATOR
                        )
                    ),
                    KeyFrame(
                        Duration.millis(800.0),
                        KeyValue(
                            item.currentYProperty(),
                            item.yValueProp.value, MyInterpolator.EASE_BOTH
                        ),
                        KeyValue(
                            item.currentXProperty(),
                            item.xValueProp.value, MyInterpolator.EASE_BOTH
                        )
                    )
                )
            }
        } else if (symbol != null) {
            plotChildren.add(symbol)
        }
    }

    override fun dataItemRemoved(
        item: Data<X, Y>,
        series: Series<X, Y>
    ) {
        val symbol = item.nodeProp.value
        symbol?.focusTraversableProperty()?.unbind()

        /* remove item from sorted list */
        val itemIndex = series.getItemIndex(item)
        if (shouldAnimate()) {
            var animate = false
            /* dataSize represents size of currently visible data. After this operation, the number will decrement by 1 */
            val dataSize = series.dataSize
            /*
            This is the size of current data list in Series. Note that it might be totaly different from dataSize as
            some big operation might have happened on the list.
             */
            val dataListSize = series.data.value.size
            if (itemIndex > 0 && itemIndex < dataSize - 1) {
                animate = true
                val p1 = series.getItem(itemIndex - 1)!!
                val p2 = series.getItem(itemIndex + 1)!!
                val x1 = xAxis.toNumericValue(p1.xValueProp.value)
                val y1 = yAxis.toNumericValue(p1.yValueProp.value)
                val x3 = xAxis.toNumericValue(p2.xValueProp.value)
                val y3 = yAxis.toNumericValue(p2.yValueProp.value)
                val x2 = xAxis.toNumericValue(item.xValueProp.value)
                val y2 = yAxis.toNumericValue(item.yValueProp.value)

                /* //1.  y intercept of the line : y = ((y3-y1)/(x3-x1)) * x2 + (x3y1 - y3x1)/(x3 -x1) */
                val y = (y3 - y1) / (x3 - x1) * x2 + (x3 * y1 - y3 * x1) / (x3 - x1)
                item.currentX.value = xAxis.toRealValue(x2)
                item.currentY.value = yAxis.toRealValue(y2)
                item.xValueProp.value = xAxis.toRealValue(x2)
                item.yValue = (yAxis.toRealValue(y)!!)
                /*
                2.  we can simply use the midpoint on the line as well..
                double x = (x3 + x1)/2;
                double y = (y3 + y1)/2;
                item.setCurrentX(x);
                item.setCurrentY(y);
                 */
            } else if (itemIndex == 0 && dataListSize > 1) {
                animate = true
                item.xValueProp.value = series.data.value[0].xValueProp.value
                item.yValue = (series.data.value[0].yValueProp.value)
            } else if (itemIndex == dataSize - 1 && dataListSize > 1) {
                animate = true
                val last = dataListSize - 1
                item.xValueProp.value = series.data.value[last].xValueProp.value
                item.yValue = (series.data.value[last].yValueProp.value)
            } else if (symbol != null) {
                /* fade out symbol */
                symbol.opacity = 0.0
                val ft = FadeTransition(Duration.millis(500.0), symbol)
                ft.toValue = 0.0
                ft.onFinished =
                    EventHandler {
                        plotChildren.remove(symbol)
                        removeDataItemFromDisplay(series, item)
                    }
                ft.play()
            } else {
                item.setSeries(null)
                removeDataItemFromDisplay(series, item)
            }
            if (animate) {
                animate(
                    KeyFrame(
                        Duration.ZERO,
                        KeyValue(
                            item.currentYProperty(),
                            item.currentY.value,
                            MyInterpolator.MY_DEFAULT_INTERPOLATOR
                        ),
                        KeyValue(
                            item.currentXProperty(),
                            item.currentX.value, MyInterpolator.MY_DEFAULT_INTERPOLATOR
                        )
                    ),
                    KeyFrame(
                        Duration.millis(800.0), {
                            item.setSeries(null)
                            plotChildren.remove(symbol)
                            removeDataItemFromDisplay(series, item)
                        },
                        KeyValue(
                            item.currentYProperty(),
                            item.yValueProp.value, MyInterpolator.EASE_BOTH
                        ),
                        KeyValue(
                            item.currentXProperty(),
                            item.xValueProp.value, MyInterpolator.EASE_BOTH
                        )
                    )
                )
            }
        } else {
            item.setSeries(null)
            plotChildren.remove(symbol)
            removeDataItemFromDisplay(series, item)
        }
        /* Note: better animation here, point should move from old position to new position at center point between prev and next symbols */
    }

    override fun updateStyleClassOf(
        s: Series<X, Y>,
        i: Int
    ) {
        val seriesLine = (s.node.value as Group).children[1] as Path
        val fillPath = (s.node.value as Group).children[0] as Path
        seriesLine.styleClass.setAll("chart-series-area-line", "series$i", s.defaultColorStyleClass)
        fillPath.styleClass.setAll("chart-series-area-fill", "series$i", s.defaultColorStyleClass)
        for (j in s.data.value.indices) {
            val item = s.data.value[j]
            val node = item.nodeProp.value
            node?.styleClass?.setAll("chart-area-symbol", "series$i", "data$j", s.defaultColorStyleClass)
        }
    }

    override fun seriesAdded(
        series: Series<X, Y>,
        seriesIndex: Int
    ) {
        /* create new paths for series */
        val seriesLine = Path()
        val fillPath = Path()
        seriesLine.strokeLineJoin = BEVEL
        val areaGroup = Group(fillPath, seriesLine)
        series.node.value = areaGroup
        /* create series Y multiplier */
        val seriesYAnimMultiplier: DoubleProperty = SimpleDoubleProperty(this, "seriesYMultiplier")
        seriesYMultiplierMap[series] = seriesYAnimMultiplier
        /* handle any data already in series */
        if (shouldAnimate()) {
            seriesYAnimMultiplier.value = 0.0
        } else {
            seriesYAnimMultiplier.value = 1.0
        }
        plotChildren.add(areaGroup)
        val keyFrames: MutableList<KeyFrame> = ArrayList()
        if (shouldAnimate()) {
            /* animate in new series */
            keyFrames.add(
                KeyFrame(
                    Duration.ZERO,
                    KeyValue(areaGroup.opacityProperty(), 0, MyInterpolator.MY_DEFAULT_INTERPOLATOR),
                    KeyValue(seriesYAnimMultiplier, 0, MyInterpolator.MY_DEFAULT_INTERPOLATOR)
                )
            )
            keyFrames.add(
                KeyFrame(
                    Duration.millis(200.0),
                    KeyValue(areaGroup.opacityProperty(), 1, MyInterpolator.MY_DEFAULT_INTERPOLATOR)
                )
            )
            keyFrames.add(
                KeyFrame(
                    Duration.millis(500.0),
                    KeyValue(seriesYAnimMultiplier, 1, MyInterpolator.MY_DEFAULT_INTERPOLATOR)
                )
            )
        }
        for (j in series.data.value.indices) {
            val item = series.data.value[j]
            val symbol = createSymbol(series, seriesIndex, item, j)
            if (symbol != null) {
                if (shouldAnimate()) {
                    symbol.opacity = 0.0
                    plotChildren.add(symbol)
                    /* fade in new symbol */
                    keyFrames.add(
                        KeyFrame(
                            Duration.ZERO,
                            KeyValue(symbol.opacityProperty(), 0, MyInterpolator.MY_DEFAULT_INTERPOLATOR)
                        )
                    )
                    keyFrames.add(
                        KeyFrame(
                            Duration.millis(200.0),
                            KeyValue(symbol.opacityProperty(), 1, MyInterpolator.MY_DEFAULT_INTERPOLATOR)
                        )
                    )
                } else {
                    plotChildren.add(symbol)
                }
            }
        }
        if (shouldAnimate()) animate(*keyFrames.toTypedArray())
    }

    override fun seriesRemoved(series: Series<X, Y>) {
        /* remove series Y multiplier */
        seriesYMultiplierMap.remove(series)
        /* remove all symbol nodes */
        if (shouldAnimate()) {
            timeline = Timeline(*createSeriesRemoveTimeLine(series, 400))
            timeline!!.play()
        } else {
            plotChildren.remove(series.node.value)
            for (d in series.data.value) plotChildren.remove(d.nodeProp.value)
            removeSeriesFromDisplay(series)
        }
    }

    /** {@inheritDoc}  */
    override fun layoutPlotChildren() {
        val constructedPath: MutableList<LineTo> =
            ArrayList(
                dataSize
            )
        for (seriesIndex in 0 until dataSize) {
            val series = data.value[seriesIndex]
            val seriesYAnimMultiplier = seriesYMultiplierMap[series]
            val children = (series.node.value as Group).children
            val fillPath = children[0] as Path
            val linePath = children[1] as Path
            makePaths(
                this, series, constructedPath, fillPath, linePath,
                seriesYAnimMultiplier!!.get(), MorePerfOptionsLineChart.SortingPolicy.X_AXIS
            )
        }
    }


    override val seriesRemovalAnimation by ::timeline
    override fun nullifySeriesRemovalAnimation() {
        timeline = null
    }


    override val lineOrArea = LineOrArea.area

    protected object StyleableProperties : StyleableProps<AreaChartForPrivateProps<*, *>>()

    override val styleableProps get() = StyleableProperties
    override fun getCssMetaData() = StyleableProperties.classCssMetaData

    companion object {
        @Suppress("unused")
        private fun doubleValue(
            number: Number?,
            nullDefault: Double = 0.0
        ): Double = number?.toDouble() ?: nullDefault

        fun <X, Y> makePaths(
            chart: XYChartForPackagePrivateProps<X, Y>,
            series: Series<X, Y>,
            constructedPath: MutableList<LineTo>,
            fillPath: Path?,
            linePath: Path,
            yAnimMultiplier: Double,
            sortAxis: MorePerfOptionsLineChart.SortingPolicy
        ) {
            val axisX = chart.xAxis
            val axisY = chart.yAxis
            val hlw = linePath.strokeWidth / 2.0
            val sortX = sortAxis == MorePerfOptionsLineChart.SortingPolicy.X_AXIS
            val sortY = sortAxis == MorePerfOptionsLineChart.SortingPolicy.Y_AXIS
            val dataXMin = if (sortX) -hlw else Double.NEGATIVE_INFINITY
            val dataXMax = if (sortX) axisX.width + hlw else Double.POSITIVE_INFINITY
            val dataYMin = if (sortY) -hlw else Double.NEGATIVE_INFINITY
            val dataYMax = if (sortY) axisY.height + hlw else Double.POSITIVE_INFINITY
            var prevDataPoint: LineTo? = null
            var nextDataPoint: LineTo? = null
            constructedPath.clear()
            val it = chart.getDisplayedDataIterator(series)
            while (it.hasNext()) {
                val item = it.next()
                val x = axisX.getDisplayPosition(item.currentX.value)
                val y =
                    axisY.getDisplayPosition(
                        axisY.toRealValue(axisY.toNumericValue(item.currentY.value) * yAnimMultiplier)!!
                    )
                val skip = java.lang.Double.isNaN(x) || java.lang.Double.isNaN(y)
                val symbol = item.nodeProp.value
                if (symbol != null) {
                    val w = symbol.prefWidth(-1.0)
                    val h = symbol.prefHeight(-1.0)
                    if (skip) {
                        symbol.resizeRelocate(-w * 2, -h * 2, w, h)
                    } else {
                        symbol.resizeRelocate(x - w / 2, y - h / 2, w, h)
                    }
                }
                if (skip) {
                    continue
                }
                if (x < dataXMin || y < dataYMin) {
                    if (prevDataPoint == null) {
                        prevDataPoint = LineTo(x, y)
                    } else if (sortX && prevDataPoint.x <= x || sortY && prevDataPoint.y <= y) {
                        prevDataPoint.x = x
                        prevDataPoint.y = y
                    }
                } else if (x <= dataXMax && y <= dataYMax) {
                    constructedPath.add(LineTo(x, y))
                } else {
                    if (nextDataPoint == null) {
                        nextDataPoint = LineTo(x, y)
                    } else if (sortX && x < nextDataPoint.x || sortY && y < nextDataPoint.y) {
                        nextDataPoint.x = x
                        nextDataPoint.y = y
                    }
                }
            }
            if (constructedPath.isNotEmpty() || prevDataPoint != null || nextDataPoint != null) {
                if (sortX) {
                    constructedPath.sortWith { e1: LineTo, e2: LineTo ->
                        e1.x.compareTo(e2.x)
                    }
                } else if (sortY) {
                    constructedPath.sortWith { e1: LineTo, e2: LineTo ->
                        e1.y.compareTo(e2.y)
                    }
                } else {
                    /* assert prevDataPoint == null && nextDataPoint == null */
                }
                if (prevDataPoint != null) {
                    constructedPath.add(0, prevDataPoint)
                }
                if (nextDataPoint != null) {
                    constructedPath.add(nextDataPoint)
                }

                /* assert !constructedPath.isEmpty() */
                val first = constructedPath[0]
                val last = constructedPath[constructedPath.size - 1]
                val displayYPos = first.y
                val lineElements = linePath.elements
                lineElements.clear()
                lineElements.add(MoveTo(first.x, displayYPos))
                lineElements.addAll(constructedPath)
                if (fillPath != null) {
                    val fillElements = fillPath.elements
                    fillElements.clear()
                    val yOrigin = axisY.getDisplayPosition(axisY.toRealValue(0.0)!!)
                    fillElements.add(MoveTo(first.x, yOrigin))
                    fillElements.addAll(constructedPath)
                    fillElements.add(LineTo(last.x, yOrigin))
                    fillElements.add(ClosePath())
                }
            }
        }
    }
}
