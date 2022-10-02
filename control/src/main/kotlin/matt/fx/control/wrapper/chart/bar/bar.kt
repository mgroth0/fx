package matt.fx.control.wrapper.chart.bar

import javafx.scene.chart.BarChart
import matt.fx.control.wrapper.chart.axis.MAxis
import matt.fx.control.wrapper.chart.xy.XYChartWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo

/**
 * Create a BarChart with optional title, axis and add to the parent pane. The optional op will be performed on the new instance.
 */
fun <X, Y> ET.barchart(title: String? = null, x: MAxis<X>, y: MAxis<Y>, op: BarChartWrapper<X, Y>.()->Unit = {}) =
  BarChartWrapper<X, Y>(x, y).attachTo(this, op) { it.title = title }

open class BarChartWrapper<X, Y>(
  node: BarChart<X, Y>,
): XYChartWrapper<X, Y, BarChart<X, Y>>(node) {
  companion object {
	fun <X, Y> BarChart<X, Y>.wrapped() = BarChartWrapper(this)
  }

  constructor(x: MAxis<X>, y: MAxis<Y>): this(BarChart(x.node, y.node))

}