package matt.fx.control.chart.axis.value.number.tickconfig

import javafx.util.StringConverter
import matt.fig.model.plot.axis.tick.TickConfigurer
import matt.fx.control.chart.axis.value.number.NumberAxisWrapper
import matt.fx.control.chart.line.LineChartWrapper
import matt.fx.control.chart.xy.XYChartWrapper
import matt.model.data.mathable.MathAndComparable

inline fun <reified X : MathAndComparable<X>, reified Y : MathAndComparable<Y>> LineChartWrapper<X, Y>.showBestTicks() {
    (xAxis as NumberAxisWrapper<X>).showBestTicksNoLayout()
    (yAxis as NumberAxisWrapper<Y>).showBestTicksNoLayout()
    requestLayout()
}

fun <T : MathAndComparable<T>> NumberAxisWrapper<T>.showBestTicksNoLayout() {
    tickConfigurer.showBestTicksNoLayout(this)
}

fun <T : MathAndComparable<T>> NumberAxisWrapper<T>.showBestTicksIn(chart: XYChartWrapper<*, *, *>) {
    showBestTicksNoLayout()
    chart.requestLayout()
}










fun <T : MathAndComparable<T>> TickConfigurer<T>.showBestTicksNoLayout(axis: NumberAxisWrapper<T>) {
    val amountShown = (axis.upperBound - axis.lowerBound)


    axis.apply {
        isTickMarkVisible = true
        isMinorTickVisible = true
        minorTickCount = this@TickConfigurer.minorTickCount
        isTickLabelsVisible = true
    }


    axis.tickUnit = bestTickUnit(amountShown)


    val converter = tickLabelConverter(amountShown)

    val stringConverter = object : StringConverter<T>() {

        override fun toString(`object`: T): String {
            return converter.invoke(`object`)
        }

        override fun fromString(string: String): T {
            TODO("Not yet implemented")
        }


    }

    axis.tickLabelFormatter = stringConverter

}

fun <T : MathAndComparable<T>> TickConfigurer<T>.showBestTicksIn(
    axis: NumberAxisWrapper<T>,
    chart: LineChartWrapper<*, *>
) {
    showBestTicksNoLayout(axis)
    chart.requestLayout()
}