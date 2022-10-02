package matt.fx.control.wrapper.chart.line

import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart.Data
import javafx.scene.chart.XYChart.Series
import matt.collect.itr.applyEach
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.wrapper.chart.axis.MAxis
import matt.hurricanefx.wrapper.chart.axis.value.number.NumberAxisWrapper
import matt.hurricanefx.wrapper.chart.xy.XYChartWrapper




open class LineChartWrapper<X, Y>(
  node: LineChart<X, Y>
): XYChartWrapper<X, Y, LineChart<X, Y>>(node) {
  constructor(x: MAxis<X>, y: MAxis<Y>): this(LineChart(x.node, y.node))

  val createSymbolsProperty by lazy { node.createSymbolsProperty().toNonNullableProp() }
  var createSymbols by createSymbolsProperty





}

