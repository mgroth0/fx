package matt.fx.control.wrapper.chart.xy.series

import javafx.scene.chart.XYChart.Data
import javafx.scene.chart.XYChart.Series
import javafx.scene.paint.Color
import matt.collect.weak.WeakMap
import matt.hurricanefx.eye.wrapper.obs.collect.mfxMutableListConverter
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNullableROProp
import matt.hurricanefx.wrapper.chart.xy.series.SeriesWrapper.Companion.wrappers
import matt.fx.graphics.wrapper.style.hex
import matt.lang.NOT_IMPLEMENTED
import matt.model.convert.NullToBlankStringConverter
import matt.obs.col.olist.MutableObsList

fun <X, Y> Series<X, Y>.wrapped() = wrappers[this] ?: SeriesWrapper(this)

class SeriesWrapper<X, Y>(internal val series: Series<X, Y> = Series<X, Y>()) {
  companion object {
	internal val wrappers = WeakMap<Series<*, *>, SeriesWrapper<*, *>>()
  }

  init {
	wrappers[series] = this
  }

  val nameProperty by lazy { series.nameProperty().toNullableProp().proxy(NullToBlankStringConverter.inverted) }
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

  var stroke: Color
	get() = NOT_IMPLEMENTED
	set(value) {
	  require(chart != null) {
		"""
		this stuff must be done AFTER series are added to chart, or else NPE
		see: https://stackoverflow.com/questions/11153370/how-to-set-specific-color-to-javafx-xychart-series
	  """.trimIndent()
	  }
	  node!!.lookup(".chart-series-line").style = "-fx-stroke: ${value.hex()}"
	}

}