package matt.fx.control.wrapper.chart.axis.value.number

import javafx.scene.chart.NumberAxis
import matt.fx.control.wrapper.chart.axis.value.OldValueAxisWrapper
import matt.fx.control.wrapper.chart.axis.value.ValueAxisWrapper
import matt.fx.control.wrapper.chart.axis.value.moregenval.ValueAxisConverter
import matt.fx.control.wrapper.chart.axis.value.number.moregennum.MoreGenericNumberAxis


fun <T: Any> minimalNumberAxis(converter: ValueAxisConverter<T>) = NumberAxisWrapper(converter).apply {
  minorTickCount = 0
  isAutoRanging = false
  isTickMarkVisible = false
  isTickLabelsVisible = false
}


class NumberAxisWrapper<T: Any>(override val node: MoreGenericNumberAxis<T>): ValueAxisWrapper<T>(node) {

  constructor(converter: ValueAxisConverter<T>): this(MoreGenericNumberAxis(converter))

  val tickUnitProperty by lazy { node.tickUnit }
  var tickUnit by tickUnitProperty


  fun maximizeTickUnit() = node.maximizeTickUnit()

  fun displayPixelOf(v: T) = node.getDisplayPosition(v)

  fun valueForDisplayPixel(pixel: Double) = node.getValueForDisplay(pixel)

}


class OldNumberAxisWrapper(node: NumberAxis = NumberAxis()): OldValueAxisWrapper<Number>(node) {

  //  constructor(converter: ValueAxisConverter<T>): this(MoreGenericNumberAxis(converter))

  /*val tickUnitProperty by lazy { node.tickUnit }
  var tickUnit by tickUnitProperty*/


  //  fun maximizeTickUnit() = node.maximizeTickUnit()

}