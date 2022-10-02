package matt.fx.control.wrapper.chart.scatter

import javafx.scene.chart.ScatterChart
import matt.fx.control.wrapper.chart.axis.MAxis
import matt.fx.control.wrapper.chart.xy.XYChartWrapper

open class ScatterChartWrapper<X, Y>(
  node: ScatterChart<X, Y>
): XYChartWrapper<X, Y, ScatterChart<X, Y>>(node) {
  companion object {
	fun <X, Y> ScatterChart<X, Y>.wrapped() = ScatterChartWrapper(this)
  }

  constructor(x: MAxis<X>, y: MAxis<Y>): this(ScatterChart(x.node, y.node))

}