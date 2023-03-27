package matt.fx.node.chart.annochart.inner

import matt.fx.control.chart.axis.value.number.NumberAxisWrapper
import matt.fx.graphics.fxthread.ensureInFXThreadInPlace
import matt.model.data.mathable.MathAndComparable

internal fun <T: MathAndComparable<T>> NumberAxisWrapper<T>.applyBounds(result: BoundCalcResult<T>) {
  ensureInFXThreadInPlace {
	lowerBound = result.lowerBound
	upperBound = result.upperBound
  }
}

internal class BoundCalcResult<T: MathAndComparable<T>>(
  val lowerBound: T, val upperBound: T
)

internal fun <T: MathAndComparable<T>> calcAutoBounds(
  mn: T?,
  mx: T?,
  forceMin: T?
) = run {
  if (mn == null || mx == null) return@run null
  val range = mx - mn
  val margin = range*0.1
  BoundCalcResult(
	lowerBound = forceMin ?: (mn - margin), upperBound = mx + margin
  )
}