package matt.fx.control.wrapper.chart.xy.series

import javafx.scene.Node
import javafx.scene.chart.XYChart.Data
import javafx.scene.chart.XYChart.Series
import javafx.scene.paint.Color
import javafx.scene.shape.Path
import matt.collect.weak.WeakMap
import matt.fx.control.wrapper.chart.xy.series.SeriesWrapper.Companion.wrappers
import matt.hurricanefx.eye.wrapper.obs.collect.mfxMutableListConverter
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNullableROProp
import matt.lang.setAll
import matt.model.op.convert.Converter
import matt.model.op.convert.NullToBlankStringConverter
import matt.obs.col.olist.MutableObsList
import matt.obs.listen.Listener

fun <X, Y> List<Data<X, Y>>.toSeries() = SeriesWrapper<X, Y>().apply {
  data.setAll(this@toSeries)
}

class SeriesConverter<X, Y>(): Converter<Series<X, Y>, SeriesWrapper<X, Y>> {
  override fun convertToB(a: Series<X, Y>): SeriesWrapper<X, Y> {
	@Suppress("UNCHECKED_CAST")
	return a.wrapped() as SeriesWrapper<X, Y>
  }

  override fun convertToA(b: SeriesWrapper<X, Y>): Series<X, Y> {
	return b.series
  }
}

fun <X, Y> Series<X, Y>.wrapped() = wrappers[this] ?: SeriesWrapper(this)

class SeriesWrapper<X, Y>(val series: Series<X, Y> = Series<X, Y>()) {
  companion object {
	internal val wrappers = WeakMap<Series<*, *>, SeriesWrapper<*, *>>()
  }

  init {
	wrappers[series] = this
  }

  val nameProperty by lazy { series.nameProperty().toNullableProp().proxyInv(NullToBlankStringConverter) }
  var name by nameProperty

  val dataProperty by lazy {
	series.dataProperty().toNonNullableProp().proxy(mfxMutableListConverter())
  }
  var data by dataProperty

  fun setTheData(theData: MutableObsList<Data<X, Y>>) {
	data = theData
  }

  val chartProperty by lazy { series.chartProperty().toNullableROProp() }
  val chart by chartProperty

  val nodeProperty by lazy { series.nodeProperty().toNullableProp() }
  var node by nodeProperty

  private var strokeListener: Listener? = null
  private var strokeThatWasSet: Color? = null
  var stroke: Color
	get() = strokeThatWasSet ?: error("no stroke was set")
	set(value) {

	  fun update(node: Node) {
		//		println("updating ${node}")
		//		val l = node.lookup(".chart-series-line")
		//
		//		println("lookup=$l setting color to ${value.hex()}")
		//		/*l?.style = "-fx-stroke: ${value.hex()}"*/
		//		if (l != null) {
		//		  (l as Path).apply {
		//			runLater {
		//			  stroke = value
		//			  fill = value
		//			  strokeWidth = 100.0
		//			}
		//		  }
		//		}
		(node as Path).apply {
		  stroke = value
		  strokeProperty().addListener { _, _, n ->
			/*yup need to do it this way so javafx cannot get their colors in*/
			if (n != value) {
			  stroke = value
			}
		  }

		  //		  runLater { /*removing this runLater breaks this*/
		  //			runLater { /*wow... i need 2. this is filthy*/
		  //			  stroke = value
		  //			}
		  //		  }


		}
	  }

	  strokeListener?.let(nodeProperty::removeListener)

	  nodeProperty.onNonNullChange {
		update(it)
	  }
	  node?.let(::update)
	  strokeThatWasSet = value
	  //	  require(chart != null) {
	  //		"""
	  //		this stuff must be done AFTER series are added to chart, or else NPE
	  //		see: https://stackoverflow.com/questions/11153370/how-to-set-specific-color-to-javafx-xychart-series
	  //	  """.trimIndent()
	  //	  }
	  //	  node!!.lookup(".chart-series-line").style = "-fx-stroke: ${value.hex()}"
	}

}