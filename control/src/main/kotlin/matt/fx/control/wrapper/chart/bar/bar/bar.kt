package matt.fx.control.wrapper.chart.bar.bar

import com.sun.javafx.charts.Legend.LegendItem
import javafx.animation.FadeTransition
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.ParallelTransition
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.NamedArg
import javafx.beans.property.DoubleProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener.Change
import javafx.collections.ObservableList
import javafx.css.CssMetaData
import javafx.css.PseudoClass
import javafx.css.Styleable
import javafx.css.StyleableDoubleProperty
import javafx.css.StyleableProperty
import javafx.css.converter.SizeConverter
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.geometry.Orientation.HORIZONTAL
import javafx.geometry.Orientation.VERTICAL
import javafx.scene.AccessibleRole.TEXT
import javafx.scene.Node
import javafx.scene.layout.StackPane
import javafx.util.Duration
import matt.fx.control.wrapper.chart.axis.cat.cat.CategoryAxisForCatAxisWrapper
import matt.fx.control.wrapper.chart.axis.value.axis.AxisForPackagePrivateProps
import matt.fx.control.wrapper.chart.axis.value.moregenval.MoreGenericValueAxis
import matt.fx.control.wrapper.chart.bar.bar.BarChartForWrapper.StyleableProperties.BAR_GAP
import matt.fx.control.wrapper.chart.bar.bar.BarChartForWrapper.StyleableProperties.CATEGORY_GAP
import matt.fx.control.wrapper.chart.bar.bar.BarChartForWrapper.StyleableProperties.classCssMetaData
import matt.fx.control.wrapper.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps
import matt.fx.graphics.anim.interp.MyInterpolator
import java.util.Collections

/**
 * A chart that plots bars indicating data values for a category. The bars can be vertical or horizontal depending on
 * which axis is a category axis.
 * @since JavaFX 2.0
 */
class BarChartForWrapper<X, Y> @JvmOverloads constructor(
  @NamedArg("xAxis") xAxis: AxisForPackagePrivateProps<X>,
  @NamedArg("yAxis") yAxis: AxisForPackagePrivateProps<Y>,
  @NamedArg("data")
  data: ObservableList<Series<X, Y>> = FXCollections.observableArrayList()
):
  XYChartForPackagePrivateProps<X, Y>(xAxis, yAxis) {
  // -------------- PRIVATE FIELDS -------------------------------------------
  private val seriesCategoryMap: MutableMap<Series<X, Y>, MutableMap<String?, Data<X, Y>>> = HashMap()
  private var orientation: Orientation? = null
  private var categoryAxis: CategoryAxisForCatAxisWrapper? = null
  private var valueAxis: MoreGenericValueAxis<*>? = null
  private var dataRemoveTimeline: Timeline? = null
  private var bottomPos = 0.0
  private var pt: ParallelTransition? = null

  // For storing data values in case removed and added immediately.
  private val XYValueMap: MutableMap<Data<X, Y>, Double> = HashMap()
  // -------------- PUBLIC PROPERTIES ----------------------------------------
  /** The gap to leave between bars in the same category  */
  private val barGap: DoubleProperty = object: StyleableDoubleProperty(4.0) {
	override fun invalidated() {
	  get()
	  requestChartLayout()
	}

	override fun getBean(): Any {
	  return this@BarChartForWrapper
	}

	override fun getName(): String {
	  return "barGap"
	}

	override fun getCssMetaData(): CssMetaData<BarChartForWrapper<*, *>, Number> {
	  return BAR_GAP
	}
  }

  fun getBarGap(): Double {
	return barGap.value
  }

  fun setBarGap(value: Double) {
	barGap.value = value
  }

  fun barGapProperty(): DoubleProperty {
	return barGap
  }

  /** The gap to leave between bars in separate categories  */
  private val categoryGap: DoubleProperty = object: StyleableDoubleProperty(10.0) {
	override fun invalidated() {
	  get()
	  requestChartLayout()
	}

	override fun getBean(): Any {
	  return this@BarChartForWrapper
	}

	override fun getName(): String {
	  return "categoryGap"
	}

	override fun getCssMetaData(): CssMetaData<BarChartForWrapper<*, *>, Number> {
	  return CATEGORY_GAP
	}
  }

  fun getCategoryGap(): Double {
	return categoryGap.value
  }

  fun setCategoryGap(value: Double) {
	categoryGap.value = value
  }

  fun categoryGapProperty(): DoubleProperty {
	return categoryGap
  }

  /**
   * Construct a new BarChart with the given axis and data. The two axis should be a ValueAxis/NumberAxis and a
   * CategoryAxis, they can be in either order depending on if you want a horizontal or vertical bar chart.
   *
   * @param xAxis The x axis to use
   * @param yAxis The y axis to use
   * @param data The data to use, this is the actual list used so any changes to it will be reflected in the chart
   * @param categoryGap The gap to leave between bars in separate categories
   */
  constructor(
	@NamedArg("xAxis") xAxis: AxisForPackagePrivateProps<X>,
	@NamedArg("yAxis") yAxis: AxisForPackagePrivateProps<Y>,
	@NamedArg("data") data: ObservableList<Series<X, Y>>,
	@NamedArg("categoryGap") categoryGap: Double
  ): this(xAxis, yAxis) {
	setData(data)
	setCategoryGap(categoryGap)
  }

  // -------------- PROTECTED METHODS ----------------------------------------
  override fun dataItemAdded(series: Series<X, Y>, itemIndex: Int, item: Data<X, Y>) {
	val category: String?
	category = if (orientation == VERTICAL) {
	  item.xValue as String?
	} else {
	  item.yValue as String?
	}
	var categoryMap = seriesCategoryMap[series]
	if (categoryMap == null) {
	  categoryMap = HashMap()
	  seriesCategoryMap[series] = categoryMap
	}
	// check if category is already present
	if (!categoryAxis!!.categories.value.contains(category)) {
	  // note: cat axis categories can be updated only when autoranging is true.
	  categoryAxis!!.categories.value.add(itemIndex, category)
	} else if (categoryMap.containsKey(category)) {
	  // RT-21162 : replacing the previous data, first remove the node from scenegraph.
	  val data = categoryMap[category]!!
	  plotChildren.remove(data.node.value)
	  removeDataItemFromDisplay(series, data)
	  requestChartLayout()
	  categoryMap.remove(category)
	}
	categoryMap[category] = item
	val bar = createBar(series, data.value.indexOf(series), item, itemIndex)
	if (shouldAnimate()) {
	  animateDataAdd(item, bar)
	} else {
	  plotChildren.add(bar)
	}
  }

  override fun dataItemRemoved(item: Data<X, Y>, series: Series<X, Y>) {
	val bar = item.node.value
	bar?.focusTraversableProperty()?.unbind()
	if (shouldAnimate()) {
	  XYValueMap.clear()
	  dataRemoveTimeline = createDataRemoveTimeline(item, bar, series)
	  dataRemoveTimeline!!.onFinished = EventHandler { event: ActionEvent? ->
		item.setSeries(null)
		removeDataItemFromDisplay(series, item)
	  }
	  dataRemoveTimeline!!.play()
	} else {
	  processDataRemove(series, item)
	  removeDataItemFromDisplay(series, item)
	}
  }

  /** {@inheritDoc}  */
  override fun dataItemChanged(item: Data<X, Y>) {
	val barVal: Double
	val currentVal: Double
	if (orientation == VERTICAL) {
	  barVal = (item.yValue as Number?)!!.toDouble()
	  currentVal = (item.currentY as Number?)!!.toDouble()
	} else {
	  barVal = (item.xValue as Number?)!!.toDouble()
	  currentVal = (item.currentX as Number?)!!.toDouble()
	}
	if (currentVal > 0 && barVal < 0) { // going from positive to negative
	  // add style class negative
	  item.node.value.styleClass.add(NEGATIVE_STYLE)
	} else if (currentVal < 0 && barVal > 0) { // going from negative to positive
	  // remove style class negative
	  // RT-21164 upside down bars: was adding NEGATIVE_STYLE styleclass
	  // instead of removing it; when going from negative to positive
	  item.node.value.styleClass.remove(NEGATIVE_STYLE)
	}
  }

  override fun seriesChanged(c: Change<out Series<*, *>>) {
	// Update style classes for all series lines and symbols
	// Note: is there a more efficient way of doing this?
	for (i in 0 until dataSize) {
	  val series = data.value[i]
	  for (j in series.data.value.indices) {
		val item = series.data.value[j]
		val bar = item.node.value
		bar.styleClass.setAll("chart-bar", "series$i", "data$j", series.defaultColorStyleClass)
	  }
	}
  }

  override fun seriesAdded(series: Series<X, Y>, seriesIndex: Int) {
	// handle any data already in series
	// create entry in the map
	val categoryMap: MutableMap<String?, Data<X, Y>> = HashMap()
	for (j in series.data.value.indices) {
	  val item = series.data.value[j]
	  val bar = createBar(series, seriesIndex, item, j)
	  var category: String?
	  category = if (orientation == VERTICAL) {
		item.xValue as String?
	  } else {
		item.yValue as String?
	  }
	  categoryMap[category] = item
	  if (shouldAnimate()) {
		animateDataAdd(item, bar)
	  } else {
		// RT-21164 check if bar value is negative to add NEGATIVE_STYLE style class
		val barVal =
		  if (orientation == VERTICAL) (item.yValue as Number?)!!.toDouble() else (item.xValue as Number?)!!.toDouble()
		if (barVal < 0) {
		  bar.styleClass.add(NEGATIVE_STYLE)
		}
		plotChildren.add(bar)
	  }
	}
	if (categoryMap.size > 0) seriesCategoryMap[series] = categoryMap
  }

  override fun seriesRemoved(series: Series<X, Y>) {
	// remove all symbol nodes
	if (shouldAnimate()) {
	  pt = ParallelTransition()
	  pt!!.onFinished = EventHandler { event: ActionEvent? ->
		removeSeriesFromDisplay(
		  series
		)
	  }
	  XYValueMap.clear()
	  for (d in series.data.value) {
		val bar = d.node.value
		// Animate series deletion
		if (seriesSize > 1) {
		  val t = createDataRemoveTimeline(d, bar, series)
		  pt!!.children.add(t)
		} else {
		  // fade out last series
		  val ft = FadeTransition(Duration.millis(700.0), bar)
		  ft.fromValue = 1.0
		  ft.toValue = 0.0
		  ft.onFinished = EventHandler { actionEvent: ActionEvent? ->
			processDataRemove(series, d)
			bar.opacity = 1.0
		  }
		  pt!!.children.add(ft)
		}
	  }
	  pt!!.play()
	} else {
	  for (d in series.data.value) {
		processDataRemove(series, d)
	  }
	  removeSeriesFromDisplay(series)
	}
  }


  @Suppress("UNCHECKED_CAST")
  private fun toNumericValueFromValueAxis(v: Any?): Double {
	if (xAxis is CategoryAxisForCatAxisWrapper) {
	  return (xAxis.toNumericValue(v as X))
	} else {
	  return (yAxis.toNumericValue(v as Y))
	}
  }

  @Suppress("UNCHECKED_CAST")
  private fun valueAxisGetDisplayPosition(v: Any?): Double {
	if (xAxis is CategoryAxisForCatAxisWrapper) {
	  return (xAxis.getDisplayPosition(v as X))
	} else {
	  return (yAxis.getDisplayPosition(v as Y))
	}
  }



  /** {@inheritDoc}  */
  override fun layoutPlotChildren() {
	val catSpace = categoryAxis!!.categorySpacing.value
	// calculate bar spacing
	val availableBarSpace = catSpace - (getCategoryGap() + getBarGap())
	var barWidth = availableBarSpace/seriesSize - getBarGap()
	val barOffset = -((catSpace - getCategoryGap())/2)
	val zeroPos = if (valueAxis!!.lowerBound.value as Double > 0.0) valueAxisGetDisplayPosition(valueAxis!!.lowerBound) else valueAxis!!.zeroPosition
	// RT-24813 : if the data in a series gets too large, barWidth can get negative.
	if (barWidth <= 0) barWidth = 1.0
	// update bar positions and sizes
	var catIndex = 0
	for (category in categoryAxis!!.categories.value) {
	  var index = 0
	  val sit = displayedSeriesIterator
	  while (sit.hasNext()) {
		val series = sit.next()
		val item = getDataItem(series, index, catIndex, category)
		if (item != null) {
		  val bar = item.node.value
		  val categoryPos: Double
		  val valPos: Double
		  if (orientation == VERTICAL) {
			categoryPos = xAxis.getDisplayPosition(item.currentX.value)
			valPos = yAxis.getDisplayPosition(item.currentY.value)
		  } else {
			categoryPos = yAxis.getDisplayPosition(item.currentY.value)
			valPos = xAxis.getDisplayPosition(item.currentX.value)
		  }
		  if (java.lang.Double.isNaN(categoryPos) || java.lang.Double.isNaN(valPos)) {
			continue
		  }
		  val bottom = Math.min(valPos, zeroPos)
		  val top = Math.max(valPos, zeroPos)
		  bottomPos = bottom
		  if (orientation == VERTICAL) {
			bar.resizeRelocate(
			  categoryPos + barOffset + (barWidth + getBarGap())*index,
			  bottom, barWidth, top - bottom
			)
		  } else {
			bar.resizeRelocate(
			  bottom, categoryPos + barOffset + (barWidth + getBarGap())*index,
			  top - bottom, barWidth
			)
		  }
		  index++
		}
	  }
	  catIndex++
	}
  }

  public override fun createLegendItemForSeries(series: Series<X, Y>, seriesIndex: Int): LegendItem {
	val legendItem = LegendItem(series.name.value)
	legendItem.symbol.styleClass.addAll(
	  "chart-bar", "series$seriesIndex",
	  "bar-legend-symbol", series.defaultColorStyleClass
	)
	return legendItem
  }

  // -------------- PRIVATE METHODS ------------------------------------------
  private fun updateMap(series: Series<X, Y>, item: Data<X, Y>) {
	val category = if (orientation == VERTICAL) item.xValue as String? else item.yValue as String?
	val categoryMap = seriesCategoryMap[series]
	if (categoryMap != null) {
	  categoryMap.remove(category)
	  if (categoryMap.isEmpty()) seriesCategoryMap.remove(series)
	}
	if (seriesCategoryMap.isEmpty() && categoryAxis!!.isAutoRanging()) categoryAxis!!.categories.value.clear()
  }

  private fun processDataRemove(series: Series<X, Y>, item: Data<X, Y>) {
	val bar = item.node.value
	plotChildren.remove(bar)
	updateMap(series, item)
  }

  private fun animateDataAdd(item: Data<X, Y>, bar: Node) {
	val barVal: Double
	if (orientation == VERTICAL) {
	  barVal = (item.yValue as Number?)!!.toDouble()
	  if (barVal < 0) {
		bar.styleClass.add(NEGATIVE_STYLE)
	  }
	  item.currentY.value = yAxis.toRealValue(if (barVal < 0) -bottomPos else bottomPos)
	  plotChildren.add(bar)
	  item.yValue.value = yAxis.toRealValue(barVal)
	  animate(
		KeyFrame(
		  Duration.ZERO, KeyValue(
			item.currentYProperty(),
			item.currentY.value,
			MyInterpolator.MY_DEFAULT_INTERPOLATOR
		  )
		),
		KeyFrame(
		  Duration.millis(700.0), KeyValue(
			item.currentYProperty(),
			item.yValue.value, MyInterpolator.EASE_BOTH
		  )
		)
	  )
	} else {
	  barVal = (item.xValue as Number?)!!.toDouble()
	  if (barVal < 0) {
		bar.styleClass.add(NEGATIVE_STYLE)
	  }
	  item.currentX.value = xAxis.toRealValue(if (barVal < 0) -bottomPos else bottomPos)
	  plotChildren.add(bar)
	  item.xValue.value = xAxis.toRealValue(barVal)
	  animate(
		KeyFrame(
		  Duration.ZERO, KeyValue(
			item.currentXProperty(),
			item.currentX.value,
			MyInterpolator.MY_DEFAULT_INTERPOLATOR
		  )
		),
		KeyFrame(
		  Duration.millis(700.0), KeyValue(
			item.currentXProperty(),
			item.xValue.value, MyInterpolator.EASE_BOTH
		  )
		)
	  )
	}
  }

  private fun createDataRemoveTimeline(item: Data<X, Y>, bar: Node?, series: Series<X, Y>): Timeline {
	val t = Timeline()
	if (orientation == VERTICAL) {
	  //            item.setYValue(getYAxis().toRealValue(getYAxis().getZeroPosition()));

	  // save data values in case the same data item gets added immediately.
	  XYValueMap[item] = (item.yValue as Number?)!!.toDouble()
	  item.yValue.value = yAxis.toRealValue(bottomPos)
	  t.keyFrames.addAll(
		KeyFrame(
		  Duration.ZERO, KeyValue(
			item.currentYProperty(), item.currentY.value,
			MyInterpolator.MY_DEFAULT_INTERPOLATOR
		  )
		),
		KeyFrame(
		  Duration.millis(700.0), { actionEvent: ActionEvent? ->
			processDataRemove(series, item)
			XYValueMap.clear()
		  }, KeyValue(
			item.currentYProperty(),
			item.yValue.value,
			MyInterpolator.EASE_BOTH
		  )
		)
	  )
	} else {
	  // save data values in case the same data item gets added immediately.
	  XYValueMap[item] = (item.xValue as Number?)!!.toDouble()
	  item.xValue.value = xAxis.toRealValue(xAxis.zeroPosition)
	  t.keyFrames.addAll(
		KeyFrame(
		  Duration.ZERO, KeyValue(
			item.currentXProperty(), item.currentX.value,
			MyInterpolator.MY_DEFAULT_INTERPOLATOR
		  )
		),
		KeyFrame(
		  Duration.millis(700.0), { actionEvent: ActionEvent? ->
			processDataRemove(series, item)
			XYValueMap.clear()
		  }, KeyValue(
			item.currentXProperty(),
			item.xValue.value,
			MyInterpolator.EASE_BOTH
		  )
		)
	  )
	}
	return t
  }

  public override fun dataBeingRemovedIsAdded(item: Data<X, Y>, series: Series<X, Y>) {
	if (dataRemoveTimeline != null) {
	  dataRemoveTimeline!!.onFinished = null
	  dataRemoveTimeline!!.stop()
	}
	processDataRemove(series, item)
	item.setSeries(null)
	removeDataItemFromDisplay(series, item)
	restoreDataValues(item)
	XYValueMap.clear()
  }

  private fun restoreDataValues(item: Data<X, Y>) {
	val value = XYValueMap[item]
	if (value != null) {
	  // Restoring original X/Y values
	  @Suppress("UNCHECKED_CAST")
	  if (orientation == VERTICAL) {
		item.setYValue(value as Y)
		item.setCurrentY(value)
	  } else {
		item.setXValue(value as X)
		item.setCurrentX(value)
	  }
	}
  }

  public override fun seriesBeingRemovedIsAdded(series: Series<X, Y>) {
	val lastSeries = if (pt!!.children.size == 1) true else false
	if (pt != null) {
	  if (!pt!!.children.isEmpty()) {
		for (a in pt!!.children) {
		  a.onFinished = null
		}
	  }
	  for (item in series.data.value) {
		processDataRemove(series, item)
		if (!lastSeries) {
		  restoreDataValues(item)
		}
	  }
	  XYValueMap.clear()
	  pt!!.onFinished = null
	  pt!!.children.clear()
	  pt!!.stop()
	  removeSeriesFromDisplay(series)
	}
  }

  private fun createBar(series: Series<X, Y>, seriesIndex: Int, item: Data<X, Y>, itemIndex: Int): Node {
	var bar = item.node.value
	if (bar == null) {
	  bar = StackPane()
	  bar.setAccessibleRole(TEXT)
	  bar.setAccessibleRoleDescription("Bar")
	  bar.focusTraversableProperty().bind(Platform.accessibilityActiveProperty())
	  item.node.value = bar
	}
	bar.styleClass.setAll("chart-bar", "series$seriesIndex", "data$itemIndex", series.defaultColorStyleClass)
	return bar
  }

  private fun getDataItem(series: Series<X, Y>, seriesIndex: Int, itemIndex: Int, category: String): Data<X, Y>? {
	val catmap: Map<String?, Data<X, Y>>? =
	  seriesCategoryMap[series]
	return catmap?.get(category)
  }

  // -------------- STYLESHEET HANDLING ------------------------------------------------------------------------------
  /*
     * Super-lazy instantiation pattern from Bill Pugh.
     */
  private object StyleableProperties {
	internal val BAR_GAP: CssMetaData<BarChartForWrapper<*, *>, Number> = object: CssMetaData<BarChartForWrapper<*, *>, Number>(
	  "-fx-bar-gap",
	  SizeConverter.getInstance(), 4.0
	) {



	  override fun isSettable(node: BarChartForWrapper<*, *>): Boolean {
		return node.barGap == null || !node.barGap.isBound
	  }

	  override fun getStyleableProperty(node: BarChartForWrapper<*, *>): StyleableProperty<Number?> {
		return node.barGapProperty() as StyleableProperty<Number?>
	  }
	}
	internal val CATEGORY_GAP: CssMetaData<BarChartForWrapper<*, *>, Number> = object: CssMetaData<BarChartForWrapper<*, *>, Number>(
	  "-fx-category-gap",
	  SizeConverter.getInstance(), 10.0
	) {
	  override fun isSettable(node: BarChartForWrapper<*, *>): Boolean {
		return node.categoryGap == null || !node.categoryGap.isBound
	  }

	  override fun getStyleableProperty(node: BarChartForWrapper<*, *>): StyleableProperty<Number?> {
		return node.categoryGapProperty() as StyleableProperty<Number?>
	  }
	}
	val classCssMetaData: List<CssMetaData<out Styleable?, *>>? by lazy {
	  val styleables: MutableList<CssMetaData<out Styleable?, *>> = ArrayList(getClassCssMetaData())
	  styleables.add(BAR_GAP)
	  styleables.add(CATEGORY_GAP)
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
  /**
   * Construct a new BarChart with the given axis and data. The two axis should be a ValueAxis/NumberAxis and a
   * CategoryAxis, they can be in either order depending on if you want a horizontal or vertical bar chart.
   *
   * @param xAxis The x axis to use
   * @param yAxis The y axis to use
   * @param data The data to use, this is the actual list used so any changes to it will be reflected in the chart
   */
  // -------------- CONSTRUCTOR ----------------------------------------------
  /**
   * Construct a new BarChart with the given axis. The two axis should be a ValueAxis/NumberAxis and a CategoryAxis,
   * they can be in either order depending on if you want a horizontal or vertical bar chart.
   *
   * @param xAxis The x axis to use
   * @param yAxis The y axis to use
   */
  init {
	styleClass.add("bar-chart")
	require(
	  xAxis is MoreGenericValueAxis<*> && yAxis is CategoryAxisForCatAxisWrapper
		  || yAxis is MoreGenericValueAxis<*> && xAxis is CategoryAxisForCatAxisWrapper
	) { "Axis type incorrect, one of X,Y should be CategoryAxis and the other NumberAxis" }
	if (xAxis is CategoryAxisForCatAxisWrapper) {
	  categoryAxis = xAxis
	  valueAxis = yAxis as MoreGenericValueAxis<*>?
	  orientation = VERTICAL
	} else {
	  categoryAxis = yAxis as CategoryAxisForCatAxisWrapper?
	  valueAxis = xAxis as MoreGenericValueAxis<*>?
	  orientation = HORIZONTAL
	}
	// update css
	pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE, orientation == HORIZONTAL)
	pseudoClassStateChanged(VERTICAL_PSEUDOCLASS_STATE, orientation == VERTICAL)
	setData(data)
  }

  companion object {
	private const val NEGATIVE_STYLE = "negative"

	/** Pseudoclass indicating this is a vertical chart.  */
	private val VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("vertical")

	/** Pseudoclass indicating this is a horizontal chart.  */
	private val HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("horizontal")
  }
}