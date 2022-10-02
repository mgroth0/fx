package matt.fx.control.wrapper.chart.line

import javafx.scene.chart.LineChart
import matt.fx.control.wrapper.chart.axis.MAxis
import matt.fx.control.wrapper.chart.xy.XYChartWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp


open class LineChartWrapper<X, Y>(
  node: LineChart<X, Y>
): XYChartWrapper<X, Y, LineChart<X, Y>>(node) {
  constructor(x: MAxis<X>, y: MAxis<Y>): this(LineChart(x.node, y.node))

  val createSymbolsProperty by lazy { node.createSymbolsProperty().toNonNullableProp() }
  var createSymbols by createSymbolsProperty





}

