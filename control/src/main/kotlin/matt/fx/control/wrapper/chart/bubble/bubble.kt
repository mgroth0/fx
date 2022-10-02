package matt.fx.control.wrapper.chart.bubble

import javafx.scene.chart.BubbleChart
import matt.fx.control.wrapper.chart.axis.MAxis
import matt.fx.control.wrapper.chart.xy.XYChartWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo

/**
 * Create a BubbleChart with optional title, axis and add to the parent pane. The optional op will be performed on the new instance.
 */
fun <X, Y> ET.bubblechart(title: String? = null, x: MAxis<X>, y: MAxis<Y>, op: BubbleChartWrapper<X, Y>.()->Unit = {}) =
  BubbleChartWrapper<X, Y>(x, y).attachTo(this, op) { it.title = title }

open class BubbleChartWrapper<X, Y>(
  node: BubbleChart<X, Y>,
): XYChartWrapper<X, Y, BubbleChart<X, Y>>(node) {
  companion object {
	fun <X, Y> BubbleChart<X, Y>.wrapped() = BubbleChartWrapper(this)
  }

  constructor(x: MAxis<X>, y: MAxis<Y>): this(BubbleChart(x.node, y.node))

}