package matt.fx.control.chart.wrap

import javafx.scene.chart.PieChart
import matt.fx.control.chart.area.AreaChartWrapper
import matt.fx.control.chart.bar.bar.BarChartForWrapper
import matt.fx.control.chart.line.LineChartWrapper
import matt.fx.control.chart.line.highperf.relinechart.MorePerfOptionsLineChart
import matt.fx.control.chart.line.highperf.relinechart.xy.area.AreaChartForPrivateProps
import matt.fx.control.chart.pie.PieChartWrapper
import matt.fx.control.chart.pie.pie.PieChartForWrapper
import matt.fx.control.chart.stackedbar.StackedBarChartWrapper
import matt.fx.control.chart.stackedbar.stackedb.StackedBarChartForWrapper
import matt.fx.control.wrapper.wrapped.findWrapper
import matt.model.data.mathable.MathAndComparable

fun PieChart.wrapped(): PieChartWrapper = findWrapper() ?: PieChartWrapper(this@wrapped)


fun <X: MathAndComparable<X>, Y: MathAndComparable<Y>> MorePerfOptionsLineChart<X, Y>.wrapped(): LineChartWrapper<X, Y> =
  findWrapper() ?: LineChartWrapper(this@wrapped)




fun AreaChartForPrivateProps<*, *>.wrapped(): AreaChartWrapper<*, *> = findWrapper() ?: AreaChartWrapper(this@wrapped)
fun BarChartForWrapper<*, *>.wrapped(): BarChartWrapper<*, *> = findWrapper() ?: BarChartWrapper(this@wrapped)
fun BubbleChartForWrapper<*, *>.wrapped(): BubbleChartWrapper<*, *> = findWrapper() ?: BubbleChartWrapper(this@wrapped)
fun ScatterChartForWrapper<*, *>.wrapped(): ScatterChartWrapper<*, *> = findWrapper() ?: ScatterChartWrapper(this@wrapped)
fun StackedBarChartForWrapper<*, *>.wrapped(): StackedBarChartWrapper<*, *> =
  findWrapper() ?: StackedBarChartWrapper(this@wrapped)



fun PieChartForWrapper.wrapped(): PieChartWrapper = findWrapper() ?: PieChartWrapper(this@wrapped)