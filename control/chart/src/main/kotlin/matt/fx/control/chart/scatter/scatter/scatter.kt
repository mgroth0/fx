package matt.fx.control.chart.scatter.scatter

import com.sun.javafx.charts.Legend.LegendItem
import javafx.animation.FadeTransition
import javafx.animation.ParallelTransition
import javafx.application.Platform
import javafx.beans.NamedArg
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.AccessibleRole.TEXT
import javafx.scene.chart.ScatterChart
import javafx.scene.layout.StackPane
import javafx.util.Duration
import matt.fx.base.rewrite.ReWrittenFxClass
import matt.fx.control.chart.axis.value.axis.AxisForPackagePrivateProps
import matt.fx.control.chart.linelike.LineLikeChartNode

@ReWrittenFxClass(ScatterChart::class)
class ScatterChartForWrapper<X, Y> @JvmOverloads constructor(
    @NamedArg("xAxis") xAxis: AxisForPackagePrivateProps<X>,
    @NamedArg("yAxis") yAxis: AxisForPackagePrivateProps<Y>,
    @NamedArg("data")
    data: ObservableList<Series<X, Y>> = FXCollections.observableArrayList()
) : LineLikeChartNode<X, Y>(xAxis, yAxis) {


    private var parallelTransition: ParallelTransition? = null

    /**
     * Construct a new ScatterChart with the given axis and data.
     *
     * @param xAxis The x axis to use
     * @param yAxis The y axis to use
     * @param data The data to use, this is the actual list used so any changes to it will be reflected in the chart
     */
    // -------------- CONSTRUCTORS ----------------------------------------------
    /**
     * Construct a new ScatterChart with the given axis and data.
     *
     * @param xAxis The x axis to use
     * @param yAxis The y axis to use
     */
    init {
        setData(data)
    }
    // -------------- METHODS ------------------------------------------------------------------------------------------
    /** {@inheritDoc}  */
    override fun dataItemAdded(
        series: Series<X, Y>,
        itemIndex: Int,
        item: Data<X, Y>
    ) {
        var symbol = item.nodeProp.value
        // check if symbol has already been created
        if (symbol == null) {
            symbol = StackPane()
            symbol.setAccessibleRole(TEXT)
            symbol.setAccessibleRoleDescription("Point")
            symbol.focusTraversableProperty().bind(Platform.accessibilityActiveProperty())
            item.nodeProp.value = symbol
        }
        // set symbol styles
        symbol.styleClass.setAll(
            "chart-symbol", "series" + data.value.indexOf(series), "data$itemIndex",
            series.defaultColorStyleClass
        )
        // add and fade in new symbol if animated
        if (shouldAnimate()) {
            symbol.opacity = 0.0
            plotChildren.add(symbol)
            val ft = FadeTransition(Duration.millis(500.0), symbol)
            ft.toValue = 1.0
            ft.play()
        } else {
            plotChildren.add(symbol)
        }
    }

    /** {@inheritDoc}  */
    override fun dataItemRemoved(
        item: Data<X, Y>,
        series: Series<X, Y>
    ) {
        val symbol = item.nodeProp.value
        symbol?.focusTraversableProperty()?.unbind()
        if (shouldAnimate()) {
            // fade out old symbol
            val ft = FadeTransition(Duration.millis(500.0), symbol)
            ft.toValue = 0.0
            ft.onFinished = EventHandler {
                plotChildren.remove(symbol)
                removeDataItemFromDisplay(series, item)
                symbol!!.opacity = 1.0
            }
            ft.play()
        } else {
            plotChildren.remove(symbol)
            removeDataItemFromDisplay(series, item)
        }
    }

    /** {@inheritDoc}  */
    override fun seriesAdded(
        series: Series<X, Y>,
        seriesIndex: Int
    ) {
        // handle any data already in series
        for (j in series.data.value.indices) {
            dataItemAdded(series, j, series.data.value[j])
        }
    }

    /** {@inheritDoc}  */
    override fun seriesRemoved(series: Series<X, Y>) {
        // remove all symbol nodes
        if (shouldAnimate()) {
            parallelTransition = ParallelTransition()
            parallelTransition!!.onFinished = EventHandler {
                removeSeriesFromDisplay(
                    series
                )
            }
            for (d in series.data.value) {
                val symbol = d.nodeProp.value
                // fade out old symbol
                val ft = FadeTransition(Duration.millis(500.0), symbol)
                ft.toValue = 0.0
                ft.onFinished = EventHandler {
                    plotChildren.remove(symbol)
                    symbol.opacity = 1.0
                }
                parallelTransition!!.children.add(ft)
            }
            parallelTransition!!.play()
        } else {
            for (d in series.data.value) {
                val symbol = d.nodeProp.value
                plotChildren.remove(symbol)
            }
            removeSeriesFromDisplay(series)
        }
    }


    override val seriesRemovalAnimation by ::parallelTransition
    override fun nullifySeriesRemovalAnimation() {
        parallelTransition = null
    }

    /** {@inheritDoc}  */
    override fun layoutPlotChildren() {
        // update symbol positions
        for (seriesIndex in 0..<dataSize) {
            val series = data.value[seriesIndex]
            val it = getDisplayedDataIterator(series)
            while (it.hasNext()) {
                val item = it.next()
                val x = xAxis.getDisplayPosition(item.currentX.value)
                val y = yAxis.getDisplayPosition(item.currentY.value)
                if (java.lang.Double.isNaN(x) || java.lang.Double.isNaN(y)) {
                    continue
                }
                val symbol = item.nodeProp.value
                if (symbol != null) {
                    val w = symbol.prefWidth(-1.0)
                    val h = symbol.prefHeight(-1.0)
                    symbol.resizeRelocate(x - w / 2, y - h / 2, w, h)
                }
            }
        }
    }

    override fun createLegendItemForSeries(
        series: Series<X, Y>,
        seriesIndex: Int
    ): LegendItem {
        val legendItem = LegendItem(series.name.value)
        val node = if (series.data.value.isEmpty()) null else series.data.value[0].nodeProp
        if (node != null) {
            legendItem.symbol.styleClass.addAll(node.value.styleClass)
        }
        return legendItem
    }
}