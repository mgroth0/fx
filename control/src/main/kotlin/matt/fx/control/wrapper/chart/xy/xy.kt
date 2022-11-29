package matt.fx.control.wrapper.chart.xy

import matt.fx.control.wrapper.chart.ChartWrapper
import matt.fx.control.wrapper.chart.axis.MAxis
import matt.fx.control.wrapper.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps
import matt.fx.control.wrapper.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps.Data
import matt.fx.control.wrapper.chart.xy.series.SeriesConverter
import matt.fx.control.wrapper.chart.xy.series.SeriesWrapper
import matt.fx.control.wrapper.wrapped.wrapped
import matt.hurricanefx.eye.wrapper.obs.collect.createMutableWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.obs.col.olist.MutableObsList
import matt.obs.col.olist.mappedlist.toSyncedList

fun <X, Y> MutableList<Data<X, Y>>.add(x: X, y: Y) = add(Data(x, y))

open class XYChartWrapper<X, Y, N: XYChartForPackagePrivateProps<X, Y>>(node: N): ChartWrapper<N>(node) {


  val data: MutableObsList<SeriesWrapper<X, Y>> by lazy {
	node.data.value.createMutableWrapper().toSyncedList(SeriesConverter<X, Y>())
  }


  open val yAxis: MAxis<Y> by lazy { node.yAxis.wrapped() }
  open val xAxis: MAxis<X> by lazy { node.xAxis.wrapped() }

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


  val horizontalZeroLineVisibleProperty by lazy { node.horizontalZeroLineVisibleProperty().toNonNullableProp() }
  val verticalZeroLineVisibleProperty by lazy { node.verticalZeroLineVisibleProperty().toNonNullableProp() }

}


/**
 * Add a new XYChart.Series with the given name to the given Chart. Optionally specify a list data for the new series or
 * add data with the optional op that will be performed on the created series object.
 */
fun <X, Y, ChartType: XYChartWrapper<X, Y, *>> ChartType.series(
  name: String,
  elements: MutableObsList<Data<X, Y>>? = null,
  op: (SeriesWrapper<X, Y>).()->Unit = {}
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
fun <X, Y> SeriesWrapper<X, Y>.data(x: X, y: Y, extra: Any? = null, op: (Data<X, Y>).()->Unit = {}) =
  Data(x, y).apply {
	if (extra != null) setExtraValue(extra)
	data.add(this)
	op(this)
  }


/**
 * Helper class for the multiseries support
 */
class MultiSeries<X, Y>(val series: List<SeriesWrapper<X, Y>>, val chart: XYChartWrapper<X, Y, *>) {
  fun data(x: X, vararg y: Y) = y.forEachIndexed { index, value -> series[index].data(x, value) }
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
fun <X, Y, ChartType: XYChartWrapper<X, Y, *>> ChartType.multiseries(
  vararg names: String,
  op: (MultiSeries<X, Y>).()->Unit = {}
): MultiSeries<X, Y> {
  val series = names.map { SeriesWrapper<X, Y>().apply { name = it } }
  val multiSeries = MultiSeries(series, this).also(op)
  data.addAll(series)
  return multiSeries
}

operator fun <X, Y> Data<X, Y>.component1(): X = xValue.value
operator fun <X, Y> Data<X, Y>.component2(): Y = yValue.value