package matt.fx.control.chart.pie

import javafx.collections.ObservableList
import matt.fx.control.chart.ChartWrapper
import matt.fx.control.chart.pie.pie.PieChartForWrapper
import matt.fx.control.chart.pie.pie.PieChartForWrapper.Data
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attach


/**
 * Create a PieChart with optional title data and add to the parent pane. The optional op will be performed on the new instance.
 */
fun ET.piechart(
  title: String? = null, data: ObservableList<Data>? = null, op: PieChartWrapper.()->Unit = {}
): PieChartWrapper {
  val chart = if (data != null) PieChartWrapper(data) else PieChartWrapper()
  chart.title = title
  return attach(chart, op)
}


open class PieChartWrapper(
  node: PieChartForWrapper = PieChartForWrapper(),
): ChartWrapper<PieChartForWrapper>(node) {

  constructor(data: ObservableList<Data>): this(PieChartForWrapper(data))


  val data: ObservableList<Data> = node.data.value


  /**
   * Add and create a PieChart.Data entry. The optional op will be performed on the data instance,
   * a good place to add event handlers to the PieChart.Data.node for example.
   *
   * @return The new Data entry
   */
  fun data(name: String, value: Double, op: PieChartForWrapper.Data.() -> Unit = {}) = PieChartForWrapper.Data(name, value).apply {
    data.add(this)
    op(this)
  }

  /**
   * Add and create multiple PieChart.Data entries from the given map.
   */
  fun data(value: Map<String, Double>) = value.forEach { data(it.key, it.value) }


}