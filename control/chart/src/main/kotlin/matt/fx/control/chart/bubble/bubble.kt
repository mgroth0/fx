package matt.fx.control.chart.bubble

import matt.fx.control.chart.axis.MAxis
import matt.fx.control.chart.bubble.bubble.BubbleChartForWrapper
import matt.fx.control.chart.xy.XYChartWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo

/**
 * Create a BubbleChart with optional title, axis and add to the parent pane. The optional op will be performed on the new instance.
 */
fun <X: Any, Y: Any> ET.bubblechart(title: String? = null, x: MAxis<X>, y: MAxis<Y>, op: BubbleChartWrapper<X, Y>.()->Unit = {}) =
  BubbleChartWrapper(x, y).attachTo(this, op) { it.title = title }

open class BubbleChartWrapper<X: Any, Y: Any>(
  node: BubbleChartForWrapper<X, Y>,
): XYChartWrapper<X, Y, BubbleChartForWrapper<X, Y>>(node) {

  constructor(x: MAxis<X>, y: MAxis<Y>): this(BubbleChartForWrapper(x.node, y.node))

}