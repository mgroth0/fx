package matt.fx.control.wrapper.chart.bar

import javafx.scene.chart.BarChart
import matt.hurricanefx.wrapper.chart.axis.MAxis
import matt.hurricanefx.wrapper.chart.xy.XYChartWrapper

open class BarChartWrapper<X, Y>(
  node: BarChart<X, Y>,
): XYChartWrapper<X, Y, BarChart<X, Y>>(node) {
  companion object {
	fun <X, Y> BarChart<X, Y>.wrapped() = BarChartWrapper(this)
  }

  constructor(x: MAxis<X>, y: MAxis<Y>): this(BarChart(x.node, y.node))

}