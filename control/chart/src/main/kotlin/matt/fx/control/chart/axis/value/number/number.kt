package matt.fx.control.chart.axis.value.number

import matt.fig.modell.plot.axis.NumericalAxis
import matt.fig.modell.plot.axis.tick.DefaultTickConfigurer
import matt.fig.modell.plot.axis.tick.TickConfigurer
import matt.fig.modell.plot.convert.ValueAxisConverter
import matt.fx.control.chart.axis.value.ValueAxisWrapper
import matt.fx.control.chart.axis.value.number.moregennum.MoreGenericNumberAxis
import matt.model.data.mathable.DoubleWrapper
import matt.model.data.mathable.MathAndComparable

fun <T : DoubleWrapper<T>> numAxis(
    tickConfigurer: DefaultTickConfigurer<T>
) = NumberAxisWrapper(tickConfigurer.converter, tickConfigurer)

fun <T : MathAndComparable<T>> numAxis(
    converter: ValueAxisConverter<T>,
    tickConfigurer: TickConfigurer<T>
) = NumberAxisWrapper(converter, tickConfigurer)


fun <T : MathAndComparable<T>> NumericalAxis<T>.numAxis() = NumberAxisWrapper(valueCodec.converter, tickConfigurer)

class NumberAxisWrapper<T : MathAndComparable<T>>(
    override val node: MoreGenericNumberAxis<T>,
    val tickConfigurer: TickConfigurer<T>
) : ValueAxisWrapper<T>(node) {

    constructor(
        converter: ValueAxisConverter<T>,
        tickConfigurer: TickConfigurer<T>
    ) : this(
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

/*

class OldNumberAxisWrapper(node: NumberAxis = NumberAxis()): OldValueAxisWrapper<Number>(node) {

  //  constructor(converter: ValueAxisConverter<T>): this(MoreGenericNumberAxis(converter))

  *//*val tickUnitProperty by lazy { node.tickUnit }
  var tickUnit by tickUnitProperty*//*



  //  fun maximizeTickUnit() = node.maximizeTickUnit()

}*/
