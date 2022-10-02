package matt.fx.control.wrapper.chart.pie

import javafx.collections.ObservableList
import javafx.scene.chart.PieChart
import javafx.scene.chart.PieChart.Data
import matt.hurricanefx.wrapper.chart.ChartWrapper

open class PieChartWrapper(
   node: PieChart = PieChart(),
): ChartWrapper<PieChart>(node) {
  companion object {
	fun PieChart.wrapped() = PieChartWrapper(this)
  }

  constructor(data: ObservableList<Data>): this(PieChart(data))


  val data: ObservableList<Data> = node.data


  /**
   * Add and create a PieChart.Data entry. The optional op will be performed on the data instance,
   * a good place to add event handlers to the PieChart.Data.node for example.
   *
   * @return The new Data entry
   */
  fun data(name: String, value: Double, op: PieChart.Data.() -> Unit = {}) = PieChart.Data(name, value).apply {
    data.add(this)
    op(this)
  }

  /**
   * Add and create multiple PieChart.Data entries from the given map.
   */
  fun data(value: Map<String, Double>) = value.forEach { data(it.key, it.value) }


}