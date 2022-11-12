package matt.fx.control.wrapper.chart.axis.value.number.tickconfig.unitless

import kotlinx.serialization.Serializable
import matt.fx.control.wrapper.chart.axis.value.moregenval.ValueAxisConverter
import matt.fx.graphics.anim.FXDoubleWrapper
import matt.model.num.NumberWrapper

/*I CANT USE VALUE CALSSES*/
/*https://youtrack.jetbrains.com/issue/KT-54513/java.lang.NoSuchMethodError-with-value-class-implementing-an-interface*/
@Serializable data class UnitLess(override val asNumber: Double): FXDoubleWrapper<UnitLess>, NumberWrapper {
  companion object {
	val ZERO = UnitLess(0.0)
	val ONE = UnitLess(1.0)
	val TWO = UnitLess(2.0)
  }
  override val asDouble get() = asNumber
  override fun fromDouble(d: Double): UnitLess {
	return UnitLess(d)
  }

  override fun toString(): String {
	return "$asNumber"
  }
}


object UnitLessConverter: ValueAxisConverter<UnitLess> {
  override fun convertToB(a: UnitLess): Double {
	return a.asNumber
  }

  override fun convertToA(b: Double): UnitLess {
	return UnitLess(b)
  }

}