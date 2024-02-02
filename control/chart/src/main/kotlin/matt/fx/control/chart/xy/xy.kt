package matt.fx.control.chart.xy

import matt.collect.itr.applyEach
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.control.chart.ChartWrapper
import matt.fx.control.chart.axis.MAxis
import matt.fx.control.chart.axis.value.ValueAxisWrapper
import matt.fx.control.chart.axis.value.number.NumberAxisWrapper
import matt.fx.control.chart.line.LineChartWrapper
import matt.fx.control.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps
import matt.fx.control.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps.Data
import matt.fx.control.chart.wrap.wrapped
import matt.fx.control.chart.xy.series.SeriesWrapper
import matt.obs.col.olist.MutableObsList

fun <X, Y> MutableList<Data<X, Y>>.add(
    x: X,
    y: Y
) = add(Data(x, y))

open class XYChartWrapper<X : Any, Y : Any, N : XYChartForPackagePrivateProps<X, Y>>(nodeDifferentNameForK2: N) :
    ChartWrapper<N>(nodeDifferentNameForK2) {


    val data: MutableObsList<SeriesWrapper<X, Y>> by lazy {
        error(
            """
                Super weird K2 error:  https://youtrack.jetbrains.com/issue/KT-63569/Kotlin-2.0.0-Beta-1-IllegalStateException-id1
                
                private val nodeDataForK2: ObjectProperty<ObservableList<Series<X, Y>>> = nodeDifferentNameForK2.data

    private fun nodeDataValueMutableWrapperForK2() = nodeDataForK2.value.createMutableWrapper()
                
                nodeDataValueMutableWrapperForK2().toSyncedList(SeriesConverter())
                
            """.trimIndent()
        )

    }

    fun temporaryK2Replacement(): XYChartForPackagePrivateProps<X, Y> {
        error(
            """
                Super weird K2 error:  https://youtrack.jetbrains.com/issue/KT-63569/Kotlin-2.0.0-Beta-1-IllegalStateException-id1
                
                
            """.trimIndent()
        )

    }

    open val yAxis: MAxis<Y> by lazy {

        temporaryK2Replacement().yAxis.wrapped()

    }
    open val xAxis: MAxis<X> by lazy { temporaryK2Replacement().xAxis.wrapped() }

    //  /*https://stackoverflow.com/questions/52179664/how-to-change-the-axis-color-of-javafx-chart*/
    //  var axesColor: Color
    //	get() = NOT_IMPLEMENTED
    //	set(value) {
    //	  val h = value.hex()
    //	  val sty = """
    //	d
    //		""".trimIndent()
    //	  style += sty
    //	  xAxis.style += sty
    //	  yAxis.style += sty
    //	}


    val horizontalZeroLineVisibleProperty: NonNullFXBackedBindableProp<Boolean> by lazy {
        temporaryK2Replacement().horizontalZeroLineVisibleProperty().toNonNullableProp()
    }
    val verticalZeroLineVisibleProperty: NonNullFXBackedBindableProp<Boolean> by lazy {
        temporaryK2Replacement().verticalZeroLineVisibleProperty().toNonNullableProp()
    }

    fun configureForHighPerformance() {
        animated = false
        (this as? LineChartWrapper)?.createSymbols = false
        isLegendVisible = false
        (listOf(yAxis) + xAxis).applyEach {
            animated = false
            isAutoRanging = false
            isTickMarkVisible = false
            isTickLabelsVisible = false
            (this as? ValueAxisWrapper)?.apply {
                isMinorTickVisible = false
                minorTickCount = 0
                (this as? NumberAxisWrapper)?.apply {
                    maximizeTickUnit()
                }
            }
        }
    }


    internal val chartContent by lazy {
        temporaryK2Replacement().chartContent
    }

    private val plotContent by lazy {
        temporaryK2Replacement().plotContent
    }
    internal val plotArea by lazy {
        temporaryK2Replacement().plotArea
    }


    val dataItemChangedAnimDur = temporaryK2Replacement().dataItemChangedAnimDur
    val dataItemChangedAnimInterp = temporaryK2Replacement().dataItemChangedAnimInterp


}


/**
 * Add a new XYChart.Series with the given name to the given Chart. Optionally specify a list data for the new series or
 * add data with the optional op that will be performed on the created series object.
 */
fun <X, Y, ChartType : XYChartWrapper<X, Y, *>> ChartType.series(
    name: String,
    elements: MutableObsList<Data<X, Y>>? = null,
    op: (SeriesWrapper<X, Y>).() -> Unit = {}
) = SeriesWrapper<X, Y>().also {
    it.name = name
    elements?.let(it::setTheData)
    op(it)
    data.add(it)
}

/**
 * Add and create a XYChart.Data entry with x, y and optional extra value. The optional op will be performed on the data instance,
 * a good place to add event handlers to the Data.node for example.
 *
 * @return The new Data entry
 */
fun <X, Y> SeriesWrapper<X, Y>.data(
    x: X,
    y: Y,
    extra: Any? = null,
    op: (Data<X, Y>).() -> Unit = {}
) =
    Data(x, y).apply {
        if (extra != null) setExtraValue(extra)
        data.add(this)
        op(this)
    }


/**
 * Helper class for the multiseries support
 */
class MultiSeries<X: Any, Y: Any>(
    val series: List<SeriesWrapper<X, Y>>,
    val chart: XYChartWrapper<X, Y, *>
) {
    fun data(
        x: X,
        vararg y: Y
    ) = y.forEachIndexed { index, value -> series[index].data(x, value) }
}

/**
 * Add multiple series XYChart.Series with data in one go. Specify a list of names for the series
 * and then add values in the op. Example:
 *
 *     multiseries("Portfolio 1", "Portfolio 2") {
 *         data(1, 23, 10)
 *         data(2, 14, 5)
 *         data(3, 15, 8)
 *         ...
 *     }
 *
 */
fun <X: Any, Y: Any, ChartType : XYChartWrapper<X, Y, *>> ChartType.multiseries(
    vararg names: String,
    op: (MultiSeries<X, Y>).() -> Unit = {}
): MultiSeries<X, Y> {
    val series = names.map { SeriesWrapper<X, Y>().apply { name = it } }
    val multiSeries = MultiSeries(series, this).also(op)
    data.addAll(series)
    return multiSeries
}

operator fun <X, Y> Data<X, Y>.component1(): X = xValueProp.value
operator fun <X, Y> Data<X, Y>.component2(): Y = yValueProp.value
