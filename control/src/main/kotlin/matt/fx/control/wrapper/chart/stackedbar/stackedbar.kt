package matt.fx.control.wrapper.chart.stackedbar

import javafx.scene.chart.StackedBarChart
import matt.fx.control.wrapper.chart.axis.MAxis
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
  node: StackedBarChart<X, Y>,
): XYChartWrapper<X, Y, StackedBarChart<X, Y>>(node) {
  companion object {
	fun <X, Y> StackedBarChart<X, Y>.wrapped() = StackedBarChartWrapper(this)
  }

  constructor(x: MAxis<X>, y: MAxis<Y>): this(StackedBarChart(x.node, y.node))

}