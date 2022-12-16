package matt.fx.node.proto.annochart

import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import matt.collect.map.lazyMap
import matt.fx.control.wrapper.chart.axis.value.number.NumberAxisWrapper
import matt.fx.control.wrapper.chart.axis.value.number.tickconfig.showBestTicksIn
import matt.fx.control.wrapper.chart.line.ChartLocater
import matt.fx.control.wrapper.chart.line.LineChartWrapper
import matt.fx.control.wrapper.chart.line.NumericChartLocater
import matt.fx.control.wrapper.chart.scatter.ScatterChartWrapper
import matt.fx.control.wrapper.chart.xy.series.SeriesWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.fx.node.proto.annochart.AnnotateableChart.VisibleDataMode.DownSampled
import matt.fx.node.proto.annochart.AnnotateableChart.VisibleDataMode.Source
import matt.fx.node.proto.annochart.annopane.Annotateable
import matt.fx.node.proto.annochart.annopane.AnnotationPane
import matt.fx.node.proto.annochart.inner.applyBounds
import matt.fx.node.proto.annochart.inner.calcAutoBounds
import matt.lang.go
import matt.model.data.mathable.MathAndComparable
import matt.model.data.xyz.Dim2D
import matt.obs.col.olist.ObsList
import matt.obs.col.olist.basicMutableObservableListOf
import matt.obs.col.olist.cat.concatenatedTo
import matt.obs.col.olist.mappedlist.toMappedList
import matt.obs.prop.BindableProperty


fun <X: MathAndComparable<X>, Y: MathAndComparable<Y>> ET.annoChart(
  xAxis: NumberAxisWrapper<X>, yAxis: NumberAxisWrapper<Y>, op: AnnotateableChart<X, Y>.()->Unit = {}
) = AnnotateableChart(
  xAxis = xAxis.minimal(),
  yAxis = yAxis.minimal(),
).attachTo(this, op)

open class AnnotateableChart<X: MathAndComparable<X>, Y: MathAndComparable<Y>> private constructor(
  stack: StackPane, xAxis: NumberAxisWrapper<X>, yAxis: NumberAxisWrapper<Y>, scatter: Boolean
): RegionWrapperImpl<Region, NW>(stack), Annotateable<X, Y>, ChartLocater<X, Y> {

  constructor(
	xAxis: NumberAxisWrapper<X>, yAxis: NumberAxisWrapper<Y>, scatter: Boolean = false
  ): this(
	StackPane(), xAxis = xAxis, yAxis = yAxis, scatter = scatter
  )


  val chart = (if (scatter) ScatterChartWrapper(x = xAxis, y = yAxis) else LineChartWrapper<X, Y>(
	x = xAxis, y = yAxis
  )).apply {
	configureForHighPerformance()
  }

  val title = chart.titleProperty

  val xAxis = chart.xAxis as NumberAxisWrapper
  val yAxis = chart.yAxis as NumberAxisWrapper

  var animated by chart::animated
  val horizontalZeroLineVisibleProperty get() = chart.horizontalZeroLineVisibleProperty
  val verticalZeroLineVisibleProperty get() = chart.verticalZeroLineVisibleProperty
  private val annotationSeries = basicMutableObservableListOf<SeriesWrapper<X, Y>>()
  val realData = basicMutableObservableListOf<SeriesWrapper<X, Y>>()
  private val annoPane = AnnotationPane(
	NumericChartLocater(
	  lineChart = chart,
	  xAxis = xAxis,
	  yAxis = yAxis
	),
	chartBoundsProp = chart.layoutBoundsProperty,
	chartHeightProp = chart.heightProperty,
	chartWidthProp = chart.widthProperty,
	xAxis = xAxis,
	yAxis = yAxis,
	annotationSeries = annotationSeries,
	realData = realData
  )

  override fun layoutXOf(x: X) = annoPane.layoutXOf(x)
  override fun layoutYOf(y: Y) = annoPane.layoutYOf(y)

  override fun staticRectangle(minX: X, maxX: X) = annoPane.staticRectangle(minX, maxX)
  override fun dynamicRectangle(minX: X, maxX: X) = annoPane.dynamicRectangle(minX, maxX)
  override fun dynamicVerticalLine(x: X) = annoPane.dynamicVerticalLine(x)
  override fun dynamicHorizontalLine(y: Y) = annoPane.dynamicHorizontalLine(y)
  override fun staticText(minX: X, text: String) = annoPane.staticText(minX, text)
  override fun dynamicText(minX: X, text: String) = annoPane.dynamicText(minX, text)
  override fun staticVerticalLine(x: X) = annoPane.staticVerticalLine(x)
  override fun staticHorizontalLine(y: Y) = annoPane.staticHorizontalLine(y)
  override fun addLegend() = annoPane.addLegend()


  private val downSampledRealData by lazy {
	realData.toMappedList { it.downsampled }
  }

  fun showBestYTicks() = yAxis.showBestTicksIn(chart)
  fun showBestTicks() {
	xAxis.showBestTicksIn(chart)
	showBestYTicks()
  }


  enum class VisibleDataMode {
	Source, DownSampled
  }

  private val dataViews = lazyMap<VisibleDataMode, ObsList<SeriesWrapper<X, Y>>>() {
	when (it) {
	  Source      -> annotationSeries.concatenatedTo(realData)
	  DownSampled -> annotationSeries.concatenatedTo(downSampledRealData)
	}
  }

  @Suppress("MemberVisibilityCanBePrivate") val visibleDataModeProp = BindableProperty(Source).apply {
	chart.data.bind(dataViews[value])
	onChange {
	  chart.data.bind(dataViews[it])
	}
  }
  var visibleDataMode by visibleDataModeProp

  init {
	stack.children.addAll(chart.node, annoPane.annotationLayer.node)
  }


  fun autoRangeBothAxes() {
	autoRangeY()
	autoRangeX()
  }


  private fun yValues() = realData.asSequence().flatMap { it.data.map { it.yValue } }
  private fun xValues() = realData.asSequence().flatMap { it.data.map { it.xValue } }
  fun autoRangeY(
	forceMin: Y? = null,
  ) {
	calcAutoBounds(
	  mn = yValues().minOrNull(), mx = yValues().maxOrNull(), forceMin = forceMin
	)?.go(yAxis::applyBounds)
  }

  fun autoRangeX(forceMin: X? = null) {
	calcAutoBounds(
	  mn = xValues().minOrNull(), mx = xValues().maxOrNull(), forceMin = forceMin
	)?.go(xAxis::applyBounds)
  }

  private fun autoRange(dim: Dim2D) = when (dim) {
	Dim2D.X -> autoRangeX()
	Dim2D.Y -> autoRangeY()
  }


  fun clearAnnotations() {
	annotationSeries.clear()
	annoPane.clear()
  }


}

