package matt.fx.control.chart.stackedbar.stackedb

import com.sun.javafx.charts.Legend.LegendItem
import javafx.animation.FadeTransition
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.ParallelTransition
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.NamedArg
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.css.CssMetaData
import javafx.css.PseudoClass
import javafx.css.Styleable
import javafx.css.StyleableDoubleProperty
import javafx.css.StyleableProperty
import javafx.css.converter.SizeConverter
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.geometry.Orientation.HORIZONTAL
import javafx.geometry.Orientation.VERTICAL
import javafx.scene.AccessibleRole.TEXT
import javafx.scene.Node
import javafx.scene.chart.StackedBarChart
import javafx.scene.layout.StackPane
import javafx.util.Duration
import matt.fx.base.rewrite.ReWrittenFxClass
import matt.fx.control.chart.axis.cat.cat.CategoryAxisForCatAxisWrapper
import matt.fx.control.chart.axis.value.axis.AxisForPackagePrivateProps
import matt.fx.control.chart.axis.value.moregenval.MoreGenericValueAxis
import matt.fx.control.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps
import matt.fx.control.chart.stackedbar.stackedb.StackedBarChartForWrapper.StyleableProperties.classCssMetaData
import matt.fx.graphics.anim.interp.MyInterpolator
import matt.reflect.j.CastedList
import java.util.Collections

/**
 * StackedBarChart is a variation of [BarChart] that plots bars indicating
 * data values for a category. The bars can be vertical or horizontal depending
 * on which axis is a category axis.
 * The bar for each series is stacked on top of the previous series.
 * @since JavaFX 2.1
 */
@ReWrittenFxClass(StackedBarChart::class)
class StackedBarChartForWrapper<X, Y> @JvmOverloads constructor(
    @NamedArg("xAxis") xAxis: AxisForPackagePrivateProps<X>,
    @NamedArg("yAxis") yAxis: AxisForPackagePrivateProps<Y>,
    @NamedArg("data")
    data: ObservableList<Series<X, Y>> = FXCollections.observableArrayList()
) :
    XYChartForPackagePrivateProps<X, Y>(xAxis, yAxis) {
        private val seriesCategoryMap: MutableMap<Series<X, Y>, MutableMap<String?, MutableList<Data<X, Y>>>> = HashMap()
        private var orientation: Orientation? = null
        private var categoryAxis: CategoryAxisForCatAxisWrapper? = null
        private var valueAxis: MoreGenericValueAxis<*>? = null
        private var parallelTransition: ParallelTransition? = null

        /* RT-23125 handling data removal when a category is removed. */
        private val categoriesListener =
            ListChangeListener<String> { c ->
                while (c.next()) {
                    for (cat in c.removed) {
                        for (series in getData()) {
                            for (data2 in series.data.value) {
                                if (cat == if (orientation == VERTICAL) data2.xValueProp.value else data2.yValueProp.value) {
                                    val animatedOn = animated.value
                                    setAnimated(false)
                                    dataItemRemoved(data2, series)
                                    setAnimated(animatedOn)
                                }
                            }
                        }
                        requestChartLayout()
                    }
                }
            }
        /** The gap to leave between bars in separate categories  */
        private val categoryGap: StyleableDoubleProperty =
            object : StyleableDoubleProperty(10.0) {
                override fun invalidated() {
                    get()
                    requestChartLayout()
                }

                override fun getBean(): Any = this@StackedBarChartForWrapper

                override fun getName(): String = "categoryGap"

                override fun getCssMetaData(): CssMetaData<StackedBarChartForWrapper<*, *>, Number> = StyleableProperties.CATEGORY_GAP
            }

        fun getCategoryGap(): Double = categoryGap.value

        fun setCategoryGap(value: Double) {
            categoryGap.value = value
        }

        fun categoryGapProperty(): StyleableDoubleProperty = categoryGap

        /**
         * Construct a new StackedBarChart with the given axis and data. The two axis should be a ValueAxis/NumberAxis and a
         * CategoryAxis, they can be in either order depending on if you want a horizontal or vertical bar chart.
         *
         * @param xAxis The x axis to use
         * @param yAxis The y axis to use
         * @param data The data to use, this is the actual list used so any changes to it will be reflected in the chart
         * @param categoryGap The gap to leave between bars in separate categories
         */
        @Suppress("unused")
        constructor(
            @NamedArg("xAxis") xAxis: AxisForPackagePrivateProps<X>,
            @NamedArg("yAxis") yAxis: AxisForPackagePrivateProps<Y>,
            @NamedArg("data") data: ObservableList<Series<X, Y>>,
            @NamedArg("categoryGap") categoryGap: Double
        ) : this(xAxis, yAxis) {
            setData(data)
            setCategoryGap(categoryGap)
        }

        override fun dataItemAdded(
            series: Series<X, Y>,
            itemIndex: Int,
            item: Data<X, Y>
        ) {
            val category: String?
            category =
                if (orientation == VERTICAL) {
                    item.xValueProp.value as String?
                } else {
                    item.yValueProp.value as String?
                }
            /*
            Don't plot if category does not already exist ?
            if (!categoryAxis.getCategories().contains(category)) return;
             */
            var categoryMap = seriesCategoryMap[series]
            if (categoryMap == null) {
                categoryMap = HashMap()
                seriesCategoryMap[series] = categoryMap
            }
            /* list to hold more that one bar "positive and negative" */
            val itemList: MutableList<Data<X, Y>>? =
                if (categoryMap[category] != null) categoryMap[category] else ArrayList<Data<X, Y>>()
            itemList!!.add(item)
            categoryMap[category] = itemList
            val bar = createBar(series, data.value.indexOf(series), item, itemIndex)
            if (shouldAnimate()) {
                animateDataAdd(item, bar)
            } else {
                plotChildren.add(bar)
            }
        }

        override fun dataItemRemoved(
            item: Data<X, Y>,
            series: Series<X, Y>
        ) {
            val bar = item.nodeProp.value
            bar?.focusTraversableProperty()?.unbind()
            if (shouldAnimate()) {
                val t = createDataRemoveTimeline(item, bar, series)
                t.onFinished =
                    EventHandler {
                        removeDataItemFromDisplay(
                            series, item
                        )
                    }
                t.play()
            } else {
                processDataRemove(series, item)
                removeDataItemFromDisplay(series, item)
            }
        }

        /** {@inheritDoc}  */
        override fun dataItemChanged(item: Data<X, Y>) {
            val barVal: Double
            val currentVal: Double
            if (orientation == VERTICAL) {
                barVal = (item.yValueProp.value as Number?)!!.toDouble()
                currentVal = (getCurrentDisplayedYValue(item) as Number?)!!.toDouble()
            } else {
                barVal = (item.xValueProp.value as Number?)!!.toDouble()
                currentVal = (getCurrentDisplayedXValue(item) as Number?)!!.toDouble()
            }
            if (currentVal > 0 && barVal < 0) {
                /*
 going from positive to negative
 add style class negative
                 */
                item.nodeProp.value.styleClass.add("negative")
            } else if (currentVal < 0 && barVal > 0) {
                /*
 going from negative to positive
 remove style class negative
                 */
                item.nodeProp.value.styleClass.remove("negative")
            }
        }


        override fun updateStyleClassOf(
            s: Series<X, Y>,
            i: Int
        ) {
            for (j in s.data.value.indices) {
                val item = s.data.value[j]
                val bar = item.nodeProp.value
                bar.styleClass.setAll("chart-bar", "series$i", "data$j", s.defaultColorStyleClass)
            }
        }


        /** {@inheritDoc}  */
        override fun seriesAdded(
            series: Series<X, Y>,
            seriesIndex: Int
        ) {
            /*
            handle any data already in series
            create entry in the map
             */
            val categoryMap: MutableMap<String?, MutableList<Data<X, Y>>> = HashMap()
            for (j in series.data.value.indices) {
                val item = series.data.value[j]
                val bar = createBar(series, seriesIndex, item, j)
                var category: String?
                category =
                    if (orientation == VERTICAL) {
                        item.xValueProp.value as String?
                    } else {
                        item.yValueProp.value as String?
                    }
                /* list of two item positive and negative */
                val itemList: MutableList<Data<X, Y>>? =
                    if (categoryMap[category] != null) categoryMap[category] else ArrayList<Data<X, Y>>()
                itemList!!.add(item)
                categoryMap[category] = itemList
                if (shouldAnimate()) {
                    animateDataAdd(item, bar)
                } else {
                    val barVal =
                        if (orientation == VERTICAL) (item.yValueProp.value as Number?)!!.toDouble()
                        else (item.xValueProp.value as Number?)!!.toDouble()
                    if (barVal < 0) {
                        bar.styleClass.add("negative")
                    }
                    plotChildren.add(bar)
                }
            }
            if (categoryMap.size > 0) {
                seriesCategoryMap[series] = categoryMap
            }
        }

        override fun seriesRemoved(series: Series<X, Y>) {
            /* remove all symbol nodes */
            if (shouldAnimate()) {
                parallelTransition = ParallelTransition()
                parallelTransition!!.onFinished =
                    EventHandler {
                        removeSeriesFromDisplay(series)
                        requestChartLayout()
                    }
                for (d in series.data.value) {
                    val bar = d.nodeProp.value
                    /* Animate series deletion */
                    if (seriesSize > 1) {
                        val t = createDataRemoveTimeline(d, bar, series)
                        parallelTransition!!.children.add(t)
                    } else {
                        /* fade out last series */
                        val ft = FadeTransition(Duration.millis(700.0), bar)
                        ft.fromValue = 1.0
                        ft.toValue = 0.0
                        ft.onFinished =
                            EventHandler {
                                processDataRemove(series, d)
                                bar.opacity = 1.0
                            }
                        parallelTransition!!.children.add(ft)
                    }
                }
                parallelTransition!!.play()
            } else {
                for (d in series.data.value) {
                    processDataRemove(series, d)
                }
                removeSeriesFromDisplay(series)
                requestChartLayout()
            }
        }

        /** {@inheritDoc}  */
        override fun updateAxisRange() {
            /*
            This override is necessary to update axis range based on cumulative Y value for the
            Y axis instead of the inherited way where the max value in the data range is used.
             */
            val categoryIsX = categoryAxis == xAxis
            if (categoryAxis!!.isAutoRanging()) {
                val cData = ArrayList<Any?>()
                for (series in data.value) {
                    for (data in series.data.value) {
                        if (data != null) cData.add(if (categoryIsX) data.xValueProp.value else data.yValueProp.value)
                    }
                }
                categoryAxis!!.invalidateRange(CastedList(cData) { it as String } /*as List<String>*/)
            }
            if (valueAxis!!.isAutoRanging()) {
                val vData: MutableList<Number> = ArrayList()
                for (category in categoryAxis!!.allDataCategories) {
                    var totalXN = 0.0
                    var totalXP = 0.0
                    val seriesIterator =
                        displayedSeriesIterator
                    while (seriesIterator.hasNext()) {
                        val series = seriesIterator.next()
                        for (item in getDataItem(series, category)!!) {
                            @Suppress("SENSELESS_COMPARISON")
                            if (item != null) {
                                val isNegative = item.nodeProp.value.styleClass.contains("negative")

                                if (categoryIsX) {
                                    val value = item.yValueProp.value
                                    if (!isNegative) {
                                        totalXP += toNumericValueFromValueAxis(value)
                                    } else {
                                        totalXN += toNumericValueFromValueAxis(value)
                                    }
                                } else {
                                    val value = item.xValueProp.value
                                    if (!isNegative) {
                                        totalXP += toNumericValueFromValueAxis(value)
                                    } else {
                                        totalXN += toNumericValueFromValueAxis(value)
                                    }
                                }
                            }
                        }
                    }
                    vData.add(totalXP)
                    vData.add(totalXN)
                }
                valueAxisInvalidateRange(CastedList(vData) { it as String } /* as List<String>*/)
            }
        }


    /*    private fun toNumericValueFromValueAxis(v: Number?): Double {
            if (xAxis is CategoryAxisForCatAxisWrapper) {
                return (xAxis.toNumericValue(v as X))
            } else {
                return (yAxis.toNumericValue(v as Y))
            }
        }*/
        @Suppress("ForbiddenAnnotation")
        @JvmName("toNumericValueFromValueAxisX")
        private fun toNumericValueFromValueAxis(v: X): Double {
            check(xAxis is CategoryAxisForCatAxisWrapper)
            return (xAxis.toNumericValue(v))
        }
        @Suppress("ForbiddenAnnotation")
        @JvmName("toNumericValueFromValueAxisY")
        private fun toNumericValueFromValueAxis(v: Y): Double {
            check(xAxis !is CategoryAxisForCatAxisWrapper)
            return (yAxis.toNumericValue(v))
        }


        private fun valueAxisGetDisplayPosition(v: Double): Double {
            if (xAxis is CategoryAxisForCatAxisWrapper) {
                return (xAxis.getDisplayPosition(xAxis.toRealValue(v)!!/* as X*/))
            } else {
                return (yAxis.getDisplayPosition(yAxis.toRealValue(v)!!/* as Y*/))
            }
        }
/*
    private fun valueAxisGetDisplayPosition(v: X): Double {
        check(xAxis is CategoryAxisForCatAxisWrapper)
            return (xAxis.getDisplayPosition(v as X))
    }
    private fun valueAxisGetDisplayPosition(v: Y): Double {
        check(xAxis !is CategoryAxisForCatAxisWrapper)
        return (yAxis.getDisplayPosition(v as Y))
    }
*/


        private fun valueAxisInvalidateRange(list: List<String>) {
            if (xAxis is CategoryAxisForCatAxisWrapper) {
                return (xAxis.invalidateRange(list))
            } else {
                return ((yAxis as CategoryAxisForCatAxisWrapper).invalidateRange(list))
            }
        }

        /** {@inheritDoc}  */
        override fun layoutPlotChildren() {
            val catSpace = categoryAxis!!.categorySpacing.value
            /* calculate bar spacing */
            val availableBarSpace = catSpace - getCategoryGap()
            val barOffset = -((catSpace - getCategoryGap()) / 2)
            /* update bar positions and sizes */
            for (category in categoryAxis!!.categories.value) {
                var currentPositiveValue = 0.0
                var currentNegativeValue = 0.0
                val seriesIterator =
                    displayedSeriesIterator
                while (seriesIterator.hasNext()) {
                    val series = seriesIterator.next()
                    for (item in getDataItem(series, category)!!) {
                        @Suppress("SENSELESS_COMPARISON")
                        if (item != null) {
                            val bar = item.nodeProp.value
                            val categoryPos: Double
                            val valNumber: Double
                            val xValue = getCurrentDisplayedXValue(item)
                            val yValue = getCurrentDisplayedYValue(item)
                            if (orientation == VERTICAL) {
                                categoryPos = xAxis.getDisplayPosition(xValue)
                                valNumber = yAxis.toNumericValue(yValue)
                            } else {
                                categoryPos = yAxis.getDisplayPosition(yValue)
                                valNumber = xAxis.toNumericValue(xValue)
                            }
                            var bottom: Double
                            var top: Double
                            val isNegative = bar.styleClass.contains("negative")
                            if (!isNegative) {
                                bottom = valueAxisGetDisplayPosition(currentPositiveValue)
                                top = valueAxisGetDisplayPosition(currentPositiveValue + valNumber)
                                currentPositiveValue += valNumber
                            } else {
                                bottom = valueAxisGetDisplayPosition(currentNegativeValue + valNumber)
                                top = valueAxisGetDisplayPosition(currentNegativeValue)
                                currentNegativeValue += valNumber
                            }
                            if (orientation == VERTICAL) {
                                bar.resizeRelocate(
                                    categoryPos + barOffset,
                                    top, availableBarSpace, bottom - top
                                )
                            } else {
                                bar.resizeRelocate(
                                    bottom,
                                    categoryPos + barOffset,
                                    top - bottom, availableBarSpace
                                )
                            }
                        }
                    }
                }
            }
        }

        override fun createLegendItemForSeries(
            series: Series<X, Y>,
            seriesIndex: Int
        ): LegendItem {
            val legendItem = LegendItem(series.name.value)
            legendItem.symbol.styleClass.addAll(
                "chart-bar", "series$seriesIndex",
                "bar-legend-symbol", series.defaultColorStyleClass
            )
            return legendItem
        }

        private fun updateMap(
            series: Series<X, Y>,
            item: Data<X, Y>
        ) {
            val category =
                if (orientation == VERTICAL) item.xValueProp.value as String? else item.yValueProp.value as String?
            val categoryMap = seriesCategoryMap[series]
            if (categoryMap != null) {
                categoryMap.remove(category)
                if (categoryMap.isEmpty()) seriesCategoryMap.remove(series)
            }
            if (seriesCategoryMap.isEmpty() && categoryAxis!!.isAutoRanging()) categoryAxis!!.categories.value.clear()
        }

        private fun processDataRemove(
            series: Series<X, Y>,
            item: Data<X, Y>
        ) {
            val bar = item.nodeProp.value
            plotChildren.remove(bar)
            updateMap(series, item)
        }

        private fun animateDataAdd(
            item: Data<X, Y>,
            bar: Node
        ) {
            val barVal: Double
            if (orientation == VERTICAL) {
                barVal = (item.yValueProp.value as Number?)!!.toDouble()
                if (barVal < 0) {
                    bar.styleClass.add("negative")
                }
                item.yValueProp.value = yAxis.toRealValue(yAxis.zeroPosition)
                setCurrentDisplayedYValue(item, yAxis.toRealValue(yAxis.zeroPosition)!!)
                plotChildren.add(bar)
                item.yValueProp.value = yAxis.toRealValue(barVal)
                animate(
                    KeyFrame(
                        Duration.ZERO,
                        KeyValue(
                            currentDisplayedYValueProperty(item),
                            getCurrentDisplayedYValue(item),
                            MyInterpolator.MY_DEFAULT_INTERPOLATOR
                        )
                    ),
                    KeyFrame(
                        Duration.millis(700.0),
                        KeyValue(
                            currentDisplayedYValueProperty(item),
                            item.yValueProp.value, MyInterpolator.EASE_BOTH
                        )
                    )
                )
            } else {
                barVal = (item.xValueProp.value as Number?)!!.toDouble()
                if (barVal < 0) {
                    bar.styleClass.add("negative")
                }
                item.xValueProp.value = xAxis.toRealValue(xAxis.zeroPosition)
                setCurrentDisplayedXValue(item, xAxis.toRealValue(xAxis.zeroPosition)!!)
                plotChildren.add(bar)
                item.xValueProp.value = xAxis.toRealValue(barVal)
                animate(
                    KeyFrame(
                        Duration.ZERO,
                        KeyValue(
                            currentDisplayedXValueProperty(item),
                            getCurrentDisplayedXValue(item),
                            MyInterpolator.MY_DEFAULT_INTERPOLATOR
                        )
                    ),
                    KeyFrame(
                        Duration.millis(700.0),
                        KeyValue(
                            currentDisplayedXValueProperty(item),
                            item.xValueProp.value, MyInterpolator.EASE_BOTH
                        )
                    )
                )
            }
        }

        @Suppress("UNUSED_PARAMETER")
        private fun createDataRemoveTimeline(
            item: Data<X, Y>,
            bar: Node?,
            series: Series<X, Y>
        ): Timeline {
            val t = Timeline()
            if (orientation == VERTICAL) {
                t.keyFrames.addAll(
                    KeyFrame(
                        Duration.ZERO,
                        KeyValue(
                            currentDisplayedYValueProperty(item),
                            getCurrentDisplayedYValue(item),
                            MyInterpolator.MY_DEFAULT_INTERPOLATOR
                        )
                    ),
                    KeyFrame(
                        Duration.millis(700.0),
                        {
                            processDataRemove(
                                series, item
                            )
                        },
                        KeyValue(
                            currentDisplayedYValueProperty(item),
                            item.yValueProp.value, MyInterpolator.EASE_BOTH
                        )
                    )
                )
            } else {
                item.xValueProp.value = xAxis.toRealValue(xAxis.zeroPosition)
                t.keyFrames.addAll(
                    KeyFrame(
                        Duration.ZERO,
                        KeyValue(
                            currentDisplayedXValueProperty(item),
                            getCurrentDisplayedXValue(item),
                            MyInterpolator.MY_DEFAULT_INTERPOLATOR
                        )
                    ),
                    KeyFrame(
                        Duration.millis(700.0),
                        {
                            processDataRemove(
                                series, item
                            )
                        },
                        KeyValue(
                            currentDisplayedXValueProperty(item),
                            item.xValueProp.value, MyInterpolator.EASE_BOTH
                        )
                    )
                )
            }
            return t
        }

        private fun createBar(
            series: Series<X, Y>,
            seriesIndex: Int,
            item: Data<X, Y>,
            itemIndex: Int
        ): Node {
            var bar = item.nodeProp.value
            if (bar == null) {
                bar = StackPane()
                bar.setAccessibleRole(TEXT)
                bar.setAccessibleRoleDescription("Bar")
                bar.focusTraversableProperty().bind(Platform.accessibilityActiveProperty())
                item.nodeProp.value = bar
            }
            bar.styleClass.setAll("chart-bar", "series$seriesIndex", "data$itemIndex", series.defaultColorStyleClass)
            return bar
        }

        private fun getDataItem(
            series: Series<X, Y>,
            category: String
        ): List<Data<X, Y>>? {
            val catmap: Map<String?, MutableList<Data<X, Y>>>? = seriesCategoryMap[series]
            return if (
                catmap != null
            ) {
                if (
                    catmap[category] != null
                ) catmap[category]
                else ArrayList()
            } else ArrayList()
        }


        /** {@inheritDoc}  */
        override fun seriesBeingRemovedIsAdded(series: Series<X, Y>) {
            if (parallelTransition != null) {
                parallelTransition!!.onFinished = null
                parallelTransition!!.stop()
                parallelTransition = null
                plotChildren.remove(series.getNode())
                for (d in series.getData()) plotChildren.remove(d.node)
                removeSeriesFromDisplay(series)
            }
        }

    /*
     * Super-lazy instantiation pattern from Bill Pugh.
     */
        private object StyleableProperties {
            val CATEGORY_GAP: CssMetaData<StackedBarChartForWrapper<*, *>, Number> =
                object : CssMetaData<StackedBarChartForWrapper<*, *>, Number>(
                    "-fx-category-gap",
                    SizeConverter.getInstance(), 10.0
                ) {
                    override fun isSettable(
                        node: StackedBarChartForWrapper<*, *>
                    ): Boolean = node.categoryGap.value == null || !node.categoryGap.isBound


                    override fun getStyleableProperty(
                        node: StackedBarChartForWrapper<*, *>
                    ): StyleableProperty<Number?> = node.categoryGapProperty()
                }
            val classCssMetaData: List<CssMetaData<out Styleable?, *>> by lazy {
                val styleables: MutableList<CssMetaData<out Styleable?, *>> = ArrayList(getClassCssMetaData())
                styleables.add(CATEGORY_GAP)
                Collections.unmodifiableList(styleables)
            }
        }

        /**
         * {@inheritDoc}
         * @since JavaFX 8.0
         */
        override fun getCssMetaData(): List<CssMetaData<out Styleable?, *>> = classCssMetaData
        /**
         * Construct a new StackedBarChart with the given axis and data. The two axis should be a ValueAxis/NumberAxis and a
         * CategoryAxis, they can be in either order depending on if you want a horizontal or vertical bar chart.
         *
         * @param xAxis The x axis to use
         * @param yAxis The y axis to use
         * @param data The data to use, this is the actual list used so any changes to it will be reflected in the chart




         * Construct a new StackedBarChart with the given axis. The two axis should be a ValueAxis/NumberAxis and a CategoryAxis,
         * they can be in either order depending on if you want a horizontal or vertical bar chart.
         *
         * @param xAxis The x axis to use
         * @param yAxis The y axis to use
         */
        init {
            styleClass.add("stacked-bar-chart")
            require(
                xAxis is MoreGenericValueAxis<*> && yAxis is CategoryAxisForCatAxisWrapper ||
                    yAxis is MoreGenericValueAxis<*> && xAxis is CategoryAxisForCatAxisWrapper
            ) { "Axis type incorrect, one of X,Y should be CategoryAxis and the other NumberAxis" }
            if (xAxis is CategoryAxisForCatAxisWrapper) {
                categoryAxis = xAxis
                valueAxis = yAxis as MoreGenericValueAxis<*>
                orientation = VERTICAL
            } else {
                categoryAxis = yAxis as CategoryAxisForCatAxisWrapper?
                valueAxis = xAxis as MoreGenericValueAxis<*>
                orientation = HORIZONTAL
            }
            /* update css */
            pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE, orientation == HORIZONTAL)
            pseudoClassStateChanged(VERTICAL_PSEUDOCLASS_STATE, orientation == VERTICAL)
            setData(data)
            categoryAxis!!.categories.value.addListener(categoriesListener)
        }

        companion object {
            /** Pseudoclass indicating this is a vertical chart.  */
            private val VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("vertical")

            /** Pseudoclass indicating this is a horizontal chart.  */
            private val HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("horizontal")
        }
    }
