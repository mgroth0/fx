package matt.fx.control.wrapper.chart.line.highperf

import javafx.scene.chart.Axis
import javafx.scene.chart.LineChart
import matt.collect.itr.applyEach
import matt.fx.control.wrapper.chart.axis.AxisWrapper
import matt.fx.control.wrapper.chart.axis.value.ValueAxisWrapper
import matt.fx.control.wrapper.chart.axis.value.number.NumberAxisWrapper
import matt.fx.control.wrapper.chart.line.LineChartWrapper
import matt.lang.err

/*https://stackoverflow.com/questions/34771612/javafx-linechart-performance*/

open class HighPerformanceLineChart<X, Y>(
  extraHighPerf: Boolean = true,
  xAxis: AxisWrapper<X, out Axis<X>>,
  yAxis: AxisWrapper<Y, out Axis<Y>>
): LineChartWrapper<X, Y>(
  if (extraHighPerf) HighPerformanceFXLineChart(
	xAxis.node, yAxis.node
  ) else LineChart(xAxis.node, yAxis.node)
) {
  init {
	animated = false
	if (extraHighPerf) animatedProperty.onChange {
	  if (it) {
		err("${this::class.simpleName} is unable to animate because dataItemAdded was NOPed")
	  }
	}
	createSymbols = false
	if (extraHighPerf) createSymbolsProperty.onChange {
	  if (it) {
		err("${this::class.simpleName} is unable to create symbols because dataItemAdded was NOPed")
	  }
	}
	isLegendVisible = false
	(listOf(yAxis) + xAxis).applyEach {
	  animated = false
	  isAutoRanging = false
	  isTickMarkVisible = false
	  isTickLabelsVisible = false
	  (this as? ValueAxisWrapper)?.apply {
		isMinorTickVisible = false
		minorTickCount = 0
		(this as? NumberAxisWrapper)?.maximizeTickUnit()
	  }
	}
  }
}


private class HighPerformanceFXLineChart<X, Y>(
  xAxis: Axis<X>, yAxis: Axis<Y>
): LineChart<X, Y>(xAxis, yAxis) {
  override fun dataItemAdded(series: Series<X, Y>?, itemIndex: Int, item: Data<X, Y>?) {    /*NOP*/
  }
}
