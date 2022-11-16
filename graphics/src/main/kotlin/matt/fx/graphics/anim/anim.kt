package matt.fx.graphics.anim

import javafx.animation.Interpolatable
import javafx.animation.Interpolator
import matt.model.mathable.DoubleWrapper
import kotlin.time.Duration

interface FXDoubleWrapper<M: FXDoubleWrapper<M>>: DoubleWrapper<M>, Interpolatable<M> {

  override fun interpolate(endValue: M, t: Double): M {
	@Suppress("UNCHECKED_CAST")
	if (t <= 0.0) return this as M
	return if (t >= 1.0) endValue else fromDouble(
	  asDouble + (endValue.asDouble - asDouble)*t,
	)
  }
}


class MyInterpolator(
  private val curver: Interpolator,
): Interpolator() {
  companion object {
	private val curveFun by lazy {
	  Interpolator::class.java.getDeclaredMethod("curve", Double::class.java)
	}
  }

  override fun curve(t: Double): Double {
	return curveFun.invoke(curver, t) as Double
  }

  override fun interpolate(startValue: Any?, endValue: Any?, fraction: Double): Any {
	return if (startValue is Duration && endValue is Duration) {
	  (startValue + (endValue - startValue)*curve(fraction))
	} else super.interpolate(startValue, endValue, fraction)
  }
}


