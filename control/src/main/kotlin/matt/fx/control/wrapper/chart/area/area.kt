package matt.fx.control.wrapper.chart.area

import javafx.scene.chart.AreaChart
import matt.fx.control.wrapper.chart.axis.MAxis
import matt.fx.control.wrapper.chart.xy.XYChartWrapper

open class AreaChartWrapper<X, Y>(
  node: AreaChart<X, Y>,
): XYChartWrapper<X, Y, AreaChart<X, Y>>(node) {
  companion object {
	fun <X, Y> AreaChart<X, Y>.wrapped() = AreaChartWrapper(this)
  }

  constructor(x: MAxis<X>, y: MAxis<Y>): this(AreaChart(x.node, y.node))

}