package matt.fx.control.chart.bar

import matt.fx.control.chart.axis.MAxis
import matt.fx.control.chart.bar.bar.BarChartForWrapper
import matt.fx.control.chart.xy.XYChartWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo

/**
 * Create a BarChart with optional title, axis and add to the parent pane. The optional op will be performed on the new instance.
 */
fun <X: Any, Y: Any> ET.barchart(title: String? = null, x: MAxis<X>, y: MAxis<Y>, op: BarChartWrapper<X, Y>.()->Unit = {}) =
  BarChartWrapper<X, Y>(x, y).attachTo(this, op) { it.title = title }

open class BarChartWrapper<X: Any, Y: Any>(
  node: BarChartForWrapper<X, Y>,
): XYChartWrapper<X, Y, BarChartForWrapper<X, Y>>(node) {

  constructor(x: MAxis<X>, y: MAxis<Y>): this(BarChartForWrapper(x.node, y.node))

}