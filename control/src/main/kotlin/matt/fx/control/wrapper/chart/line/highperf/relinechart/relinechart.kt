package matt.fx.control.wrapper.chart.line.highperf.relinechart

import com.sun.javafx.charts.Legend.LegendItem
import javafx.animation.Animation.Status.RUNNING
import javafx.animation.FadeTransition
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ObjectPropertyBase
import javafx.beans.property.SimpleDoubleProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener.Change
import javafx.collections.ObservableList
import javafx.css.CssMetaData
import javafx.css.Styleable
import javafx.css.StyleableBooleanProperty
import javafx.css.StyleableProperty
import javafx.css.converter.BooleanConverter
import javafx.event.EventHandler
import javafx.scene.AccessibleRole.TEXT
import javafx.scene.Node
import javafx.scene.layout.StackPane
import javafx.scene.shape.LineTo
import javafx.scene.shape.Path
import javafx.scene.shape.StrokeLineJoin.BEVEL
import javafx.util.Duration
import matt.fx.control.wrapper.chart.axis.value.axis.AxisForPackagePrivateProps
import matt.fx.control.wrapper.chart.line.highperf.relinechart.MorePerfOptionsLineChart.SortingPolicy.X_AXIS
import matt.fx.control.wrapper.chart.line.highperf.relinechart.MorePerfOptionsLineChart.StyleableProperties.classCssMetaData
import matt.fx.control.wrapper.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps
import matt.fx.control.wrapper.chart.line.highperf.relinechart.xy.area.AreaChartForPrivateProps
import matt.fx.graphics.anim.interp.MyInterpolator
import java.util.Collections

open class MorePerfOptionsLineChart<X, Y> @JvmOverloads constructor(
  xAxis: AxisForPackagePrivateProps<X>,
  yAxis: AxisForPackagePrivateProps<Y>,
  data: ObservableList<Series<X, Y>> = FXCollections.observableArrayList()
): XYChartForPackagePrivateProps<X, Y>(xAxis, yAxis) {
  // -------------- PRIVATE FIELDS ------------------------------------------
  /** A multiplier for the Y values that we store for each series, it is used to animate in a new series  */
  private val seriesYMultiplierMap: MutableMap<Series<X, Y>, DoubleProperty> = HashMap()
  private var dataRemoveTimeline: Timeline? = null
  private var seriesOfDataRemoved: Series<X, Y>? = null
  private var dataItemBeingRemoved: Data<X, Y>? = null
  private var fadeSymbolTransition: FadeTransition? = null
  private val XYValueMap: MutableMap<Data<X, Y>, Double> = HashMap()
  private var seriesRemoveTimeline: Timeline? = null
  // -------------- PUBLIC PROPERTIES ----------------------------------------
  /** When true, CSS styleable symbols are created for any data items that don't have a symbol node specified.  */
  private val createSymbols: BooleanProperty = object: StyleableBooleanProperty(true) {
	override fun invalidated() {
	  for (seriesIndex in getData().indices) {
		val series = getData()[seriesIndex]
		for (itemIndex in series.data.value.indices) {
		  val item = series.data.value[itemIndex]
		  var symbol = item.node.value
		  if (get() && symbol == null) { // create any symbols
			symbol = createSymbol(series, getData().indexOf(series), item, itemIndex)
			plotChildren.add(symbol)
		  } else if (!get() && symbol != null) { // remove symbols
			plotChildren.remove(symbol)
			item.node.value = null
		  }
		}
	  }
	  requestChartLayout()
	}

	override fun getBean(): Any {
	  return this@MorePerfOptionsLineChart
	}

	override fun getName(): String {
	  return "createSymbols"
	}

	override fun getCssMetaData(): CssMetaData<MorePerfOptionsLineChart<*, *>, Boolean> {
	  return StyleableProperties.CREATE_SYMBOLS
	}
  }

  /**
   * Indicates whether symbols for data points will be created or not.
   *
   * @return true if symbols for data points will be created and false otherwise.
   */
  fun getCreateSymbols(): Boolean {
	return createSymbols.value
  }

  @Suppress("unused")
  fun setCreateSymbols(value: Boolean) {
	createSymbols.value = value
  }

  fun createSymbolsProperty(): BooleanProperty {
	return createSymbols
  }

  /**
   * Indicates whether the data passed to LineChart should be sorted by natural order of one of the axes.
   * If this is set to [SortingPolicy.NONE], the order in [.dataProperty] will be used.
   *
   * @since JavaFX 8u40
   * @see SortingPolicy
   *
   * @defaultValue SortingPolicy#X_AXIS
   */
  private val axisSortingPolicy: ObjectProperty<SortingPolicy> = object: ObjectPropertyBase<SortingPolicy>(X_AXIS) {
	override fun invalidated() {
	  requestChartLayout()
	}

	override fun getBean(): Any {
	  return this@MorePerfOptionsLineChart
	}

	override fun getName(): String {
	  return "axisSortingPolicy"
	}
  }

  fun getAxisSortingPolicy(): SortingPolicy {
	return axisSortingPolicy.value
  }

  @Suppress("unused") fun setAxisSortingPolicy(value: SortingPolicy) {
	axisSortingPolicy.value = value
  }

  @Suppress("unused") fun axisSortingPolicyProperty(): ObjectProperty<SortingPolicy> {
	return axisSortingPolicy
  }
  /**
   * Construct a new LineChart with the given axis and data.
   *
   * @param xAxis The x axis to use
   * @param yAxis The y axis to use
   * @param data The data to use, this is the actual list used so any changes to it will be reflected in the chart
   */
  // -------------- CONSTRUCTORS ----------------------------------------------
  /**
   * Construct a new LineChart with the given axis.
   *
   * @param xAxis The x axis to use
   * @param yAxis The y axis to use
   */
  init {
	setData(data)
  }
  // -------------- METHODS ------------------------------------------------------------------------------------------
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
		  xData?.add(data.xValueProp.value)
		  yData?.add(data.yValueProp.value)
		}
	  }
	  // RT-32838 No need to invalidate range if there is one data item - whose value is zero.
	  if (xData != null && !(xData.size == 1 && xAxis.toNumericValue(xData[0]!!) == 0.0)) {
		xa.invalidateRange(xData)
	  }
	  if (yData != null && !(yData.size == 1 && yAxis.toNumericValue(yData[0]!!) == 0.0)) {
		ya.invalidateRange(yData)
	  }
	}
  }

  /*MATT WAS HERE*/
  var enableDataItemAdded = true
  override fun dataItemAdded(series: Series<X, Y>, itemIndex: Int, item: Data<X, Y>) {
	if (!enableDataItemAdded) return
	val symbol = createSymbol(series, data.value.indexOf(series), item, itemIndex)
	if (shouldAnimate()) {
	  if (dataRemoveTimeline != null && dataRemoveTimeline!!.status == RUNNING) {
		if (seriesOfDataRemoved == series) {
		  dataRemoveTimeline!!.stop()
		  dataRemoveTimeline = null
		  plotChildren.remove(dataItemBeingRemoved!!.node.value)
		  removeDataItemFromDisplay(seriesOfDataRemoved!!, dataItemBeingRemoved)
		  seriesOfDataRemoved = null
		  dataItemBeingRemoved = null
		}
	  }
	  var animate = false
	  @Suppress("SENSELESS_COMPARISON")
	  if (itemIndex > 0 && itemIndex < series.data.value.size - 1) {
		animate = true
		val p1 = series.data.value[itemIndex - 1]
		val p2 = series.data.value[itemIndex + 1]
		if (p1 != null && p2 != null) {
		  val x1 = xAxis.toNumericValue(p1.xValueProp.value)
		  val y1 = yAxis.toNumericValue(p1.yValueProp.value)
		  val x3 = xAxis.toNumericValue(p2.xValueProp.value)
		  val y3 = yAxis.toNumericValue(p2.yValueProp.value)
		  val x2 = xAxis.toNumericValue(item.xValueProp.value)
		  //double y2 = getYAxis().toNumericValue(item.getYValue());
		  if (x2 > x1 && x2 < x3) {
			//1. y intercept of the line : y = ((y3-y1)/(x3-x1)) * x2 + (x3y1 - y3x1)/(x3 -x1)
			val y = (y3 - y1)/(x3 - x1)*x2 + (x3*y1 - y3*x1)/(x3 - x1)
			item.currentY.value = yAxis.toRealValue(y)
			item.setCurrentX(xAxis.toRealValue(x2)!!)
		  } else {
			//2. we can simply use the midpoint on the line as well..
			val x = (x3 + x1)/2
			val y = (y3 + y1)/2
			item.currentX.value = xAxis.toRealValue(x)
			item.setCurrentY(yAxis.toRealValue(y)!!)
		  }
		}
	  } else if (itemIndex == 0 && series.data.value.size > 1) {
		animate = true
		item.currentX.value = series.data.value[1].xValueProp.value
		item.setCurrentY(series.data.value[1].yValueProp.value)
	  } else if (itemIndex == series.data.value.size - 1 && series.data.value.size > 1) {
		animate = true
		val last = series.data.value.size - 2
		item.currentX.value = series.data.value[last].xValueProp.value
		item.setCurrentY(series.data.value[last].yValueProp.value)
	  } else if (symbol != null) {
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
			{
			  @Suppress("SENSELESS_COMPARISON")
			  if (symbol != null && !plotChildren.contains(
				  symbol
				)
			  ) plotChildren.add(symbol)
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
			Duration.millis(700.0), KeyValue(
			  item.currentYProperty(),
			  item.yValueProp.value, MyInterpolator.EASE_BOTH
			),
			KeyValue(
			  item.currentXProperty(),
			  item.xValueProp.value, MyInterpolator.EASE_BOTH
			)
		  )
		)
	  }
	} else {
	  @Suppress("SENSELESS_COMPARISON")
	  if (symbol != null) plotChildren.add(symbol)
	}
  }

  override fun dataItemRemoved(item: Data<X, Y>, series: Series<X, Y>) {
	val symbol = item.node.value
	symbol?.focusTraversableProperty()?.unbind()

	// remove item from sorted list
	val itemIndex = series.getItemIndex(item)
	if (shouldAnimate()) {
	  XYValueMap.clear()
	  var animate = false
	  // dataSize represents size of currently visible data. After this operation, the number will decrement by 1
	  val dataSize = series.dataSize
	  // This is the size of current data list in Series. Note that it might be totaly different from dataSize as
	  // some big operation might have happened on the list.
	  val dataListSize = series.data.value.size
	  if (itemIndex > 0 && itemIndex < dataSize - 1) {
		animate = true
		val p1 = series.getItem(itemIndex - 1)
		val p2 = series.getItem(itemIndex + 1)
		val x1 = xAxis.toNumericValue(p1!!.xValueProp.value)
		val y1 = yAxis.toNumericValue(p1.yValueProp.value)
		val x3 = xAxis.toNumericValue(p2!!.xValueProp.value)
		val y3 = yAxis.toNumericValue(p2.yValueProp.value)
		val x2 = xAxis.toNumericValue(item.xValueProp.value)
		val y2 = yAxis.toNumericValue(item.yValueProp.value)
		if (x2 > x1 && x2 < x3) {
		  //                //1.  y intercept of the line : y = ((y3-y1)/(x3-x1)) * x2 + (x3y1 - y3x1)/(x3 -x1)
		  val y = (y3 - y1)/(x3 - x1)*x2 + (x3*y1 - y3*x1)/(x3 - x1)
		  item.currentX.value = xAxis.toRealValue(x2)
		  item.currentY.value = yAxis.toRealValue(y2)
		  item.xValueProp.value = xAxis.toRealValue(x2)
		  item.yValue = (yAxis.toRealValue(y)!!)
		} else {
		  //2.  we can simply use the midpoint on the line as well..
		  val x = (x3 + x1)/2
		  val y = (y3 + y1)/2
		  item.currentX.value = xAxis.toRealValue(x)
		  item.setCurrentY(yAxis.toRealValue(y)!!)
		}
	  } else if (itemIndex == 0 && dataListSize > 1) {
		animate = true
		item.xValueProp.value = series.data.value[0].xValueProp.value
		item.yValue = (series.data.value[0].yValueProp.value)
	  } else if (itemIndex == dataSize - 1 && dataListSize > 1) {
		animate = true
		val last = dataListSize - 1
		item.xValueProp.value = series.data.value[last].xValueProp.value
		item.yValue = (series.data.value[last].yValueProp.value)
	  } else if (symbol != null) {
		// fade out symbol
		fadeSymbolTransition = FadeTransition(Duration.millis(500.0), symbol)
		fadeSymbolTransition!!.toValue = 0.0
		fadeSymbolTransition!!.onFinished = EventHandler {
		  item.setSeries(null)
		  plotChildren.remove(symbol)
		  removeDataItemFromDisplay(series, item)
		  symbol.opacity = 1.0
		}
		fadeSymbolTransition!!.play()
	  } else {
		item.setSeries(null)
		removeDataItemFromDisplay(series, item)
	  }
	  if (animate) {
		dataRemoveTimeline = createDataRemoveTimeline(item, symbol, series)
		seriesOfDataRemoved = series
		dataItemBeingRemoved = item
		dataRemoveTimeline!!.play()
	  }
	} else {
	  item.setSeries(null)
	  if (symbol != null) plotChildren.remove(symbol)
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
	  val seriesNode = s.node.value
	  seriesNode?.styleClass?.setAll("chart-series-line", "series$i", s.defaultColorStyleClass)
	  for (j in s.data.value.indices) {
		val symbol = s.data.value[j].node.value
		symbol?.styleClass?.setAll("chart-line-symbol", "series$i", "data$j", s.defaultColorStyleClass)
	  }
	}
  }

  override fun seriesAdded(series: Series<X, Y>, seriesIndex: Int) {
	// create new path for series
	val seriesLine = Path()
	seriesLine.strokeLineJoin = BEVEL
	series.node.value = seriesLine
	// create series Y multiplier
	val seriesYAnimMultiplier: DoubleProperty = SimpleDoubleProperty(this, "seriesYMultiplier")
	seriesYMultiplierMap[series] = seriesYAnimMultiplier
	// handle any data already in series
	if (shouldAnimate()) {
	  seriesLine.opacity = 0.0
	  seriesYAnimMultiplier.value = 0.0
	} else {
	  seriesYAnimMultiplier.value = 1.0
	}
	plotChildren.add(seriesLine)
	val keyFrames: MutableList<KeyFrame> = ArrayList()
	if (shouldAnimate()) {
	  // animate in new series
	  keyFrames.add(
		KeyFrame(
		  Duration.ZERO,
		  KeyValue(seriesLine.opacityProperty(), 0, MyInterpolator.MY_DEFAULT_INTERPOLATOR),
		  KeyValue(seriesYAnimMultiplier, 0, MyInterpolator.MY_DEFAULT_INTERPOLATOR)
		)
	  )
	  keyFrames.add(
		KeyFrame(
		  Duration.millis(200.0),
		  KeyValue(seriesLine.opacityProperty(), 1, MyInterpolator.MY_DEFAULT_INTERPOLATOR)
		)
	  )
	  keyFrames.add(
		KeyFrame(
		  Duration.millis(500.0),
		  KeyValue(seriesYAnimMultiplier, 1, MyInterpolator.MY_DEFAULT_INTERPOLATOR)
		)
	  )
	}
	for (j in series.data.value.indices) {
	  val item = series.data.value[j]
	  val symbol = createSymbol(series, seriesIndex, item, j)
	  @Suppress("SENSELESS_COMPARISON")
	  if (symbol != null) {
		if (shouldAnimate()) symbol.opacity = 0.0
		plotChildren.add(symbol)
		if (shouldAnimate()) {
		  // fade in new symbol
		  keyFrames.add(
			KeyFrame(Duration.ZERO, KeyValue(symbol.opacityProperty(), 0, MyInterpolator.MY_DEFAULT_INTERPOLATOR))
		  )
		  keyFrames.add(
			KeyFrame(
			  Duration.millis(200.0), KeyValue(symbol.opacityProperty(), 1, MyInterpolator.MY_DEFAULT_INTERPOLATOR)
			)
		  )
		}
	  }
	}
	if (shouldAnimate()) animate(*keyFrames.toTypedArray())
  }

  override fun seriesRemoved(series: Series<X, Y>) {
	// remove all symbol nodes
	seriesYMultiplierMap.remove(series)
	if (shouldAnimate()) {
	  seriesRemoveTimeline = Timeline(*createSeriesRemoveTimeLine(series, 900))
	  seriesRemoveTimeline!!.play()
	} else {
	  plotChildren.remove(series.node.value)
	  for (d in series.data.value) plotChildren.remove(d.node.value)
	  removeSeriesFromDisplay(series)
	}
  }

  /** {@inheritDoc}  */
  override fun layoutPlotChildren() {
	val constructedPath = ArrayList<LineTo>(
	  dataSize
	)
	for (seriesIndex in 0 until dataSize) {
	  val series = data.value[seriesIndex]
	  val seriesYAnimMultiplier = seriesYMultiplierMap[series]
	  val seriesNode = series.node.value
	  if (seriesNode is Path) {
		AreaChartForPrivateProps.makePaths(
		  this,
		  series,
		  constructedPath,
		  null,
		  seriesNode,
		  seriesYAnimMultiplier!!.get(),
		  getAxisSortingPolicy()
		)
	  }
	}
  }

  /** {@inheritDoc}  */
  override fun dataBeingRemovedIsAdded(item: Data<X, Y>, series: Series<X, Y>) {
	if (fadeSymbolTransition != null) {
	  fadeSymbolTransition!!.onFinished = null
	  fadeSymbolTransition!!.stop()
	}
	if (dataRemoveTimeline != null) {
	  dataRemoveTimeline!!.onFinished = null
	  dataRemoveTimeline!!.stop()
	}
	val symbol = item.node.value
	if (symbol != null) plotChildren.remove(symbol)
	item.setSeries(null)
	removeDataItemFromDisplay(series, item)

	// restore values to item
	val value = XYValueMap[item]
	if (value != null) {
	  @Suppress("UNCHECKED_CAST")
	  item.yValue = (value as Y)
	  @Suppress("UNCHECKED_CAST")
	  item.setCurrentY(value as Y)
	}
	XYValueMap.clear()
  }

  /** {@inheritDoc}  */
  override fun seriesBeingRemovedIsAdded(series: Series<X, Y>) {
	if (seriesRemoveTimeline != null) {
	  seriesRemoveTimeline!!.onFinished = null
	  seriesRemoveTimeline!!.stop()
	  plotChildren.remove(series.node.value)
	  for (d in series.data.value) plotChildren.remove(d.node.value)
	  removeSeriesFromDisplay(series)
	}
  }

  private fun createDataRemoveTimeline(item: Data<X, Y>, symbol: Node?, series: Series<X, Y>): Timeline {
	val t = Timeline()
	// save data values in case the same data item gets added immediately.
	XYValueMap[item] = (item.yValueProp.value as Number?)!!.toDouble()
	t.keyFrames.addAll(
	  KeyFrame(
		Duration.ZERO, KeyValue(
		  item.currentYProperty(),
		  item.currentY.value, MyInterpolator.MY_DEFAULT_INTERPOLATOR
		), KeyValue(
		  item.currentXProperty(),
		  item.currentX.value, MyInterpolator.MY_DEFAULT_INTERPOLATOR
		)
	  ),
	  KeyFrame(
		Duration.millis(500.0), {
		  if (symbol != null) plotChildren.remove(symbol)
		  removeDataItemFromDisplay(series, item)
		  XYValueMap.clear()
		},
		KeyValue(
		  item.currentYProperty(),
		  item.yValueProp.value, MyInterpolator.EASE_BOTH
		),
		KeyValue(
		  item.currentXProperty(),
		  item.xValueProp.value, MyInterpolator.EASE_BOTH
		)
	  )
	)
	return t
  }

  private fun createSymbol(series: Series<X, Y>, seriesIndex: Int, item: Data<X, Y>, itemIndex: Int): Node? {
	var symbol = item.getNode()
	// check if symbol has already been created
	if (symbol == null && getCreateSymbols()) {
	  symbol = StackPane()
	  symbol.setAccessibleRole(TEXT)
	  symbol.setAccessibleRoleDescription("Point")
	  symbol.focusTraversableProperty().bind(Platform.accessibilityActiveProperty())
	  item.node.value = symbol
	}
	// set symbol styles
	symbol?.styleClass?.addAll(
	  "chart-line-symbol", "series$seriesIndex",
	  "data$itemIndex", series.defaultColorStyleClass
	)
	return symbol
  }

  override fun createLegendItemForSeries(series: Series<X, Y>, seriesIndex: Int): LegendItem {
	val legendItem = LegendItem(series.name.value)
	legendItem.symbol.styleClass.addAll(
	  "chart-line-symbol", "series$seriesIndex",
	  series.defaultColorStyleClass
	)
	return legendItem
  }

  // -------------- STYLESHEET HANDLING --------------------------------------
  private object StyleableProperties {
	val CREATE_SYMBOLS: CssMetaData<MorePerfOptionsLineChart<*, *>, Boolean> =
	  object: CssMetaData<MorePerfOptionsLineChart<*, *>, Boolean>(
		"-fx-create-symbols",
		BooleanConverter.getInstance(), true
	  ) {
		override fun isSettable(node: MorePerfOptionsLineChart<*, *>): Boolean {
		  return node.createSymbols.value == null || !node.createSymbols.isBound
		}

		override fun getStyleableProperty(node: MorePerfOptionsLineChart<*, *>): StyleableProperty<Boolean?> {
		  @Suppress("UNCHECKED_CAST")
		  return node.createSymbolsProperty() as StyleableProperty<Boolean?>
		}
	  }

	val classCssMetaData: List<CssMetaData<out Styleable?, *>> by lazy {
	  Collections.unmodifiableList(ArrayList(getClassCssMetaData()).apply {
		add(CREATE_SYMBOLS)
	  })
	}

  }

  override fun getCssMetaData(): List<CssMetaData<out Styleable?, *>>? {
	return classCssMetaData
  }

  enum class SortingPolicy {
	NONE,
	X_AXIS,
	Y_AXIS
  }

  companion object
}