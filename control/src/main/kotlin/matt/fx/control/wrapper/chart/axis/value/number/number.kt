package matt.fx.control.wrapper.chart.axis.value.number

import javafx.scene.chart.NumberAxis
import matt.fx.control.wrapper.chart.axis.value.OldValueAxisWrapper
import matt.fx.control.wrapper.chart.axis.value.ValueAxisWrapper
import matt.fx.control.wrapper.chart.axis.value.moregenval.DoubleAxisConverter
import matt.fx.control.wrapper.chart.axis.value.moregenval.HzConverter
import matt.fx.control.wrapper.chart.axis.value.moregenval.MicroVoltConverter
import matt.fx.control.wrapper.chart.axis.value.moregenval.ValueAxisConverter
import matt.fx.control.wrapper.chart.axis.value.number.moregennum.MoreGenericNumberAxis
import matt.fx.control.wrapper.chart.axis.value.number.tickconfig.ByteSizeTickConfigurer
import matt.fx.control.wrapper.chart.axis.value.number.tickconfig.DefaultIntTickConfigurer
import matt.fx.control.wrapper.chart.axis.value.number.tickconfig.DefaultTickConfigurer
import matt.fx.control.wrapper.chart.axis.value.number.tickconfig.DurationWrapperTickConfigurer
import matt.fx.control.wrapper.chart.axis.value.number.tickconfig.TickConfigurer
import matt.fx.control.wrapper.chart.axis.value.number.tickconfig.UnitLessTickConfigurer
import matt.fx.control.wrapper.chart.axis.value.number.tickconfig.unitless.UnitLessConverter
import matt.fx.graphics.dur.MilliSecondDurationWrapperConverter
import matt.math.index.IndexWrapperConverter
import matt.math.index.IndexWrapperIntConverter
import matt.model.data.byte.ByteSizeDoubleConverter
import matt.model.data.mathable.DoubleWrapper
import matt.model.data.mathable.MathAndComparable
import matt.model.data.percent.PercentDoubleConverter


fun timeAxis() = NumberAxisWrapper(MilliSecondDurationWrapperConverter, DurationWrapperTickConfigurer)
fun voltageAxis() = numAxis(DefaultTickConfigurer(MicroVoltConverter))
fun frequencyAxis() = numAxis(DefaultTickConfigurer(HzConverter))
fun unitlessAxis() = numAxis(UnitLessTickConfigurer)
fun indexAxis() = NumberAxisWrapper(IndexWrapperConverter, DefaultIntTickConfigurer(IndexWrapperIntConverter))
fun byteAxis() = NumberAxisWrapper(ByteSizeDoubleConverter, ByteSizeTickConfigurer)
fun percentAxis() = numAxis(DefaultTickConfigurer(PercentDoubleConverter))

fun <T: DoubleWrapper<T>> numAxis(tickConfigurer: DefaultTickConfigurer<T>) =
  NumberAxisWrapper(tickConfigurer.converter, tickConfigurer)

class NumberAxisWrapper<T: MathAndComparable<T>>(
  override val node: MoreGenericNumberAxis<T>,
  val tickConfigurer: TickConfigurer<T>
): ValueAxisWrapper<T>(node) {

  constructor(converter: ValueAxisConverter<T>, tickConfigurer: TickConfigurer<T>): this(
	MoreGenericNumberAxis(converter), tickConfigurer
  )

  fun minimize() {
	minorTickCount = 0
	isAutoRanging = false
	isTickMarkVisible = false
	isTickLabelsVisible = false
  }

  fun minimal() = apply { minimize() }

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