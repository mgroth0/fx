package matt.fx.control.chart.linelike

import com.sun.javafx.charts.Legend.LegendItem
import javafx.animation.Animation
import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.css.CssMetaData
import javafx.css.Styleable
import javafx.css.StyleableBooleanProperty
import javafx.css.StyleableProperty
import javafx.scene.AccessibleRole.TEXT
import javafx.scene.Node
import javafx.scene.layout.StackPane
import matt.fx.control.chart.axis.value.axis.AxisForPackagePrivateProps
import matt.fx.control.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps
import matt.fx.control.chart.linelike.LineLikeChartNodeWithOptionalSymbols.LineOrArea.area
import matt.fx.control.css.BooleanCssMetaData
import matt.lang.go
import java.util.*


abstract class LineLikeChartNode<X, Y>(
    xAxis: AxisForPackagePrivateProps<X>,
    yAxis: AxisForPackagePrivateProps<Y>,
) : XYChartForPackagePrivateProps<X, Y>(xAxis, yAxis) {


    final override fun dataItemChanged(item: Data<X, Y>) {}


    protected abstract val seriesRemovalAnimation: Animation?
    protected abstract fun nullifySeriesRemovalAnimation()

    /** {@inheritDoc}  */
    final override fun seriesBeingRemovedIsAdded(series: Series<X, Y>) {
        seriesRemovalAnimation?.go { anim ->
            anim.onFinished = null
            anim.stop()
            nullifySeriesRemovalAnimation()
            plotChildren.remove(series.getNode())
            for (d in series.getData()) plotChildren.remove(d.node)
            removeSeriesFromDisplay(series)
        }
    }


}


abstract class LineLikeChartNodeWithOptionalSymbols<X, Y>(
    xAxis: AxisForPackagePrivateProps<X>,
    yAxis: AxisForPackagePrivateProps<Y>,

    ) : LineLikeChartNode<X, Y>(xAxis, yAxis) {

    val createSymbols: BooleanProperty = object : StyleableBooleanProperty(true) {
        override fun invalidated() {
            for (seriesIndex in getData().indices) {
                val series = getData()[seriesIndex]
                for (itemIndex in series.data.value.indices) {
                    val item = series.data.value[itemIndex]
                    var symbol = item.nodeProp.value
                    if (get() && symbol == null) { // create any symbols
                        symbol = createSymbol(series, getData().indexOf(series), item, itemIndex)
                        plotChildren.add(symbol)
                    } else if (!get() && symbol != null) { // remove symbols
                        plotChildren.remove(symbol)
                        item.nodeProp.value = null
                    }
                }
            }
            requestChartLayout()
        }

        override fun getBean(): Any {
            return this@LineLikeChartNodeWithOptionalSymbols
        }

        override fun getName(): String {
            return "createSymbols"
        }

        override fun getCssMetaData(): CssMetaData<out LineLikeChartNodeWithOptionalSymbols<*, *>, Boolean> {
            return styleableProps.CREATE_SYMBOLS
        }
    }

    fun createSymbolsProperty(): BooleanProperty {
        return createSymbols
    }

    fun getCreateSymbols(): Boolean {
        return createSymbols.value
    }

    fun setCreateSymbols(value: Boolean) {
        createSymbols.value = value
    }

    enum class LineOrArea { line, area }

    protected abstract val lineOrArea: LineOrArea

    protected fun createSymbol(
        series: Series<X, Y>,
        seriesIndex: Int,
        item: Data<X, Y>,
        itemIndex: Int
    ): Node? {
        var symbol = item.node
        // check if symbol has already been created
        if (symbol == null && getCreateSymbols()) {
            symbol = StackPane()
            symbol.setAccessibleRole(TEXT)
            symbol.setAccessibleRoleDescription("Point")
            symbol.focusTraversableProperty().bind(Platform.accessibilityActiveProperty())
            item.node = symbol
        }
        // set symbol styles
        // Note not sure if we want to add or check, ie be more careful and efficient here
        symbol?.styleClass?.setAll(
            "chart-${lineOrArea.name}-symbol", "series$seriesIndex", "data$itemIndex",
            series.defaultColorStyleClass
        )
        return symbol
    }

    final override fun createLegendItemForSeries(
        series: Series<X, Y>,
        seriesIndex: Int
    ): LegendItem {
        val legendItem = LegendItem(series.name.value)
        val styClass = legendItem.symbol.styleClass
        styClass.addAll(
            "chart-${lineOrArea.name}-symbol", "series$seriesIndex"
        )
        if (lineOrArea == area) {
            styClass.add("area-legend-symbol")
        }

        styClass.add(series.defaultColorStyleClass)

        return legendItem
    }


    private fun createSymbolsIsBound(): Boolean = createSymbols.isBound()

    protected abstract val styleableProps: StyleableProps<*>

    protected abstract class StyleableProps<T : LineLikeChartNodeWithOptionalSymbols<*, *>> {


        internal val CREATE_SYMBOLS: CssMetaData<T, Boolean> = object : BooleanCssMetaData<T>(
            "-fx-create-symbols", true
        ) {

            override fun isSettable(node: T): Boolean {
                return !node.createSymbolsIsBound()
            }

            @Suppress("UNCHECKED_CAST")
            override fun getStyleableProperty(node: T): StyleableProperty<Boolean> {
                return node.createSymbolsProperty() as StyleableProperty<Boolean>
            }
        }


        val classCssMetaData: List<CssMetaData<out Styleable?, *>> by lazy {
            val styleables: MutableList<CssMetaData<out Styleable?, *>> = ArrayList(getClassCssMetaData())
            styleables.add(CREATE_SYMBOLS)
            Collections.unmodifiableList(styleables)
        }

    }

}

