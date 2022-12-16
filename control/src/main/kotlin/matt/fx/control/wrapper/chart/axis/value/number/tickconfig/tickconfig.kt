package matt.fx.control.wrapper.chart.axis.value.number.tickconfig

import javafx.util.StringConverter
import matt.fx.control.wrapper.chart.axis.value.moregenval.ValueAxisConverter
import matt.fx.control.wrapper.chart.axis.value.number.NumberAxisWrapper
import matt.fx.control.wrapper.chart.axis.value.number.tickconfig.calcticks.calcBestTicks
import matt.fx.control.wrapper.chart.axis.value.number.tickconfig.unitless.UnitLess
import matt.fx.control.wrapper.chart.axis.value.number.tickconfig.unitless.UnitLessConverter
import matt.fx.control.wrapper.chart.line.LineChartWrapper
import matt.fx.control.wrapper.chart.xy.XYChartWrapper
import matt.fx.graphics.dur.DurationWrapper
import matt.fx.graphics.dur.wrapped
import matt.lang.function.Convert
import matt.math.jmath.decimalOrScientificNotation
import matt.model.data.byte.ByteSize
import matt.model.data.byte.gigabytes
import matt.model.data.byte.kilobytes
import matt.model.data.byte.megabytes
import matt.model.data.mathable.DoubleWrapper
import matt.model.data.mathable.IntWrapper
import matt.model.data.mathable.MathAndComparable
import matt.model.op.convert.Converter
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

inline fun <reified X: MathAndComparable<X>, reified Y: MathAndComparable<Y>> LineChartWrapper<X, Y>.showBestTicks() {
  (xAxis as NumberAxisWrapper<X>).showBestTicksNoLayout()
  (yAxis as NumberAxisWrapper<Y>).showBestTicksNoLayout()
  requestLayout()
}

fun <T: MathAndComparable<T>> NumberAxisWrapper<T>.showBestTicksNoLayout() {
  tickConfigurer.showBestTicksNoLayout(this)
}

fun <T: MathAndComparable<T>> NumberAxisWrapper<T>.showBestTicksIn(chart: XYChartWrapper<*, *, *>) {
  showBestTicksNoLayout()
  chart.requestLayout()
}


open class DefaultTickConfigurer<T: DoubleWrapper<T>>(val converter: ValueAxisConverter<T>):
  TickConfigurer<T>(minorTickCount = 0) {
  private val default = converter.convertToA(100.0)

  private val labelConvert: Convert<T, String> = {
	converter.convertToB(it).decimalOrScientificNotation()
  }

  private val zero = converter.convertToA(0.0)
  private val two = converter.convertToA(2.0)

  override fun bestTickUnit(range: T) = calcBestTicks(range) ?: default

  override fun tickLabelConverter(range: T) = labelConvert
}

open class DefaultIntTickConfigurer<T: IntWrapper<T>>(val converter: Converter<T, Int>):
  TickConfigurer<T>(minorTickCount = 0) {
  private val default = converter.convertToA(100)

  private val labelConvert: Convert<T, String> = {
	converter.convertToB(it).toDouble().decimalOrScientificNotation()
  }

  private val zero = converter.convertToA(0)
  private val two = converter.convertToA(2)

  override fun bestTickUnit(range: T) = calcBestTicks(range) ?: default

  override fun tickLabelConverter(range: T) = labelConvert
}


object UnitLessTickConfigurer: DefaultTickConfigurer<UnitLess>(UnitLessConverter)


object DurationWrapperTickConfigurer: TickConfigurer<DurationWrapper>(minorTickCount = 10) {
  override fun bestTickUnit(range: DurationWrapper) = when {
	range < 10.milliseconds.wrapped()  -> 1.milliseconds.wrapped()
	range < 100.milliseconds.wrapped() -> 10.milliseconds.wrapped()
	range < 1.seconds.wrapped()        -> 100.milliseconds.wrapped()
	range < 10.seconds.wrapped()       -> 1.seconds.wrapped()
	range < 50.seconds.wrapped()       -> 5.seconds.wrapped()
	range < 100.seconds.wrapped()      -> 10.seconds.wrapped()
	range < 10.minutes.wrapped()       -> 1.minutes.wrapped()
	range < 100.minutes.wrapped()      -> 10.minutes.wrapped()
	range < 10.hours.wrapped()         -> 1.hours.wrapped()
	else                               -> 24.hours.wrapped()
  }

  override fun tickLabelConverter(range: DurationWrapper): Convert<DurationWrapper, String> {
	val bestUnit = when {
	  range < 10.milliseconds.wrapped()  -> DurationUnit.MILLISECONDS
	  range < 100.milliseconds.wrapped() -> DurationUnit.MILLISECONDS
	  range < 1.seconds.wrapped()        -> DurationUnit.MILLISECONDS
	  range < 10.seconds.wrapped()       -> DurationUnit.SECONDS
	  range < 100.seconds.wrapped()      -> DurationUnit.SECONDS
	  range < 10.minutes.wrapped()       -> DurationUnit.MINUTES
	  range < 100.minutes.wrapped()      -> DurationUnit.MINUTES
	  range < 10.hours.wrapped()         -> DurationUnit.HOURS
	  else                               -> DurationUnit.MILLISECONDS
	}

	return {
	  it.toString(unit = bestUnit, decimals = 2)
	}

  }
}

object ByteSizeTickConfigurer: TickConfigurer<ByteSize>(minorTickCount = 10) {
  override fun bestTickUnit(range: ByteSize) = when {
	range.bytes < 10       -> ByteSize(1)
	range.bytes < 100      -> ByteSize(10)

	range < 1.kilobytes   -> ByteSize(100)
	range < 10.kilobytes  -> 1.kilobytes
	range < 100.kilobytes -> 10.kilobytes

	range < 1.megabytes   -> 100.kilobytes
	range < 10.megabytes  -> 1.megabytes
	range < 100.megabytes  -> 10.megabytes

	range < 1.gigabytes    -> 100.megabytes
	range < 10.gigabytes   -> 1.gigabytes
	range < 100.gigabytes  -> 10.gigabytes


	else                   -> ByteSize(Long.MAX_VALUE)
  }

  override fun tickLabelConverter(range: ByteSize): Convert<ByteSize, String> {
	return {
	  it.toString()
	}

  }
}


abstract class TickConfigurer<T: MathAndComparable<T>>(
  val minorTickCount: Int
) {

  abstract fun bestTickUnit(range: T): T
  abstract fun tickLabelConverter(range: T): Convert<T, String>

  fun showBestTicksNoLayout(axis: NumberAxisWrapper<T>) {
	val amountShown = (axis.upperBound - axis.lowerBound)


	axis.apply {
	  isTickMarkVisible = true
	  isMinorTickVisible = true
	  minorTickCount = this@TickConfigurer.minorTickCount
	  isTickLabelsVisible = true
	}


	axis.tickUnit = bestTickUnit(amountShown)


	val converter = tickLabelConverter(amountShown)

	val stringConverter = object: StringConverter<T>() {

	  override fun toString(`object`: T): String {
		return converter.invoke(`object`)
	  }

	  override fun fromString(string: String): T {
		TODO("Not yet implemented")
	  }


	}

	axis.tickLabelFormatter = stringConverter

  }

  fun showBestTicksIn(axis: NumberAxisWrapper<T>, chart: LineChartWrapper<*, *>) {
	showBestTicksNoLayout(axis)
	chart.requestLayout()
  }
}