package matt.fx.control.chart.stackedarea.stackedarea

import javafx.animation.FadeTransition
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.beans.NamedArg
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener.Change
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.shape.ClosePath
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.shape.StrokeLineJoin.BEVEL
import javafx.util.Duration
import matt.fx.control.chart.axis.value.axis.AxisForPackagePrivateProps
import matt.fx.control.chart.axis.value.moregenval.MoreGenericValueAxis
import matt.fx.control.chart.axis.value.number.moregennum.MoreGenericNumberAxis
import matt.fx.control.chart.linelike.LineLikeChartNodeWithOptionalSymbols
import matt.fx.control.chart.stackedarea.stackedarea.StackedAreaChartForWrapper.PartOf.CURRENT
import matt.fx.control.chart.stackedarea.stackedarea.StackedAreaChartForWrapper.PartOf.PREVIOUS
import matt.model.data.mathable.DoubleWrapper
import java.util.Collections
import java.util.NavigableMap
import java.util.TreeMap
import kotlin.math.min

class StackedAreaChartForWrapper<X : Any, Y : Any> @JvmOverloads constructor(
    @NamedArg("xAxis") xAxis: AxisForPackagePrivateProps<X>,
    @NamedArg("yAxis") yAxis: AxisForPackagePrivateProps<Y>,
    @NamedArg("data") data: ObservableList<Series<X, Y>> = FXCollections.observableArrayList()
) : LineLikeChartNodeWithOptionalSymbols<X, Y>(xAxis, yAxis) {
    /** A multiplier for teh Y values that we store for each series, it is used to animate in a new series  */
    private val seriesYMultiplierMap: MutableMap<Series<X, Y>, DoubleProperty> = HashMap()
    private var timeline: Timeline? = null
    // -------------- PUBLIC PROPERTIES ----------------------------------------


    /**
     * Construct a new Area Chart with the given axis and data.
     *
     *
     * Note: yAxis must be a ValueAxis, otherwise `IllegalArgumentException` is thrown.
     *
     * @param xAxis The x axis to use
     * @param yAxis The y axis to use
     * @param data The data to use, this is the actual list used so any changes to it will be reflected in the chart
     *
     * @throws java.lang.IllegalArgumentException if yAxis is not a ValueAxis
     */
    // -------------- CONSTRUCTORS ----------------------------------------------
    /**
     * Construct a new Area Chart with the given axis
     *
     * @param xAxis The x axis to use
     * @param yAxis The y axis to use
     */
    init {
        require(yAxis is MoreGenericValueAxis<*>) { "Axis type incorrect, yAxis must be of ValueAxis type." }
        setData(data)
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
                val x1 = xAxis.toNumericValue(p1.xValue)
                val y1 = yAxis.toNumericValue(p1.yValue)
                val x3 = xAxis.toNumericValue(p2.xValue)
                val y3 = yAxis.toNumericValue(p2.yValue)
                val x2 = xAxis.toNumericValue(item.xValue)
                @Suppress("UNUSED_VARIABLE") val y2 = yAxis.toNumericValue(item.yValue)

//                //1. y intercept of the line : y = ((y3-y1)/(x3-x1)) * x2 + (x3y1 - y3x1)/(x3 -x1)
                val y = (y3 - y1) / (x3 - x1) * x2 + (x3 * y1 - y3 * x1) / (x3 - x1)
                item.currentY.value = yAxis.toRealValue(y)
                item.setCurrentX(xAxis.toRealValue(x2)!!)
                //2. we can simply use the midpoint on the line as well..
//                double x = (x3 + x1)/2;
//                double y = (y3 + y1)/2;
//                item.setCurrentX(x);
//                item.setCurrentY(y);
            } else if (itemIndex == 0 && series.data.value.size > 1) {
                animate = true
                item.currentX.value = series.data.value[1].xValue
                item.setCurrentY(series.data.value[1].yValue)
            } else if (itemIndex == series.data.value.size - 1 && series.data.value.size > 1) {
                animate = true
                val last = series.data.value.size - 2
                item.currentX.value = series.data.value[last].xValue
                item.setCurrentY(series.data.value[last].yValue)
            } else if (symbol != null) {
                // fade in new symbol
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
                            item.currentY.value
                        ),
                        KeyValue(
                            item.currentXProperty(),
                            item.currentX.value
                        )
                    ),
                    KeyFrame(
                        Duration.millis(800.0), KeyValue(
                            item.currentYProperty(),
                            item.yValue, Interpolator.EASE_BOTH
                        ),
                        KeyValue(
                            item.currentXProperty(),
                            item.xValue, Interpolator.EASE_BOTH
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
        val symbol = item.node
        symbol?.focusTraversableProperty()?.unbind()

        // remove item from sorted list
        val itemIndex = series.getItemIndex(item)
        if (shouldAnimate()) {
            var animate = false
            // dataSize represents size of currently visible data. After this operation, the number will decrement by 1
            val dataSize = series.dataSize
            // This is the size of current data list in Series. Note that it might be totaly different from dataSize as
            // some big operation might have happened on the list.
            val dataListSize = series.data.value.size
            if (itemIndex > 0 && itemIndex < dataSize - 1) {
                animate = true
                val p1 = series.getItem(itemIndex - 1)
                val p2 = series.getItem(itemIndex + 1)
                val x1 = xAxis.toNumericValue(p1!!.xValue)
                val y1 = yAxis.toNumericValue(p1.yValue)
                val x3 = xAxis.toNumericValue(p2!!.xValue)
                val y3 = yAxis.toNumericValue(p2.yValue)
                val x2 = xAxis.toNumericValue(item.xValue)
                val y2 = yAxis.toNumericValue(item.yValue)

//                //1.  y intercept of the line : y = ((y3-y1)/(x3-x1)) * x2 + (x3y1 - y3x1)/(x3 -x1)
                val y = (y3 - y1) / (x3 - x1) * x2 + (x3 * y1 - y3 * x1) / (x3 - x1)
                item.currentX.value = xAxis.toRealValue(x2)
                item.currentY.value = yAxis.toRealValue(y2)
                item.xValue = xAxis.toRealValue(x2)!!
                item.yValue = (yAxis.toRealValue(y)!!)
                //2.  we can simply use the midpoint on the line as well..
//                double x = (x3 + x1)/2;
//                double y = (y3 + y1)/2;
//                item.setCurrentX(x);
//                item.setCurrentY(y);
            } else if (itemIndex == 0 && dataListSize > 1) {
                animate = true
                item.xValue = (series.data.value[0].xValue)
                item.yValue = (series.data.value[0].yValue)
            } else if (itemIndex == dataSize - 1 && dataListSize > 1) {
                animate = true
                val last = dataListSize - 1
                item.xValue = (series.data.value[last].xValue)
                item.yValue = (series.data.value[last].yValue)
            } else if (symbol != null) {
                // fade out symbol
                symbol.opacity = 0.0
                val ft = FadeTransition(Duration.millis(500.0), symbol)
                ft.toValue = 0.0
                ft.onFinished = EventHandler {
                    plotChildren.remove(symbol)
                    removeDataItemFromDisplay(series, item)
                    symbol.opacity = 1.0
                }
                ft.play()
            } else {
                item.setSeries(null)
                removeDataItemFromDisplay(series, item)
            }
            if (animate) {
                animate(
                    KeyFrame(
                        Duration.ZERO, KeyValue(
                            item.currentYProperty(),
                            item.currentY.value
                        ), KeyValue(
                            item.currentXProperty(),
                            item.currentX.value
                        )
                    ),
                    KeyFrame(
                        Duration.millis(800.0), {
                            plotChildren.remove(symbol)
                            removeDataItemFromDisplay(series, item)
                        },
                        KeyValue(
                            item.currentYProperty(),
                            item.yValue, Interpolator.EASE_BOTH
                        ),
                        KeyValue(
                            item.currentXProperty(),
                            item.xValue, Interpolator.EASE_BOTH
                        )
                    )
                )
            }
        } else {
            plotChildren.remove(symbol)
            removeDataItemFromDisplay(series, item)
        }
        //Note: better animation here, point should move from old position to new position at center point between prev and next symbols
    }


    override fun seriesChanged(c: Change<out Series<*, *>>) {
        // Update style classes for all series lines and symbols
        for (i in 0..<dataSize) {
            val s = data.value[i]
            val seriesLine = (s.node.value as Group).children[1] as Path
            val fillPath = (s.node.value as Group).children[0] as Path
            seriesLine.styleClass.setAll("chart-series-area-line", "series$i", s.defaultColorStyleClass)
            fillPath.styleClass.setAll("chart-series-area-fill", "series$i", s.defaultColorStyleClass)
            for (j in s.data.value.indices) {
                val item = s.data.value[j]
                val node = item.node
                node?.styleClass?.setAll("chart-area-symbol", "series$i", "data$j", s.defaultColorStyleClass)
            }
        }
    }

    override fun seriesAdded(
        series: Series<X, Y>,
        seriesIndex: Int
    ) {
        // create new paths for series
        val seriesLine = Path()
        val fillPath = Path()
        seriesLine.strokeLineJoin = BEVEL
        fillPath.strokeLineJoin = BEVEL
        val areaGroup = Group(fillPath, seriesLine)
        series.node.value = areaGroup
        // create series Y multiplier
        val seriesYAnimMultiplier: DoubleProperty = SimpleDoubleProperty(this, "seriesYMultiplier")
        seriesYMultiplierMap[series] = seriesYAnimMultiplier
        // handle any data already in series
        if (shouldAnimate()) {
            seriesYAnimMultiplier.value = 0.0
        } else {
            seriesYAnimMultiplier.value = 1.0
        }
        plotChildren.add(areaGroup)
        val keyFrames: MutableList<KeyFrame> = ArrayList()
        if (shouldAnimate()) {
            // animate in new series
            keyFrames.add(
                KeyFrame(
                    Duration.ZERO,
                    KeyValue(areaGroup.opacityProperty(), 0),
                    KeyValue(seriesYAnimMultiplier, 0)
                )
            )
            keyFrames.add(
                KeyFrame(
                    Duration.millis(200.0),
                    KeyValue(areaGroup.opacityProperty(), 1)
                )
            )
            keyFrames.add(
                KeyFrame(
                    Duration.millis(500.0),
                    KeyValue(seriesYAnimMultiplier, 1)
                )
            )
        }
        for (j in series.data.value.indices) {
            val item = series.data.value[j]
            val symbol = createSymbol(series, seriesIndex, item, j)
            if (symbol != null) {
                if (shouldAnimate()) symbol.opacity = 0.0
                plotChildren.add(symbol)
                if (shouldAnimate()) {
                    // fade in new symbol
                    keyFrames.add(KeyFrame(Duration.ZERO, KeyValue(symbol.opacityProperty(), 0)))
                    keyFrames.add(KeyFrame(Duration.millis(200.0), KeyValue(symbol.opacityProperty(), 1)))
                }
            }
        }
        if (shouldAnimate()) animate(*keyFrames.toTypedArray<KeyFrame>())
    }

    override fun seriesRemoved(series: Series<X, Y>) {
        // remove series Y multiplier
        seriesYMultiplierMap.remove(series)
        // remove all symbol nodes
        if (shouldAnimate()) {
            timeline = Timeline(*createSeriesRemoveTimeLine(series, 400))
            timeline!!.play()
        } else {
            plotChildren.remove(series.node.value)
            for (d in series.data.value) plotChildren.remove(d.node)
            removeSeriesFromDisplay(series)
        }
    }

    /** {@inheritDoc}  */
    override fun updateAxisRange() {
        // This override is necessary to update axis range based on cumulative Y value for the
        // Y axis instead of the normal way where max value in the data range is used.
        val xa = xAxis
        val ya = yAxis
        if (xa.isAutoRanging()) {
            val xData = ArrayList<X>()
            for (series in data.value) {
                for (data in series.data.value) {
                    xData.add(data.xValue)
                }
            }
            xa.invalidateRange(xData)
        }
        if (ya.isAutoRanging()) {
            var totalMinY = Double.MAX_VALUE
            val seriesIterator =
                displayedSeriesIterator
            var first = true
            val accum: NavigableMap<Double, Double> = TreeMap()
            val prevAccum: NavigableMap<Double, Double> = TreeMap()
            val currentValues: NavigableMap<Double, Double> = TreeMap()
            while (seriesIterator.hasNext()) {
                currentValues.clear()
                val series = seriesIterator.next()
                for (item in series.data.value) {
                    if (item != null) {
                        val xv = xa.toNumericValue(item.xValue)
                        val yv = ya.toNumericValue(item.yValue)
                        currentValues[xv] = yv
                        if (first) {
                            // On the first pass, just fill the map
                            accum[xv] = yv
                            // minimum is applicable only in the first series
                            totalMinY = min(totalMinY, yv)
                        } else {
                            if (prevAccum.containsKey(xv)) {
                                accum[xv] = prevAccum[xv]!! + yv
                            } else {
                                // If the point wasn't yet in the previous (accumulated) series
                                val he = prevAccum.higherEntry(xv)
                                val le = prevAccum.lowerEntry(xv)
                                if (he != null && le != null) {
                                    // If there's both point above and below this point, interpolate
                                    accum[xv] = (xv - le.key) / (he.key - le.key) *
                                        (le.value + he.value) + yv
                                } else if (he != null) {
                                    // The point is before the first point in the previously accumulated series
                                    accum[xv] = he.value + yv
                                } else if (le != null) {
                                    // The point is after the last point in the previously accumulated series
                                    accum[xv] = le.value + yv
                                } else {
                                    // The previously accumulated series is empty
                                    accum[xv] = yv
                                }
                            }
                        }
                    }
                }
                // Now update all the keys that were in the previous series, but not in the new one
                for ((k, v) in prevAccum) {
                    if (accum.keys.contains(k)) {
                        continue
                    }
                    // Look at the values of the current series
                    val he = currentValues.higherEntry(k)
                    val le = currentValues.lowerEntry(k)
                    if (he != null && le != null) {
                        // Interpolate the for the point from current series and add the accumulated value
                        accum[k] = (k - le.key) / (he.key - le.key) *
                            (le.value + he.value) + v
                    } else if (he != null) {
                        // There accumulated value is before the first value in the current series
                        accum[k] = he.value + v
                    } else if (le != null) {
                        // There accumulated value is after the last value in the current series
                        accum[k] = le.value + v
                    } else {
                        // The current series are empty
                        accum[k] = v
                    }
                }
                prevAccum.clear()
                prevAccum.putAll(accum)
                accum.clear()
                first =
                    totalMinY == Double.MAX_VALUE // If there was already some value in the series, we can consider as
                // being past the first series
            }
            if (totalMinY != Double.MAX_VALUE) ya.invalidateRange(
                java.util.Arrays.asList(
                    ya.toRealValue(totalMinY)!!,
                    ya.toRealValue(Collections.max(prevAccum.values))!!
                )
            )
        }
    }

    /** {@inheritDoc}  */
    override fun layoutPlotChildren() {
        val currentSeriesData = ArrayList<DataPointInfo<X, Y>>()
        // AggregateData hold the data points of both the current and the previous series.
        // The goal is to collect all the data, sort it and iterate.
        val aggregateData = ArrayList<DataPointInfo<X, Y>>()

        for (seriesIndex in 0..<dataSize) { // for every series
            val series = data.value[seriesIndex]
            aggregateData.clear()
            // copy currentSeriesData accumulated in the previous iteration to aggregate.
            for (data in currentSeriesData) {
                data.partOf = PREVIOUS
                aggregateData.add(data)
            }
            currentSeriesData.clear()
            // now copy actual data of the current series.
            val it = getDisplayedDataIterator(series)
            while (it.hasNext()) {
                val item = it.next()
                val itemInfo = DataPointInfo(
                    item, item.xValue,
                    item.yValue, CURRENT
                )
                aggregateData.add(itemInfo)
            }
            val seriesYAnimMultiplier = seriesYMultiplierMap[series]
            val seriesLine = (series.node.value as Group).children[1] as Path
            val fillPath = (series.node.value as Group).children[0] as Path
            seriesLine.elements.clear()
            fillPath.elements.clear()
            var dataIndex = 0
            // Sort data points from prev and current series
            sortAggregateList(aggregateData)
            val yAxis = yAxis
            val xAxis = xAxis
            var firstCurrent = false
            var lastCurrent = false
            val firstCurrentIndex = findNextCurrent(aggregateData, -1)
            val lastCurrentIndex = findPreviousCurrent(aggregateData, aggregateData.size)
            var basePosition = yAxis.zeroPosition
            if (java.lang.Double.isNaN(basePosition)) {
                val valueYAxis = yAxis as MoreGenericNumberAxis<Y>
                basePosition = if ((valueYAxis.lowerBound.value as DoubleWrapper<*>).asDouble > 0) {
                    valueYAxis.getDisplayPosition(valueYAxis.lowerBound.value)
                } else {
                    valueYAxis.getDisplayPosition(valueYAxis.upperBound.value)
                }
            }
            // Iterate over the aggregate data : this process accumulates data points
            // cumulatively from the bottom to top of stack
            for (dataInfo in aggregateData) {
                if (dataIndex == lastCurrentIndex) lastCurrent = true
                if (dataIndex == firstCurrentIndex) firstCurrent = true
                val item = dataInfo.dataItem
                if (dataInfo.partOf == CURRENT) { // handle data from current series
                    var pIndex = findPreviousPrevious(aggregateData, dataIndex)
                    val nIndex = findNextPrevious(aggregateData, dataIndex)
                    var prevPoint: DataPointInfo<X, Y>?
                    var nextPoint: DataPointInfo<X, Y>?
                    if (pIndex == -1 || nIndex == -1 && aggregateData[pIndex].x != dataInfo.x) {
                        if (firstCurrent) {
                            // Need to add the drop down point.
                            val ddItem: Data<X, Y> = Data(dataInfo.x!!, yAxis.toRealValue(0.0)!!)
                            addDropDown(
                                currentSeriesData, ddItem, ddItem.xValue, ddItem.yValue,
                                xAxis.getDisplayPosition(ddItem.currentX.value), basePosition
                            )
                        }
                        val x = xAxis.getDisplayPosition(item!!.currentX.value)
                        val y = yAxis.getDisplayPosition(
                            yAxis.toRealValue(yAxis.toNumericValue(item.currentY.value!!) * seriesYAnimMultiplier!!.value!!)!!
                        )
                        addPoint(
                            currentSeriesData, item, item.xValue, item.yValue, x, y,
                            CURRENT, false, if (firstCurrent) false else true
                        )
                        if (dataIndex == lastCurrentIndex) {
                            // need to add drop down point
                            val ddItem: Data<X, Y> = Data(dataInfo.x!!, yAxis.toRealValue(0.0)!!)
                            addDropDown(
                                currentSeriesData, ddItem, ddItem.xValue, ddItem.yValue,
                                xAxis.getDisplayPosition(ddItem.currentX.value), basePosition
                            )
                        }
                    } else {
                        prevPoint = aggregateData[pIndex]
                        if (prevPoint.x == dataInfo.x) { // Need to add Y values
                            // Check if prevPoint is a dropdown - as the stable sort preserves the order.
                            // If so, find the non dropdown previous point on previous series.
                            if (prevPoint.dropDown) {
                                pIndex = findPreviousPrevious(aggregateData, pIndex)
                                prevPoint = aggregateData[pIndex]
                                // If lastCurrent - add this drop down
                            }
                            if (prevPoint.x == dataInfo.x) { // simply add
                                val x = xAxis.getDisplayPosition(item!!.currentX.value)
                                val yv = yAxis.toNumericValue(item.currentY.value) + yAxis.toNumericValue(prevPoint.y!!)
                                val y = yAxis.getDisplayPosition(
                                    yAxis.toRealValue(yv * seriesYAnimMultiplier!!.value)!!
                                )
                                addPoint(
                                    currentSeriesData,
                                    item,
                                    dataInfo.x!!,
                                    yAxis.toRealValue(yv)!!,
                                    x,
                                    y,
                                    CURRENT,
                                    false,
                                    if (firstCurrent) false else true
                                )
                            }
                            if (lastCurrent) {
                                addDropDown(
                                    currentSeriesData,
                                    item,
                                    prevPoint.x!!,
                                    prevPoint.y!!,
                                    prevPoint.displayX,
                                    prevPoint.displayY
                                )
                            }
                        } else {
                            // interpolate
                            nextPoint = if (nIndex == -1) null else aggregateData[nIndex]
                            prevPoint = if (pIndex == -1) null else aggregateData[pIndex]
                            val yValue = yAxis.toNumericValue(item!!.currentY.value)
                            if (prevPoint != null && nextPoint != null) {
                                val x = xAxis.getDisplayPosition(item.currentX.value)
                                val displayY = interpolate(
                                    prevPoint.displayX,
                                    prevPoint.displayY, nextPoint.displayX, nextPoint.displayY, x
                                )
                                val dataY = interpolate(
                                    xAxis.toNumericValue(prevPoint.x!!),
                                    yAxis.toNumericValue(prevPoint.y!!),
                                    xAxis.toNumericValue(nextPoint.x!!),
                                    yAxis.toNumericValue(nextPoint.y!!),
                                    xAxis.toNumericValue(dataInfo.x!!)
                                )
                                if (firstCurrent) {
                                    // now create the drop down point
                                    val ddItem: Data<X, Y> = Data(dataInfo.x!!, yAxis.toRealValue(dataY)!!)
                                    addDropDown(
                                        currentSeriesData,
                                        ddItem,
                                        dataInfo.x!!,
                                        yAxis.toRealValue(dataY)!!,
                                        x,
                                        displayY
                                    )
                                }
                                val y =
                                    yAxis.getDisplayPosition(yAxis.toRealValue((yValue + dataY) * seriesYAnimMultiplier!!.value)!!)
                                // Add the current point
                                addPoint(
                                    currentSeriesData,
                                    item,
                                    dataInfo.x!!,
                                    yAxis.toRealValue(yValue + dataY)!!,
                                    x,
                                    y,
                                    CURRENT,
                                    false,
                                    if (firstCurrent) false else true
                                )
                                if (dataIndex == lastCurrentIndex) {
                                    // add drop down point
                                    val ddItem: Data<X, Y> = Data(dataInfo.x!!, yAxis.toRealValue(dataY)!!)
                                    addDropDown(
                                        currentSeriesData,
                                        ddItem,
                                        dataInfo.x!!,
                                        yAxis.toRealValue(dataY)!!,
                                        x,
                                        displayY
                                    )
                                }
                                // Note: add drop down if last current
                            } else {
                                // we do not need to take care of this as it is
                                // already handled above with check of if(pIndex == -1 or nIndex == -1)
                            }
                        }
                    }
                } else { // handle data from Previous series.
                    val pIndex = findPreviousCurrent(aggregateData, dataIndex)
                    val nIndex = findNextCurrent(aggregateData, dataIndex)
                    var prevPoint: DataPointInfo<X, Y>
                    var nextPoint: DataPointInfo<X, Y>
                    if (dataInfo.dropDown) {
                        if (xAxis.toNumericValue(dataInfo.x!!) <=
                            xAxis.toNumericValue(aggregateData[firstCurrentIndex].x!!) ||
                            xAxis.toNumericValue(dataInfo.x!!) > xAxis.toNumericValue(aggregateData[lastCurrentIndex].x!!)
                        ) {
                            addDropDown(
                                currentSeriesData,
                                item,
                                dataInfo.x!!,
                                dataInfo.y!!,
                                dataInfo.displayX,
                                dataInfo.displayY
                            )
                        }
                    } else {
                        if (pIndex == -1 || nIndex == -1) {
                            addPoint(
                                currentSeriesData,
                                item!!,
                                dataInfo.x!!,
                                dataInfo.y!!,
                                dataInfo.displayX,
                                dataInfo.displayY,
                                CURRENT,
                                true,
                                false
                            )
                        } else {
                            nextPoint = aggregateData[nIndex]
                            if (nextPoint.x == dataInfo.x) {
                                // do nothing as the current point is already there.
                            } else {
                                // interpolate on the current series.
                                prevPoint = aggregateData[pIndex]
                                val x = xAxis.getDisplayPosition(item!!.currentX.value)
                                val dataY = interpolate(
                                    xAxis.toNumericValue(prevPoint.x!!),
                                    yAxis.toNumericValue(prevPoint.y!!),
                                    xAxis.toNumericValue(nextPoint.x!!),
                                    yAxis.toNumericValue(nextPoint.y!!),
                                    xAxis.toNumericValue(dataInfo.x!!)
                                )
                                val yv = yAxis.toNumericValue(dataInfo.y!!) + dataY
                                val y = yAxis.getDisplayPosition(
                                    yAxis.toRealValue(yv * seriesYAnimMultiplier!!.value)!!
                                )
                                addPoint(
                                    currentSeriesData,
                                    Data(dataInfo.x!!, yAxis.toRealValue(dataY)!!),
                                    dataInfo.x!!,
                                    yAxis.toRealValue(yv)!!,
                                    x,
                                    y,
                                    CURRENT,
                                    true,
                                    true
                                )
                            }
                        }
                    }
                }
                dataIndex++
                if (firstCurrent) firstCurrent = false
                if (lastCurrent) lastCurrent = false
            } // end of inner for loop

            // Draw the SeriesLine and Series fill
            if (!currentSeriesData.isEmpty()) {
                seriesLine.elements.add(MoveTo(currentSeriesData[0].displayX, currentSeriesData[0].displayY))
                fillPath.elements.add(MoveTo(currentSeriesData[0].displayX, currentSeriesData[0].displayY))
            }
            for (point in currentSeriesData) {
                if (point.lineTo) {
                    seriesLine.elements.add(LineTo(point.displayX, point.displayY))
                } else {
                    seriesLine.elements.add(MoveTo(point.displayX, point.displayY))
                }
                fillPath.elements.add(LineTo(point.displayX, point.displayY))
                // draw symbols only for actual data points and skip for interpolated points.
                if (!point.skipSymbol) {
                    val symbol = point.dataItem!!.node
                    if (symbol != null) {
                        val w = symbol.prefWidth(-1.0)
                        val h = symbol.prefHeight(-1.0)
                        symbol.resizeRelocate(point.displayX - w / 2, point.displayY - h / 2, w, h)
                    }
                }
            }
            for (i in aggregateData.size - 1 downTo 1) {
                val point = aggregateData[i]
                if (PREVIOUS == point.partOf) {
                    fillPath.elements.add(LineTo(point.displayX, point.displayY))
                }
            }
            if (!fillPath.elements.isEmpty()) {
                fillPath.elements.add(ClosePath())
            }
        } // end of out for loop
    }

    private fun addDropDown(
        currentSeriesData: ArrayList<DataPointInfo<X, Y>>,
        item: Data<X, Y>?,
        xValue: X,
        yValue: Y,
        x: Double,
        y: Double
    ) {
        val dropDownDataPoint = DataPointInfo<X, Y>(true)
        dropDownDataPoint.setValues(item, xValue, yValue, x, y, CURRENT, true, false)
        currentSeriesData.add(dropDownDataPoint)
    }

    private fun addPoint(
        currentSeriesData: ArrayList<DataPointInfo<X, Y>>,
        item: Data<X, Y>,
        xValue: X,
        yValue: Y,
        x: Double,
        y: Double,
        partOf: PartOf,
        symbol: Boolean,
        lineTo: Boolean
    ) {
        val currentDataPoint = DataPointInfo<X, Y>()
        currentDataPoint.setValues(item, xValue, yValue, x, y, partOf, symbol, lineTo)
        currentSeriesData.add(currentDataPoint)
    }

    override val seriesRemovalAnimation by ::timeline
    override fun nullifySeriesRemovalAnimation() {
        timeline = null
    }


    //-------------------- helper methods to retrieve data points from the previous
    // or current data series.
    private fun findNextCurrent(
        points: ArrayList<DataPointInfo<X, Y>>,
        index: Int
    ): Int {
        for (i in index + 1..<points.size) {
            if (points[i].partOf == CURRENT) {
                return i
            }
        }
        return -1
    }

    private fun findPreviousCurrent(
        points: ArrayList<DataPointInfo<X, Y>>,
        index: Int
    ): Int {
        for (i in index - 1 downTo 0) {
            if (points[i].partOf == CURRENT) {
                return i
            }
        }
        return -1
    }

    private fun findPreviousPrevious(
        points: ArrayList<DataPointInfo<X, Y>>,
        index: Int
    ): Int {
        for (i in index - 1 downTo 0) {
            if (points[i].partOf == PREVIOUS) {
                return i
            }
        }
        return -1
    }

    private fun findNextPrevious(
        points: ArrayList<DataPointInfo<X, Y>>,
        index: Int
    ): Int {
        for (i in index + 1..<points.size) {
            if (points[i].partOf == PREVIOUS) {
                return i
            }
        }
        return -1
    }

    private fun sortAggregateList(aggregateList: ArrayList<DataPointInfo<X, Y>>) {
        Collections.sort(aggregateList,
            Comparator { o1: DataPointInfo<X, Y>, o2: DataPointInfo<X, Y> ->
                val d1 = o1.dataItem
                val d2 = o2.dataItem
                val val1 = xAxis.toNumericValue(d1!!.xValue)
                val val2 = xAxis.toNumericValue(d2!!.xValue)
                if (val1 < val2) -1 else if (val1 == val2) 0 else 1
            })
    }

    private fun interpolate(
        lowX: Double,
        lowY: Double,
        highX: Double,
        highY: Double,
        x: Double
    ): Double {
        // using y = mx+c find the y for the given x.
        return (highY - lowY) / (highX - lowX) * (x - lowX) + lowY
    }


    override val lineOrArea = LineOrArea.area

    // -------------- INNER CLASSES --------------------------------------------
    /*
     * Helper class to hold data and display and other information for each
     * data point
     */
    internal class DataPointInfo<X, Y> {
        var x: X? = null
        var y: Y? = null
        var displayX = 0.0
        var displayY = 0.0
        var dataItem: Data<X, Y>? = null
        var partOf: PartOf? = null
        var skipSymbol = false // interpolated point - skip drawing symbol
        var lineTo = false // should there be a lineTo to this point on SeriesLine.
        var dropDown = false // Is this a drop down point ( non data point).

        //----- Constructors --------------------
        constructor()
        constructor(
            item: Data<X, Y>?,
            x: X,
            y: Y,
            partOf: PartOf?
        ) {
            dataItem = item
            this.x = x
            this.y = y
            this.partOf = partOf
        }

        constructor(dropDown: Boolean) {
            this.dropDown = dropDown
        }

        fun setValues(
            item: Data<X, Y>?,
            x: X,
            y: Y,
            dx: Double,
            dy: Double,
            partOf: PartOf?,
            skipSymbol: Boolean,
            lineTo: Boolean
        ) {
            dataItem = item
            this.x = x
            this.y = y
            displayX = dx
            displayY = dy
            this.partOf = partOf
            this.skipSymbol = skipSymbol
            this.lineTo = lineTo
        }
    }

    // To indicate if the data point belongs to the current or the previous series.
    internal enum class PartOf {
        CURRENT,
        PREVIOUS
    }

    protected object StyleableProperties : StyleableProps<StackedAreaChartForWrapper<*, *>>()

    override val styleableProps get() = StyleableProperties
    override fun getCssMetaData() = StyleableProperties.classCssMetaData

    companion object {
        // -------------- METHODS ------------------------------------------------------------------------------------------
        private fun doubleValue(
            number: Number?,
            nullDefault: Double = 0.0
        ): Double = number?.toDouble() ?: nullDefault
    }
}
