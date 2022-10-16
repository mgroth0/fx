package matt.fx.graphics.dur

import javafx.animation.Interpolatable
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import matt.cbor.ser.MyCborSerializer
import matt.math.mathable.MathAndComparable
import matt.model.convert.Converter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.DurationUnit.SECONDS

object DurationByTheSecondSerializer: MyCborSerializer<DurationWrapper>(DurationWrapper::class) {
  override fun deserialize(decoder: Decoder): DurationWrapper {
	return decoder.decodeDouble().seconds.wrapped()
  }

  override fun serialize(encoder: Encoder, value: DurationWrapper) {
	encoder.encodeDouble(value.toDouble(SECONDS))
  }

}


fun Duration.wrapped() = DurationWrapper(this)

@Serializable(with = DurationByTheSecondSerializer::class)
@JvmInline
value class DurationWrapper(val dur: Duration): Interpolatable<DurationWrapper>,
												MathAndComparable<DurationWrapper> {

  override fun toString() = dur.toString()

  fun toString(unit: DurationUnit, decimals: Int = 0) = dur.toString(unit, decimals)

  override fun interpolate(endValue: DurationWrapper, t: Double): DurationWrapper {
	if (t <= 0.0) return this
	return if (t >= 1.0) endValue else
	  DurationWrapper(dur + (endValue.dur - this.dur)*t)
  }

  fun toDouble(unit: DurationUnit) = dur.toDouble(unit)

  val inWholeMilliseconds get() = dur.inWholeMilliseconds
  override fun plus(m: DurationWrapper): DurationWrapper {
	return (dur + m.dur).wrapped()
  }

  override fun minus(m: DurationWrapper): DurationWrapper {
	return (dur - m.dur).wrapped()
  }

  override fun div(m: DurationWrapper): Double {

	return (dur/m.dur)
  }

  override fun div(n: Number): DurationWrapper {
	return (dur/n.toDouble()).wrapped()
  }

  override fun times(n: Number): DurationWrapper {
	return (dur*n.toDouble()).wrapped()
  }


  override fun compareTo(other: DurationWrapper): Int {
	return dur.compareTo(other.dur)
  }

  fun remMillis(d: DurationWrapper): DurationWrapper {
	return inWholeMilliseconds.rem(d.inWholeMilliseconds).milliseconds.wrapped()
  }

}

object MilliSecondDurationWrapperConverter: Converter<DurationWrapper, Double> {
  override fun convertToB(a: DurationWrapper): Double {
	return a.inWholeMilliseconds.toDouble()
  }

  override fun convertToA(b: Double): DurationWrapper {
	return b.milliseconds.wrapped()
  }

}