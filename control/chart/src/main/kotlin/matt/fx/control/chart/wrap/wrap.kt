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
import matt.fx.control.wrapper.wrapped.util.cannotFindWrapper
import matt.fx.control.wrapper.wrapped.findWrapper
import matt.model.data.mathable.MathAndComparable

/*fun PieChart.wrapped(): PieChartWrapper = findWrapper() ?: PieChartWrapper(this@wrapped)*/


fun <X : MathAndComparable<X>, Y : MathAndComparable<Y>> MorePerfOptionsLineChart<X, Y>.wrapped(): LineChartWrapper<X, Y> =
    findWrapper() ?: LineChartWrapper(this@wrapped)


@Suppress("UNCHECKED_CAST") fun AreaChartForPrivateProps<*, *>.wrapped(): AreaChartWrapper<*, *> =
    findWrapper() ?: AreaChartWrapper(
        this@wrapped as AreaChartForPrivateProps<Any, Any>
    )

@Suppress("UNCHECKED_CAST") fun BarChartForWrapper<*, *>.wrapped(): BarChartWrapper<*, *> =
    findWrapper() ?: BarChartWrapper(this@wrapped as BarChartForWrapper<Any, Any>)

@Suppress("UNCHECKED_CAST") fun BubbleChartForWrapper<*, *>.wrapped(): BubbleChartWrapper<*, *> =
    findWrapper() ?: BubbleChartWrapper(this@wrapped as BubbleChartForWrapper<Any, Any>)

@Suppress("UNCHECKED_CAST") fun ScatterChartForWrapper<*, *>.wrapped(): ScatterChartWrapper<*, *> =
    findWrapper() ?: ScatterChartWrapper(this@wrapped as ScatterChartForWrapper<Any, Any>)

@Suppress("UNCHECKED_CAST") fun StackedBarChartForWrapper<*, *>.wrapped(): StackedBarChartWrapper<*, *> =
    findWrapper() ?: StackedBarChartWrapper(this@wrapped as StackedBarChartForWrapper<Any, Any>)


fun PieChartForWrapper.wrapped(): PieChartWrapper = findWrapper() ?: PieChartWrapper(this@wrapped)


fun <T : MathAndComparable<T>> MoreGenericNumberAxis<T>.wrapped(): NumberAxisWrapper<*> =
    findWrapper() ?: error("not implemented: NumberAxisWrapper(this@wrapped)")

/*fun NumberAxis.wrapped(): OldNumberAxisWrapper = findWrapper() ?: OldNumberAxisWrapper(this@wrapped)*/
fun CategoryAxisForCatAxisWrapper.wrapped(): CategoryAxisWrapper = findWrapper() ?: CategoryAxisWrapper(this@wrapped)


/*@Suppress("UNCHECKED_CAST") fun <T: Number> MoreGenericValueAxis<T>.wrapped(): OldValueAxisWrapper<T> =
  findWrapper() ?: when (this) {
	is MoreGenericNumberAxis -> wrapped() as OldValueAxisWrapper<T>
	else                     -> cannotFindWrapper()
  }*/


@Suppress("UNCHECKED_CAST")
fun <T> AxisForPackagePrivateProps<T>.wrapped(): AxisWrapper<T, AxisForPackagePrivateProps<T>> =
    findWrapper() ?: when (this) {
        is MoreGenericNumberAxis         -> wrapped()
        is MoreGenericValueAxis          -> (this as MoreGenericValueAxis<out Number>).wrapped() as AxisWrapper<T, AxisForPackagePrivateProps<T>>
        is CategoryAxisForCatAxisWrapper -> wrapped() as AxisWrapper<T, AxisForPackagePrivateProps<T>>
        else                             -> cannotFindWrapper()
    }
