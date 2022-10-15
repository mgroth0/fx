package matt.fx.control.wrapper.chart.axis.value.number.moregennum

import com.sun.javafx.charts.ChartLayoutAnimator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.beans.property.BooleanProperty
import javafx.beans.property.BooleanPropertyBase
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.css.CssMetaData
import javafx.css.Styleable
import javafx.geometry.Dimension2D
import javafx.geometry.Side
import javafx.util.Duration
import javafx.util.StringConverter
import matt.fx.control.wrapper.chart.axis.value.moregenval.MoreGenericValueAxis
import matt.fx.control.wrapper.chart.axis.value.moregenval.ValueAxisConverter
import matt.obs.prop.BindableProperty
import java.text.DecimalFormat


/**
 * An axis class that plots a range of numbers with major tick marks every tickUnit.
 * You can use any Number type with this axis.
 * @since JavaFX 2.0
 */
class MoreGenericNumberAxis<T: Any>(
  converter: ValueAxisConverter<T>,
  lowerBound: T? = null,
  upperBound: T? = null,
): MoreGenericValueAxis<T>(
  lowerBound = lowerBound,
  upperBound = upperBound,
  converter = converter
) {
  private var currentAnimationID: Any? = null
  private val animator = ChartLayoutAnimator(this)
  private val currentFormatterProperty: StringProperty = SimpleStringProperty(this, "currentFormatter", "")
  private val defaultFormatter = DefaultFormatter(this)
  // -------------- PUBLIC PROPERTIES --------------------------------------------------------------------------------
  /** When true zero is always included in the visible range. This only has effect if auto-ranging is on.  */
  private val forceZeroInRange: BooleanProperty = object: BooleanPropertyBase(true) {
	override fun invalidated() {
	  // This will affect layout if we are auto ranging
	  if (isAutoRanging) {
		requestAxisLayout()
		invalidateRange()
	  }
	}

	override fun getBean(): Any {
	  return this@MoreGenericNumberAxis
	}

	override fun getName(): String {
	  return "forceZeroInRange"
	}
  }

  fun isForceZeroInRange(): Boolean {
	return forceZeroInRange.value
  }

  fun setForceZeroInRange(value: Boolean) {
	forceZeroInRange.value = value
  }

  fun forceZeroInRangeProperty(): BooleanProperty {
	return forceZeroInRange
  }

  /*matt was here: this is my attempt to remove ticks*/
  fun maximizeTickUnit() {
	tickUnit.value = Double.MAX_VALUE.convert()
  }

  /**  The value between each major tick mark in data units. This is automatically set if we are auto-ranging.  */
  val tickUnit = BindableProperty(5.0.convert()).apply {
	onChange {
	  if (!isAutoRanging) {
		invalidateRange()
		requestAxisLayout()
	  }
	}
  }

  /*
	private val tickUnit:  = object: StyleableDoubleProperty(5.0) {
	  override fun invalidated() {

	  }

	  override fun getCssMetaData(): CssMetaData<MoreGenericNumberAxis<*>, Number> {
		return StyleableProperties.TICK_UNIT
	  }

	  override fun getBean(): Any {
		return this@MoreGenericNumberAxis
	  }

	  override fun getName(): String {
		return "tickUnit"
	  }
	}

	fun getTickUnit(): Double {
	  return tickUnit.get()
	}

	fun setTickUnit(value: Double) {
	  tickUnit.set(value)
	}

	fun tickUnitProperty(): DoubleProperty {
	  return tickUnit
	}*/


  // -------------- PROTECTED METHODS --------------------------------------------------------------------------------
  /**
   * Get the string label name for a tick mark with the given value.
   *
   * @param value The value to format into a tick label string
   * @return A formatted string for the given value
   */
  override fun getTickMarkLabel(value: T): String {
	val formatter = tickLabelFormatter
	if (formatter.value == null) formatter.value = defaultFormatter
	return formatter.value.toString(value)
  }

  /**
   * Called to get the current axis range.
   *
   * @return A range object that can be passed to setRange() and calculateTickValues()
   */
  override fun getRange(): Any {
	return arrayOf<Any>(
	  lowerBound.value.convert(),
	  upperBound.value.convert(),
	  tickUnit.value.convert(),
	  scale,
	  currentFormatterProperty.get()
	)
  }

  /**
   * Called to set the current axis range to the given range. If isAnimating() is true then this method should
   * animate the range to the new range.
   *
   * @param range A range object returned from autoRange()
   * @param animate If true animate the change in range
   */
  override fun setRange(range: Any, animate: Boolean) {
	@Suppress("UNCHECKED_CAST")
	val rangeProps = range as Array<Any>
	val lowerBound = rangeProps[0] as Double
	val upperBound = rangeProps[1] as Double
	val tickUnit = rangeProps[2] as Double
	val scale = rangeProps[3] as Double
	val formatter = rangeProps[4] as String
	currentFormatterProperty.set(formatter)
	val oldLowerBound = this.lowerBound.value
	this.lowerBound.value = (lowerBound.convert())
	this.upperBound.value = (upperBound.convert())
	this.tickUnit.value = (tickUnit.convert())
	if (animate) {
	  animator.stop(currentAnimationID)
	  currentAnimationID = animator.animate(
		KeyFrame(
		  Duration.ZERO,
		  KeyValue(currentLowerBound, oldLowerBound.convert()),
		  KeyValue(scalePropertyImpl(), getScale())
		),
		KeyFrame(
		  Duration.millis(700.0),
		  KeyValue(currentLowerBound, lowerBound),
		  KeyValue(scalePropertyImpl(), scale)
		)
	  )
	} else {
	  currentLowerBound.set(lowerBound)
	  setScale(scale)
	}
  }

  /**
   * Calculates a list of all the data values for each tick mark in range
   *
   * @param length The length of the axis in display units
   * @param range A range object returned from autoRange()
   * @return A list of tick marks that fit along the axis if it was the given length
   */
  override fun calculateTickValues(length: Double, range: Any): List<T> {
	@Suppress("UNCHECKED_CAST")
	val rangeProps = range as Array<Any>
	val lowerBound = rangeProps[0] as Double
	val upperBound = rangeProps[1] as Double
	val tickUnit = rangeProps[2] as Double
	val tickValues: MutableList<Number> = ArrayList()
	if (lowerBound == upperBound) {
	  tickValues.add(lowerBound)
	} else if (tickUnit <= 0) {
	  tickValues.add(lowerBound)
	  tickValues.add(upperBound)
	} else if (tickUnit > 0) {
	  tickValues.add(lowerBound)
	  if ((upperBound - lowerBound)/tickUnit > 2000) {
		// This is a ridiculous amount of major tick marks, something has probably gone wrong
		System.err.println(
		  "Warning we tried to create more than 2000 major tick marks on a NumberAxis. " +
			  "Lower Bound=" + lowerBound + ", Upper Bound=" + upperBound + ", Tick Unit=" + tickUnit
		)
	  } else {
		if (lowerBound + tickUnit < upperBound) {
		  // If tickUnit is integer, start with the nearest integer
		  var major = if (Math.rint(tickUnit) == tickUnit) Math.ceil(lowerBound) else lowerBound + tickUnit
		  val count = Math.ceil((upperBound - major)/tickUnit).toInt()
		  var i = 0
		  while (major < upperBound && i < count) {
			if (!tickValues.contains(major)) {
			  tickValues.add(major)
			}
			major += tickUnit
			i++
		  }
		}
	  }
	  tickValues.add(upperBound)
	}
	return tickValues.map { converter.convertToA(it.toDouble()) }
  }

  /**
   * Calculates a list of the data values for every minor tick mark
   *
   * @return List of data values where to draw minor tick marks
   */
  override fun calculateMinorTickMarks(): List<T> {
	val minorTickMarks: MutableList<Number> = ArrayList()
	val lowerBound = lowerBound
	val upperBound = upperBound
	val tickUnit = this.tickUnit.value.convert()
	val minorUnit = tickUnit/Math.max(1, minorTickCount.get())
	if (tickUnit > 0) {
	  if (((upperBound.value.convert() - lowerBound.value.convert())/minorUnit) > 10000) {
		// This is a ridiculous amount of major tick marks, something has probably gone wrong
		System.err.println(
		  ("Warning we tried to create more than 10000 minor tick marks on a NumberAxis. " +
			  "Lower Bound=" + this.lowerBound.value + ", Upper Bound=" + this.upperBound.value + ", Tick Unit=" + tickUnit)
		)
		return minorTickMarks.map { converter.convertToA(it.toDouble()) }
	  }
	  val tickUnitIsInteger = Math.rint(tickUnit) == tickUnit
	  if (tickUnitIsInteger) {
		var minor = Math.floor(lowerBound.value.convert()) + minorUnit
		val count = Math.ceil((Math.ceil(lowerBound.value.convert()) - minor)/minorUnit).toInt()
		var i = 0
		while (minor < Math.ceil(lowerBound.value.convert()) && i < count) {
		  if (minor > lowerBound.value.convert()) {
			minorTickMarks.add(minor)
		  }
		  minor += minorUnit
		  i++
		}
	  }
	  var major = if (tickUnitIsInteger) Math.ceil(lowerBound.value.convert()) else lowerBound.value.convert()
	  val count = Math.ceil((upperBound.value.convert() - major)/tickUnit).toInt()
	  var i = 0
	  while (major < upperBound.value.convert() && i < count) {
		val next = Math.min(major + tickUnit, upperBound.value.convert())
		var minor = major + minorUnit
		val minorCount = Math.ceil((next - minor)/minorUnit).toInt()
		var j = 0
		while (minor < next && j < minorCount) {
		  minorTickMarks.add(minor)
		  minor += minorUnit
		  j++
		}
		major += tickUnit
		i++
	  }
	}
	return minorTickMarks.map { converter.convertToA(it.toDouble()) }
  }

  /**
   * Measures the size of the label for a given tick mark value. This uses the font that is set for the tick marks.
   *
   * @param value tick mark value
   * @param range range to use during calculations
   * @return size of tick mark label for given value
   */
  override fun measureTickMarkSize(value: T, range: Any): Dimension2D {
	@Suppress("UNCHECKED_CAST")
	val rangeProps = range as Array<Any>
	val formatter = rangeProps[4] as String
	return measureTickMarkSize(value, tickLabelRotation, formatter)
  }

  /**
   * Measures the size of the label for a given tick mark value. This uses the font that is set for the tick marks.
   *
   * @param value     tick mark value
   * @param rotation  The text rotation
   * @param numFormatter The number formatter
   * @return size of tick mark label for given value
   */
  private fun measureTickMarkSize(value: T, rotation: Double, numFormatter: String): Dimension2D {
	val labelText: String
	val formatter = tickLabelFormatter
	if (formatter.value == null) formatter.value = defaultFormatter
	if (formatter.value is DefaultFormatter) {
	  labelText = (formatter.value as DefaultFormatter).toString(value, numFormatter)
	} else {
	  labelText = formatter.value.toString(value)
	}
	return measureTickMarkLabelSize(labelText, rotation)
  }

  /**
   * Called to set the upper and lower bound and anything else that needs to be auto-ranged.
   *
   * @param minValue The min data value that needs to be plotted on this axis
   * @param maxValue The max data value that needs to be plotted on this axis
   * @param length The length of the axis in display coordinates
   * @param labelSize The approximate average size a label takes along the axis
   * @return The calculated range
   */
  override fun autoRange(minValue: Double, maxValue: Double, length: Double, labelSize: Double): Any {
	@Suppress("NAME_SHADOWING")
	var minValue = minValue

	@Suppress("NAME_SHADOWING")
	var maxValue = maxValue

	val side = getEffectiveSideMethod.invoke(this) as Side
	// check if we need to force zero into range
	if (isForceZeroInRange()) {
	  if (maxValue < 0) {
		maxValue = 0.0
	  } else if (minValue > 0) {
		minValue = 0.0
	  }
	}
	// calculate the number of tick-marks we can fit in the given length
	var numOfTickMarks = Math.floor(length/labelSize).toInt()
	// can never have less than 2 tick marks one for each end
	numOfTickMarks = Math.max(numOfTickMarks, 2)
	val minorTickCount = Math.max(minorTickCount.get(), 1)
	var range = maxValue - minValue
	if (range != 0.0 && range/(numOfTickMarks*minorTickCount) <= Math.ulp(minValue)) {
	  range = 0.0
	}
	// pad min and max by 2%, checking if the range is zero
	val paddedRange: Double =
	  if ((range == 0.0)) if (minValue == 0.0) 2.0 else Math.abs(minValue)*0.02 else Math.abs(range)*1.02
	val padding = (paddedRange - range)/2
	// if min and max are not zero then add padding to them
	var paddedMin = minValue - padding
	var paddedMax = maxValue + padding
	// check padding has not pushed min or max over zero line
	if ((paddedMin < 0 && minValue >= 0) || (paddedMin > 0 && minValue <= 0)) {
	  // padding pushed min above or below zero so clamp to 0
	  paddedMin = 0.0
	}
	if ((paddedMax < 0 && maxValue >= 0) || (paddedMax > 0 && maxValue <= 0)) {
	  // padding pushed min above or below zero so clamp to 0
	  paddedMax = 0.0
	}
	// calculate tick unit for the number of ticks can have in the given data range
	var tickUnit = paddedRange/numOfTickMarks.toDouble()
	// search for the best tick unit that fits
	var tickUnitRounded = 0.0
	var minRounded = 0.0
	var maxRounded = 0.0
	var count = 0
	var reqLength = Double.MAX_VALUE
	var formatter = "0.00000000"
	// loop till we find a set of ticks that fit length and result in a total of less than 20 tick marks
	while (reqLength > length || count > 20) {
	  var exp = Math.floor(Math.log10(tickUnit)).toInt()
	  val mant = tickUnit/Math.pow(10.0, exp.toDouble())
	  var ratio = mant
	  if (mant > 5.0) {
		exp++
		ratio = 1.0
	  } else if (mant > 1.0) {
		ratio = if (mant > 2.5) 5.0 else 2.5
	  }
	  if (exp > 1) {
		formatter = "#,##0"
	  } else if (exp == 1) {
		formatter = "0"
	  } else {
		val ratioHasFrac = Math.rint(ratio) != ratio
		val formatterB = StringBuilder("0")
		val n = if (ratioHasFrac) Math.abs(exp) + 1 else Math.abs(exp)
		if (n > 0) formatterB.append(".")
		for (i in 0 until n) {
		  formatterB.append("0")
		}
		formatter = formatterB.toString()
	  }
	  tickUnitRounded = ratio*Math.pow(10.0, exp.toDouble())
	  // move min and max to nearest tick mark
	  minRounded = Math.floor(paddedMin/tickUnitRounded)*tickUnitRounded
	  maxRounded = Math.ceil(paddedMax/tickUnitRounded)*tickUnitRounded
	  // calculate the required length to display the chosen tick marks for real, this will handle if there are
	  // huge numbers involved etc or special formatting of the tick mark label text
	  var maxReqTickGap = 0.0
	  var last = 0.0
	  count = Math.ceil((maxRounded - minRounded)/tickUnitRounded).toInt()
	  var major = minRounded
	  var i = 0
	  while (major <= maxRounded && i < count) {
		val markSize = measureTickMarkSize(converter.convertToA(major), tickLabelRotation, formatter)
		val size = if (side.isVertical) markSize.height else markSize.width
		if (i == 0) { // first
		  last = size/2
		} else {
		  maxReqTickGap = Math.max(maxReqTickGap, last + 6 + (size/2))
		}
		major += tickUnitRounded
		i++
	  }
	  reqLength = (count - 1)*maxReqTickGap
	  tickUnit = tickUnitRounded

	  // fix for RT-35600 where a massive tick unit was being selected
	  // unnecessarily. There is probably a better solution, but this works
	  // well enough for now.
	  if (numOfTickMarks == 2 && reqLength > length) {
		break
	  }
	  if (reqLength > length || count > 20) tickUnit *= 2.0 // This is just for the while loop, if there are still too many ticks
	}
	// calculate new scale
	val newScale = calculateNewScale(length, minRounded, maxRounded)
	// return new range
	return arrayOf<Any>(minRounded, maxRounded, tickUnitRounded, newScale, formatter)
  }
  /*
	// -------------- STYLESHEET HANDLING ------------------------------------------------------------------------------
	private object StyleableProperties {
	  val TICK_UNIT: CssMetaData<MoreGenericNumberAxis<*>, Number> =
		object: CssMetaData<MoreGenericNumberAxis<*>, Number>(
		  "-fx-tick-unit",
		  SizeConverter.getInstance(), 5.0
		) {
		  override fun isSettable(n: MoreGenericNumberAxis<*>): Boolean {
			return n.tickUnit.value == null || !n.tickUnit.isBound
		  }

		  override fun getStyleableProperty(n: MoreGenericNumberAxis<*>): StyleableProperty<Number> {
			@Suppress("UNCHECKED_CAST")
			return n.tickUnitProperty() as StyleableProperty<Number>
		  }
		}
	  var classCssMetaData: List<CssMetaData<out Styleable?, *>>? = null
		private set

	  */
  /**
   * Gets the `CssMetaData` associated with this class, which may include the
   * `CssMetaData` of its superclasses.
   * @return the `CssMetaData`
   * @since JavaFX 8.0
   *//*

	init {
	  val styleables: MutableList<CssMetaData<out Styleable?, *>> = ArrayList(
		getClassCssMetaData()
	  )
	  styleables.add(TICK_UNIT)
	  classCssMetaData = Collections.unmodifiableList(styleables)
	}
  }*/

  /**
   * {@inheritDoc}
   * @since JavaFX 8.0
   */
  override fun getCssMetaData(): List<CssMetaData<out Styleable?, *>> {
	return listOf()
	/*return StyleableProperties.classCssMetaData*/
  }
  // -------------- INNER CLASSES ------------------------------------------------------------------------------------
  /**
   * Default number formatter for NumberAxis, this stays in sync with auto-ranging and formats values appropriately.
   * You can wrap this formatter to add prefixes or suffixes;
   * @since JavaFX 2.0
   */
  inner class DefaultFormatter(axis: MoreGenericNumberAxis<*>): StringConverter<T>() {
	private var formatter: DecimalFormat
	private var prefix: String? = null
	private var suffix: String? = null

	/**
	 * Construct a DefaultFormatter for the given NumberAxis
	 *
	 * @param axis The axis to format tick marks for
	 */
	init {
	  formatter = if (axis.isAutoRanging) DecimalFormat(axis.currentFormatterProperty.get()) else DecimalFormat()
	  val axisListener =
		ChangeListener { _: ObservableValue<*>?, _: Any?, _: Any? ->
		  formatter = if (axis.isAutoRanging) DecimalFormat(
			axis.currentFormatterProperty.get()
		  ) else DecimalFormat()
		}
	  axis.currentFormatterProperty.addListener(axisListener)
	  axis.autoRangingProperty().addListener(axisListener)
	}

	/**
	 * Construct a DefaultFormatter for the given NumberAxis with a prefix and/or suffix.
	 *
	 * @param axis The axis to format tick marks for
	 * @param prefix The prefix to append to the start of formatted number, can be null if not needed
	 * @param suffix The suffix to append to the end of formatted number, can be null if not needed
	 */
	constructor(axis: MoreGenericNumberAxis<*>, prefix: String?, suffix: String?): this(axis) {
	  this.prefix = prefix
	  this.suffix = suffix
	}

	/**
	 * Converts the object provided into its string form.
	 * Format of the returned string is defined by this converter.
	 * @return a string representation of the object passed in.
	 * @see StringConverter.toString
	 */
	override fun toString(`object`: T): String {
	  return toString(`object`, formatter)
	}

	internal fun toString(`object`: T, numFormatter: String?): String {
	  return if (numFormatter == null || numFormatter.isEmpty()) {
		toString(`object`, formatter)
	  } else {
		toString(`object`, DecimalFormat(numFormatter))
	  }
	}

	private fun toString(`object`: T, formatter: DecimalFormat): String {
	  if (prefix != null && suffix != null) {
		return prefix + formatter.format(`object`) + suffix
	  } else if (prefix != null) {
		return prefix + formatter.format(`object`)
	  } else return if (suffix != null) {
		formatter.format(`object`) + suffix
	  } else {
		formatter.format(`object`)
	  }
	}

	/**
	 * Converts the string provided into a Number defined by the this converter.
	 * Format of the string and type of the resulting object is defined by this converter.
	 * @return a Number representation of the string passed in.
	 * @see StringConverter.toString
	 */
	override fun fromString(string: String): T {
	  /*try {*/
	  val prefixLength = if ((prefix == null)) 0 else prefix!!.length
	  val suffixLength = if ((suffix == null)) 0 else suffix!!.length
	  return converter.convertToA(
		formatter.parse(string.substring(prefixLength, string.length - suffixLength)).toDouble()
	  )
	  /*} catch (e: ParseException) {
		return null
	  }*/
	}
  }

}
