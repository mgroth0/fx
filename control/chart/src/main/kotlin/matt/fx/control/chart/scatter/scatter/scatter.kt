/*
 * Copyright (c) 2010, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
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
import javafx.scene.layout.StackPane
import javafx.util.Duration
import matt.fx.control.chart.axis.value.axis.AxisForPackagePrivateProps
import matt.fx.control.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps

/**
 * Chart type that plots symbols for the data points in a series.
 * @since JavaFX 2.0
 */
class ScatterChartForWrapper<X, Y> @JvmOverloads constructor(
    @NamedArg("xAxis") xAxis: AxisForPackagePrivateProps<X>,
    @NamedArg("yAxis") yAxis: AxisForPackagePrivateProps<Y>,
    @NamedArg("data")
    data: ObservableList<Series<X, Y>> = FXCollections.observableArrayList()
) :

    XYChartForPackagePrivateProps<X, Y>(xAxis, yAxis) {


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
    override fun dataItemChanged(item: Data<X, Y>) {}

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
			parallelTransition  = ParallelTransition()
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

	/** {@inheritDoc}  */
	override fun seriesBeingRemovedIsAdded(series: Series<X, Y>) {
		if (parallelTransition != null) {
			parallelTransition!!.onFinished = null
			parallelTransition!!.stop()
			parallelTransition = null
			plotChildren.remove(series.getNode())
			for (d in series.getData()) plotChildren.remove(d.node)
			removeSeriesFromDisplay(series)
		}
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