package matt.fx.node.proto.annochart

import javafx.geometry.Insets
import javafx.scene.Group
import javafx.scene.chart.Chart
import javafx.scene.chart.XYChart
import javafx.scene.chart.XYChart.Data
import javafx.scene.layout.Border
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import matt.async.safe.with
import matt.fx.control.wrapper.chart.axis.value.number.NumberAxisWrapper
import matt.fx.control.wrapper.chart.line.highperf.HighPerformanceLineChart
import matt.fx.control.wrapper.chart.xy.series.SeriesWrapper
import matt.fx.control.wrapper.label.label
import matt.fx.graphics.fxthread.ensureInFXThreadInPlace
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.line.line
import matt.fx.graphics.wrapper.node.shape.circle.circle
import matt.fx.graphics.wrapper.node.shape.rect.RectangleWrapper
import matt.fx.graphics.wrapper.node.shape.rect.rectangle
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.grid.gridpane
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.fx.graphics.wrapper.text.TextWrapper
import matt.fx.graphics.wrapper.text.text
import matt.lang.go
import matt.lang.setAll
import matt.log.warn.warnOnce
import matt.model.data.mathable.MathAndComparable
import matt.obs.col.change.mirror
import matt.obs.col.olist.basicMutableObservableListOf
import matt.obs.math.double.op.div
import matt.obs.math.double.op.times


fun <X: MathAndComparable<X>, Y: MathAndComparable<Y>> ET.annoChart(
  xAxis: NumberAxisWrapper<X>,
  yAxis: NumberAxisWrapper<Y>,
  op: AnnotateableChart<X, Y>.()->Unit = {}
) = AnnotateableChart(
  extraHighPerf = false,
  xAxis = xAxis.minimal(),
  yAxis = yAxis.minimal(),
).attachTo(this, op)

open class AnnotateableChart<X: MathAndComparable<X>, Y: MathAndComparable<Y>> private constructor(
  stack: StackPane,
  extraHighPerf: Boolean,
  xAxis: NumberAxisWrapper<X>,
  yAxis: NumberAxisWrapper<Y>,
): RegionWrapperImpl<Region, NW>(stack) {

  constructor(
	extraHighPerf: Boolean = true,
	xAxis: NumberAxisWrapper<X>,
	yAxis: NumberAxisWrapper<Y>,
  ): this(
	StackPane(), extraHighPerf = extraHighPerf, xAxis = xAxis, yAxis = yAxis
  )


  val chart = HighPerformanceLineChart<X, Y>(
	extraHighPerf = extraHighPerf, xAxis = xAxis, yAxis = yAxis
  )

  val title = chart.titleProperty

  val xAxis = chart.xAxis as NumberAxisWrapper
  val yAxis = chart.yAxis as NumberAxisWrapper

  var animated by chart::animated
  val horizontalZeroLineVisibleProperty get() = chart.horizontalZeroLineVisibleProperty
  val verticalZeroLineVisibleProperty get() = chart.verticalZeroLineVisibleProperty

  private val annotationLayer = PaneWrapperImpl<Pane, NW>(Pane())

  private val annotationSeries = basicMutableObservableListOf<SeriesWrapper<X, Y>>()

  val realData = basicMutableObservableListOf<SeriesWrapper<X, Y>>()


  init {
	stack.children.addAll(chart.node, annotationLayer.node)

	/*javaFX has an INTERNAL bug, which I found (logic is bad in XYChart line 131) which means I have to make changes one at a time*/
	var annoPartStart = 0
	var annoPartEndExclusive = annotationSeries.size
	var realPartStart = annotationSeries.size
	var realPartEndExclusive = annotationSeries.size + realData.size

	val sem = java.util.concurrent.Semaphore(1)

	chart.data.setAll(annotationSeries + realData)

	annotationSeries.onChange {
	  sem.with {
		chart.data.subList(annoPartStart, annoPartEndExclusive).mirror(it)
		annoPartStart = 0
		annoPartEndExclusive = annotationSeries.size
		realPartStart = annotationSeries.size
		realPartEndExclusive = annotationSeries.size + realData.size
	  }
	}
	realData.onChange {
	  sem.with {
		chart.data.subList(realPartStart, realPartEndExclusive).mirror(it)
		annoPartStart = 0
		annoPartEndExclusive = annotationSeries.size
		realPartStart = annotationSeries.size
		realPartEndExclusive = annotationSeries.size + realData.size
	  }
	}

	/*	fun updateData() {
		  //		  sem.acquire()

		  //
		  //		  chart.data.clear()
		  //		  runLater {
		  chart.data.setAll(annotationSeries + realData)
		  //			sem.release()
		  //		  }
		}
		updateData()
		listOf(annotationSeries, realData).forEach {
		  it.onChange {
			updateData()
		  }
		}*/
  }

  fun autoRangeBothAxes() {
	autoRangeY()
	autoRangeX()
  }

  private inner class BoundCalcResult(
	val lowerBound: Y,
	val upperBound: Y
  )

  fun autoRangeY(
	forceMin: Y? = null,
  ) {
	val mn = realData.mapNotNull { it.data.minOfOrNull { it.yValue } }.minOrNull()
	val mx = realData.mapNotNull { it.data.maxOfOrNull { it.yValue } }.maxOrNull()
	val r = run {
	  if (mn == null || mx == null) return@run null
	  val range = mx - mn
	  val margin = range*0.1
	  BoundCalcResult(
		lowerBound = forceMin ?: (mn - margin),
		upperBound = mx + margin
	  )
	}
	r?.go {
	  ensureInFXThreadInPlace {
		yAxis.apply {
		  lowerBound = r.lowerBound
		  upperBound = r.upperBound
		}
	  }
	}
  }

  fun autoRangeX() {
	val mn = realData.mapNotNull { it.data.minOfOrNull { it.xValue } }.minOrNull()
	val mx = realData.mapNotNull { it.data.maxOfOrNull { it.xValue } }.maxOrNull()
	if (mn == null || mx == null) return
	val range = mx - mn
	val margin = range*0.1
	xAxis.apply {
	  lowerBound = mn - margin
	  upperBound = mx + margin
	}
  }


  fun addLegend() {
	annotationLayer.gridpane<NW> {
	  border = Border.stroke(Color.WHITE)
	  padding = Insets(8.0)
	  layoutXProperty.bind(this@AnnotateableChart.widthProperty/2.0)
	  layoutYProperty.bind(this@AnnotateableChart.heightProperty/2.0)
	  this@AnnotateableChart.realData.forEach {
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

  private val annotationColor: Color = Color.YELLOW

  private val chartContent by lazy {
	(Chart::class.java.getDeclaredField("chartContent").run {
	  isAccessible = true
	  get(chart.node)
	} as Pane)
  }

  private val plotContent by lazy {
	(XYChart::class.java.getDeclaredField("plotContent").run {
	  isAccessible = true
	  get(chart.node)
	} as Group)
  }
  private val plotArea by lazy {
	(XYChart::class.java.getDeclaredField("plotArea").run {
	  isAccessible = true
	  get(chart.node)
	} as Group)
  }

  fun layoutXOf(v: X) = xAxis.displayPixelOf(v) + plotArea.boundsInParent.minX + chartContent.boundsInParent.minX

  fun layoutYOf(v: Y): Double {
	warnOnce("layoutYOf probably needs work since layoutXOf was so complicated")
	return yAxis.displayPixelOf(v) + (yAxis.boundsInScene.minY - boundsInScene.minY)
  }


  fun staticRectangle(minX: X, maxX: X): RectangleWrapper {
	require(width == annotationLayer.width)
	require(width == chart.width)
	val minXPixel = layoutXOf(minX)
	val maxXPixel = layoutXOf(maxX)
	return annotationLayer.rectangle(
	  x = minXPixel,
	  width = maxXPixel - minXPixel,
	  y = 0.0,
	  height = this@AnnotateableChart.height
	) {
	  heightProperty.bind(this@AnnotateableChart.heightProperty)
	  stroke = this@AnnotateableChart.annotationColor
	  fill = Color.TRANSPARENT
	}
  }

  fun staticText(minX: X, text: String): TextWrapper {
	val minXPixel = layoutXOf(minX)
	return annotationLayer.text(
	  text
	) {
	  x = minXPixel
	  yProperty.bind(this@AnnotateableChart.heightProperty*.90)

	}
  }


  fun staticVerticalLine(x: X) {
	val xPixel = layoutXOf(x)
	annotationLayer.line(
	  startX = xPixel, startY = 0.0, endX = xPixel, endY = 10.0
	) {
	  startYProperty.bind(this@AnnotateableChart.annotationLayer.heightProperty*0.25)
	  endYProperty.bind(this@AnnotateableChart.annotationLayer.heightProperty*0.75)
	  stroke = this@AnnotateableChart.annotationColor
	}
  }

  fun dynamicVerticalLine(x: X) {
	val lineSeries = SeriesWrapper<X, Y>()
	annotationSeries.add(lineSeries.apply {
	  data.setAll(listOf(Data(x, yAxis.upperBound).apply {
		yAxis.upperBoundProperty.onChangeUntilExclusive({ this !in lineSeries.data || lineSeries !in annotationSeries },
		  {
			yValue = it
		  })
	  }, Data(x, yAxis.lowerBound).apply {
		yAxis.lowerBoundProperty.onChangeUntilExclusive({ this !in lineSeries.data || lineSeries !in annotationSeries },
		  {
			yValue = it
		  })
	  }))
	}.apply {
	  stroke = annotationColor
	})
  }


}