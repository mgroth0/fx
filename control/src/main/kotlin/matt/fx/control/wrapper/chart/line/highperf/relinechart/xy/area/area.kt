package matt.fx.control.wrapper.chart.line.highperf.relinechart.xy.area

import com.sun.javafx.charts.Legend.LegendItem
import javafx.animation.FadeTransition
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.NamedArg
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener.Change
import javafx.collections.ObservableList
import javafx.css.CssMetaData
import javafx.css.Styleable
import javafx.css.StyleableBooleanProperty
import javafx.css.StyleableProperty
import javafx.css.converter.BooleanConverter
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.AccessibleRole.TEXT
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.chart.LineChart.SortingPolicy
import javafx.scene.chart.LineChart.SortingPolicy.X_AXIS
import javafx.scene.chart.LineChart.SortingPolicy.Y_AXIS
import javafx.scene.layout.StackPane
import javafx.scene.shape.ClosePath
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.shape.StrokeLineJoin.BEVEL
import javafx.util.Duration
import matt.fx.control.wrapper.chart.axis.value.axis.AxisForPackagePrivateProps
import matt.fx.control.wrapper.chart.line.highperf.relinechart.MorePerfOptionsLineChart
import matt.fx.control.wrapper.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps
import matt.fx.control.wrapper.chart.line.highperf.relinechart.xy.area.AreaChartForPrivateProps.StyleableProperties.classCssMetaData
import matt.fx.graphics.anim.interp.MyInterpolator
import java.util.Collections

/**
 * AreaChart - Plots the area between the line that connects the data points and
 * the 0 line on the Y axis.
 * @since JavaFX 2.0
 */
class AreaChartForPrivateProps<X, Y> @JvmOverloads constructor(
  @NamedArg("xAxis") xAxis: AxisForPackagePrivateProps<X>,
  @NamedArg("yAxis") yAxis: AxisForPackagePrivateProps<Y>,
  @NamedArg("data")
  data: ObservableList<Series<X, Y>> = FXCollections.observableArrayList()
):
  XYChartForPackagePrivateProps<X, Y>(xAxis, yAxis) {
  // -------------- PRIVATE FIELDS ------------------------------------------
  /** A multiplier for teh Y values that we store for each series, it is used to animate in a new series  */
  private val seriesYMultiplierMap: MutableMap<Series<X, Y>, DoubleProperty> = HashMap()
  // -------------- PUBLIC PROPERTIES ----------------------------------------
  /**
   * When true, CSS styleable symbols are created for any data items that don't have a symbol node specified.
   * @since JavaFX 8.0
   */
  private val createSymbols: BooleanProperty = object: StyleableBooleanProperty(true) {
	override fun invalidated() {
	  for (seriesIndex in getData().indices) {
		val series = getData()[seriesIndex]
		for (itemIndex in series.data.value.indices) {
		  val item = series.data.value[itemIndex]
		  var symbol = item.node.value
		  if (get() && symbol == null) { // create any symbols
			symbol = createSymbol(series, getData().indexOf(series), item, itemIndex)
			if (null != symbol) {
			  plotChildren.add(symbol)
			}
		  } else if (!get() && symbol != null) { // remove symbols
			plotChildren.remove(symbol)
			symbol = null
			item.node.value = null
		  }
		}
	  }
	  requestChartLayout()
	}

	override fun getBean(): Any {
	  return this
	}

	override fun getName(): String {
	  return "createSymbols"
	}

	override fun getCssMetaData(): CssMetaData<AreaChartForPrivateProps<*, *>, Boolean> {
	  return StyleableProperties.CREATE_SYMBOLS
	}
  }

  /**
   * Indicates whether symbols for data points will be created or not.
   *
   * @return true if symbols for data points will be created and false otherwise.
   * @since JavaFX 8.0
   */
  fun getCreateSymbols(): Boolean {
	return createSymbols.value
  }

  fun setCreateSymbols(value: Boolean) {
	createSymbols.value = value
  }

  fun createSymbolsProperty(): BooleanProperty {
	return createSymbols
  }
  /**
   * Construct a new Area Chart with the given axis and data
   *
   * @param xAxis The x axis to use
   * @param yAxis The y axis to use
   * @param data The data to use, this is the actual list used so any changes to it will be reflected in the chart
   */
  // -------------- CONSTRUCTORS ----------------------------------------------
  /**
   * Construct a new Area Chart with the given axis
   *
   * @param xAxis The x axis to use
   * @param yAxis The y axis to use
   */
  init {
	setData(data)
  }

  /** {@inheritDoc}  */
  override fun updateAxisRange() {
	val xa = xAxis
	val ya = yAxis
	var xData: MutableList<X>? = null
	var yData: MutableList<Y>? = null
	if (xa.isAutoRanging()) xData = ArrayList()
	if (ya.isAutoRanging()) yData = ArrayList()
	if (xData != null || yData != null) {
	  for (series in data.value) {
		for (data in series.data.value) {
		  xData?.add(data.xValue.value)
		  yData?.add(data.yValue.value)
		}
	  }
	  if (xData != null && !(xData.size == 1 && xAxis.toNumericValue(xData[0]) == 0.0)) {
		xa.invalidateRange(xData)
	  }
	  if (yData != null && !(yData.size == 1 && yAxis.toNumericValue(yData[0]) == 0.0)) {
		ya.invalidateRange(yData)
	  }
	}
  }

  override fun dataItemAdded(series: Series<X, Y>, itemIndex: Int, item: Data<X, Y>) {
	val symbol = createSymbol(series, data.value.indexOf(series), item, itemIndex)
	if (shouldAnimate()) {
	  var animate = false
	  if (itemIndex > 0 && itemIndex < series.data.value.size - 1) {
		animate = true
		val p1 = series.data.value[itemIndex - 1]
		val p2 = series.data.value[itemIndex + 1]
		val x1 = xAxis.toNumericValue(p1.xValue.value)
		val y1 = yAxis.toNumericValue(p1.yValue.value)
		val x3 = xAxis.toNumericValue(p2.xValue.value)
		val y3 = yAxis.toNumericValue(p2.yValue.value)
		val x2 = xAxis.toNumericValue(item.xValue.value)
		val y2 = yAxis.toNumericValue(item.yValue.value)

		//                //1. y intercept of the line : y = ((y3-y1)/(x3-x1)) * x2 + (x3y1 - y3x1)/(x3 -x1)
		val y = (y3 - y1)/(x3 - x1)*x2 + (x3*y1 - y3*x1)/(x3 - x1)
		item.currentY.value = yAxis.toRealValue(y)
		item.setCurrentX(xAxis.toRealValue(x2))
		//2. we can simply use the midpoint on the line as well..
		//                double x = (x3 + x1)/2;
		//                double y = (y3 + y1)/2;
		//                item.setCurrentX(x);
		//                item.setCurrentY(y);
	  } else if (itemIndex == 0 && series.data.value.size > 1) {
		animate = true
		item.currentX.value = series.data.value[1].xValue.value
		item.setCurrentY(series.data.value[1].yValue.value)
	  } else if (itemIndex == series.data.value.size - 1 && series.data.value.size > 1) {
		animate = true
		val last = series.data.value.size - 2
		item.currentX.value = series.data.value[last].xValue.value
		item.currentY .value= series.data.value[last].yValue.value
	  }
	  if (symbol != null) {
		// fade in new symbol
		symbol.opacity = 0.0
		plotChildren.add(symbol)
		val ft = FadeTransition(Duration.millis(500.0), symbol)
		ft.toValue = 1.0
		ft.play()
	  }
	  if (animate) {
		animate(
		  KeyFrame(
			Duration.ZERO,
			{ e: ActionEvent? ->
			  if (symbol != null && !plotChildren.contains(symbol)) {
				plotChildren.add(symbol)
			  }
			},
			KeyValue(
			  item.currentYProperty(),
			  item.currentY.value,
			  MyInterpolator.MY_DEFAULT_INTERPOLATOR
			),
			KeyValue(
			  item.currentXProperty(),
			  item.currentX.value,
			  MyInterpolator.MY_DEFAULT_INTERPOLATOR
			)
		  ),
		  KeyFrame(
			Duration.millis(800.0), KeyValue(
			  item.currentYProperty(),
			  item.yValue.value, MyInterpolator.EASE_BOTH
			),
			KeyValue(
			  item.currentXProperty(),
			  item.xValue.value, MyInterpolator.EASE_BOTH
			)
		  )
		)
	  }
	} else if (symbol != null) {
	  plotChildren.add(symbol)
	}
  }

  override fun dataItemRemoved(item: Data<X, Y>, series: Series<X, Y>) {
	val symbol = item.node.value
	symbol?.focusTraversableProperty()?.unbind()

	// remove item from sorted list
	val itemIndex = series.getItemIndex(item)
	if (shouldAnimate()) {
	  var animate = false
	  // dataSize represents size of currently visible data. After this operation, the number will decrement by 1
	  val dataSize = series.dataSize
	  // This is the size of current data list in Series. Note that it might be totaly different from dataSize as
	  // some big operation might have happened on the list.
	  val dataListSize = series.data.value.size
	  if (itemIndex > 0 && itemIndex < dataSize - 1) {
		animate = true
		val p1 = series.getItem(itemIndex - 1)!!
		val p2 = series.getItem(itemIndex + 1)!!
		val x1 = xAxis.toNumericValue(p1.xValue.value)
		val y1 = yAxis.toNumericValue(p1.yValue.value)
		val x3 = xAxis.toNumericValue(p2.xValue.value)
		val y3 = yAxis.toNumericValue(p2.yValue.value)
		val x2 = xAxis.toNumericValue(item.xValue.value)
		val y2 = yAxis.toNumericValue(item.yValue.value)

		//                //1.  y intercept of the line : y = ((y3-y1)/(x3-x1)) * x2 + (x3y1 - y3x1)/(x3 -x1)
		val y = (y3 - y1)/(x3 - x1)*x2 + (x3*y1 - y3*x1)/(x3 - x1)
		item.currentX.value = xAxis.toRealValue(x2)
		item.currentY.value = yAxis.toRealValue(y2)
		item.xValue.value = xAxis.toRealValue(x2)
		item.setYValue(yAxis.toRealValue(y))
		//2.  we can simply use the midpoint on the line as well..
		//                double x = (x3 + x1)/2;
		//                double y = (y3 + y1)/2;
		//                item.setCurrentX(x);
		//                item.setCurrentY(y);
	  } else if (itemIndex == 0 && dataListSize > 1) {
		animate = true
		item.xValue.value = series.data.value[0].xValue.value
		item.setYValue(series.data.value[0].yValue.value)
	  } else if (itemIndex == dataSize - 1 && dataListSize > 1) {
		animate = true
		val last = dataListSize - 1
		item.xValue.value = series.data.value[last].xValue.value
		item.setYValue(series.data.value[last].yValue.value)
	  } else if (symbol != null) {
		// fade out symbol
		symbol.opacity = 0.0
		val ft = FadeTransition(Duration.millis(500.0), symbol)
		ft.toValue = 0.0
		ft.onFinished = EventHandler { actionEvent: ActionEvent? ->
		  plotChildren.remove(symbol)
		  removeDataItemFromDisplay(series, item)
		}
		ft.play()
	  } else {
		item.setSeries(null)
		removeDataItemFromDisplay(series, item)
	  }
	  if (animate) {
		animate(
		  KeyFrame(
			Duration.ZERO, KeyValue(
			  item.currentYProperty(),
			  item.currentY.value,
			  MyInterpolator.MY_DEFAULT_INTERPOLATOR
			), KeyValue(
			  item.currentXProperty(),
			  item.currentX.value,MyInterpolator.MY_DEFAULT_INTERPOLATOR
			)
		  ),
		  KeyFrame(
			Duration.millis(800.0), { actionEvent: ActionEvent? ->
			  item.setSeries(null)
			  plotChildren.remove(symbol)
			  removeDataItemFromDisplay(series, item)
			},
			KeyValue(
			  item.currentYProperty(),
			  item.yValue.value, MyInterpolator.EASE_BOTH
			),
			KeyValue(
			  item.currentXProperty(),
			  item.xValue.value, MyInterpolator.EASE_BOTH
			)
		  )
		)
	  }
	} else {
	  item.setSeries(null)
	  plotChildren.remove(symbol)
	  removeDataItemFromDisplay(series, item)
	}
	//Note: better animation here, point should move from old position to new position at center point between prev and next symbols
  }

  /** {@inheritDoc}  */
  override fun dataItemChanged(item: Data<X, Y>) {}
  override fun seriesChanged(c: Change<out Series<*, *>>) {
	// Update style classes for all series lines and symbols
	// Note: is there a more efficient way of doing this?
	for (i in 0 until dataSize) {
	  val s = data.value[i]
	  val seriesLine = (s.node as Group).children[1] as Path
	  val fillPath = (s.node as Group).children[0] as Path
	  seriesLine.styleClass.setAll("chart-series-area-line", "series$i", s.defaultColorStyleClass)
	  fillPath.styleClass.setAll("chart-series-area-fill", "series$i", s.defaultColorStyleClass)
	  for (j in s.data.value.indices) {
		val item = s.data.value[j]
		val node = item.node.value
		node?.styleClass?.setAll("chart-area-symbol", "series$i", "data$j", s.defaultColorStyleClass)
	  }
	}
  }

  override fun seriesAdded(series: Series<X, Y>, seriesIndex: Int) {
	// create new paths for series
	val seriesLine = Path()
	val fillPath = Path()
	seriesLine.strokeLineJoin = BEVEL
	val areaGroup = Group(fillPath, seriesLine)
	series.node.value= areaGroup
	// create series Y multiplier
	val seriesYAnimMultiplier: DoubleProperty = SimpleDoubleProperty(this, "seriesYMultiplier")
	seriesYMultiplierMap[series] = seriesYAnimMultiplier
	// handle any data already in series
	if (shouldAnimate()) {
	  seriesYAnimMultiplier.value = 0.0
	} else {
	  seriesYAnimMultiplier.value = 1.0
	}
	plotChildren.add(areaGroup)
	val keyFrames: MutableList<KeyFrame> = ArrayList()
	if (shouldAnimate()) {
	  // animate in new series
	  keyFrames.add(
		KeyFrame(
		  Duration.ZERO,
		  KeyValue(areaGroup.opacityProperty(), 0,MyInterpolator.MY_DEFAULT_INTERPOLATOR),
		  KeyValue(seriesYAnimMultiplier, 0,MyInterpolator.MY_DEFAULT_INTERPOLATOR)
		)
	  )
	  keyFrames.add(
		KeyFrame(
		  Duration.millis(200.0),
		  KeyValue(areaGroup.opacityProperty(), 1,MyInterpolator.MY_DEFAULT_INTERPOLATOR)
		)
	  )
	  keyFrames.add(
		KeyFrame(
		  Duration.millis(500.0),
		  KeyValue(seriesYAnimMultiplier, 1,MyInterpolator.MY_DEFAULT_INTERPOLATOR)
		)
	  )
	}
	for (j in series.data.value.indices) {
	  val item = series.data.value[j]
	  val symbol = createSymbol(series, seriesIndex, item, j)
	  if (symbol != null) {
		if (shouldAnimate()) {
		  symbol.opacity = 0.0
		  plotChildren.add(symbol)
		  // fade in new symbol
		  keyFrames.add(KeyFrame(Duration.ZERO, KeyValue(symbol.opacityProperty(), 0,MyInterpolator.MY_DEFAULT_INTERPOLATOR)))
		  keyFrames.add(KeyFrame(Duration.millis(200.0), KeyValue(symbol.opacityProperty(), 1,MyInterpolator.MY_DEFAULT_INTERPOLATOR)))
		} else {
		  plotChildren.add(symbol)
		}
	  }
	}
	if (shouldAnimate()) animate(*keyFrames.toTypedArray())
  }

  override fun seriesRemoved(series: Series<X, Y>) {
	// remove series Y multiplier
	seriesYMultiplierMap.remove(series)
	// remove all symbol nodes
	if (shouldAnimate()) {
	  val tl = Timeline(*createSeriesRemoveTimeLine(series, 400))
	  tl.play()
	} else {
	  plotChildren.remove(series.node.value)
	  for (d in series.data.value) plotChildren.remove(d.node.value)
	  removeSeriesFromDisplay(series)
	}
  }

  /** {@inheritDoc}  */
  override fun layoutPlotChildren() {
	val constructedPath: MutableList<LineTo> = ArrayList(
	  dataSize
	)
	for (seriesIndex in 0 until dataSize) {
	  val series = data.value[seriesIndex]
	  val seriesYAnimMultiplier = seriesYMultiplierMap[series]
	  val children = (series.node as Group).children
	  val fillPath = children[0] as Path
	  val linePath = children[1] as Path
	  makePaths(
		this, series, constructedPath, fillPath, linePath,
		seriesYAnimMultiplier!!.get(), X_AXIS
	  )
	}
  }

  private fun createSymbol(series: Series<X, Y>, seriesIndex: Int, item: Data<X, Y>, itemIndex: Int): Node? {
	var symbol = item.node.value
	// check if symbol has already been created
	if (symbol == null && getCreateSymbols()) {
	  symbol = StackPane()
	  symbol.setAccessibleRole(TEXT)
	  symbol.setAccessibleRoleDescription("Point")
	  symbol.focusTraversableProperty().bind(Platform.accessibilityActiveProperty())
	  item.node.value = symbol
	}
	// set symbol styles
	// Note: not sure if we want to add or check, ie be more careful and efficient here
	symbol?.styleClass?.setAll(
	  "chart-area-symbol", "series$seriesIndex", "data$itemIndex",
	  series.defaultColorStyleClass
	)
	return symbol
  }

  public override fun createLegendItemForSeries(series: Series<X, Y>, seriesIndex: Int): LegendItem {
	val legendItem = LegendItem(series.name.value)
	legendItem.symbol.styleClass.addAll(
	  "chart-area-symbol", "series$seriesIndex",
	  "area-legend-symbol", series.defaultColorStyleClass
	)
	return legendItem
  }

  // -------------- STYLESHEET HANDLING --------------------------------------
  private object StyleableProperties {
	internal val CREATE_SYMBOLS: CssMetaData<AreaChartForPrivateProps<*, *>, Boolean> = object: CssMetaData<AreaChartForPrivateProps<*, *>, Boolean>(
	  "-fx-create-symbols",
	  BooleanConverter.getInstance(), java.lang.Boolean.TRUE
	) {
	  override fun isSettable(node: AreaChartForPrivateProps<*, *>): Boolean {
		return node.createSymbols == null || !node.createSymbols.isBound
	  }

	  override fun getStyleableProperty(node: AreaChartForPrivateProps<*, *>): StyleableProperty<Boolean> {
		return node.createSymbolsProperty() as StyleableProperty<Boolean>
	  }
	}
	val classCssMetaData: List<CssMetaData<out Styleable?, *>> by lazy {
	  val styleables: MutableList<CssMetaData<out Styleable?, *>> = ArrayList(getClassCssMetaData())
	  styleables.add(CREATE_SYMBOLS)
	  Collections.unmodifiableList(styleables)
	}

  }

  /**
   * {@inheritDoc}
   * @since JavaFX 8.0
   */
  override fun getCssMetaData(): List<CssMetaData<out Styleable?, *>>? {
	return classCssMetaData
  }

  companion object {
	// -------------- METHODS ------------------------------------------------------------------------------------------
	private fun doubleValue(number: Number?, nullDefault: Double = 0.0): Double {
	  return number?.toDouble() ?: nullDefault
	}

	fun <X, Y> makePaths(
	  chart: XYChartForPackagePrivateProps<X, Y>,
	  series: Series<X, Y>,
	  constructedPath: MutableList<LineTo>,
	  fillPath: Path?,
	  linePath: Path,
	  yAnimMultiplier: Double,
	  sortAxis: MorePerfOptionsLineChart.SortingPolicy
	) {
	  val axisX = chart.xAxis
	  val axisY = chart.yAxis
	  val hlw = linePath.strokeWidth/2.0
	  val sortX = sortAxis == X_AXIS
	  val sortY = sortAxis == Y_AXIS
	  val dataXMin = if (sortX) -hlw else Double.NEGATIVE_INFINITY
	  val dataXMax = if (sortX) axisX.width + hlw else Double.POSITIVE_INFINITY
	  val dataYMin = if (sortY) -hlw else Double.NEGATIVE_INFINITY
	  val dataYMax = if (sortY) axisY.height + hlw else Double.POSITIVE_INFINITY
	  var prevDataPoint: LineTo? = null
	  var nextDataPoint: LineTo? = null
	  constructedPath.clear()
	  val it = chart.getDisplayedDataIterator(series)
	  while (it.hasNext()) {
		val item = it.next()
		val x = axisX.getDisplayPosition(item.currentX.value)
		val y = axisY.getDisplayPosition(
		  axisY.toRealValue(axisY.toNumericValue(item.currentY.value)*yAnimMultiplier)
		)
		val skip = java.lang.Double.isNaN(x) || java.lang.Double.isNaN(y)
		val symbol = item.node.value
		if (symbol != null) {
		  val w = symbol.prefWidth(-1.0)
		  val h = symbol.prefHeight(-1.0)
		  if (skip) {
			symbol.resizeRelocate(-w*2, -h*2, w, h)
		  } else {
			symbol.resizeRelocate(x - w/2, y - h/2, w, h)
		  }
		}
		if (skip) {
		  continue
		}
		if (x < dataXMin || y < dataYMin) {
		  if (prevDataPoint == null) {
			prevDataPoint = LineTo(x, y)
		  } else if (sortX && prevDataPoint.x <= x || sortY && prevDataPoint.y <= y) {
			prevDataPoint.x = x
			prevDataPoint.y = y
		  }
		} else if (x <= dataXMax && y <= dataYMax) {
		  constructedPath.add(LineTo(x, y))
		} else {
		  if (nextDataPoint == null) {
			nextDataPoint = LineTo(x, y)
		  } else if (sortX && x < nextDataPoint.x || sortY && y < nextDataPoint.y) {
			nextDataPoint.x = x
			nextDataPoint.y = y
		  }
		}
	  }
	  if (!constructedPath.isEmpty() || prevDataPoint != null || nextDataPoint != null) {
		if (sortX) {
		  Collections.sort(
			constructedPath
		  ) { e1: LineTo, e2: LineTo ->
			java.lang.Double.compare(
			  e1.x, e2.x
			)
		  }
		} else if (sortY) {
		  Collections.sort(
			constructedPath
		  ) { e1: LineTo, e2: LineTo ->
			java.lang.Double.compare(
			  e1.y, e2.y
			)
		  }
		} else {
		  // assert prevDataPoint == null && nextDataPoint == null
		}
		if (prevDataPoint != null) {
		  constructedPath.add(0, prevDataPoint)
		}
		if (nextDataPoint != null) {
		  constructedPath.add(nextDataPoint)
		}

		// assert !constructedPath.isEmpty()
		val first = constructedPath[0]
		val last = constructedPath[constructedPath.size - 1]
		val displayYPos = first.y
		val lineElements = linePath.elements
		lineElements.clear()
		lineElements.add(MoveTo(first.x, displayYPos))
		lineElements.addAll(constructedPath)
		if (fillPath != null) {
		  val fillElements = fillPath.elements
		  fillElements.clear()
		  val yOrigin = axisY.getDisplayPosition(axisY.toRealValue(0.0))
		  fillElements.add(MoveTo(first.x, yOrigin))
		  fillElements.addAll(constructedPath)
		  fillElements.add(LineTo(last.x, yOrigin))
		  fillElements.add(ClosePath())
		}
	  }
	}
  }
}