package matt.fx.control.chart.bubble.bubble

import com.sun.javafx.charts.Legend.LegendItem
import javafx.animation.FadeTransition
import javafx.animation.ParallelTransition
import javafx.application.Platform
import javafx.beans.NamedArg
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.AccessibleAttribute
import javafx.scene.AccessibleAttribute.TEXT
import javafx.scene.AccessibleRole
import javafx.scene.Node
import javafx.scene.layout.StackPane
import javafx.scene.shape.Ellipse
import javafx.util.Duration
import matt.fx.control.chart.axis.cat.cat.CategoryAxisForCatAxisWrapper
import matt.fx.control.chart.axis.value.axis.AxisForPackagePrivateProps
import matt.fx.control.chart.axis.value.moregenval.MoreGenericValueAxis
import matt.fx.control.chart.axis.value.number.moregennum.MoreGenericNumberAxis
import matt.fx.control.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps
import kotlin.math.abs

/**
 * Chart type that plots bubbles for the data points in a series. The extra value property of Data is used to represent
 * the radius of the bubble it should be a java.lang.Number.
 * @since JavaFX 2.0
 */
class BubbleChartForWrapper<X, Y> @JvmOverloads constructor(
  @NamedArg("xAxis") xAxis: AxisForPackagePrivateProps<X>,
  @NamedArg("yAxis") yAxis: AxisForPackagePrivateProps<Y>,
  @NamedArg("data")
  data: ObservableList<Series<X, Y>> = FXCollections.observableArrayList()
):
  XYChartForPackagePrivateProps<X, Y>(xAxis, yAxis) {
  /**
   * Construct a new BubbleChart with the given axis and data. BubbleChart does not
   * use a Category Axis. Both X and Y axes should be of type NumberAxis.
   *
   * @param xAxis The x axis to use
   * @param yAxis The y axis to use
   * @param data The data to use, this is the actual list used so any changes to it will be reflected in the chart
   */
  // -------------- CONSTRUCTORS ----------------------------------------------
  /**
   * Construct a new BubbleChart with the given axis. BubbleChart does not use a Category Axis.
   * Both X and Y axes should be of type NumberAxis.
   *
   * @param xAxis The x axis to use
   * @param yAxis The y axis to use
   */
  init {
	require(xAxis is MoreGenericValueAxis<*> && yAxis is MoreGenericValueAxis<*>) { "Axis type incorrect, X and Y should both be NumberAxis" }
	setData(data)
  }

  /** {@inheritDoc}  */
  override fun layoutPlotChildren() {
	// update bubble positions
	for (seriesIndex in 0 ..< dataSize) {
	  val series = data.value[seriesIndex]
	  //            for (Data<X,Y> item = series.begin; item != null; item = item.next) {
	  val iter = getDisplayedDataIterator(series)
	  while (iter.hasNext()) {
		val item = iter.next()
		val x = xAxis.getDisplayPosition(item.currentX.value)
		val y = yAxis.getDisplayPosition(item.currentY.value)
		if (java.lang.Double.isNaN(x) || java.lang.Double.isNaN(y)) {
		  continue
		}
		val bubble = item.nodeProp.value
		var ellipse: Ellipse
		if (bubble != null) {
		  if (bubble is StackPane) {
			val region = item.nodeProp.value as StackPane
			ellipse = if (region.shape == null) {
			  Ellipse(
				getDoubleValue(item.extraValue.value, 1.0), getDoubleValue(item.extraValue.value, 1.0)
			  )
			} else if (region.shape is Ellipse) {
			  region.shape as Ellipse
			} else {
			  return
			}
			ellipse.radiusX =
			  getDoubleValue(item.extraValue.value, 1.0)*if (xAxis is MoreGenericNumberAxis) abs(xAxis.scale.value) else 1.0
			ellipse.radiusY =
			  getDoubleValue(item.extraValue.value, 1.0)*if (yAxis is MoreGenericNumberAxis) abs(yAxis.scale.value) else 1.0
			// Note: workaround for RT-7689 - saw this in ProgressControlSkin
			// The region doesn't update itself when the shape is mutated in place, so we
			// null out and then restore the shape in order to force invalidation.
			region.shape = null
			region.shape = ellipse
			region.isScaleShape = false
			region.isCenterShape = false
			region.isCacheShape = false
			// position the bubble
			bubble.setLayoutX(x)
			bubble.setLayoutY(y)
		  }
		}
	  }
	}
  }

  override fun dataItemAdded(series: Series<X, Y>, itemIndex: Int, item: Data<X, Y>) {
	val bubble = createBubble(series, data.value.indexOf(series), item, itemIndex)
	if (shouldAnimate()) {
	  // fade in new bubble
	  bubble.opacity = 0.0
	  plotChildren.add(bubble)
	  val ft = FadeTransition(Duration.millis(500.0), bubble)
	  ft.toValue = 1.0
	  ft.play()
	} else {
	  plotChildren.add(bubble)
	}
  }

  override fun dataItemRemoved(item: Data<X, Y>, series: Series<X, Y>) {
	val bubble = item.nodeProp.value
	if (shouldAnimate()) {
	  // fade out old bubble
	  val ft = FadeTransition(Duration.millis(500.0), bubble)
	  ft.toValue = 0.0
	  ft.onFinished = EventHandler {
		plotChildren.remove(bubble)
		removeDataItemFromDisplay(series, item)
		bubble.opacity = 1.0
	  }
	  ft.play()
	} else {
	  plotChildren.remove(bubble)
	  removeDataItemFromDisplay(series, item)
	}
  }

  /** {@inheritDoc}  */
  override fun dataItemChanged(item: Data<X, Y>) {}
  override fun seriesAdded(series: Series<X, Y>, seriesIndex: Int) {
	// handle any data already in series
	for (j in series.data.value.indices) {
	  val item = series.data.value[j]
	  val bubble = createBubble(series, seriesIndex, item, j)
	  if (shouldAnimate()) {
		bubble.opacity = 0.0
		plotChildren.add(bubble)
		// fade in new bubble
		val ft = FadeTransition(Duration.millis(500.0), bubble)
		ft.toValue = 1.0
		ft.play()
	  } else {
		plotChildren.add(bubble)
	  }
	}
  }

  override fun seriesRemoved(series: Series<X, Y>) {
	// remove all bubble nodes
	if (shouldAnimate()) {
	  val pt = ParallelTransition()
	  pt.onFinished = EventHandler {
		removeSeriesFromDisplay(
		  series
		)
	  }
	  for (d in series.data.value) {
		val bubble = d.nodeProp.value
		// fade out old bubble
		val ft = FadeTransition(Duration.millis(500.0), bubble)
		ft.toValue = 0.0
		ft.onFinished = EventHandler {
		  plotChildren.remove(bubble)
		  bubble.opacity = 1.0
		}
		pt.children.add(ft)
	  }
	  pt.play()
	} else {
	  for (d in series.data.value) {
		val bubble = d.nodeProp.value
		plotChildren.remove(bubble)
	  }
	  removeSeriesFromDisplay(series)
	}
  }

  /**
   * Create a Bubble for a given data item if it doesn't already have a node
   *
   *
   * @param series
   * @param seriesIndex The index of the series containing the item
   * @param item        The data item to create node for
   * @param itemIndex   The index of the data item in the series
   * @return Node used for given data item
   */
  private fun createBubble(series: Series<X, Y>, seriesIndex: Int, item: Data<X, Y>, itemIndex: Int): Node {
	var bubble = item.nodeProp.value
	// check if bubble has already been created
	if (bubble == null) {
	  bubble = object: StackPane() {
		override fun queryAccessibleAttribute(attribute: AccessibleAttribute, vararg parameters: Any): Any {
		  return when (attribute) {
			TEXT -> {
			  val accText = accessibleText
			  if (item.extraValue.value == null) {
				accText
			  } else {
				accText + " Bubble radius is " + item.extraValue
			  }
			}

			else -> super.queryAccessibleAttribute(attribute, *parameters)
		  }
		}
	  }
	  bubble.setAccessibleRole(AccessibleRole.TEXT)
	  bubble.setAccessibleRoleDescription("Bubble")
	  bubble.focusTraversableProperty().bind(Platform.accessibilityActiveProperty())
	  item.nodeProp.value = bubble
	}
	// set bubble styles
	bubble.styleClass.setAll(
	  "chart-bubble", "series$seriesIndex", "data$itemIndex",
	  series.defaultColorStyleClass
	)
	return bubble
  }

  /**
   * This is called when the range has been invalidated and we need to update it. If the axis are auto
   * ranging then we compile a list of all data that the given axis has to plot and call invalidateRange() on the
   * axis passing it that data.
   */
  override fun updateAxisRange() {
	// For bubble chart we need to override this method as we need to let the axis know that they need to be able
	// to cover the whole area occupied by the bubble not just its center data value
	val xa = xAxis
	val ya = yAxis
	var xData: MutableList<X>? = null
	var yData: MutableList<Y>? = null
	if (xa.isAutoRanging()) xData = ArrayList()
	if (ya.isAutoRanging()) yData = ArrayList()
	val xIsCategory = xa is CategoryAxisForCatAxisWrapper
	val yIsCategory = ya is CategoryAxisForCatAxisWrapper
	if (xData != null || yData != null) {
	  for (series in data.value) {
		for (data in series.data.value) {
		  if (xData != null) {
			if (xIsCategory) {
			  xData.add(data.xValueProp.value)
			} else {
			  xData.add(xa.toRealValue(xa.toNumericValue(data.xValueProp.value) + getDoubleValue(data.extraValue, 0.0))!!)
			  xData.add(xa.toRealValue(xa.toNumericValue(data.xValueProp.value) - getDoubleValue(data.extraValue, 0.0))!!)
			}
		  }
		  if (yData != null) {
			if (yIsCategory) {
			  yData.add(data.yValueProp.value)
			} else {
			  yData.add(ya.toRealValue(ya.toNumericValue(data.yValueProp.value) + getDoubleValue(data.extraValue, 0.0))!!)
			  yData.add(ya.toRealValue(ya.toNumericValue(data.yValueProp.value) - getDoubleValue(data.extraValue, 0.0))!!)
			}
		  }
		}
	  }
	  if (xData != null) xa.invalidateRange(xData)
	  if (yData != null) ya.invalidateRange(yData)
	}
  }

  override fun createLegendItemForSeries(series: Series<X, Y>, seriesIndex: Int): LegendItem {
	val legendItem = LegendItem(series.name.value)
	legendItem.symbol.styleClass.addAll(
	  "series$seriesIndex", "chart-bubble",
	  "bubble-legend-symbol", series.defaultColorStyleClass
	)
	return legendItem
  }

  companion object {
	// -------------- METHODS ------------------------------------------------------------------------------------------
	/**
	 * Used to get a double value from a object that can be a Number object or null
	 *
	 * @param number Object possibly a instance of Number
	 * @param nullDefault What value to return if the number object is null or not a Number
	 * @return number converted to double or nullDefault
	 */
	private fun getDoubleValue(number: Any, nullDefault: Double): Double {
	  return if (number !is Number) nullDefault else number.toDouble()
	}
  }
}