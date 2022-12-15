package matt.fx.control.wrapper.chart.axis.value.number.tickconfig.calcticks

import matt.model.data.mathable.NumberWrapper

internal fun <T: NumberWrapper<T>> calcBestTicks(
  range: NumberWrapper<T>
): T? {
  if (range.isZero || range.isInfinite || range.isNaN) return null
  val absRange = range.abs
  val goodDecimalAbove = if (absRange < range.of(2)) {
	var maybeGoodDecimalAbove = range.of(1)
	while (absRange < maybeGoodDecimalAbove/5.0) maybeGoodDecimalAbove /= 10.0
	maybeGoodDecimalAbove
  } else {
	var maybeGoodDecimalAbove = range.of(10)
	while (absRange > maybeGoodDecimalAbove*1.2) {
	  maybeGoodDecimalAbove *= 10.0
	}
	maybeGoodDecimalAbove
  }
  return goodDecimalAbove/10.0

}