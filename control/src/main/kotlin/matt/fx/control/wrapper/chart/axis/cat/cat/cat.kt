
package matt.fx.control.wrapper.chart.axis.cat.cat

import com.sun.javafx.charts.ChartLayoutAnimator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ObjectPropertyBase
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.ReadOnlyDoubleWrapper
import javafx.beans.property.SimpleDoubleProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ListChangeListener.Change
import javafx.collections.ObservableList
import javafx.css.CssMetaData
import javafx.css.Styleable
import javafx.css.StyleableBooleanProperty
import javafx.css.StyleableDoubleProperty
import javafx.css.StyleableProperty
import javafx.css.converter.BooleanConverter
import javafx.css.converter.SizeConverter
import javafx.geometry.Dimension2D
import javafx.util.Duration
import matt.fx.control.wrapper.chart.axis.cat.cat.CategoryAxisForCatAxisWrapper.StyleableProperties.classCssMetaData
import matt.fx.control.wrapper.chart.axis.value.axis.AxisForPackagePrivateProps
import java.util.Collections

/**
 * A axis implementation that will works on string categories where each
 * value as a unique category(tick mark) along the axis.
 * @since JavaFX 2.0
 */
class CategoryAxisForCatAxisWrapper: AxisForPackagePrivateProps<String> {
  // -------------- PRIVATE FIELDS -------------------------------------------
  internal val allDataCategories: MutableList<String> = ArrayList()
  private var changeIsLocal = false

  /** This is the gap between one category and the next along this axis  */
  private val firstCategoryPos: DoubleProperty = SimpleDoubleProperty(this, "firstCategoryPos", 0.0)
  private var currentAnimationID: Any? = null
  private val animator = ChartLayoutAnimator(this)
  private val itemsListener =
	ListChangeListener { c: Change<out String?> ->
	  while (c.next()) {
		if (!c.addedSubList.isEmpty()) {
		  // remove duplicates else they will get rendered on the chart.
		  // Ideally we should be using a Set for categories.
		  for (addedStr in c.addedSubList) checkAndRemoveDuplicates(addedStr!!)
		}
		if (!isAutoRanging()) {
		  allDataCategories.clear()
		  allDataCategories.addAll(getCategories()!!)
		  isRangeValid = false
		}
		requestAxisLayout()
	  }
	}
  // -------------- PUBLIC PROPERTIES ----------------------------------------
  /** The margin between the axis start and the first tick-mark  */
  private val startMargin: DoubleProperty = object: StyleableDoubleProperty(5.0) {
	override fun invalidated() {
	  requestAxisLayout()
	}

	override fun getCssMetaData(): CssMetaData<CategoryAxisForCatAxisWrapper, Number> {
	  return StyleableProperties.START_MARGIN
	}

	override fun getBean(): Any {
	  return this@CategoryAxisForCatAxisWrapper
	}

	override fun getName(): String {
	  return "startMargin"
	}
  }

  fun getStartMargin(): Double {
	return startMargin.value
  }

  fun setStartMargin(value: Double) {
	startMargin.value = value
  }

  fun startMarginProperty(): DoubleProperty {
	return startMargin
  }

  /** The margin between the last tick mark and the axis end  */
  private val endMargin: DoubleProperty = object: StyleableDoubleProperty(5.0) {
	override fun invalidated() {
	  requestAxisLayout()
	}

	override fun getCssMetaData(): CssMetaData<CategoryAxisForCatAxisWrapper, Number> {
	  return StyleableProperties.END_MARGIN
	}

	override fun getBean(): Any {
	  return this@CategoryAxisForCatAxisWrapper
	}

	override fun getName(): String {
	  return "endMargin"
	}
  }

  fun getEndMargin(): Double {
	return endMargin.value
  }

  fun setEndMargin(value: Double) {
	endMargin.value = value
  }

  fun endMarginProperty(): DoubleProperty {
	return endMargin
  }

  /** If this is true then half the space between ticks is left at the start
   * and end
   */
  private val gapStartAndEnd: BooleanProperty = object: StyleableBooleanProperty(true) {
	override fun invalidated() {
	  requestAxisLayout()
	}

	override fun getCssMetaData(): CssMetaData<CategoryAxisForCatAxisWrapper, Boolean> {
	  return StyleableProperties.GAP_START_AND_END
	}

	override fun getBean(): Any {
	  return this@CategoryAxisForCatAxisWrapper
	}

	override fun getName(): String {
	  return "gapStartAndEnd"
	}
  }

  fun isGapStartAndEnd(): Boolean {
	return gapStartAndEnd.value
  }

  fun setGapStartAndEnd(value: Boolean) {
	gapStartAndEnd.value = value
  }

  fun gapStartAndEndProperty(): BooleanProperty {
	return gapStartAndEnd
  }

  internal val categories: ObjectProperty<ObservableList<String>> =
	object: ObjectPropertyBase<ObservableList<String>>() {
	  var old: ObservableList<String>? = null
	  override fun invalidated() {
		require(duplicate == null) { "Duplicate category added; " + duplicate + " already present" }
		val newItems = get()
		if (old !== newItems) {
		  // Add and remove listeners
		  if (old != null) old!!.removeListener(itemsListener)
		  newItems?.addListener(itemsListener)
		  old = newItems
		}
	  }

	  override fun getBean(): Any {
		return this@CategoryAxisForCatAxisWrapper
	  }

	  override fun getName(): String {
		return "categories"
	  }
	}

  /**
   * The ordered list of categories plotted on this axis. This is set automatically
   * based on the charts data if autoRanging is true. If the application sets the categories
   * then auto ranging is turned off. If there is an attempt to add duplicate entry into this list,
   * an [IllegalArgumentException] is thrown.
   * @param value the ordered list of categories plotted on this axis
   */
  fun setCategories(value: ObservableList<String>) {
	categories.set(value)
	if (!changeIsLocal) {
	  setAutoRanging(false)
	  allDataCategories.clear()
	  allDataCategories.addAll(getCategories()!!)
	}
	requestAxisLayout()
  }

  private fun checkAndRemoveDuplicates(category: String) {
	if (duplicate != null) {
	  getCategories()!!.remove(category)
	  throw IllegalArgumentException("Duplicate category ; $category already present")
	}
  }

  private val duplicate: String?
	get() {
	  if (getCategories() != null) {
		for (i in getCategories()!!.indices) {
		  for (j in getCategories()!!.indices) {
			if (getCategories()!![i] == getCategories()!![j] && i != j) {
			  return getCategories()!![i]
			}
		  }
		}
	  }
	  return null
	}

  /**
   * Returns a [ObservableList] of categories plotted on this axis.
   *
   * @return ObservableList of categories for this axis.
   */
  fun getCategories(): ObservableList<String>? {
	return categories.get()
  }

  /** This is the gap between one category and the next along this axis  */
  internal val categorySpacing = ReadOnlyDoubleWrapper(this, "categorySpacing", 1.0)
  fun getCategorySpacing(): Double {
	return categorySpacing.get()
  }

  fun categorySpacingProperty(): ReadOnlyDoubleProperty {
	return categorySpacing.readOnlyProperty
  }
  // -------------- CONSTRUCTORS -------------------------------------------------------------------------------------
  /**
   * Create a auto-ranging category axis with an empty list of categories.
   */
  constructor() {
	changeIsLocal = true
	setCategories(FXCollections.observableArrayList())
	changeIsLocal = false
  }

  /**
   * Create a category axis with the given categories. This will not auto-range but be fixed with the given categories.
   *
   * @param categories List of the categories for this axis
   */
  constructor(categories: ObservableList<String>) {
	setCategories(categories)
  }

  // -------------- PRIVATE METHODS ----------------------------------------------------------------------------------
  private fun calculateNewSpacing(length: Double, categories: List<String>?): Double {
	var newCategorySpacing = 1.0
	if (categories != null) {
	  val bVal = (if (isGapStartAndEnd()) categories.size else categories.size - 1).toDouble()
	  // RT-14092 flickering  : check if bVal is 0
	  newCategorySpacing = if (bVal == 0.0) 1.0 else (length - getStartMargin() - getEndMargin())/bVal
	}
	// if autoranging is off setRange is not called so we update categorySpacing
	if (!isAutoRanging()) categorySpacing.set(newCategorySpacing)
	return newCategorySpacing
  }

  private fun calculateNewFirstPos(length: Double, catSpacing: Double): Double {
	val side = effectiveSide
	@Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER") var newPos = 1.0
	val offset: Double = if (isGapStartAndEnd()) catSpacing/2 else 0.0
	newPos = if (side.isHorizontal) {
	  0 + getStartMargin() + offset
	} else { // VERTICAL
	  length - getStartMargin() - offset
	}
	// if autoranging is off setRange is not called so we update first cateogory pos.
	if (!isAutoRanging()) firstCategoryPos.set(newPos)
	return newPos
  }
  // -------------- PROTECTED METHODS --------------------------------------------------------------------------------
  /**
   * Called to get the current axis range.
   *
   * @return A range object that can be passed to setRange() and calculateTickValues()
   */


  override val range: Any
	get() = arrayOf(
	  getCategories(), categorySpacing.get(), firstCategoryPos.get(),
	  effectiveTickLabelRotation
	)

  /**
   * Called to set the current axis range to the given range. If isAnimating() is true then this method should
   * animate the range to the new range.
   *
   * @param range A range object returned from autoRange()
   * @param animate If true animate the change in range
   */
  override fun setRange(range: Any, animate: Boolean) {
	@Suppress("UNCHECKED_CAST")
	val rangeArray = range as Array<Any>
	@Suppress("UNCHECKED_CAST")
	val categories = rangeArray[0] as List<String>
	//        if (categories.isEmpty()) new java.lang.Throwable().printStackTrace();
	val newCategorySpacing = rangeArray[1] as Double
	val newFirstCategoryPos = rangeArray[2] as Double
	effectiveTickLabelRotation = (rangeArray[3] as Double)
	changeIsLocal = true
	setCategories(FXCollections.observableArrayList(categories))
	changeIsLocal = false
	if (animate) {
	  animator.stop(currentAnimationID)
	  currentAnimationID = animator.animate(
		KeyFrame(
		  Duration.ZERO,
		  KeyValue(firstCategoryPos, firstCategoryPos.get()),
		  KeyValue(categorySpacing, categorySpacing.get())
		),
		KeyFrame(
		  Duration.millis(1000.0),
		  KeyValue(firstCategoryPos, newFirstCategoryPos),
		  KeyValue(categorySpacing, newCategorySpacing)
		)
	  )
	} else {
	  categorySpacing.set(newCategorySpacing)
	  firstCategoryPos.set(newFirstCategoryPos)
	}
  }

  /**
   * This calculates the categories based on the data provided to invalidateRange() method. This must not
   * effect the state of the axis, changing any properties of the axis. Any results of the auto-ranging should be
   * returned in the range object. This will we passed to setRange() if it has been decided to adopt this range for
   * this axis.
   *
   * @param length The length of the axis in screen coordinates
   * @return Range information, this is implementation dependent
   */
  override fun autoRange(length: Double): Any {
	val side = effectiveSide
	// TODO check if we can display all categories
	val newCategorySpacing = calculateNewSpacing(length, allDataCategories)
	val newFirstPos = calculateNewFirstPos(length, newCategorySpacing)
	var tickLabelRotation = tickLabelRotation.value
	if (length >= 0) {
	  val requiredLengthToDisplay = calculateRequiredSize(side.isVertical, tickLabelRotation)
	  if (requiredLengthToDisplay > length) {
		// try to rotate the text to increase the density
		if (side.isHorizontal && tickLabelRotation != 90.0) {
		  tickLabelRotation = 90.0
		}
		if (side.isVertical && tickLabelRotation != 0.0) {
		  tickLabelRotation = 0.0
		}
	  }
	}
	return arrayOf(allDataCategories, newCategorySpacing, newFirstPos, tickLabelRotation)
  }

  private fun calculateRequiredSize(axisVertical: Boolean, tickLabelRotation: Double): Double {
	// Calculate the max space required between categories labels
	var maxReqTickGap = 0.0
	var last = 0.0
	var first = true
	for (category in allDataCategories) {
	  val textSize = measureTickMarkSize(category, tickLabelRotation)
	  val size = if (axisVertical || tickLabelRotation != 0.0) textSize.height else textSize.width
	  // TODO better handle calculations for rotated text, overlapping text etc
	  if (first) {
		first = false
		last = size/2
	  } else {
		maxReqTickGap = Math.max(maxReqTickGap, last + 6 + size/2)
	  }
	}
	return getStartMargin() + maxReqTickGap*allDataCategories.size + getEndMargin()
  }

  /**
   * Calculate a list of all the data values for each tick mark in range
   *
   * @param length The length of the axis in display units
   * @return A list of tick marks that fit along the axis if it was the given length
   */
  @Suppress("UNCHECKED_CAST")
  override fun calculateTickValues(length: Double, range: Any): List<String> {
	val rangeArray = range as Array<Any>
	return rangeArray[0] as List<String>
  }


  /**
   * Get the string label name for a tick mark with the given value
   *
   * @param value The value to format into a tick label string
   * @return A formatted string for the given value
   */
  override fun getTickMarkLabel(value: String): String {
	// TODO use formatter
	return value
  }

  /**
   * Measure the size of the label for given tick mark value. This uses the font that is set for the tick marks
   *
   * @param value tick mark value
   * @param range range to use during calculations
   * @return size of tick mark label for given value
   */
  @Suppress("UNCHECKED_CAST")
  override fun measureTickMarkSize(value: String, range: Any): Dimension2D {
	val rangeArray = range as Array<Any>
	val tickLabelRotation = rangeArray[3] as Double
	return measureTickMarkSize(value, tickLabelRotation)
  }



  // -------------- METHODS ------------------------------------------------------------------------------------------
  /**
   * Called when data has changed and the range may not be valid any more. This is only called by the chart if
   * isAutoRanging() returns true. If we are auto ranging it will cause layout to be requested and auto ranging to
   * happen on next layout pass.
   *
   * @param data The current set of all data that needs to be plotted on this axis
   */
  override fun invalidateRange(data: List<String>) {
	super.invalidateRange(data)
	// Create unique set of category names
	val categoryNames: MutableList<String> = ArrayList()
	categoryNames.addAll(allDataCategories)
	//RT-21141 allDataCategories needs to be updated based on data -
	// and should maintain the order it originally had for the categories already present.
	// and remove categories not present in data
	for (cat in allDataCategories) {
	  if (!data.contains(cat)) categoryNames.remove(cat)
	}
	// add any new category found in data
	//        for(String cat : data) {
	for (i in data.indices) {
	  val len = categoryNames.size
	  if (!categoryNames.contains(data[i])) categoryNames.add(if (i > len) len else i, data[i])
	}
	allDataCategories.clear()
	allDataCategories.addAll(categoryNames)
  }

  fun getAllDataCategories(): List<String> {
	return allDataCategories
  }

  /**
   * Get the display position along this axis for a given value.
   *
   * If the value is not equal to any of the categories, Double.NaN is returned
   *
   * @param value The data value to work out display position for
   * @return display position or Double.NaN if value not one of the categories
   */
  override fun getDisplayPosition(value: String): Double {
	// find index of value
	val cat = getCategories()
	if (!cat!!.contains(value)) {
	  return Double.NaN
	}
	return if (effectiveSide.isHorizontal) {
	  firstCategoryPos.get() + cat.indexOf(value)*categorySpacing.get()
	} else {
	  firstCategoryPos.get() + cat.indexOf(value)*categorySpacing.get()*-1
	}
  }

  /**
   * Get the data value for the given display position on this axis. If the axis
   * is a CategoryAxis this will be the nearest value.
   *
   * @param  displayPosition A pixel position on this axis
   * @return the nearest data value to the given pixel position or
   * null if not on axis;
   */
  override fun getValueForDisplay(displayPosition: Double): String? {
	return if (effectiveSide.isHorizontal) {
	  if (displayPosition < 0 || displayPosition > width) return null
	  val d = (displayPosition - firstCategoryPos.get())/categorySpacing.get()
	  toRealValue(d)
	} else { // VERTICAL
	  if (displayPosition < 0 || displayPosition > height) return null
	  val d = (displayPosition - firstCategoryPos.get())/(categorySpacing.get()*-1)
	  toRealValue(d)
	}
  }

  /**
   * Checks if the given value is plottable on this axis
   *
   * @param value The value to check if its on axis
   * @return true if the given value is plottable on this axis
   */
  override fun isValueOnAxis(value: String): Boolean {
	return getCategories()!!.indexOf("" + value) != -1
  }

  /**
   * All axis values must be representable by some numeric value. This gets the numeric value for a given data value.
   *
   * @param value The data value to convert
   * @return Numeric value for the given data value
   */
  override fun toNumericValue(value: String): Double {
	return getCategories()!!.indexOf(value).toDouble()
  }

  /**
   * All axis values must be representable by some numeric value. This gets the data value for a given numeric value.
   *
   * @param value The numeric value to convert
   * @return Data value for given numeric value
   */
  override fun toRealValue(value: Double): String? {
	val index = Math.round(value).toInt()
	val categories: List<String>? = getCategories()
	return if (index >= 0 && index < categories!!.size) {
	  getCategories()!![index]
	} else {
	  null
	}
  }

  /**
   * Get the display position of the zero line along this axis. As there is no concept of zero on a CategoryAxis
   * this is always Double.NaN.
   *
   * @return always Double.NaN for CategoryAxis
   */
  override val zeroPosition: Double
	get() = Double.NaN

  // -------------- STYLESHEET HANDLING ------------------------------------------------------------------------------
  private object StyleableProperties {
	val START_MARGIN: CssMetaData<CategoryAxisForCatAxisWrapper, Number> = object: CssMetaData<CategoryAxisForCatAxisWrapper, Number>(
	  "-fx-start-margin",
	  SizeConverter.getInstance(), 5.0
	) {
	  override fun isSettable(n: CategoryAxisForCatAxisWrapper): Boolean {
		return n.startMargin.value == null || !n.startMargin.isBound
	  }

	  @Suppress("UNCHECKED_CAST")
	  override fun getStyleableProperty(n: CategoryAxisForCatAxisWrapper): StyleableProperty<Number?> {
		return n.startMarginProperty() as StyleableProperty<Number?>
	  }
	}
	val END_MARGIN: CssMetaData<CategoryAxisForCatAxisWrapper, Number> = object: CssMetaData<CategoryAxisForCatAxisWrapper, Number>(
	  "-fx-end-margin",
	  SizeConverter.getInstance(), 5.0
	) {
	  override fun isSettable(n: CategoryAxisForCatAxisWrapper): Boolean {
		return n.endMargin.value == null || !n.endMargin.isBound
	  }

	  @Suppress("UNCHECKED_CAST")
	  override fun getStyleableProperty(n: CategoryAxisForCatAxisWrapper): StyleableProperty<Number?> {
		return n.endMarginProperty() as StyleableProperty<Number?>
	  }
	}
	val GAP_START_AND_END: CssMetaData<CategoryAxisForCatAxisWrapper, Boolean> = object: CssMetaData<CategoryAxisForCatAxisWrapper, Boolean>(
	  "-fx-gap-start-and-end",
	  BooleanConverter.getInstance(), java.lang.Boolean.TRUE
	) {
	  override fun isSettable(n: CategoryAxisForCatAxisWrapper): Boolean {
		return n.gapStartAndEnd.value == null || !n.gapStartAndEnd.isBound
	  }

	  @Suppress("UNCHECKED_CAST")
	  override fun getStyleableProperty(n: CategoryAxisForCatAxisWrapper): StyleableProperty<Boolean?> {
		return n.gapStartAndEndProperty() as StyleableProperty<Boolean?>
	  }
	}
	val classCssMetaData: List<CssMetaData<out Styleable?, *>> by lazy {
	  val styleables: MutableList<CssMetaData<out Styleable?, *>> = ArrayList(getClassCssMetaData())
	  styleables.add(START_MARGIN)
	  styleables.add(END_MARGIN)
	  styleables.add(GAP_START_AND_END)
	   Collections.unmodifiableList(styleables)
	}

  }

  /**
   * {@inheritDoc}
   * @since JavaFX 8.0
   */
  override fun getCssMetaData(): List<CssMetaData<out Styleable?, *>> {
	return classCssMetaData
  }

  companion object
}