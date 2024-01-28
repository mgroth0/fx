package matt.fx.graphics.anim.interp

import javafx.animation.Interpolatable
import javafx.animation.Interpolator
import matt.fx.base.rewrite.ReWrittenFxClass
import matt.model.data.interp.BasicInterpolatable
import java.lang.reflect.Method


fun Interpolator.wrap() = InterpolatorWrapper(this)

class InterpolatorWrapper(private val curver: Interpolator): MyInterpolator() {
  companion object {
	private val curveFun: Method by lazy {
	  Interpolator::class.java.getDeclaredMethod("curve", Double::class.java).apply {
		isAccessible = true
	  }
	}
  }

  override fun curve(t: Double): Double {
	return curveFun.invoke(curver, t) as Double
  }
}




@ReWrittenFxClass(Interpolator::class)
abstract class MyInterpolator: Interpolator() {

  companion object {

	val DISCRETE = Interpolator.DISCRETE.wrap()
	val LINEAR = Interpolator.LINEAR.wrap()
	val EASE_BOTH = Interpolator.EASE_BOTH.wrap()
	val EASE_IN = Interpolator.EASE_IN.wrap()
	val EASE_OUT = Interpolator.EASE_OUT.wrap()
	fun SPLINE(x1: Double, y1: Double, x2: Double, y2: Double) = Interpolator.SPLINE(x1, y1, x2, y2).wrap()
	/*and there is more where that comes from...*/

	val MY_DEFAULT_INTERPOLATOR = LINEAR
  }

  final override fun interpolate(startValue: Any?, endValue: Any?, fraction: Double): Any {
	return when {
	  (startValue is Number && endValue is Number)                                 -> super.interpolate(
		startValue, endValue, fraction
	  )

	  (startValue is Interpolatable<*> && endValue is Interpolatable<*>)           -> super.interpolate(
		startValue, endValue, fraction
	  )

	  (startValue is BasicInterpolatable<*> && endValue is BasicInterpolatable<*>) -> startValue.interpolate(
		endValue, curve(fraction)
	  )

	  else                                                                         -> super.interpolate(
		startValue, endValue, fraction
	  )
	}
  }


}