package matt.fx.control.wrapper.chart.axis.value.number.tickconfig

import javafx.util.StringConverter
import matt.fx.control.wrapper.chart.axis.value.number.NumberAxisWrapper
import matt.fx.control.wrapper.chart.axis.value.number.tickconfig.unitless.UnitLess
import matt.fx.control.wrapper.chart.line.LineChartWrapper
import matt.fx.graphics.dur.DurationWrapper
import matt.fx.graphics.dur.wrapped
import matt.lang.function.Convert
import matt.math.jmath.decimalOrScientificNotation
import matt.model.byte.ByteSize
import matt.model.byte.gigabytes
import matt.model.byte.killobytes
import matt.model.byte.megabytes
import matt.model.mathable.MathAndComparable
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

inline fun <reified T: MathAndComparable<T>> NumberAxisWrapper<T>.showBestTicksNoLayout() {
  @Suppress("UNCHECKED_CAST") when (T::class) {
	DurationWrapper::class -> DurationWrapperTickConfigurer.showBestTicksNoLayout(
	  this as NumberAxisWrapper<DurationWrapper>
	)

	UnitLess::class        -> UnitLessTickConfigurer.showBestTicksNoLayout(this as NumberAxisWrapper<UnitLess>)
	ByteSize::class        -> ByteSizeTickConfigurer.showBestTicksNoLayout(this as NumberAxisWrapper<ByteSize>)
  }
}

inline fun <reified T: MathAndComparable<T>> NumberAxisWrapper<T>.showBestTicksIn(chart: LineChartWrapper<*, *>) {
  showBestTicksNoLayout()
  chart.requestLayout()
}


object UnitLessTickConfigurer: TickConfigurer<UnitLess>(
  minorTickCount = 0
) {
  private val default = UnitLess(100.0)

  private val labelConvert: Convert<UnitLess, String> = {
	it.asNumber.decimalOrScientificNotation()
  }

  override fun bestTickUnit(range: UnitLess): UnitLess {

	if (range == UnitLess.ZERO) return default
	else {
	  val theAbs = UnitLess(kotlin.math.abs(range.asDouble))
	  val goodDecimalAbove = if (theAbs < UnitLess.TWO) {
		var maybeGoodDecimalAbove = UnitLess(1.0)
		while (theAbs < maybeGoodDecimalAbove/5.0) {
		  maybeGoodDecimalAbove /= 10.0
		}
		maybeGoodDecimalAbove
	  } else {
		var maybeGoodDecimalAbove = UnitLess(10.0)
		while (theAbs > maybeGoodDecimalAbove*1.2) {
		  maybeGoodDecimalAbove *= 10.0
		}
		maybeGoodDecimalAbove
	  }
	  return goodDecimalAbove/10.0
	}

  }

  override fun tickLabelConverter(range: UnitLess) = labelConvert
}

object DurationWrapperTickConfigurer: TickConfigurer<DurationWrapper>(minorTickCount = 10) {
  override fun bestTickUnit(range: DurationWrapper) = when {
	range < 10.milliseconds.wrapped()  -> 1.milliseconds.wrapped()
	range < 100.milliseconds.wrapped() -> 10.milliseconds.wrapped()
	range < 1.seconds.wrapped()        -> 100.milliseconds.wrapped()
	range < 10.seconds.wrapped()       -> 1.seconds.wrapped()
	range < 50.seconds.wrapped()      -> 5.seconds.wrapped()
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

	range < 1.killobytes   -> ByteSize(100)
	range < 10.killobytes  -> 1.killobytes
	range < 100.killobytes -> 10.killobytes

	range < 1.megabytes    -> 100.killobytes
	range < 10.megabytes   -> 1.megabytes
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
	val durationShown = (axis.upperBound - axis.lowerBound)

	axis.apply {
	  isTickMarkVisible = true
	  isMinorTickVisible = true
	  minorTickCount = minorTickCount
	  isTickLabelsVisible = true
	}


	axis.tickUnit = bestTickUnit(durationShown)


	val converter = tickLabelConverter(durationShown)

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