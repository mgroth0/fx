package matt.fx.control.wrapper.chart.bubble

import javafx.scene.chart.BubbleChart
import matt.fx.control.wrapper.chart.axis.MAxis
import matt.fx.control.wrapper.chart.xy.XYChartWrapper

open class BubbleChartWrapper<X, Y>(
  node: BubbleChart<X, Y>,
): XYChartWrapper<X, Y, BubbleChart<X, Y>>(node) {
  companion object {
	fun <X, Y> BubbleChart<X, Y>.wrapped() = BubbleChartWrapper(this)
  }

  constructor(x: MAxis<X>, y: MAxis<Y>): this(BubbleChart(x.node, y.node))

}