package matt.fx.control.wrapper.chart.stackedbar.stackedb

import com.sun.javafx.charts.Legend.LegendItem
import javafx.animation.FadeTransition
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.ParallelTransition
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.NamedArg
import javafx.beans.property.DoubleProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
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
import javafx.scene.chart.Axis
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.StackedBarChart
import javafx.scene.chart.ValueAxis
import javafx.scene.layout.StackPane
import javafx.util.Duration
import matt.fx.control.wrapper.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps
import matt.fx.graphics.anim.interp.MyInterpolator
import java.util.Collections

/**
 * StackedBarChart is a variation of [BarChart] that plots bars indicating
 * data values for a category. The bars can be vertical or horizontal depending
 * on which axis is a category axis.
 * The bar for each series is stacked on top of the previous series.
 * @since JavaFX 2.1
 */
class StackedBarChartForWrapper<X, Y> @JvmOverloads constructor(
  @NamedArg("xAxis") xAxis: Axis<X>?,
  @NamedArg("yAxis") yAxis: Axis<Y>?,
  @NamedArg("data")
  data: ObservableList<Series<X, Y>?>? = FXCollections.observableArrayList()
):
  XYChartForPackagePrivateProps<X?, Y?>(xAxis, yAxis) {
  // -------------- PRIVATE FIELDS -------------------------------------------
  private val seriesCategoryMap: MutableMap<Series<X?, Y?>, MutableMap<String?, MutableList<Data<X?, Y?>>?>> = HashMap()
  private var orientation: Orientation? = null
  private var categoryAxis: CategoryAxis? = null
  private var valueAxis: ValueAxis<*>? = null

  // RT-23125 handling data removal when a category is removed.
  private val categoriesListener =
	ListChangeListener<String> { c ->
	  while (c.next()) {
		for (cat in c.removed) {
		  for (series in getData()) {
			for (data in series.data) {
			  if (cat == if (orientation == VERTICAL) data.xValue else data.yValue) {
				val animatedOn = animated
				animated = false
				dataItemRemoved(data, series)
				animated = animatedOn
			  }
			}
		  }
		  requestChartLayout()
		}
	  }
	}
  // -------------- PUBLIC PROPERTIES ----------------------------------------
  /** The gap to leave between bars in separate categories  */
  private val categoryGap: DoubleProperty = object: StyleableDoubleProperty(10.0) {
	override fun invalidated() {
	  get()
	  requestChartLayout()
	}

	override fun getBean(): Any {
	  return this@StackedBarChart
	}

	override fun getName(): String {
	  return "categoryGap"
	}

	override fun getCssMetaData(): CssMetaData<StackedBarChart<*, *>, Number> {
	  return StyleableProperties.CATEGORY_GAP
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
   * Construct a new StackedBarChart with the given axis and data. The two axis should be a ValueAxis/NumberAxis and a
   * CategoryAxis, they can be in either order depending on if you want a horizontal or vertical bar chart.
   *
   * @param xAxis The x axis to use
   * @param yAxis The y axis to use
   * @param data The data to use, this is the actual list used so any changes to it will be reflected in the chart
   * @param categoryGap The gap to leave between bars in separate categories
   */
  constructor(
	@NamedArg("xAxis") xAxis: Axis<X>?,
	@NamedArg("yAxis") yAxis: Axis<Y>?,
	@NamedArg("data") data: ObservableList<Series<X, Y>?>?,
	@NamedArg("categoryGap") categoryGap: Double
  ): this(xAxis, yAxis) {
	setData(data)
	setCategoryGap(categoryGap)
  }

  // -------------- METHODS --------------------------------------------------
  override fun dataItemAdded(series: Series<X?, Y?>, itemIndex: Int, item: Data<X?, Y?>) {
	val category: String?
	category = if (orientation == VERTICAL) {
	  item.xValue as String?
	} else {
	  item.yValue as String?
	}
	// Don't plot if category does not already exist ?
	//        if (!categoryAxis.getCategories().contains(category)) return;
	var categoryMap = seriesCategoryMap[series]
	if (categoryMap == null) {
	  categoryMap = HashMap()
	  seriesCategoryMap[series] = categoryMap
	}
	// list to hold more that one bar "positive and negative"
	val itemList: MutableList<Data<X?, Y?>>? =
	  if (categoryMap[category] != null) categoryMap[category] else ArrayList<Data<X, Y>>()
	itemList!!.add(item)
	categoryMap[category] = itemList
	//        categoryMap.put(category, item);
	val bar = createBar(series, data.indexOf(series), item, itemIndex)
	if (shouldAnimate()) {
	  animateDataAdd(item, bar)
	} else {
	  plotChildren.add(bar)
	}
  }

  override fun dataItemRemoved(item: Data<X?, Y?>, series: Series<X?, Y?>) {
	val bar = item.node
	bar?.focusTraversableProperty()?.unbind()
	if (shouldAnimate()) {
	  val t = createDataRemoveTimeline(item, bar, series)
	  t.onFinished = EventHandler { event: ActionEvent? ->
		removeDataItemFromDisplay(
		  series, item
		)
	  }
	  t.play()
	} else {
	  processDataRemove(series, item)
	  removeDataItemFromDisplay(series, item)
	}
  }

  /** {@inheritDoc}  */
  override fun dataItemChanged(item: Data<X?, Y?>) {
	val barVal: Double
	val currentVal: Double
	if (orientation == VERTICAL) {
	  barVal = (item.yValue as Number?)!!.toDouble()
	  currentVal = (getCurrentDisplayedYValue(item) as Number?)!!.toDouble()
	} else {
	  barVal = (item.xValue as Number?)!!.toDouble()
	  currentVal = (getCurrentDisplayedXValue(item) as Number?)!!.toDouble()
	}
	if (currentVal > 0 && barVal < 0) { // going from positive to negative
	  // add style class negative
	  item.node.styleClass.add("negative")
	} else if (currentVal < 0 && barVal > 0) { // going from negative to positive
	  // remove style class negative
	  item.node.styleClass.remove("negative")
	}
  }

  override fun seriesChanged(c: Change<out Series<*, *>?>?) {
	// Update style classes for all series lines and symbols
	// Note: is there a more efficient way of doing this?
	for (i in 0 until dataSize) {
	  val series = data[i]
	  for (j in series.data.indices) {
		val item = series.data[j]
		val bar = item.node
		bar.styleClass.setAll("chart-bar", "series$i", "data$j", series.defaultColorStyleClass)
	  }
	}
  }

  /** {@inheritDoc}  */
  override fun seriesAdded(series: Series<X?, Y?>, seriesIndex: Int) {
	// handle any data already in series
	// create entry in the map
	val categoryMap: MutableMap<String?, MutableList<Data<X?, Y?>>?> = HashMap()
	for (j in series.data.indices) {
	  val item = series.data[j]
	  val bar = createBar(series, seriesIndex, item, j)
	  var category: String?
	  category = if (orientation == VERTICAL) {
		item.xValue as String?
	  } else {
		item.yValue as String?
	  }
	  // list of two item positive and negative
	  val itemList: MutableList<Data<X?, Y?>>? =
		if (categoryMap[category] != null) categoryMap[category] else ArrayList<Data<X, Y>>()
	  itemList!!.add(item)
	  categoryMap[category] = itemList
	  if (shouldAnimate()) {
		animateDataAdd(item, bar)
	  } else {
		val barVal =
		  if (orientation == VERTICAL) (item.yValue as Number?)!!.toDouble() else (item.xValue as Number?)!!.toDouble()
		if (barVal < 0) {
		  bar.styleClass.add("negative")
		}
		plotChildren.add(bar)
	  }
	}
	if (categoryMap.size > 0) {
	  seriesCategoryMap[series] = categoryMap
	}
  }

  override fun seriesRemoved(series: Series<X?, Y?>) {
	// remove all symbol nodes
	if (shouldAnimate()) {
	  val pt = ParallelTransition()
	  pt.onFinished = EventHandler { event: ActionEvent? ->
		removeSeriesFromDisplay(series)
		requestChartLayout()
	  }
	  for (d in series.data) {
		val bar = d.node
		// Animate series deletion
		if (seriesSize > 1) {
		  val t = createDataRemoveTimeline(d, bar, series)
		  pt.children.add(t)
		} else {
		  // fade out last series
		  val ft = FadeTransition(Duration.millis(700.0), bar)
		  ft.fromValue = 1.0
		  ft.toValue = 0.0
		  ft.onFinished = EventHandler { actionEvent: ActionEvent? ->
			processDataRemove(series, d)
			bar.opacity = 1.0
		  }
		  pt.children.add(ft)
		}
	  }
	  pt.play()
	} else {
	  for (d in series.data) {
		processDataRemove(series, d)
	  }
	  removeSeriesFromDisplay(series)
	  requestChartLayout()
	}
  }

  /** {@inheritDoc}  */
  override fun updateAxisRange() {
	// This override is necessary to update axis range based on cumulative Y value for the
	// Y axis instead of the inherited way where the max value in the data range is used.
	val categoryIsX = categoryAxis == xAxis
	if (categoryAxis!!.isAutoRanging) {
	  val cData: MutableList<*> = ArrayList<Any?>()
	  for (series in data) {
		for (data in series.data) {
		  if (data != null) cData.add(if (categoryIsX) data.xValue else data.yValue)
		}
	  }
	  categoryAxis!!.invalidateRange(cData)
	}
	if (valueAxis!!.isAutoRanging) {
	  val vData: MutableList<Number> = ArrayList()
	  for (category in categoryAxis!!.allDataCategories) {
		var totalXN = 0.0
		var totalXP = 0.0
		val seriesIterator =
		  displayedSeriesIterator
		while (seriesIterator.hasNext()) {
		  val series = seriesIterator.next()
		  for (item in getDataItem(series, category)!!) {
			if (item != null) {
			  val isNegative = item.node.styleClass.contains("negative")
			  val value = (if (categoryIsX) item.yValue else item.xValue) as Number?
			  if (!isNegative) {
				totalXP += valueAxis!!.toNumericValue(value)
			  } else {
				totalXN += valueAxis!!.toNumericValue(value)
			  }
			}
		  }
		}
		vData.add(totalXP)
		vData.add(totalXN)
	  }
	  valueAxis!!.invalidateRange(vData)
	}
  }

  /** {@inheritDoc}  */
  override fun layoutPlotChildren() {
	val catSpace = categoryAxis!!.categorySpacing
	// calculate bar spacing
	val availableBarSpace = catSpace - getCategoryGap()
	val barOffset = -((catSpace - getCategoryGap())/2)
	// update bar positions and sizes
	for (category in categoryAxis!!.categories) {
	  var currentPositiveValue = 0.0
	  var currentNegativeValue = 0.0
	  val seriesIterator =
		displayedSeriesIterator
	  while (seriesIterator.hasNext()) {
		val series = seriesIterator.next()
		for (item in getDataItem(series, category)!!) {
		  if (item != null) {
			val bar = item.node
			val categoryPos: Double
			val valNumber: Double
			val xValue = getCurrentDisplayedXValue(item)
			val yValue = getCurrentDisplayedYValue(item)
			if (orientation == VERTICAL) {
			  categoryPos = xAxis.getDisplayPosition(xValue)
			  valNumber = yAxis.toNumericValue(yValue)
			} else {
			  categoryPos = yAxis.getDisplayPosition(yValue)
			  valNumber = xAxis.toNumericValue(xValue)
			}
			var bottom: Double
			var top: Double
			val isNegative = bar.styleClass.contains("negative")
			if (!isNegative) {
			  bottom = valueAxis!!.getDisplayPosition(currentPositiveValue)
			  top = valueAxis!!.getDisplayPosition(currentPositiveValue + valNumber)
			  currentPositiveValue += valNumber
			} else {
			  bottom = valueAxis!!.getDisplayPosition(currentNegativeValue + valNumber)
			  top = valueAxis!!.getDisplayPosition(currentNegativeValue)
			  currentNegativeValue += valNumber
			}
			if (orientation == VERTICAL) {
			  bar.resizeRelocate(
				categoryPos + barOffset,
				top, availableBarSpace, bottom - top
			  )
			} else {
			  bar.resizeRelocate(
				bottom,
				categoryPos + barOffset,
				top - bottom, availableBarSpace
			  )
			}
		  }
		}
	  }
	}
  }

  public override fun createLegendItemForSeries(series: Series<X?, Y?>, seriesIndex: Int): LegendItem {
	val legendItem = LegendItem(series.name)
	legendItem.symbol.styleClass.addAll(
	  "chart-bar", "series$seriesIndex",
	  "bar-legend-symbol", series.defaultColorStyleClass
	)
	return legendItem
  }

  private fun updateMap(series: Series<X?, Y?>, item: Data<X?, Y?>) {
	val category = if (orientation == VERTICAL) item.xValue as String? else item.yValue as String?
	val categoryMap = seriesCategoryMap[series]
	if (categoryMap != null) {
	  categoryMap.remove(category)
	  if (categoryMap.isEmpty()) seriesCategoryMap.remove(series)
	}
	if (seriesCategoryMap.isEmpty() && categoryAxis!!.isAutoRanging) categoryAxis!!.categories.clear()
  }

  private fun processDataRemove(series: Series<X?, Y?>, item: Data<X?, Y?>) {
	val bar = item.node
	plotChildren.remove(bar)
	updateMap(series, item)
  }

  private fun animateDataAdd(item: Data<X?, Y?>, bar: Node) {
	val barVal: Double
	if (orientation == VERTICAL) {
	  barVal = (item.yValue as Number?)!!.toDouble()
	  if (barVal < 0) {
		bar.styleClass.add("negative")
	  }
	  item.yValue = yAxis.toRealValue(yAxis.zeroPosition)
	  setCurrentDisplayedYValue(item, yAxis.toRealValue(yAxis.zeroPosition))
	  plotChildren.add(bar)
	  item.yValue = yAxis.toRealValue(barVal)
	  animate(
		KeyFrame(
		  Duration.ZERO, KeyValue(
			currentDisplayedYValueProperty(item),
			getCurrentDisplayedYValue(item),
			MyInterpolator.MY_DEFAULT_INTERPOLATOR
		  )
		),
		KeyFrame(
		  Duration.millis(700.0), KeyValue(
			currentDisplayedYValueProperty(item),
			item.yValue, MyInterpolator.EASE_BOTH
		  )
		)
	  )
	} else {
	  barVal = (item.xValue as Number?)!!.toDouble()
	  if (barVal < 0) {
		bar.styleClass.add("negative")
	  }
	  item.xValue = xAxis.toRealValue(xAxis.zeroPosition)
	  setCurrentDisplayedXValue(item, xAxis.toRealValue(xAxis.zeroPosition))
	  plotChildren.add(bar)
	  item.xValue = xAxis.toRealValue(barVal)
	  animate(
		KeyFrame(
		  Duration.ZERO, KeyValue(
			currentDisplayedXValueProperty(item),
			getCurrentDisplayedXValue(item),
			MyInterpolator.MY_DEFAULT_INTERPOLATOR
		  )
		),
		KeyFrame(
		  Duration.millis(700.0), KeyValue(
			currentDisplayedXValueProperty(item),
			item.xValue, MyInterpolator.EASE_BOTH
		  )
		)
	  )
	}
  }

  private fun createDataRemoveTimeline(item: Data<X?, Y?>, bar: Node?, series: Series<X?, Y?>): Timeline {
	val t = Timeline()
	if (orientation == VERTICAL) {
	  item.yValue = yAxis.toRealValue(yAxis.zeroPosition)
	  t.keyFrames.addAll(
		KeyFrame(
		  Duration.ZERO, KeyValue(
			currentDisplayedYValueProperty(item),
			getCurrentDisplayedYValue(item),
			MyInterpolator.MY_DEFAULT_INTERPOLATOR
		  )
		),
		KeyFrame(
		  Duration.millis(700.0),
		  { actionEvent: ActionEvent? ->
			processDataRemove(
			  series, item
			)
		  }, KeyValue(
			currentDisplayedYValueProperty(item),
			item.yValue, MyInterpolator.EASE_BOTH
		  )
		)
	  )
	} else {
	  item.xValue = xAxis.toRealValue(xAxis.zeroPosition)
	  t.keyFrames.addAll(
		KeyFrame(
		  Duration.ZERO, KeyValue(
			currentDisplayedXValueProperty(item),
			getCurrentDisplayedXValue(item),
			MyInterpolator.MY_DEFAULT_INTERPOLATOR
		  )
		),
		KeyFrame(
		  Duration.millis(700.0),
		  { actionEvent: ActionEvent? ->
			processDataRemove(
			  series, item
			)
		  }, KeyValue(
			currentDisplayedXValueProperty(item),
			item.xValue, MyInterpolator.EASE_BOTH
		  )
		)
	  )
	}
	return t
  }

  private fun createBar(series: Series<X?, Y?>, seriesIndex: Int, item: Data<X?, Y?>, itemIndex: Int): Node {
	var bar = item.node
	if (bar == null) {
	  bar = StackPane()
	  bar.setAccessibleRole(TEXT)
	  bar.setAccessibleRoleDescription("Bar")
	  bar.focusTraversableProperty().bind(Platform.accessibilityActiveProperty())
	  item.node = bar
	}
	bar.styleClass.setAll("chart-bar", "series$seriesIndex", "data$itemIndex", series.defaultColorStyleClass)
	return bar
  }

  private fun getDataItem(series: Series<X?, Y?>, category: String): List<Data<X?, Y?>>? {
	val catmap: Map<String?, MutableList<Data<X?, Y?>>?>? =
	  seriesCategoryMap[series]
	return if (catmap != null) if (catmap[category] != null) catmap[category] else ArrayList<Data<X, Y>>() else ArrayList<Data<X, Y>>()
  }

  // -------------- STYLESHEET HANDLING ------------------------------------------------------------------------------
  /*
     * Super-lazy instantiation pattern from Bill Pugh.
     */
  private object StyleableProperties {
	private val CATEGORY_GAP: CssMetaData<StackedBarChart<*, *>, Number> =
	  object: CssMetaData<StackedBarChart<*, *>, Number?>(
		"-fx-category-gap",
		SizeConverter.getInstance(), 10.0
	  ) {
		override fun isSettable(node: StackedBarChart<*, *>): Boolean {
		  return node.categoryGap == null || !node.categoryGap.isBound
		}

		override fun getStyleableProperty(node: StackedBarChart<*, *>): StyleableProperty<Number?> {
		  return node.categoryGapProperty() as StyleableProperty<Number?>
		}
	  }
	val classCssMetaData: List<CssMetaData<out Styleable?, *>>? = null
	  /**
	   * Gets the `CssMetaData` associated with this class, which may include the
	   * `CssMetaData` of its superclasses.
	   * @return the `CssMetaData`
	   * @since JavaFX 8.0
	   */
	  get() = classCssMetaData

	init {
	  val styleables: MutableList<CssMetaData<out Styleable?, *>> = ArrayList(getClassCssMetaData())
	  styleables.add(CATEGORY_GAP)
	  classCssMetaData = Collections.unmodifiableList(styleables)
	}
  }

  /**
   * {@inheritDoc}
   * @since JavaFX 8.0
   */
  override fun getCssMetaData(): List<CssMetaData<out Styleable?, *>>? {
	return Companion.classCssMetaData
  }
  /**
   * Construct a new StackedBarChart with the given axis and data. The two axis should be a ValueAxis/NumberAxis and a
   * CategoryAxis, they can be in either order depending on if you want a horizontal or vertical bar chart.
   *
   * @param xAxis The x axis to use
   * @param yAxis The y axis to use
   * @param data The data to use, this is the actual list used so any changes to it will be reflected in the chart
   */
  // -------------- CONSTRUCTOR ----------------------------------------------
  /**
   * Construct a new StackedBarChart with the given axis. The two axis should be a ValueAxis/NumberAxis and a CategoryAxis,
   * they can be in either order depending on if you want a horizontal or vertical bar chart.
   *
   * @param xAxis The x axis to use
   * @param yAxis The y axis to use
   */
  init {
	styleClass.add("stacked-bar-chart")
	require(
	  xAxis is ValueAxis<*> && yAxis is CategoryAxis || yAxis is ValueAxis<*> && xAxis is CategoryAxis
	) { "Axis type incorrect, one of X,Y should be CategoryAxis and the other NumberAxis" }
	if (xAxis is CategoryAxis) {
	  categoryAxis = xAxis
	  valueAxis = yAxis as ValueAxis<*>?
	  orientation = VERTICAL
	} else {
	  categoryAxis = yAxis as CategoryAxis?
	  valueAxis = xAxis as ValueAxis<*>?
	  orientation = HORIZONTAL
	}
	// update css
	pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE, orientation == HORIZONTAL)
	pseudoClassStateChanged(VERTICAL_PSEUDOCLASS_STATE, orientation == VERTICAL)
	setData(data)
	categoryAxis!!.categories.addListener(categoriesListener)
  }

  companion object {
	/** Pseudoclass indicating this is a vertical chart.  */
	private val VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("vertical")

	/** Pseudoclass indicating this is a horizontal chart.  */
	private val HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("horizontal")
  }
}