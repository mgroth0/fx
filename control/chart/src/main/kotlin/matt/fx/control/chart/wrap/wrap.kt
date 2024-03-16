package matt.fx.control.chart.wrap

import matt.fx.control.chart.area.AreaChartWrapper
import matt.fx.control.chart.axis.AxisWrapper
import matt.fx.control.chart.axis.cat.CategoryAxisWrapper
import matt.fx.control.chart.axis.cat.cat.CategoryAxisForCatAxisWrapper
import matt.fx.control.chart.axis.value.axis.AxisForPackagePrivateProps
import matt.fx.control.chart.axis.value.moregenval.MoreGenericValueAxis
import matt.fx.control.chart.axis.value.number.NumberAxisWrapper
import matt.fx.control.chart.axis.value.number.moregennum.MoreGenericNumberAxis
import matt.fx.control.chart.bar.BarChartWrapper
import matt.fx.control.chart.bar.bar.BarChartForWrapper
import matt.fx.control.chart.bubble.BubbleChartWrapper
import matt.fx.control.chart.bubble.bubble.BubbleChartForWrapper
import matt.fx.control.chart.line.LineChartWrapper
import matt.fx.control.chart.line.highperf.relinechart.MorePerfOptionsLineChart
import matt.fx.control.chart.line.highperf.relinechart.xy.area.AreaChartForPrivateProps
import matt.fx.control.chart.pie.PieChartWrapper
import matt.fx.control.chart.pie.pie.PieChartForWrapper
import matt.fx.control.chart.scatter.ScatterChartWrapper
import matt.fx.control.chart.scatter.scatter.ScatterChartForWrapper
import matt.fx.control.chart.stackedbar.StackedBarChartWrapper
import matt.fx.control.chart.stackedbar.stackedb.StackedBarChartForWrapper
import matt.fx.control.wrapper.wrapped.findWrapper
import matt.fx.control.wrapper.wrapped.util.cannotFindWrapper
import matt.model.data.mathable.MathAndComparable

/*fun PieChart.wrapped(): PieChartWrapper = findWrapper() ?: PieChartWrapper(this@wrapped)*/


fun <X : MathAndComparable<X>, Y : MathAndComparable<Y>> MorePerfOptionsLineChart<X, Y>.wrapped(): LineChartWrapper<X, Y> =
    findWrapper() ?: LineChartWrapper(this@wrapped)


fun AreaChartForPrivateProps<Any, Any>.wrapped(): AreaChartWrapper<*, *> =
    findWrapper() ?: AreaChartWrapper(
        this@wrapped
    )

fun BarChartForWrapper<Any, Any>.wrapped(): BarChartWrapper<*, *> =
    findWrapper() ?: BarChartWrapper(this@wrapped)

fun BubbleChartForWrapper<Any, Any>.wrapped(): BubbleChartWrapper<*, *> =
    findWrapper() ?: BubbleChartWrapper(this@wrapped)

fun ScatterChartForWrapper<Any, Any>.wrapped(): ScatterChartWrapper<*, *> =
    findWrapper() ?: ScatterChartWrapper(this@wrapped)

fun StackedBarChartForWrapper<Any, Any>.wrapped(): StackedBarChartWrapper<*, *> =
    findWrapper() ?: StackedBarChartWrapper(this@wrapped)


fun PieChartForWrapper.wrapped(): PieChartWrapper = findWrapper() ?: PieChartWrapper(this@wrapped)


fun <T : MathAndComparable<T>> MoreGenericNumberAxis<T>.wrapped(): NumberAxisWrapper<*> =
    findWrapper() ?: error("not implemented: NumberAxisWrapper(this@wrapped)")

/*fun NumberAxis.wrapped(): OldNumberAxisWrapper = findWrapper() ?: OldNumberAxisWrapper(this@wrapped)*/
fun CategoryAxisForCatAxisWrapper.wrapped(): CategoryAxisWrapper = findWrapper() ?: CategoryAxisWrapper(this@wrapped)


/* fun <T: Number> MoreGenericValueAxis<T>.wrapped(): OldValueAxisWrapper<T> =
  findWrapper() ?: when (this) {
	is MoreGenericNumberAxis -> wrapped() as OldValueAxisWrapper<T>
	else                     -> cannotFindWrapper()
  }*/



fun <T> AxisForPackagePrivateProps<T>.wrapped(): AxisWrapper<T, AxisForPackagePrivateProps<T>> =
    findWrapper() ?: when (this) {
        is MoreGenericNumberAxis         -> wrapped()
        is MoreGenericValueAxis          -> error("FX IS DEAD")
        is CategoryAxisForCatAxisWrapper -> error("FX IS DEAD")
        else                             -> cannotFindWrapper()
    }
