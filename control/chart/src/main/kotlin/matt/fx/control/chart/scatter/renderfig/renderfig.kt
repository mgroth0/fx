package matt.fx.control.chart.scatter.renderfig

import javafx.collections.FXCollections
import javafx.scene.shape.Circle
import matt.async.thread.namedThread
import matt.color.IntColor
import matt.fig.modell.plot.GenericPlotData
import matt.fig.modell.plot.axis.Axis
import matt.fig.modell.plot.axis.BasicNumberAxis
import matt.fig.modell.plot.axis.CategoricalAxis
import matt.fig.modell.plot.axis.NumericalAxis
import matt.fig.modell.series.Series
import matt.fig.render.GenericPlotRenderer
import matt.file.JioFile
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
import matt.fx.control.fxapp.runFXAppBlocking
import matt.fx.graphics.fxthread.ensureInFXThreadInPlace
import matt.fx.graphics.wrapper.getterdsl.buildNode
import matt.fx.graphics.wrapper.style.FXColor
import matt.fx.graphics.wrapper.style.hex
import matt.fx.graphics.wrapper.style.toFXColor
import matt.lang.file.toJFile
import matt.lang.go
import matt.lang.model.file.betterURLIGuess
import matt.model.data.dir.VerticalOrHorizontal
import matt.model.data.dir.VerticalOrHorizontal.Horizontal
import matt.model.data.dir.VerticalOrHorizontal.Vertical
import matt.model.data.mathable.MathAndComparable
import matt.model.flowlogic.latch.SimpleThreadLatch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random
import kotlin.reflect.full.functions
import kotlin.reflect.full.valueParameters


class FxPlotRendererForNonFxApp<X, Y> : GenericPlotRenderer<X, Y, ChartWrapper<*>> {
    companion object {
        private var startedFx = AtomicBoolean(false)
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
    override fun render(figData: GenericPlotData<X, Y>): ChartWrapper<*> {
        return ensureInFXThreadInPlace {
            buildNode {
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

                    val tmpStylesheet = JioFile.createTempFile("fx-stylesheet-", ".css")
                    tmpStylesheet.toJFile().deleteOnExit()
                    val stylesheetUrl = tmpStylesheet.betterURLIGuess
                    node.stylesheets.add(stylesheetUrl)

                    figData.series.forEachIndexed { idx, it ->

                        val addFun = data::class.functions.first { it.name == "add" && it.valueParameters.size == 1 }
                        val s = it.toFxSeries()
                        addFun.call(data, s)

                        /*
                        Setting the color this way is the most robust! most likely to work, and also applies most robustly everywhere including legends.
                        * */

                        val cls = when (idx) {
                            0    -> ".chart-symbol"
                            else -> ".default-color${idx}.chart-symbol"
                        }
                        val c = it.color.colorToUse()
                        /*See Modena.css for reference!*/
                        /*Need to override any properties that is overriden in the CSS of the default Modena per index, or else I will get random diamonds and stuff.*/
                        /*this does not work well! Please stop using fx to make charts. Switching to python now...*/
                        tmpStylesheet.appendLine(
                            """
                        $cls { 
                            -fx-background-color: ${c.hex()};
                            -fx-padding: 5;
                        }
                    """.trimIndent()
                        )
                        /**/
                        /*

                        -fx-background-radius: 0;


                        -fx-background-insets: 0;

                        -fx-shape: "";

                        */


                        /*SET LEGEND COLOR*/
                        /* node.style += """
                               .default-color${idx + 1}.chart-symbol {
                                  -fx-background-color: blue;
                               }
                        """.trimIndent()*/


//                    warn("very dirty listeners here!")


//
//                    fun setColor() {
//                        val item = node.legend.items[idx]
//                        fun setColor2() {
//                            val c = it.color.colorToUse()
//                            val theLegendSymbol = (item.symbol as Region)
//
//                            theLegendSymbol.style = "-fx-background-color: blue;"
////                            theLegendSymbol.background = Background.fill(c)
//
//                            theLegendSymbol.isVisible = false
//
//                            println("LEGEND SYMBOL: " + mapOf(
//                                "theLegendSymbol" to theLegendSymbol.toString(),
//                                "style" to theLegendSymbol.style,
//                                "stylesheets" to theLegendSymbol.stylesheets.elementsToString(),
//                                "background" to theLegendSymbol.background.toString()
//                            ).toJsonString())
//                            println("SETTING COLOR:$c")
//                        }
//                        item.symbolProperty().addListener { _, _, _ ->
//                            setColor2()
//                        }
//                        setColor2()
//                    }

//                    node.legend.items.addListener(ListChangeListener {
//                        setColor()
//                    })
//
//                    setColor()
                    }

                }
            }
        }
    }
}

private fun IntColor?.colorToUse() = this?.toFXColor() ?: FXColor.BLUE

fun Series<*, *>.toFxSeries(): SeriesWrapper<MathAndComparable<*>, MathAndComparable<*>> {
    val seriesColor = color.colorToUse()
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


fun Axis.toFxAxis(): AxisWrapper<out Comparable<*>, out AxisForPackagePrivateProps<out Comparable<*>>> {
    val r = when (this) {
        is CategoricalAxis    -> CategoryAxisWrapper().apply {
            categories = FXCollections.observableList(
                this@toFxAxis.sortedCategories
            )
        }

        is BasicNumberAxis<*> -> {
            numAxis().also {
                upperBound?.go { u ->
                    val prop = it.node.upperBound::value
                    /*used to just prop.set() before 2.0.0-Beta1 made that a compiler error*/
                    prop.setter.call(u)
                }
                lowerBound?.go { l ->
                    val prop = it.node.lowerBound::value
                    /*used to just prop.set() before 2.0.0-Beta1 made that a compiler error*/
                    prop.setter.call(l)
                }
                minorTickCount?.go { c ->
                    it.minorTickCount = c
                }
                majorTickUnit?.go { tu ->
                    /*used to just prop.set() before 2.0.0-Beta1 made that a compiler error*/
                    it::tickUnit.setter.call(tu)
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