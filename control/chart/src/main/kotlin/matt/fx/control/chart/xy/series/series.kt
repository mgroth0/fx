package matt.fx.control.chart.xy.series

import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.Path
import matt.collect.list.downSampled
import matt.collect.weak.WeakMap
import matt.fx.control.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps.Data
import matt.fx.control.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps.Series
import matt.fx.control.chart.xy.series.SeriesWrapper.Companion.wrappers
import matt.fx.base.wrapper.obs.collect.list.mfxMutableListConverter
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.base.wrapper.obs.obsval.toNullableROProp
import matt.lang.setall.setAll
import matt.model.op.convert.Converter
import matt.model.op.convert.NullToBlankStringConverter
import matt.obs.col.olist.MutableObsList
import matt.obs.listen.Listener
import matt.obs.prop.BindableProperty

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
  val strokeProp = BindableProperty<Color?>(null).apply {
	strokeListener = onChange {
	  fun update(node: Node) {
		(node as Path).apply {
		  stroke = it
		  strokeProperty().addListener { _, _, n ->
			/*yup need to do it this way so javafx cannot get their colors in*/
			if (n != value) {
			  stroke = value
			}
		  }
		}
	  }
	  strokeListener?.let(nodeProperty::removeListener)
	  nodeProperty.onNonNullChange {
		update(it)
	  }
	  node?.let(::update)
	}
  }

  var stroke by strokeProp


  val downsampled by lazy {
	val ds = SeriesWrapper<X, Y>()
	data.onChange {
	  ds.data.setAll(data.downSampled())
	}
	ds.nameProperty.bind(nameProperty)
	ds.strokeProp.bind(strokeProp)
	ds
  }


}