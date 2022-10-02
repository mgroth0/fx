package matt.fx.control.wrapper.chart.area

import javafx.scene.chart.AreaChart
import matt.fx.control.wrapper.chart.axis.MAxis
import matt.fx.control.wrapper.chart.xy.XYChartWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo

/**
 * Create an AreaChart with optional title, axis and add to the parent pane. The optional op will be performed on the new instance.
 */
fun <X, Y> ET.areachart(title: String? = null, x: MAxis<X>, y: MAxis<Y>, op: AreaChartWrapper<X, Y>.()->Unit = {}) =
  AreaChartWrapper<X, Y>(x, y).attachTo(this, op) { it.title = title }

open class AreaChartWrapper<X, Y>(
  node: AreaChart<X, Y>,
): XYChartWrapper<X, Y, AreaChart<X, Y>>(node) {
  companion object {
	fun <X, Y> AreaChart<X, Y>.wrapped() = AreaChartWrapper(this)
  }

  constructor(x: MAxis<X>, y: MAxis<Y>): this(AreaChart(x.node, y.node))

}