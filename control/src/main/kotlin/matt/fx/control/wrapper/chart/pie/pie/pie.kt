package matt.fx.control.wrapper.chart.pie.pie

import com.sun.javafx.charts.Legend
import com.sun.javafx.charts.Legend.LegendItem
import com.sun.javafx.collections.NonIterableChange
import javafx.animation.Animation.Status.RUNNING
import javafx.animation.FadeTransition
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.binding.StringBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.DoublePropertyBase
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ObjectPropertyBase
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.StringProperty
import javafx.beans.property.StringPropertyBase
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ListChangeListener.Change
import javafx.collections.ObservableList
import javafx.css.CssMetaData
import javafx.css.Styleable
import javafx.css.StyleableBooleanProperty
import javafx.css.StyleableDoubleProperty
import javafx.css.StyleableProperty
import javafx.css.converter.BooleanConverter
import javafx.css.converter.SizeConverter
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.NodeOrientation.LEFT_TO_RIGHT
import javafx.geometry.Side.LEFT
import javafx.geometry.Side.RIGHT
import javafx.scene.AccessibleRole.TEXT
import javafx.scene.Node
import javafx.scene.layout.Region
import javafx.scene.shape.Arc
import javafx.scene.shape.ArcTo
import javafx.scene.shape.ArcType.ROUND
import javafx.scene.shape.ClosePath
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.text.Text
import javafx.scene.transform.Scale
import javafx.util.Duration
import matt.fx.control.wrapper.chart.line.highperf.relinechart.xy.chart.ChartForPrivateProps
import matt.fx.control.wrapper.chart.pie.pie.PieChartForWrapper.StyleableProperties.classCssMetaData
import java.util.BitSet
import java.util.Collections
import java.util.Objects

/**
 * Displays a PieChart. The chart content is populated by pie slices based on
 * data set on the PieChart.
 *
 *  The clockwise property is set to true by default, which means slices are
 * placed in the clockwise order. The labelsVisible property is used to either display
 * pie slice labels or not.
 *
 * @since JavaFX 2.0
 */
class PieChartForWrapper @JvmOverloads constructor(data: ObservableList<Data> = FXCollections.observableArrayList()):
  ChartForPrivateProps() {
  private val colorBits = BitSet(8)
  private var pieRadius = 0.0
  private var begin: Data? = null
  private val labelLinePath: Path = object: Path() {
	override fun usesMirroring(): Boolean {
	  return false
	}
  }
  private var labelLayoutInfos: List<LabelLayoutInfo>? = null
  private val legend = Legend()
  private var dataItemBeingRemoved: Data? = null
  private var dataRemoveTimeline: Timeline? = null
  private val dataChangeListener = ListChangeListener { c: Change<out Data> ->
	while (c.next()) {
	  // RT-28090 Probably a sort happened, just reorder the pointers.
	  if (c.wasPermutated()) {
		var ptr: Data? = begin
		for (i in getData()!!.indices) {
		  val item: Data = getData()!!.get(i)
		  updateDataItemStyleClass(item, i)
		  if (i == 0) {
			begin = item
			ptr = begin
			begin!!.next = null
		  } else {
			ptr!!.next = item
			item.next = null
			ptr = item
		  }
		}
		updateLegend()
		requestChartLayout()
		return@ListChangeListener
	  }
	  // recreate linked list & set chart on new data
	  for (i in c.getFrom() until c.getTo()) {
		val item: Data = getData()!!.get(i)
		item.setChart(this@PieChartForWrapper)
		if (begin == null) {
		  begin = item
		  begin!!.next = null
		} else {
		  if (i == 0) {
			item.next = begin
			begin = item
		  } else {
			var ptr: Data? = begin
			for (j in 0 until (i - 1)) {
			  ptr = ptr!!.next
			}
			item.next = ptr!!.next
			ptr.next = item
		  }
		}
	  }
	  // call data added/removed methods
	  for (item: Data in c.getRemoved()) {
		dataItemRemoved(item)
	  }
	  for (i in c.getFrom() until c.getTo()) {
		val item: Data = getData()!!.get(i)
		// assign default color to the added slice
		// TODO: check nearby colors
		item.defaultColorIndex = colorBits.nextClearBit(0)
		colorBits.set(item.defaultColorIndex)
		dataItemAdded(item, i)
	  }
	  if (c.wasRemoved() || c.wasAdded()) {
		for (i in getData()!!.indices) {
		  val item: Data = getData()!!.get(i)
		  updateDataItemStyleClass(item, i)
		}
		updateLegend()
	  }
	}
	// re-layout everything
	requestChartLayout()
  }
  // -------------- PUBLIC PROPERTIES ----------------------------------------
  /** PieCharts data  */
  internal val data: ObjectProperty<ObservableList<Data>> = object: ObjectPropertyBase<ObservableList<Data>>() {
	private var old: ObservableList<Data>? = null
	override fun invalidated() {
	  val current: ObservableList<Data>? = value
	  // add remove listeners
	  if (old != null) old!!.removeListener(dataChangeListener)
	  current?.addListener(dataChangeListener)
	  // fire data change event if series are added or removed
	  if (old != null || current != null) {
		val removed = if (old != null) old!! else emptyList()
		val toIndex = current?.size ?: 0
		// let data listener know all old data have been removed and new data that has been added
		if (toIndex > 0 || !removed.isEmpty()) {
		  dataChangeListener.onChanged(object: NonIterableChange<Data>(0, toIndex, current) {
			override fun getRemoved(): List<Data> {
			  return removed
			}

			override fun wasPermutated(): Boolean {
			  return false
			}

			override fun getPermutation(): IntArray {
			  return IntArray(0)
			}
		  })
		}
	  } else if (old?.let { it.size > 0 } ?: false) {
		// let series listener know all old series have been removed
		dataChangeListener.onChanged(object: NonIterableChange<Data?>(0, 0, current) {
		  override fun getRemoved(): List<Data> {
			return old!!
		  }

		  override fun wasPermutated(): Boolean {
			return false
		  }

		  override fun getPermutation(): IntArray {
			return IntArray(0)
		  }
		})
	  }
	  old = current
	}

	override fun getBean(): Any {
	  return this@PieChartForWrapper
	}

	override fun getName(): String {
	  return "data"
	}
  }

  fun getData(): ObservableList<Data>? {
	return data.value
  }

  fun setData(value: ObservableList<Data>) {
	data.value = value
  }

  fun dataProperty(): ObjectProperty<ObservableList<Data>> {
	return data
  }

  /** The angle to start the first pie slice at  */
  private val startAngle: DoubleProperty = object: StyleableDoubleProperty(0.0) {
	public override fun invalidated() {
	  get()
	  requestChartLayout()
	}

	override fun getBean(): Any {
	  return this@PieChartForWrapper
	}

	override fun getName(): String {
	  return "startAngle"
	}

	override fun getCssMetaData(): CssMetaData<PieChartForWrapper, Number> {
	  return StyleableProperties.START_ANGLE
	}
  }

  fun getStartAngle(): Double {
	return startAngle.value
  }

  fun setStartAngle(value: Double) {
	startAngle.value = value
  }

  fun startAngleProperty(): DoubleProperty {
	return startAngle
  }

  /** When true we start placing slices clockwise from the startAngle  */
  private val clockwise: BooleanProperty = object: StyleableBooleanProperty(true) {
	public override fun invalidated() {
	  get()
	  requestChartLayout()
	}

	override fun getBean(): Any {
	  return this@PieChartForWrapper
	}

	override fun getName(): String {
	  return "clockwise"
	}

	override fun getCssMetaData(): CssMetaData<PieChartForWrapper, Boolean> {
	  return StyleableProperties.CLOCKWISE
	}
  }

  fun setClockwise(value: Boolean) {
	clockwise.value = value
  }

  fun isClockwise(): Boolean {
	return clockwise.value
  }

  fun clockwiseProperty(): BooleanProperty {
	return clockwise
  }

  /** The length of the line from the outside of the pie to the slice labels.  */
  private val labelLineLength: DoubleProperty = object: StyleableDoubleProperty(20.0) {
	public override fun invalidated() {
	  get()
	  requestChartLayout()
	}

	override fun getBean(): Any {
	  return this@PieChartForWrapper
	}

	override fun getName(): String {
	  return "labelLineLength"
	}

	override fun getCssMetaData(): CssMetaData<PieChartForWrapper, Number> {
	  return StyleableProperties.LABEL_LINE_LENGTH
	}
  }

  fun getLabelLineLength(): Double {
	return labelLineLength.value
  }

  fun setLabelLineLength(value: Double) {
	labelLineLength.value = value
  }

  fun labelLineLengthProperty(): DoubleProperty {
	return labelLineLength
  }

  /** When true pie slice labels are drawn  */
  private val labelsVisible: BooleanProperty = object: StyleableBooleanProperty(true) {
	public override fun invalidated() {
	  get()
	  requestChartLayout()
	}

	override fun getBean(): Any {
	  return this@PieChartForWrapper
	}

	override fun getName(): String {
	  return "labelsVisible"
	}

	override fun getCssMetaData(): CssMetaData<PieChartForWrapper, Boolean> {
	  return StyleableProperties.LABELS_VISIBLE
	}
  }

  fun setLabelsVisible(value: Boolean) {
	labelsVisible.value = value
  }

  /**
   * Indicates whether pie slice labels are drawn or not
   * @return true if pie slice labels are visible and false otherwise.
   */
  fun getLabelsVisible(): Boolean {
	return labelsVisible.value
  }

  fun labelsVisibleProperty(): BooleanProperty {
	return labelsVisible
  }
  /**
   * Construct a new PieChart with the given data
   *
   * @param data The data to use, this is the actual list used so any changes to it will be reflected in the chart
   */
  // -------------- CONSTRUCTOR ----------------------------------------------
  /**
   * Construct a new empty PieChart.
   */
  init {
	chartChildren.add(labelLinePath)
	labelLinePath.styleClass.add("chart-pie-label-line")
	setLegend(legend)
	setData(data)
	// set chart content mirroring to be always false i.e. chartContent mirrorring is not done
	// when  node orientation is right-to-left for PieChart.
	useChartContentMirroring = false
  }

  // -------------- METHODS --------------------------------------------------
  private fun dataNameChanged(item: Data) {
	item.textNode.text = item.getName()
	requestChartLayout()
	updateLegend()
  }

  private fun dataPieValueChanged(item: Data) {
	if (shouldAnimate()) {
	  animate(
		KeyFrame(
		  Duration.ZERO, KeyValue(
			item.currentPieValueProperty(),
			item.getCurrentPieValue()
		  )
		),
		KeyFrame(
		  Duration.millis(500.0), KeyValue(
			item.currentPieValueProperty(),
			item.getPieValue(), Interpolator.EASE_BOTH
		  )
		)
	  )
	} else {
	  item.setCurrentPieValue(item.getPieValue())
	  requestChartLayout() // RT-23091
	}
  }

  private fun createArcRegion(item: Data): Node {
	var arcRegion: Node = item.getNode()
	// check if symbol has already been created
	@Suppress("SENSELESS_COMPARISON")
	if (arcRegion == null) {
	  arcRegion = Region()
	  arcRegion.setNodeOrientation(LEFT_TO_RIGHT)
	  arcRegion.setPickOnBounds(false)
	  item.setNode(arcRegion)
	}
	return arcRegion
  }

  private fun createPieLabel(item: Data): Text {
	val text = item.textNode
	text.text = item.getName()
	return text
  }

  private fun updateDataItemStyleClass(item: Data, index: Int) {
	val node: Node = item.getNode()
	@Suppress("SENSELESS_COMPARISON")
	if (node != null) {
	  // Note: not sure if we want to add or check, ie be more careful and efficient here
	  node.styleClass.setAll(
		"chart-pie", "data$index",
		"default-color" + item.defaultColorIndex%8
	  )
	  if (item.getPieValue() < 0) {
		node.styleClass.add("negative")
	  }
	}
  }

  private fun dataItemAdded(item: Data, @Suppress("UNUSED_PARAMETER") index: Int) {
	// create shape
	val shape = createArcRegion(item)
	val text = createPieLabel(item)
	item.getChart()!!.chartChildren.add(shape)
	if (shouldAnimate()) {
	  // if the same data item is being removed, first stop the remove animation,
	  // remove the item and then start the add animation.
	  if (dataRemoveTimeline != null && dataRemoveTimeline!!.status == RUNNING) {
		if (dataItemBeingRemoved == item) {
		  dataRemoveTimeline!!.stop()
		  dataRemoveTimeline = null
		  chartChildren.remove(item.textNode)
		  chartChildren.remove(shape)
		  removeDataItemRef(item)
		}
	  }
	  animate(
		KeyFrame(
		  Duration.ZERO,
		  KeyValue(item.currentPieValueProperty(), item.getCurrentPieValue()),
		  KeyValue(item.radiusMultiplierProperty(), item.getRadiusMultiplier())
		),
		KeyFrame(
		  Duration.millis(500.0),
		  { actionEvent: ActionEvent? ->
			text.setOpacity(0.0)
			// RT-23597 : item's chart might have been set to null if
			// this item is added and removed before its add animation finishes.
			if (item.getChart() == null) item.setChart(this@PieChartForWrapper)
			item.getChart()!!.chartChildren.add(text)
			val ft = FadeTransition(Duration.millis(150.0), text)
			ft.setToValue(1.0)
			ft.play()
		  },
		  KeyValue(item.currentPieValueProperty(), item.getPieValue(), Interpolator.EASE_BOTH),
		  KeyValue(item.radiusMultiplierProperty(), 1, Interpolator.EASE_BOTH)
		)
	  )
	} else {
	  chartChildren.add(text)
	  item.setRadiusMultiplier(1.0)
	  item.setCurrentPieValue(item.getPieValue())
	}

	// we sort the text nodes to always be at the end of the children list, so they have a higher z-order
	// (Fix for RT-34564)
	for (i in chartChildren.indices) {
	  val n = chartChildren[i]
	  (n as? Text)?.toFront()
	}
  }

  private fun removeDataItemRef(item: Data) {
	if (begin == item) {
	  begin = item.next
	} else {
	  var ptr = begin
	  while (ptr != null && ptr.next != item) {
		ptr = ptr.next
	  }
	  if (ptr != null) ptr.next = item.next
	}
  }

  private fun createDataRemoveTimeline(item: Data): Timeline {
	val shape = item.getNode()
	val t = Timeline()
	t.keyFrames.addAll(
	  KeyFrame(
		Duration.ZERO,
		KeyValue(item.currentPieValueProperty(), item.getCurrentPieValue()),
		KeyValue(item.radiusMultiplierProperty(), item.getRadiusMultiplier())
	  ),
	  KeyFrame(
		Duration.millis(500.0),
		{ actionEvent: ActionEvent? ->
		  // removing item
		  colorBits.clear(item.defaultColorIndex)
		  chartChildren.remove(shape)
		  // fade out label
		  val ft = FadeTransition(Duration.millis(150.0), item.textNode)
		  ft.setFromValue(1.0)
		  ft.setToValue(0.0)
		  ft.setOnFinished(object: EventHandler<ActionEvent?> {
			override fun handle(actionEvent: ActionEvent?) {
			  chartChildren.remove(item.textNode)
			  // remove chart references from old data - RT-22553
			  item.setChart(null)
			  removeDataItemRef(item)
			  item.textNode.setOpacity(1.0)
			}
		  })
		  ft.play()
		},
		KeyValue(item.currentPieValueProperty(), 0, Interpolator.EASE_BOTH),
		KeyValue(item.radiusMultiplierProperty(), 0)
	  )
	)
	return t
  }

  private fun dataItemRemoved(item: Data) {
	val shape = item.getNode()
	if (shouldAnimate()) {
	  dataRemoveTimeline = createDataRemoveTimeline(item)
	  dataItemBeingRemoved = item
	  animate(dataRemoveTimeline)
	} else {
	  colorBits.clear(item.defaultColorIndex)
	  chartChildren.remove(item.textNode)
	  chartChildren.remove(shape)
	  // remove chart references from old data
	  item.setChart(null)
	  removeDataItemRef(item)
	}
  }

  /** {@inheritDoc}  */
  @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
  override fun layoutChartChildren(top: Double, left: Double, contentWidth: Double, contentHeight: Double) {
	var total = 0.0
	var item = begin
	while (item != null) {
	  total += Math.abs(item.getCurrentPieValue())
	  item = item.next
	}
	val scale: Double = if (total != 0.0) 360/total else 0.0

	// calculate combined bounds of all labels & pie radius
	var labelsX: DoubleArray? = null
	var labelsY: DoubleArray? = null
	var labelAngles: DoubleArray? = null
	var labelScale = 1.0
	var fullPie: MutableList<LabelLayoutInfo>? = null
	var shouldShowLabels = getLabelsVisible()
	if (shouldShowLabels) {
	  var xPad = 0.0
	  var yPad = 0.0
	  labelsX = DoubleArray(dataSize)
	  labelsY = DoubleArray(dataSize)
	  labelAngles = DoubleArray(dataSize)
	  fullPie = ArrayList()
	  var index = 0
	  var start = getStartAngle()
	  var item = begin
	  while (item != null) {

		// remove any scale on the text node
		item.textNode.transforms.clear()
		val size =
		  if (isClockwise()) -scale*Math.abs(item.getCurrentPieValue()) else scale*Math.abs(item.getCurrentPieValue())
		labelAngles[index] = normalizeAngle(start + size/2)
		val sproutX = calcX(labelAngles.get(index), getLabelLineLength(), 0.0)
		val sproutY = calcY(labelAngles.get(index), getLabelLineLength(), 0.0)
		labelsX[index] = sproutX
		labelsY[index] = sproutY
		xPad = Math.max(xPad, 2*(item.textNode.layoutBounds.width + LABEL_TICK_GAP + Math.abs(sproutX)))
		if (sproutY > 0) { // on bottom
		  yPad = Math.max(yPad, 2*Math.abs(sproutY + item.textNode.layoutBounds.maxY))
		} else { // on top
		  yPad = Math.max(yPad, 2*Math.abs(sproutY + item.textNode.layoutBounds.minY))
		}
		start += size
		index++
		item = item.next
	  }
	  pieRadius = Math.min(contentWidth - xPad, contentHeight - yPad)/2
	  // check if this makes the pie too small
	  if (pieRadius < MIN_PIE_RADIUS) {
		// calculate scale for text to fit labels in
		val roomX = contentWidth - MIN_PIE_RADIUS - MIN_PIE_RADIUS
		val roomY = contentHeight - MIN_PIE_RADIUS - MIN_PIE_RADIUS
		labelScale = Math.min(
		  roomX/xPad,
		  roomY/yPad
		)
		// hide labels if pie radius is less than minimum
		if (begin == null && labelScale < 0.7 || begin!!.textNode.font.size*labelScale < 9) {
		  shouldShowLabels = false
		  labelScale = 1.0
		} else {
		  // set pieRadius to minimum
		  pieRadius = MIN_PIE_RADIUS.toDouble()
		  // apply scale to all label positions
		  for (i in labelsX.indices) {
			labelsX[i] = labelsX.get(i)*labelScale
			labelsY[i] = labelsY.get(i)*labelScale
		  }
		}
	  }
	}
	if (!shouldShowLabels) {
	  pieRadius = Math.min(contentWidth, contentHeight)/2
	  labelLinePath.elements.clear()
	}
	if (chartChildren.size > 0) {
	  val centerX = contentWidth/2 + left
	  val centerY = contentHeight/2 + top
	  var index = 0
	  run {
		var item2: Data? = begin
		while (item2 != null) {

		  // layout labels for pie slice
		  item2.textNode.setVisible(shouldShowLabels)
		  if (shouldShowLabels) {
			val size: Double = if ((isClockwise())) (-scale*Math.abs(
			  item2.getCurrentPieValue()
			)) else (scale*Math.abs(
			  item2.getCurrentPieValue()
			))
			val isLeftSide: Boolean = !(labelAngles!!.get(index) > -90 && labelAngles.get(index) < 90)
			val sliceCenterEdgeX: Double =
			  calcX(labelAngles.get(index), pieRadius, centerX)
			val sliceCenterEdgeY: Double =
			  calcY(labelAngles.get(index), pieRadius, centerY)
			val xval: Double =
			  if (isLeftSide) ((labelsX!!.get(index) + sliceCenterEdgeX) - item2.textNode.getLayoutBounds()
				.getMaxX() - LABEL_TICK_GAP) else (labelsX!!.get(
				index
			  ) + sliceCenterEdgeX - item2.textNode.getLayoutBounds()
				.getMinX() + LABEL_TICK_GAP)
			val yval: Double =
			  (labelsY!!.get(index) + sliceCenterEdgeY) - (item2.textNode.getLayoutBounds().getMinY()/2) - 2

			// do the line (Path)for labels
			val lineEndX: Double = sliceCenterEdgeX + labelsX.get(index)
			val lineEndY: Double = sliceCenterEdgeY + labelsY.get(index)
			val info: LabelLayoutInfo = LabelLayoutInfo(
			  sliceCenterEdgeX,
			  sliceCenterEdgeY, lineEndX, lineEndY, xval, yval, item2.textNode, Math.abs(size)
			)
			fullPie!!.add(info)

			// set label scales
			if (labelScale < 1) {
			  item2.textNode.getTransforms().add(
				Scale(
				  labelScale, labelScale,
				  if (isLeftSide) item2.textNode.getLayoutBounds().getWidth() else 0.0, 0.0
				)
			  )
			}
		  }
		  index++
		  item2 = item2.next
		}
	  }

	  // update/draw pie slices
	  var sAngle = getStartAngle()
	  var item = begin
	  while (item != null) {
		val node: Node = item.getNode()
		var arc: Arc? = null
		@Suppress("SENSELESS_COMPARISON")
		if (node != null) {
		  if (node is Region) {
			val arcRegion = node
			if (arcRegion.shape == null) {
			  arc = Arc()
			  arcRegion.shape = arc
			} else {
			  arc = arcRegion.shape as Arc
			}
			arcRegion.isScaleShape = false
			arcRegion.isCenterShape = false
			arcRegion.isCacheShape = false
		  }
		}
		val size = if (isClockwise()) -scale*Math.abs(item.getCurrentPieValue()) else scale*Math.abs(
		  item.getCurrentPieValue()
		)
		// update slice arc size
		arc!!.startAngle = sAngle
		arc.length = size
		arc.type = ROUND
		arc.radiusX = pieRadius*item.getRadiusMultiplier()
		arc.radiusY = pieRadius*item.getRadiusMultiplier()
		node.layoutX = centerX
		node.layoutY = centerY
		sAngle += size
		item = item.next
	  }
	  // finally draw the text and line
	  if (fullPie != null) {
		// Check for collision and resolve by hiding the label of the smaller pie slice
		resolveCollision(fullPie)
		if (fullPie != labelLayoutInfos) {
		  labelLinePath.elements.clear()
		  for (info: LabelLayoutInfo in fullPie) {
			if (info.text.isVisible) drawLabelLinePath(info)
		  }
		  labelLayoutInfos = fullPie
		}
	  }
	}
  }

  // We check for pie slice label collision and if collision is detected, we then
  // compare the size of the slices, and hide the label of the smaller slice.
  private fun resolveCollision(list: List<LabelLayoutInfo>) {
	val boxH = if (begin != null) begin!!.textNode.layoutBounds.height.toInt() else 0
	for (i in list.indices) {
	  for (j in i + 1 until list.size) {
		val box1 = list[i]
		val box2 = list[j]
		if ((box1.text.isVisible && box2.text.isVisible &&
			  (if (fuzzyGT(box2.textY, box1.textY)) fuzzyLT(box2.textY - boxH - box1.textY, 2.0) else fuzzyLT(
				box1.textY - boxH - box2.textY, 2.0
			  )) &&
			  if (fuzzyGT(box1.textX, box2.textX)) fuzzyLT(
				box1.textX - box2.textX, box2.text.prefWidth(-1.0)
			  ) else fuzzyLT(
				box2.textX - box1.textX, box1.text.prefWidth(-1.0)
			  ))
		) {
		  if (fuzzyLT(box1.size, box2.size)) {
			box1.text.isVisible = false
		  } else {
			box2.text.isVisible = false
		  }
		}
	  }
	}
  }

  private fun fuzzyCompare(o1: Double, o2: Double): Int {
	val fuzz = 0.00001
	return (if (((Math.abs(o1 - o2)) < fuzz)) 0 else (if ((o1 < o2)) -1 else 1))
  }

  private fun fuzzyGT(o1: Double, o2: Double): Boolean {
	return fuzzyCompare(o1, o2) == 1
  }

  private fun fuzzyLT(o1: Double, o2: Double): Boolean {
	return fuzzyCompare(o1, o2) == -1
  }

  private fun drawLabelLinePath(info: LabelLayoutInfo) {
	info.text.layoutX = info.textX
	info.text.layoutY = info.textY
	labelLinePath.elements.add(MoveTo(info.startX, info.startY))
	labelLinePath.elements.add(LineTo(info.endX, info.endY))
	labelLinePath.elements.add(MoveTo(info.endX - LABEL_BALL_RADIUS, info.endY))
	labelLinePath.elements.add(
	  ArcTo(
		LABEL_BALL_RADIUS, LABEL_BALL_RADIUS,
		90.0, info.endX, info.endY - LABEL_BALL_RADIUS, false, true
	  )
	)
	labelLinePath.elements.add(
	  ArcTo(
		LABEL_BALL_RADIUS, LABEL_BALL_RADIUS,
		90.0, info.endX + LABEL_BALL_RADIUS, info.endY, false, true
	  )
	)
	labelLinePath.elements.add(
	  ArcTo(
		LABEL_BALL_RADIUS, LABEL_BALL_RADIUS,
		90.0, info.endX, info.endY + LABEL_BALL_RADIUS, false, true
	  )
	)
	labelLinePath.elements.add(
	  ArcTo(
		LABEL_BALL_RADIUS, LABEL_BALL_RADIUS,
		90.0, info.endX - LABEL_BALL_RADIUS, info.endY, false, true
	  )
	)
	labelLinePath.elements.add(ClosePath())
  }

  /**
   * This is called whenever a series is added or removed and the legend needs to be updated
   */
  private fun updateLegend() {
	val legendNode = getLegend()
	@Suppress("SENSELESS_COMPARISON")
	if (legendNode != null && legendNode !== legend) return  // RT-23596 dont update when user has set legend.
	legend.isVertical = (legendSide.value == LEFT) || (legendSide.value == RIGHT)
	val legendList: MutableList<LegendItem> = ArrayList()
	if (getData() != null) {
	  for (item: Data in getData()!!) {
		val legenditem = LegendItem(item.getName())
		legenditem.symbol.styleClass.addAll(item.getNode().styleClass)
		legenditem.symbol.styleClass.add("pie-legend-symbol")
		legendList.add(legenditem)
	  }
	}
	legend.items.setAll(legendList)
	if (legendList.size > 0) {
	  @Suppress("SENSELESS_COMPARISON")
	  if (legendNode == null) {
		setLegend(legend)
	  }
	} else {
	  setLegend(null)
	}
  }

  private val dataSize: Int
	get() {
	  var count = 0
	  var d = begin
	  while (d != null) {
		count++
		d = d.next
	  }
	  return count
	}

  // -------------- INNER CLASSES --------------------------------------------
  // Class holding label line layout info for collision detection and removal
  private class LabelLayoutInfo(
	var startX: Double, var startY: Double, var endX: Double, var endY: Double,
	var textX: Double, var textY: Double, var text: Text, var size: Double
  ) {
	override fun equals(other: Any?): Boolean {
	  if (this === other) return true
	  if (other == null || javaClass != other.javaClass) return false
	  val that = other as LabelLayoutInfo
	  return (java.lang.Double.compare(that.startX, startX) == 0) && (
		  java.lang.Double.compare(that.startY, startY) == 0) && (
		  java.lang.Double.compare(that.endX, endX) == 0) && (
		  java.lang.Double.compare(that.endY, endY) == 0) && (
		  java.lang.Double.compare(that.textX, textX) == 0) && (
		  java.lang.Double.compare(that.textY, textY) == 0) && (
		  java.lang.Double.compare(that.size, size) == 0)
	}

	override fun hashCode(): Int {
	  return Objects.hash(startX, startY, endX, endY, textX, textY, size)
	}
  }

  /**
   * PieChart Data Item, represents one slice in the PieChart
   *
   * @since JavaFX 2.0
   */
  class Data(name: String?, value: Double) {
	val textNode = Text()

	/**
	 * Next pointer for the next data item : so we can do animation on data delete.
	 */
	var next: Data? = null

	/**
	 * Default color index for this slice.
	 */
	var defaultColorIndex = 0
	// -------------- PUBLIC PROPERTIES ------------------------------------
	/**
	 * The chart which this data belongs to.
	 */
	private val chart = ReadOnlyObjectWrapper<PieChartForWrapper?>(this, "chart")
	fun getChart(): PieChartForWrapper? {
	  return chart.value
	}

	fun setChart(value: PieChartForWrapper?) {
	  chart.value = value
	}

	@Suppress("unused") fun chartProperty(): ReadOnlyObjectProperty<PieChartForWrapper?> {
	  return chart.readOnlyProperty
	}

	/**
	 * The name of the pie slice
	 */
	private val name: StringProperty = object: StringPropertyBase() {
	  override fun invalidated() {
		if (getChart() != null) getChart()!!.dataNameChanged(this@Data)
	  }

	  override fun getBean(): Any {
		return this@Data
	  }

	  override fun getName(): String {
		return "name"
	  }
	}

	fun setName(value: String?) {
	  name.value = value
	}

	fun getName(): String {
	  return name.value
	}

	fun nameProperty(): StringProperty {
	  return name
	}

	/**
	 * The value of the pie slice
	 */
	private val pieValue: DoubleProperty = object: DoublePropertyBase() {
	  override fun invalidated() {
		if (getChart() != null) getChart()!!.dataPieValueChanged(this@Data)
	  }

	  override fun getBean(): Any {
		return this@Data
	  }

	  override fun getName(): String {
		return "pieValue"
	  }
	}

	fun getPieValue(): Double {
	  return pieValue.value
	}

	fun setPieValue(value: Double) {
	  pieValue.value = value
	}

	fun pieValueProperty(): DoubleProperty {
	  return pieValue
	}

	/**
	 * The current pie value, used during animation. This will be the last data value, new data value or
	 * anywhere in between
	 */
	private val currentPieValue: DoubleProperty = SimpleDoubleProperty(this, "currentPieValue")
	fun getCurrentPieValue(): Double {
	  return currentPieValue.value
	}

	fun setCurrentPieValue(value: Double) {
	  currentPieValue.value = value
	}

	fun currentPieValueProperty(): DoubleProperty {
	  return currentPieValue
	}

	/**
	 * Multiplier that is used to animate the radius of the pie slice
	 */
	private val radiusMultiplier: DoubleProperty = SimpleDoubleProperty(this, "radiusMultiplier")
	fun getRadiusMultiplier(): Double {
	  return radiusMultiplier.value
	}

	fun setRadiusMultiplier(value: Double) {
	  radiusMultiplier.value = value
	}

	fun radiusMultiplierProperty(): DoubleProperty {
	  return radiusMultiplier
	}

	/**
	 * Readonly access to the node that represents the pie slice. You can use this to add mouse event listeners etc.
	 */
	private val node = ReadOnlyObjectWrapper<Node>(this, "node")

	/**
	 * Returns the node that represents the pie slice. You can use this to
	 * add mouse event listeners etc.
	 * @return the node that represents the pie slice
	 */
	fun getNode(): Node {
	  return node.value
	}

	fun setNode(value: Node) {
	  node.value = value
	}

	fun nodeProperty(): ReadOnlyObjectProperty<Node> {
	  return node.readOnlyProperty
	}
	// -------------- CONSTRUCTOR -------------------------------------------------
	/**
	 * Constructs a PieChart.Data object with the given name and value.
	 *
	 * @param name  name for Pie
	 * @param value pie value
	 */
	init {
	  setName(name)
	  setPieValue(value)
	  textNode.styleClass.addAll("text", "chart-pie-label")
	  textNode.accessibleRole = TEXT
	  textNode.accessibleRoleDescription = "slice"
	  textNode.focusTraversableProperty().bind(Platform.accessibilityActiveProperty())
	  textNode.accessibleTextProperty().bind(object: StringBinding() {
		init {
		  bind(nameProperty(), currentPieValueProperty())
		}

		override fun computeValue(): String {
		  return getName() + " represents " + getCurrentPieValue() + " percent"
		}
	  })
	}
	// -------------- PUBLIC METHODS ----------------------------------------------
	/**
	 * Returns a string representation of this `Data` object.
	 *
	 * @return a string representation of this `Data` object.
	 */
	override fun toString(): String {
	  return "Data[" + getName() + "," + getPieValue() + "]"
	}
  }

  // -------------- STYLESHEET HANDLING --------------------------------------
  /*
     * Super-lazy instantiation pattern from Bill Pugh.
     */
  private object StyleableProperties {
	val CLOCKWISE: CssMetaData<PieChartForWrapper, Boolean> = object: CssMetaData<PieChartForWrapper, Boolean>(
	  "-fx-clockwise",
	  BooleanConverter.getInstance(), java.lang.Boolean.TRUE
	) {
	  override fun isSettable(node: PieChartForWrapper): Boolean {
		return node.clockwise == null || !node.clockwise.isBound
	  }

	  override fun getStyleableProperty(node: PieChartForWrapper): StyleableProperty<Boolean?> {
		return node.clockwiseProperty() as StyleableProperty<Boolean?>
	  }
	}
	val LABELS_VISIBLE: CssMetaData<PieChartForWrapper, Boolean> = object: CssMetaData<PieChartForWrapper, Boolean>(
	  "-fx-pie-label-visible",
	  BooleanConverter.getInstance(), java.lang.Boolean.TRUE
	) {
	  override fun isSettable(node: PieChartForWrapper): Boolean {
		return node.labelsVisible == null || !node.labelsVisible.isBound
	  }

	  override fun getStyleableProperty(node: PieChartForWrapper): StyleableProperty<Boolean?> {
		return node.labelsVisibleProperty() as StyleableProperty<Boolean?>
	  }
	}
	val LABEL_LINE_LENGTH: CssMetaData<PieChartForWrapper, Number> = object: CssMetaData<PieChartForWrapper, Number>(
	  "-fx-label-line-length",
	  SizeConverter.getInstance(), 20.0
	) {
	  override fun isSettable(node: PieChartForWrapper): Boolean {
		return node.labelLineLength == null || !node.labelLineLength.isBound
	  }

	  override fun getStyleableProperty(node: PieChartForWrapper): StyleableProperty<Number?> {
		return node.labelLineLengthProperty() as StyleableProperty<Number?>
	  }
	}
	val START_ANGLE: CssMetaData<PieChartForWrapper, Number> = object: CssMetaData<PieChartForWrapper, Number>(
	  "-fx-start-angle",
	  SizeConverter.getInstance(), 0.0
	) {
	  override fun isSettable(node: PieChartForWrapper): Boolean {
		return node.startAngle == null || !node.startAngle.isBound
	  }

	  override fun getStyleableProperty(node: PieChartForWrapper): StyleableProperty<Number?> {
		return node.startAngleProperty() as StyleableProperty<Number?>
	  }
	}
	val classCssMetaData: List<CssMetaData<out Styleable?, *>>? by lazy {
	  val styleables: MutableList<CssMetaData<out Styleable?, *>> = ArrayList(getClassCssMetaData())
	  styleables.add(CLOCKWISE)
	  styleables.add(LABELS_VISIBLE)
	  styleables.add(LABEL_LINE_LENGTH)
	  styleables.add(START_ANGLE)
	  Collections.unmodifiableList(styleables)
	}

  }

  /**
   * {@inheritDoc}
   * @since JavaFX 8.0
   */
  override fun getCssMetaData(): List<CssMetaData<out Styleable?, *>>? {
	return classCssMetaData
  }

  companion object {
	// -------------- PRIVATE FIELDS -----------------------------------------------------------------------------------
	private val MIN_PIE_RADIUS = 25
	private val LABEL_TICK_GAP = 6.0
	private val LABEL_BALL_RADIUS = 2.0
	private fun calcX(angle: Double, radius: Double, centerX: Double): Double {
	  return (centerX + radius*Math.cos(Math.toRadians(-angle)))
	}

	private fun calcY(angle: Double, radius: Double, centerY: Double): Double {
	  return (centerY + radius*Math.sin(Math.toRadians(-angle)))
	}

	/** Normalize any angle into -180 to 180 deg range  */
	private fun normalizeAngle(angle: Double): Double {
	  var a = angle%360
	  if (a <= -180) a += 360.0
	  if (a > 180) a -= 360.0
	  return a
	}
  }
}