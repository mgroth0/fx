package matt.fx.control.chart.scatter.renderfig

import javafx.collections.FXCollections
import javafx.scene.shape.Circle
import matt.fig.model.plot.PlotData
import matt.fig.model.plot.axis.Axis
import matt.fig.model.plot.axis.BasicNumberAxis
import matt.fig.model.plot.axis.CategoricalAxis
import matt.fig.model.plot.axis.NumericalAxis
import matt.fig.model.series.Series
import matt.fig.render.PlotRenderer
import matt.fx.control.chart.ChartWrapper
import matt.fx.control.chart.axis.AxisWrapper
import matt.fx.control.chart.axis.cat.CategoryAxisWrapper
import matt.fx.control.chart.axis.value.axis.AxisForPackagePrivateProps
import matt.fx.control.chart.axis.value.number.NumberAxisWrapper
import matt.fx.control.chart.axis.value.number.numAxis
import matt.fx.control.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps
import matt.fx.control.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps.Data
import matt.fx.control.chart.scatter.scatterChart
import matt.fx.control.chart.xy.series.SeriesWrapper
import matt.fx.graphics.wrapper.getterdsl.buildNode
import matt.fx.graphics.wrapper.style.FXColor
import matt.fx.graphics.wrapper.style.toFXColor
import matt.lang.go
import matt.model.data.dir.VerticalOrHorizontal
import matt.model.data.dir.VerticalOrHorizontal.Horizontal
import matt.model.data.dir.VerticalOrHorizontal.Vertical
import matt.model.data.mathable.MathAndComparable
import kotlin.random.Random
import kotlin.reflect.full.functions
import kotlin.reflect.full.valueParameters

object FxPlotRenderer : PlotRenderer<ChartWrapper<*>> {
    override fun render(figData: PlotData<*,*>): ChartWrapper<*> {
        return buildNode {
            val axisX = figData.xAxis.toFxAxis()
            val axisY = figData.yAxis.toFxAxis()
            scatterChart(
                x = axisX,
                y = axisY
            ) {
                animated = false
                figData.title?.go {
                    title = it
                }
                node.horizontalGridLinesVisibleProperty().set(false)
                node.verticalGridLinesVisibleProperty().set(false)
                fun showStrips(dir: VerticalOrHorizontal) {
                    when (dir) {
                        Horizontal -> {
                            node.horizontalGridLinesVisibleProperty().set(true)
                        }

                        Vertical   -> {
                            node.verticalGridLinesVisibleProperty().set(true)
                        }
                    }
                    node.lookup(".chart-${dir.name.lowercase()}-grid-lines").style =
                        "-fx-stroke-dash-array: null; -fx-stroke-width: 0.5;"
                }
                if (figData.xAxis is CategoricalAxis) {
                    showStrips(Vertical)
                }
                if (figData.yAxis is CategoricalAxis) {
                    showStrips(Horizontal)
                }


                figData.series.forEach {

                    val addFun = data::class.functions.first { it.name == "add" && it.valueParameters.size == 1 }
                    val s = it.toFxSeries()
                    addFun.call(data, s)


                }

            }
        }
    }
}


fun Series<*, *>.toFxSeries(): SeriesWrapper<MathAndComparable<*>, MathAndComparable<*>> {
    val seriesColor = color?.toFXColor() ?: FXColor.BLUE
    val thePoints = points.map {
        Data(
            xValue = it.x as MathAndComparable<*>,
            yValue = it.y as MathAndComparable<*>
        ).also {
            val circ = Circle(5.0, seriesColor)
            it.node = circ
            if (jitter) {
                circ.translateY = Random.nextDouble() * 10.0 - 5.0
            }
        }
    }
    val thePointsO = FXCollections.observableList(thePoints)
    return SeriesWrapper<MathAndComparable<*>, MathAndComparable<*>>(
        label?.let {
            XYChartForPackagePrivateProps.Series(
                it,
                thePointsO
            )
        } ?: XYChartForPackagePrivateProps.Series(
            thePointsO
        )
    )
}


fun Axis.toFxAxis(): AxisWrapper<out Comparable<*>, out AxisForPackagePrivateProps<out Comparable<*>>> {
    val r = when (this) {
        is CategoricalAxis -> CategoryAxisWrapper().apply {
            categories = FXCollections.observableList(
                this@toFxAxis.sortedCategories
            )
        }

        is BasicNumberAxis<*> -> {
            numAxis().also {
                upperBound?.go { u ->
                    val prop = it.node.upperBound::value
                    prop.set(u)
                }
                lowerBound?.go { l ->
                    val prop = it.node.lowerBound::value
                    prop.set(l)
                }
                minorTickCount?.go { c ->
                    it.minorTickCount = c
                }
                majorTickUnit?.go { tu ->
                    it::tickUnit.set(tu)
                }
                it.isTickMarkVisible = showTicks
            }
        }
    }
    r.apply {
        animated = false
        isAutoRanging = false
    }
    label?.also {
        r.axisLabel = it
    }

    return r
}

fun NumericalAxis<*>.toFxAxis(): AxisWrapper<*, *> = (this as Axis).toFxAxis() as NumberAxisWrapper