package matt.fx.control.chart.area

import matt.fx.control.chart.axis.MAxis
import matt.fx.control.chart.line.highperf.relinechart.xy.area.AreaChartForPrivateProps
import matt.fx.control.chart.xy.XYChartWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo

/**
 * Create an AreaChart with optional title, axis and add to the parent pane. The optional op will be performed on the new instance.
 */
fun <X: Any, Y: Any> ET.areachart(title: String? = null, x: MAxis<X>, y: MAxis<Y>, op: AreaChartWrapper<X, Y>.() -> Unit = {}) =
    AreaChartWrapper(x, y).attachTo(this, op) { it.title = title }

open class AreaChartWrapper<X: Any, Y: Any>(
    node: AreaChartForPrivateProps<X, Y>
): XYChartWrapper<X, Y, AreaChartForPrivateProps<X, Y>>(node) {

    constructor(x: MAxis<X>, y: MAxis<Y>): this(AreaChartForPrivateProps(x.node, y.node))
}
