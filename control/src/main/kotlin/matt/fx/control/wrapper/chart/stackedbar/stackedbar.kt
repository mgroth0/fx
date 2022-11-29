package matt.fx.control.wrapper.chart.stackedbar

import matt.fx.control.wrapper.chart.axis.MAxis
import matt.fx.control.wrapper.chart.stackedbar.stackedb.StackedBarChartForWrapper
import matt.fx.control.wrapper.chart.xy.XYChartWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo


/**
 * Create a BarChart with optional title, axis and add to the parent pane. The optional op will be performed on the new instance.
 */
fun <X, Y> ET.stackedbarchart(
  title: String? = null, x: MAxis<X>, y: MAxis<Y>, op: StackedBarChartWrapper<X, Y>.()->Unit = {}
) = StackedBarChartWrapper<X, Y>(x, y).attachTo(this, op) { it.title = title }


open class StackedBarChartWrapper<X, Y>(
  node: StackedBarChartForWrapper<X, Y>,
): XYChartWrapper<X, Y, StackedBarChartForWrapper<X, Y>>(node) {

  constructor(x: MAxis<X>, y: MAxis<Y>): this(StackedBarChartForWrapper(x.node, y.node))

}