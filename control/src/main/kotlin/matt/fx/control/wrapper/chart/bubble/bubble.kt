package matt.fx.control.wrapper.chart.bubble

import matt.fx.control.wrapper.chart.axis.MAxis
import matt.fx.control.wrapper.chart.bubble.bubble.BubbleChartForWrapper
import matt.fx.control.wrapper.chart.xy.XYChartWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo

/**
 * Create a BubbleChart with optional title, axis and add to the parent pane. The optional op will be performed on the new instance.
 */
fun <X, Y> ET.bubblechart(title: String? = null, x: MAxis<X>, y: MAxis<Y>, op: BubbleChartWrapper<X, Y>.()->Unit = {}) =
  BubbleChartWrapper<X, Y>(x, y).attachTo(this, op) { it.title = title }

open class BubbleChartWrapper<X, Y>(
  node: BubbleChartForWrapper<X, Y>,
): XYChartWrapper<X, Y, BubbleChartForWrapper<X, Y>>(node) {

  constructor(x: MAxis<X>, y: MAxis<Y>): this(BubbleChartForWrapper(x.node, y.node))

}