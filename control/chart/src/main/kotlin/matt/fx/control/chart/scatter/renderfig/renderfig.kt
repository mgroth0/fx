package matt.fx.control.chart.scatter.renderfig

import javafx.collections.FXCollections
import javafx.scene.shape.Circle
import matt.async.thread.namedThread
import matt.color.common.IntColor
import matt.fig.modell.plot.GenericPlotData
import matt.fig.modell.plot.axis.Axis
import matt.fig.modell.plot.axis.BasicNumberAxis
import matt.fig.modell.plot.axis.CategoricalAxis
import matt.fig.modell.series.Series
import matt.fig.render.GenericPlotRenderer
import matt.file.JioFile
import matt.fx.control.chart.ChartWrapper
import matt.fx.control.chart.axis.AxisWrapper
import matt.fx.control.chart.axis.cat.CategoryAxisWrapper
import matt.fx.control.chart.axis.value.axis.AxisForPackagePrivateProps
import matt.fx.control.chart.axis.value.number.numAxis
import matt.fx.control.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps
import matt.fx.control.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps.Data
import matt.fx.control.chart.scatter.scatterChart
import matt.fx.control.chart.xy.series.SeriesWrapper
import matt.fx.control.fxapp.runFXAppBlocking
import matt.fx.graphics.fxthread.ensureInFXThreadInPlace
import matt.fx.graphics.wrapper.getterdsl.buildNode
import matt.fx.graphics.wrapper.style.FXColor
import matt.fx.graphics.wrapper.style.hex
import matt.fx.graphics.wrapper.style.toFXColor
import matt.lang.atomic.AtomicBool
import matt.lang.common.go
import matt.lang.file.deleteOnExit
import matt.lang.model.file.betterURLIGuess
import matt.model.data.dir.VerticalOrHorizontal
import matt.model.data.dir.VerticalOrHorizontal.Horizontal
import matt.model.data.dir.VerticalOrHorizontal.Vertical
import matt.model.data.mathable.MathAndComparable
import matt.model.flowlogic.latch.j.SimpleThreadLatch
import kotlin.random.Random
import kotlin.reflect.full.functions
import kotlin.reflect.full.valueParameters


class FxPlotRendererForNonFxApp<X, Y> : GenericPlotRenderer<X, Y, ChartWrapper<*>> {
    companion object {
        private val startedFx = AtomicBool(false)
    }

    override fun render(figData: GenericPlotData<X, Y>): ChartWrapper<*> {
        if (!startedFx.getAndSet(true)) {
            val ready = SimpleThreadLatch()
            namedThread("fx thread") {
                runFXAppBlocking {
                    ready.open()
                }
            }
            ready.await()
        }
        return FxPlotRenderer<X, Y>().render(figData)
    }
}


class FxPlotRenderer<X, Y> : GenericPlotRenderer<X, Y, ChartWrapper<*>> {

    override fun render(figData: GenericPlotData<X, Y>): ChartWrapper<*> =
        ensureInFXThreadInPlace {
            buildNode {
                val axisX = figData.xAxis.toFxAxis<X>()
                val axisY = figData.yAxis.toFxAxis<Y>()
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

                    val tmpStylesheet = JioFile.createTempFile("fx-stylesheet-", ".css")
                    tmpStylesheet.deleteOnExit()
                    val stylesheetUrl = tmpStylesheet.betterURLIGuess
                    node.stylesheets.add(stylesheetUrl)

                    figData.series.forEachIndexed { idx, it ->

                        val addFun = data::class.functions.first { it.name == "add" && it.valueParameters.size == 1 }
                        val s = it.toFxSeries()
                        addFun.call(data, s)

                        /*
                        Setting the color this way is the most robust! most likely to work, and also applies most robustly everywhere including legends.
                         * */

                        val cls =
                            when (idx) {
                                0    -> ".chart-symbol"
                                else -> ".default-color$idx.chart-symbol"
                            }
                        val c = it.color.colorToUse()
                        /*See Modena.css for reference!


                        Need to override any properties that is overriden in the CSS of the default Modena per index, or else I will get random diamonds and stuff.


                        this does not work well! Please stop using fx to make charts. Switching to python now...*/
                        tmpStylesheet.appendLine(
                            """
                            $cls { 
                                -fx-background-color: ${c.hex()};
                                -fx-padding: 5;
                            }
                            """.trimIndent()
                        )
                        /*





                        -fx-background-radius: 0;


                        -fx-background-insets: 0;

                        -fx-shape: "";




                        SET LEGEND COLOR



                         node.style += """
                               .default-color${idx + 1}.chart-symbol {
                                  -fx-background-color: blue;
                               }
                        """.trimIndent()*/
                    }
                }
            }
        }
}

private fun IntColor?.colorToUse() = this?.toFXColor() ?: FXColor.BLUE

fun Series<*, *>.toFxSeries(): SeriesWrapper<MathAndComparable<*>, MathAndComparable<*>> {
    val seriesColor = color.colorToUse()
    val thePoints =
        points.map {
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
    return SeriesWrapper(
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


fun <T> Axis.toFxAxis(): AxisWrapper<out Any, out AxisForPackagePrivateProps<out Any>> {
    val r =
        when (this) {
            is CategoricalAxis    ->
                CategoryAxisWrapper().apply {
                    categories =
                        FXCollections.observableList(
                            this@toFxAxis.sortedCategories
                        )
                } as AxisWrapper<out Any, out AxisForPackagePrivateProps<out Any>>


            is BasicNumberAxis<*> -> {
                @Suppress("MUTABLE_PROPERTY_WITH_CAPTURED_TYPE") /*newly required for compiling this without a warning in 2.0.0-Beta4*/
                numAxis().also {
                    upperBound?.go { u ->
                        val prop = it.node.upperBound::value
                        /*used to just prop.set() before 2.0.0-Beta1 made that a compiler error (still a bug as of 2.0.0-Beta3)
                        Something changed in 2.0.0-Beta4 ... now it is an even worse error but if I change it back now its a new warning?*/
                        prop.setter.call(u)
                        /*prop.set(u)*/
                    }
                    lowerBound?.go { l ->
                        val prop = it.node.lowerBound::value
                        /*used to just prop.set() before 2.0.0-Beta1 made that a compiler error (still a bug as of 2.0.0-Beta3)
                        Something changed in 2.0.0-Beta4 ... now it is an even worse error but if I change it back now its a new warning?*/
                        prop.setter.call(l)
                        /*prop.set(l)*/
                    }
                    minorTickCount?.go { c ->
                        it.minorTickCount = c
                    }
                    majorTickUnit?.go { tu ->
                        /*used to just prop.set() before 2.0.0-Beta1 made that a compiler error (still a bug as of 2.0.0-Beta3)
                        Something changed in 2.0.0-Beta4 ... now it is an even worse error but if I change it back now its a new warning?*/
                        it::tickUnit.setter.call(tu)
                        /*it::tickUnit.set(tu)*/
                    }
                    it.isTickMarkVisible = showTicks
                } as AxisWrapper<out Any, out AxisForPackagePrivateProps<out Any>>
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


/*fun <T: MathAndComparable<T>> NumericalAxis<T>.toFxAxis(): AxisWrapper<*, *> = (this as Axis).toFxAxis<T>() as NumberAxisWrapper<T>*/
