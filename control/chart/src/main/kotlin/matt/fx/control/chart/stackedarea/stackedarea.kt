package matt.fx.control.chart.stackedarea

import matt.fx.control.chart.axis.MAxis
import matt.fx.control.chart.stackedarea.stackedarea.StackedAreaChartForWrapper
import matt.fx.control.chart.xy.XYChartWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo

fun <X : Any, Y : Any> ET.stackedareachart(
    title: String? = null,
    x: MAxis<X>,
    y: MAxis<Y>,
    op: StackedAreaChartWrapper<X, Y>.() -> Unit = {}
) = StackedAreaChartWrapper<X, Y>(x, y).attachTo(this, op) { it.title = title }


open class StackedAreaChartWrapper<X : Any, Y : Any>(
    node: StackedAreaChartForWrapper<X, Y>
) : XYChartWrapper<X, Y, StackedAreaChartForWrapper<X, Y>>(node) {

    constructor(
        x: MAxis<X>,
        y: MAxis<Y>
    ) : this(StackedAreaChartForWrapper(x.node, y.node))
}
