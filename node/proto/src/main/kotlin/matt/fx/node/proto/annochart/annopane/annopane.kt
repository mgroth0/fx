package matt.fx.node.proto.annochart.annopane

import javafx.geometry.Bounds
import javafx.geometry.Insets
import javafx.scene.layout.Border
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import matt.fx.control.wrapper.chart.axis.value.number.NumberAxisWrapper
import matt.fx.control.wrapper.chart.line.ChartLocater
import matt.fx.control.wrapper.chart.line.highperf.relinechart.xy.XYChartForPackagePrivateProps.Data
import matt.fx.control.wrapper.chart.xy.series.SeriesWrapper
import matt.fx.control.wrapper.label.label
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.line.LineWrapper
import matt.fx.graphics.wrapper.node.line.line
import matt.fx.graphics.wrapper.node.shape.circle.circle
import matt.fx.graphics.wrapper.node.shape.rect.RectangleWrapper
import matt.fx.graphics.wrapper.node.shape.rect.rectangle
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.grid.gridpane
import matt.fx.graphics.wrapper.text.TextWrapper
import matt.fx.graphics.wrapper.text.text
import matt.lang.setAll
import matt.model.data.mathable.MathAndComparable
import matt.obs.col.olist.MutableObsList
import matt.obs.math.double.op.div
import matt.obs.math.double.op.times
import matt.obs.prop.ObsVal

interface Annotateable<X: MathAndComparable<X>, Y: MathAndComparable<Y>> {
  fun staticRectangle(minX: X, maxX: X): RectangleWrapper
  fun dynamicRectangle(minX: X, maxX: X): RectangleWrapper
  fun staticText(minX: X, text: String): TextWrapper
  fun dynamicVerticalLine(x: X)
  fun dynamicText(minX: X, text: String): TextWrapper
  fun staticVerticalLine(x: X): LineWrapper
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
): ChartLocater<X, Y> by loc, Annotateable<X,Y> {

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

  override fun dynamicVerticalLine(x: X) {
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
  }


  fun addLegend() {
	annotationLayer.gridpane<NW> {
	  border = Border.stroke(Color.WHITE)
	  padding = Insets(8.0)
	  layoutXProperty.bind(chartWidthProp/2.0)
	  layoutYProperty.bind(chartHeightProp/2.0)
	  realData.forEach {
		row {
		  circle(radius = 10.0) {
			fill = it.stroke
		  }
		  label(it.name) {
			padding = Insets(5.0)
		  }
		}
	  }
	}
  }

  fun clearAnnotations() {
	annotationSeries.clear()
	annotationLayer.clear()
  }

}