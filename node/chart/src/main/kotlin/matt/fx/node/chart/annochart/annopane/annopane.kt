package matt.fx.node.chart.annochart.annopane

import javafx.geometry.Bounds
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import matt.fx.control.chart.axis.value.number.NumberAxisWrapper
import matt.fx.control.chart.line.ChartLocater
import matt.fx.control.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps.Data
import matt.fx.control.chart.xy.series.SeriesWrapper
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.line.LineWrapper
import matt.fx.graphics.wrapper.node.line.line
import matt.fx.graphics.wrapper.node.shape.circle.CircleWrapper
import matt.fx.graphics.wrapper.node.shape.rect.RectangleWrapper
import matt.fx.graphics.wrapper.node.shape.rect.rectangle
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.text.TextWrapper
import matt.fx.graphics.wrapper.text.text
import matt.fx.node.proto.annochart.annopane.legend.MyLegend
import matt.fx.node.proto.annochart.annopane.legend.MyLegend.LegendItem
import matt.lang.setall.setAll
import matt.model.data.mathable.MathAndComparable
import matt.obs.col.olist.MutableObsList
import matt.obs.col.olist.toBasicObservableList
import matt.obs.math.double.op.div
import matt.obs.math.double.op.times
import matt.obs.prop.ObsVal

interface Annotateable<X: MathAndComparable<X>, Y: MathAndComparable<Y>> {
  fun staticRectangle(minX: X, maxX: X): RectangleWrapper
  fun dynamicRectangle(minX: X, maxX: X): RectangleWrapper
  fun staticText(minX: X, text: String): TextWrapper
  fun dynamicVerticalLine(x: X): AnnotationPane<X, Y>.DynamicVerticalLine
  fun dynamicHorizontalLine(y: Y): AnnotationPane<X, Y>.DynamicHorizontalLine
  fun dynamicText(minX: X, text: String): TextWrapper
  fun staticVerticalLine(x: X): LineWrapper
  fun staticHorizontalLine(y: Y): LineWrapper
  fun addLegend(): MyLegend
}

class AnnotationPane<X: MathAndComparable<X>, Y: MathAndComparable<Y>>(
  loc: ChartLocater<X, Y>,
  chartBoundsProp: ObsVal<Bounds>,
  private val chartHeightProp: ObsVal<Double>,
  private val chartWidthProp: ObsVal<Double>,
  private val xAxis: NumberAxisWrapper<X>,
  private val yAxis: NumberAxisWrapper<Y>,
  private val annotationSeries: MutableObsList<SeriesWrapper<X, Y>>,
  private val realData: MutableObsList<SeriesWrapper<X, Y>>
): ChartLocater<X, Y> by loc, Annotateable<X, Y> {

  val chartBounds by chartBoundsProp

  val annotationLayer = PaneWrapperImpl<Pane, NW>(Pane())
  fun clear() = annotationLayer.clear()

  override fun staticRectangle(minX: X, maxX: X): RectangleWrapper {
	val minXPixel = layoutXOf(minX)
	val maxXPixel = layoutXOf(maxX)
	return annotationLayer.rectangle(
	  x = minXPixel,
	  width = maxXPixel - minXPixel,
	  y = 0.0,
	  height = chartBounds.height
	) {
	  heightProperty.bind(chartHeightProp)
	  stroke = Color.BLUE
	  fill = Color.TRANSPARENT
	}
  }

  override fun dynamicRectangle(minX: X, maxX: X): RectangleWrapper {


	return annotationLayer.rectangle(
	  y = 0.0,
	  height = chartBounds.height
	) {
	  heightProperty.bind(chartHeightProp)
	  stroke = Color.BLUE
	  fill = Color.TRANSPARENT
	  strokeWidth = 5.0

	  fun update(rect: RectangleWrapper) {
		val minXPixel = layoutXOf(minX)
		val maxXPixel = layoutXOf(maxX)
		rect.isVisible = minXPixel >= -100
			&& minXPixel < annotationLayer.width + 100
			&& maxXPixel >= -100
			&& maxXPixel < annotationLayer.width + 100
		if (rect.isVisible) {
		  rect.x = minXPixel
		  rect.width = maxXPixel - minXPixel
		}
	  }
	  update(this)

	  xAxis.upperBoundProperty.onChangeWithWeak(this) { w, _ ->
		update(w)
	  }
	  xAxis.lowerBoundProperty.onChangeWithWeak(this) { w, _ ->
		update(w)
	  }


	}
  }

  override fun staticText(
	minX: X, text: String
  ): TextWrapper {
	val minXPixel = layoutXOf(minX)
	return annotationLayer.text(
	  text
	) {
	  x = minXPixel
	  yProperty.bind(chartHeightProp*.90)

	}
  }

  override fun dynamicText(
	minX: X, text: String
  ): TextWrapper {
	var minXPixel = layoutXOf(minX)
	val txt = annotationLayer.text(
	  text
	) {
	  x = minXPixel
	  yProperty.bind(chartHeightProp*.90)
	}

	fun update(tw: TextWrapper) {
	  minXPixel = layoutXOf(minX)
	  tw.isVisible = minXPixel >= -100 && minXPixel < annotationLayer.width + 100
	  if (tw.isVisible) {
		tw.x = minXPixel
	  }
	}
	xAxis.upperBoundProperty.onChangeWithWeak(txt) { w, _ ->
	  update(w)
	}
	xAxis.lowerBoundProperty.onChangeWithWeak(txt) { w, _ ->
	  update(w)
	}
	return txt
  }


  override fun staticVerticalLine(x: X): LineWrapper {
	val xPixel = layoutXOf(x)
	return annotationLayer.line(
	  startX = xPixel, startY = 0.0, endX = xPixel, endY = 10.0
	) {
	  startYProperty.bind(annotationLayer.heightProperty*0.25)
	  endYProperty.bind(annotationLayer.heightProperty*0.75)
	  stroke = Color.YELLOW
	}
  }

  override fun staticHorizontalLine(y: Y): LineWrapper {
	val yPixel = layoutYOf(y)
	return annotationLayer.line(
	  startX = 0.0, startY = yPixel, endX = 10.0, endY = yPixel
	) {
	  startXProperty.bind(annotationLayer.widthProperty*0.25)
	  endXProperty.bind(annotationLayer.widthProperty*0.75)
	  stroke = Color.YELLOW
	}
  }

  inner abstract class DynamicLine(protected val lineSeries: SeriesWrapper<X, Y>) {
	val stroke = lineSeries.strokeProp
	var visible
	  get() = lineSeries in annotationSeries
	  set(value) {
		if (value) {
		  if (lineSeries !in annotationSeries) {
			annotationSeries += lineSeries
		  }
		} else {
		  annotationSeries -= lineSeries
		}
	  }
  }

  inner class DynamicVerticalLine(lineSeries: SeriesWrapper<X, Y>): DynamicLine(lineSeries) {
	var x: X
	  get() = lineSeries.data.first().xValue
	  set(value) {
		lineSeries.data.forEach {
		  it.xValue = value
		}
	  }
  }

  inner class DynamicHorizontalLine(lineSeries: SeriesWrapper<X, Y>): DynamicLine(lineSeries) {
	var y: Y
	  get() = lineSeries.data.first().yValue
	  set(value) {
		lineSeries.data.forEach {
		  it.yValue = value
		}
	  }
  }

  override fun dynamicVerticalLine(x: X): DynamicVerticalLine {
	val lineSeries = SeriesWrapper<X, Y>()
	annotationSeries.add(lineSeries.apply {
	  val points = listOf(
		yAxis.upperBoundProperty, yAxis.lowerBoundProperty
	  ).map {
		Data(x, it.value).apply {
		  it.onChangeWithWeak(this) { dat, bound ->
			dat.yValue = bound
		  }
		}
	  }
	  data.setAll(points)
	}.apply {
	  stroke = Color.YELLOW
	})
	return DynamicVerticalLine(lineSeries)
  }

  override fun dynamicHorizontalLine(y: Y): DynamicHorizontalLine {
	val lineSeries = SeriesWrapper<X, Y>()
	annotationSeries.add(lineSeries.apply {
	  val points = listOf(
		xAxis.lowerBoundProperty, xAxis.upperBoundProperty
	  ).map {
		Data(it.value, y).apply {
		  it.onChangeWithWeak(this) { dat, bound ->
			dat.xValue = bound
		  }
		}
	  }
	  data.setAll(points)
	}.apply {
	  stroke = Color.YELLOW
	})
	return DynamicHorizontalLine(lineSeries)
  }


  override fun addLegend(): MyLegend {
	val legend = MyLegend(
	  realData.map {
		LegendItem({
		  CircleWrapper(radius = 10.0).apply {
			fill = it.stroke
		  }
		}, it.name)
	  }.toBasicObservableList()
	).apply {
	  layoutXProperty.bind(chartWidthProp/2.0)
	  layoutYProperty.bind(chartHeightProp/2.0)
	}
	annotationLayer.add(legend)
	return legend
  }

  fun clearAnnotations() {
	annotationSeries.clear()
	annotationLayer.clear()
  }

}