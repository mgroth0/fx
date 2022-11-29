package matt.fx.control.wrapper.chart.scatter

import javafx.scene.chart.ScatterChart
import matt.fx.control.wrapper.chart.axis.MAxis
import matt.fx.control.wrapper.chart.scatter.scatter.ScatterChartForWrapper
import matt.fx.control.wrapper.chart.xy.XYChartWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo

/**
 * Create a ScatterChart with optional title, axis and add to the parent pane. The optional op will be performed on the new instance.
 */
fun <X, Y> ET.scatterchart(
  title: String? = null,
  x: MAxis<X>,
  y: MAxis<Y>,
  op: ScatterChartWrapper<X, Y>.()->Unit = {}
) =
  ScatterChartWrapper(x, y).attachTo(this, op) { it.title = title }

open class ScatterChartWrapper<X, Y>(
  node: ScatterChartForWrapper<X, Y>
): XYChartWrapper<X, Y, ScatterChartForWrapper<X, Y>>(node) {

  constructor(x: MAxis<X>, y: MAxis<Y>): this(ScatterChartForWrapper(x.node, y.node))

}