package matt.fx.control.wrapper.chart.line.highperf

import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import matt.collect.itr.applyEach
import matt.fx.control.wrapper.chart.line.num.NumberLineChart
import matt.hurricanefx.eye.lib.onChange
import matt.lang.err

/*https://stackoverflow.com/questions/34771612/javafx-linechart-performance*/

open class HighPerformanceLineChart: NumberLineChart(HighPerformanceFXLineChart()) {
  init {
	animated = false
	animatedProperty.onChange {
	  if (it) {
		err("${this::class.simpleName} is unable to animate because dataItemAdded was NOPed")
	  }
	}
	createSymbols = false
	createSymbolsProperty.onChange {
	  if (it) {
		err("${this::class.simpleName} is unable to create symbols because dataItemAdded was NOPed")
	  }
	}
	isLegendVisible = false
	(listOf(yAxis) + xAxis).applyEach {
	  animated = false
	  isAutoRanging = false
	  isTickMarkVisible = false
	  isMinorTickVisible = false
	  minorTickCount = 0
	  isTickLabelsVisible = false
	  tickUnit = Double.MAX_VALUE
	}
  }
}


private class HighPerformanceFXLineChart: LineChart<Number, Number>(NumberAxis(), NumberAxis()) {
  override fun dataItemAdded(series: Series<Number, Number>?, itemIndex: Int, item: Data<Number, Number>?) {
	/*NOP*/
  }
}
