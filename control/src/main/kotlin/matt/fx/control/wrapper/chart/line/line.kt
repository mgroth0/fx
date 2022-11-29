package matt.fx.control.wrapper.chart.line

import matt.fx.control.wrapper.chart.line.highperf.relinechart.MorePerfOptionsLineChart
import matt.fx.control.wrapper.chart.line.highperf.relinechart.xy.XYChart.Data
import matt.fx.control.wrapper.chart.axis.MAxis
import matt.fx.control.wrapper.chart.axis.value.number.NumberAxisWrapper
import matt.fx.control.wrapper.chart.xy.XYChartWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.math.point.BasicPoint
import matt.model.data.mathable.MathAndComparable


/**
 * Create a LineChart with optional title, axis and add to the parent pane. The optional op will be performed on the new instance.
 */
fun <X: MathAndComparable<X>, Y: MathAndComparable<Y>> ET.linechart(
  title: String? = null,
  x: MAxis<X>,
  y: MAxis<Y>,
  op: LineChartWrapper<X, Y>.()->Unit = {}
) =
  LineChartWrapper(x, y).attachTo(this, op) { it.title = title }

open class LineChartWrapper<X: MathAndComparable<X>, Y: MathAndComparable<Y>>(
  node: MorePerfOptionsLineChart<X, Y>
): XYChartWrapper<X, Y, MorePerfOptionsLineChart<X, Y>>(node) {
  constructor(x: MAxis<X>, y: MAxis<Y>): this(MorePerfOptionsLineChart(x.node, y.node))

  val createSymbolsProperty by lazy { node.createSymbolsProperty().toNonNullableProp() }
  var createSymbols by createSymbolsProperty


  fun displayPixelOf(x: X, y: Y) = BasicPoint(
	x = (xAxis as NumberAxisWrapper<X>).displayPixelOf(x),
	y = (yAxis as NumberAxisWrapper<Y>).displayPixelOf(y)
  )



  fun valueForPosition(xPixels: Double, yPixels: Double) {
	Data<X, Y>(
	  (xAxis as NumberAxisWrapper<X>).valueForDisplayPixel(xPixels),
	  (yAxis as NumberAxisWrapper<Y>).valueForDisplayPixel(yPixels)
	)
  }

}

