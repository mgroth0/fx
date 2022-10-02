package matt.fx.control.wrapper.chart.stackedbar

import javafx.scene.chart.StackedBarChart
import matt.fx.control.wrapper.chart.axis.MAxis
import matt.fx.control.wrapper.chart.xy.XYChartWrapper

open class StackedBarChartWrapper<X, Y>(
  node: StackedBarChart<X, Y>,
): XYChartWrapper<X, Y, StackedBarChart<X, Y>>(node) {
  companion object {
	fun <X, Y> StackedBarChart<X, Y>.wrapped() = StackedBarChartWrapper(this)
  }

  constructor(x: MAxis<X>, y: MAxis<Y>): this(StackedBarChart(x.node, y.node))

}