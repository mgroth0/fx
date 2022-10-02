package matt.fx.control.wrapper.chart.line

import javafx.scene.chart.LineChart
import matt.fx.control.wrapper.chart.axis.MAxis
import matt.fx.control.wrapper.chart.xy.XYChartWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp



/**
 * Create a LineChart with optional title, axis and add to the parent pane. The optional op will be performed on the new instance.
 */
fun <X, Y> ET.linechart(title: String? = null, x: MAxis<X>, y: MAxis<Y>, op: LineChartWrapper<X, Y>.()->Unit = {}) =
  LineChartWrapper(x, y).attachTo(this, op) { it.title = title }

open class LineChartWrapper<X, Y>(
  node: LineChart<X, Y>
): XYChartWrapper<X, Y, LineChart<X, Y>>(node) {
  constructor(x: MAxis<X>, y: MAxis<Y>): this(LineChart(x.node, y.node))

  val createSymbolsProperty by lazy { node.createSymbolsProperty().toNonNullableProp() }
  var createSymbols by createSymbolsProperty





}

