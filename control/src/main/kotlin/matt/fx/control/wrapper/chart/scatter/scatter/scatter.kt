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
package matt.fx.control.wrapper.chart.scatter.scatter

import com.sun.javafx.charts.Legend.LegendItem
import javafx.animation.FadeTransition
import javafx.animation.ParallelTransition
import javafx.application.Platform
import javafx.beans.NamedArg
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.AccessibleRole.TEXT
import javafx.scene.chart.Axis
import javafx.scene.layout.StackPane
import javafx.util.Duration
import matt.fx.control.wrapper.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps

/**
 * Chart type that plots symbols for the data points in a series.
 * @since JavaFX 2.0
 */
class ScatterChartForWrapper<X, Y> @JvmOverloads constructor(
  @NamedArg("xAxis") xAxis: Axis<X>?,
  @NamedArg("yAxis") yAxis: Axis<Y>?,
  @NamedArg("data")
  data: ObservableList<Series<X, Y>?>? = FXCollections.observableArrayList()
):
  XYChartForPackagePrivateProps<X?, Y?>(xAxis, yAxis) {
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
  override fun dataItemAdded(series: Series<X?, Y?>, itemIndex: Int, item: Data<X?, Y?>) {
	var symbol = item.node
	// check if symbol has already been created
	if (symbol == null) {
	  symbol = StackPane()
	  symbol.setAccessibleRole(TEXT)
	  symbol.setAccessibleRoleDescription("Point")
	  symbol.focusTraversableProperty().bind(Platform.accessibilityActiveProperty())
	  item.node = symbol
	}
	// set symbol styles
	symbol.styleClass.setAll(
	  "chart-symbol", "series" + data.indexOf(series), "data$itemIndex",
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
  override fun dataItemRemoved(item: Data<X?, Y?>, series: Series<X?, Y?>) {
	val symbol = item.node
	symbol?.focusTraversableProperty()?.unbind()
	if (shouldAnimate()) {
	  // fade out old symbol
	  val ft = FadeTransition(Duration.millis(500.0), symbol)
	  ft.toValue = 0.0
	  ft.onFinished = EventHandler { actionEvent: ActionEvent? ->
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
  override fun dataItemChanged(item: Data<X?, Y?>) {}

  /** {@inheritDoc}  */
  override fun seriesAdded(series: Series<X?, Y?>, seriesIndex: Int) {
	// handle any data already in series
	for (j in series.data.indices) {
	  dataItemAdded(series, j, series.data[j])
	}
  }

  /** {@inheritDoc}  */
  override fun seriesRemoved(series: Series<X?, Y?>) {
	// remove all symbol nodes
	if (shouldAnimate()) {
	  val pt = ParallelTransition()
	  pt.onFinished = EventHandler { event: ActionEvent? ->
		removeSeriesFromDisplay(
		  series
		)
	  }
	  for (d in series.data) {
		val symbol = d.node
		// fade out old symbol
		val ft = FadeTransition(Duration.millis(500.0), symbol)
		ft.toValue = 0.0
		ft.onFinished = EventHandler { actionEvent: ActionEvent? ->
		  plotChildren.remove(symbol)
		  symbol.opacity = 1.0
		}
		pt.children.add(ft)
	  }
	  pt.play()
	} else {
	  for (d in series.data) {
		val symbol = d.node
		plotChildren.remove(symbol)
	  }
	  removeSeriesFromDisplay(series)
	}
  }

  /** {@inheritDoc}  */
  override fun layoutPlotChildren() {
	// update symbol positions
	for (seriesIndex in 0 until dataSize) {
	  val series = data[seriesIndex]
	  val it = getDisplayedDataIterator(series)
	  while (it.hasNext()) {
		val item = it.next()
		val x = xAxis.getDisplayPosition(item.currentX)
		val y = yAxis.getDisplayPosition(item.currentY)
		if (java.lang.Double.isNaN(x) || java.lang.Double.isNaN(y)) {
		  continue
		}
		val symbol = item.node
		if (symbol != null) {
		  val w = symbol.prefWidth(-1.0)
		  val h = symbol.prefHeight(-1.0)
		  symbol.resizeRelocate(x - w/2, y - h/2, w, h)
		}
	  }
	}
  }

  public override fun createLegendItemForSeries(series: Series<X?, Y?>, seriesIndex: Int): LegendItem {
	val legendItem = LegendItem(series.name)
	val node = if (series.data.isEmpty()) null else series.data[0].node
	if (node != null) {
	  legendItem.symbol.styleClass.addAll(node.styleClass)
	}
	return legendItem
  }
}