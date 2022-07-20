/*a lot of this was from tornadofx*/

package matt.fx.graphics.layout

import javafx.beans.property.DoubleProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Bounds
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.geometry.Rectangle2D
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.canvas.Canvas
import javafx.scene.control.Accordion
import javafx.scene.control.Label
import javafx.scene.control.Pagination
import javafx.scene.control.ScrollPane
import javafx.scene.control.Separator
import javafx.scene.control.SplitPane
import javafx.scene.control.TitledPane
import javafx.scene.control.ToolBar
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.ConstraintsBase
import javafx.scene.layout.FlowPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.TilePane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import matt.fx.graphics.style.all
import matt.fx.graphics.style.copy
import matt.fx.graphics.style.horizontal
import matt.fx.graphics.style.insets
import matt.fx.graphics.style.vertical
import matt.hurricanefx.eye.lib.proxypropDouble
import matt.hurricanefx.tornadofx.fx.getChildList
import matt.hurricanefx.tornadofx.fx.opcr
import matt.hurricanefx.tornadofx.nodes.add
import matt.hurricanefx.wrapper.AccordionWrapper
import matt.hurricanefx.wrapper.AnchorPaneWrapper
import matt.hurricanefx.wrapper.BorderPaneWrapper
import matt.hurricanefx.wrapper.CanvasWrapper
import matt.hurricanefx.wrapper.GroupWrapper
import matt.hurricanefx.wrapper.HBoxWrapper
import matt.hurricanefx.wrapper.NodeWrapper
import matt.hurricanefx.wrapper.PaginationWrapper
import matt.hurricanefx.wrapper.PaneWrapper
import matt.hurricanefx.wrapper.RegionWrapper
import matt.hurricanefx.wrapper.ScrollPaneWrapper
import matt.hurricanefx.wrapper.SeparatorWrapper
import matt.hurricanefx.wrapper.SplitPaneWrapper
import matt.hurricanefx.wrapper.StackPaneWrapper
import matt.hurricanefx.wrapper.TitledPaneWrapper
import matt.hurricanefx.wrapper.ToolBarWrapper
import matt.hurricanefx.wrapper.VBoxWrapper
import matt.hurricanefx.wrapper.wrapped
import kotlin.random.Random


infix fun RegionWrapper.minBind(other: RegionWrapper) {
  minHeightProperty.bind(other.heightProperty)
  minWidthProperty.bind(other.widthProperty)
}

infix fun RegionWrapper.minBind(other: Stage) {
  minHeightProperty.bind(other.heightProperty())
  minWidthProperty.bind(other.widthProperty())
}


infix fun RegionWrapper.maxBind(other: RegionWrapper) {
  maxHeightProperty.bind(other.heightProperty)
  maxWidthProperty.bind(other.widthProperty)
}

infix fun RegionWrapper.maxBind(other: Stage) {
  maxHeightProperty.bind(other.heightProperty())
  maxWidthProperty.bind(other.widthProperty())
}


infix fun RegionWrapper.perfectBind(other: RegionWrapper) {
  this minBind other
  this maxBind other
}

infix fun RegionWrapper.perfectBind(other: Stage) {
  this minBind other
  this maxBind other
}

fun PaneWrapper.spacer() {
  this.children.add(Pane().apply {
	minWidth = 20.0
	minHeight = 20.0
  })
}

fun Bounds.toRect() = Rectangle2D(minX, minY, width, height)
fun Rectangle2D.shrink(n: Int) = Rectangle2D(minX + n, minY + n, width - (n*2), height - (n*2))

var Node.hgrow: Priority?
  get() = HBox.getHgrow(this)
  set(value) {
	HBox.setHgrow(this, value)
  }
var Node.vgrow: Priority?
  get() = VBox.getVgrow(this)
  set(value) {
	VBox.setVgrow(this, value)
	// Input Container vgrow must propagate to Field and Fieldset
  }

var NodeWrapper<*>.hgrow: Priority?
  get() = HBox.getHgrow(this.node)
  set(value) {
	HBox.setHgrow(this.node, value)
  }
var NodeWrapper<*>.vgrow: Priority?
  get() = VBox.getVgrow(this.node)
  set(value) {
	VBox.setVgrow(this.node, value)
	// Input Container vgrow must propagate to Field and Fieldset
  }


fun ToolBar.spacer(prio: Priority = Priority.ALWAYS, op: Pane.()->Unit = {}): Pane {
  val pane = Pane().apply {
	hgrow = prio
  }
  op(pane)
  wrapped().add(pane.wrapped())
  return pane
}

fun HBoxWrapper.spacer(prio: Priority = Priority.ALWAYS, op: Pane.()->Unit = {}) =
  opcr(this, PaneWrapper().apply { HBox.setHgrow(this, prio) }, op)

fun VBoxWrapper.spacer(prio: Priority = Priority.ALWAYS, op: Pane.()->Unit = {}) =
  opcr(this, PaneWrapper().apply { VBox.setVgrow(this, prio) }, op)


private val GridPaneRowIdKey = "TornadoFX.GridPaneRowId"
private val GridPaneParentObjectKey = "TornadoFX.GridPaneParentObject"

fun GridPane.row(title: String? = null, op: Pane.()->Unit = {}) {
  properties[GridPaneRowIdKey] =
	if (properties.containsKey(GridPaneRowIdKey)) properties[GridPaneRowIdKey] as Int + 1 else 0

  // Allow the caller to add children to a fake pane
  val fake = Pane()
  fake.properties[GridPaneParentObjectKey] = this
  if (title != null) fake.children.add(Label(title))

  op(fake)

  // Create a new row in the GridPane and add the children added to the fake pane
  addRow(properties[GridPaneRowIdKey] as Int, *fake.children.toTypedArray())
}

/**
 * Removes the corresponding row to which this [node] belongs to.
 *
 * It does the opposite of the [GridPane.row] cleaning all internal state properly.
 *
 * @return the row index of the removed row.
 */
fun GridPane.removeRow(node: Node): Int {
  val rowIdKey = properties[GridPaneRowIdKey] as Int?
  if (rowIdKey != null) {
	when (rowIdKey) {
	  0    -> properties.remove(GridPaneRowIdKey)
	  else -> properties[GridPaneRowIdKey] = rowIdKey - 1
	}
  }
  val rowIndex = GridPane.getRowIndex(node) ?: 0
  val nodesToDelete = mutableListOf<Node>()
  children.forEach { child ->
	val childRowIndex = GridPane.getRowIndex(child) ?: 0
	if (childRowIndex == rowIndex) {
	  nodesToDelete.add(child)
	  // Remove row index property from the node
	  GridPane.setRowIndex(child, null)
	  GridPane.setColumnIndex(child, null)
	} else if (childRowIndex > rowIndex) {
	  GridPane.setRowIndex(child, childRowIndex - 1)
	}
  }
  children.removeAll(nodesToDelete)
  return rowIndex
}

fun GridPane.removeAllRows() {
  children.forEach {
	GridPane.setRowIndex(it, null)
	GridPane.setColumnIndex(it, null)
  }
  children.clear()
  properties.remove(GridPaneRowIdKey)
}

fun GridPane.constraintsForColumn(columnIndex: Int) = constraintsFor(columnConstraints, columnIndex)

fun GridPane.constraintsForRow(rowIndex: Int) = constraintsFor(rowConstraints, rowIndex)

//constraints for row and matt.hurricanefx.tableview.coolColumn can be handled the same way
internal inline fun <reified T: ConstraintsBase> constraintsFor(constraints: ObservableList<T>, index: Int): T {
  //    while (constraints.size <= index) constraints.add(T::class.createInstance())
  while (constraints.size <= index) constraints.add(T::class.constructors.first().call())
  return constraints[index]
}

val Parent.gridpaneColumnConstraints: ColumnConstraints?
  get() {
	var cursor = this
	var next = parent
	while (next != null) {
	  val gridReference = when {
		next is GridPane    -> next to GridPane.getColumnIndex(cursor)?.let { it }
		// perhaps we're still in the row builder
		next.parent == null -> (next.properties[GridPaneParentObjectKey] as? GridPane)?.let {
		  it to next.getChildList()?.indexOf(cursor)
		}

		else                -> null
	  }

	  if (gridReference != null) {
		val (grid, columnIndex) = gridReference
		if (columnIndex != null && columnIndex >= 0) return grid.constraintsForColumn(columnIndex)
	  }
	  cursor = next
	  next = next.parent
	}
	return null
  }

fun Parent.gridpaneColumnConstraints(op: ColumnConstraints.()->Unit) = gridpaneColumnConstraints?.apply { op() }


fun EventTarget.toolbar(vararg nodes: Node, op: ToolBarWrapper.()->Unit = {}): ToolBarWrapper {
  val toolbar = ToolBarWrapper()
  if (nodes.isNotEmpty()) toolbar.items.addAll(nodes)
  opcr(this.wrapped(), toolbar, op)
  return toolbar
}


@Deprecated(
  "No need to wrap ToolBar children in children{} anymore. Remove the wrapper and all builder items will still be added as before.",
  ReplaceWith("no children{} wrapper"),
  DeprecationLevel.WARNING
)
fun ToolBar.children(op: ToolBar.()->Unit) = apply { op() }

fun NodeWrapper<*>.hbox(spacing: Number? = null, alignment: Pos? = null, op: HBoxWrapper.()->Unit = {}): HBoxWrapper {
  val hbox = HBoxWrapper(HBox())
  if (alignment != null) hbox.alignment = alignment
  if (spacing != null) hbox.spacing = spacing.toDouble()
  return opcr(this, hbox, op)
}

fun NodeWrapper<*>.vbox(spacing: Number? = null, alignment: Pos? = null, op: VBoxWrapper.()->Unit = {}): VBoxWrapper {
  val vbox = VBoxWrapper(VBox())
  if (alignment != null) vbox.alignment = alignment
  if (spacing != null) vbox.spacing = spacing.toDouble()
  return opcr(this, vbox, op)
}

fun ToolBar.separator(orientation: Orientation = Orientation.HORIZONTAL, op: Separator.()->Unit = {}): Separator {
  val separator = Separator(orientation).also(op)
  wrapped().add(separator.wrapped())
  return separator
}

fun EventTarget.separator(orientation: Orientation = Orientation.HORIZONTAL, op: SeparatorWrapper.()->Unit = {}) =
  opcr(this.wrapped(), SeparatorWrapper(orientation), op)

fun EventTarget.group(initialChildren: Iterable<Node>? = null, op: GroupWrapper.()->Unit = {}) =
  opcr(this.wrapped(), GroupWrapper().apply { if (initialChildren != null) children.addAll(initialChildren) }, op)

fun EventTarget.stackpane(initialChildren: Iterable<Node>? = null, op: StackPaneWrapper.()->Unit = {}) =
  opcr(this.wrapped(), StackPaneWrapper().apply { if (initialChildren != null) children.addAll(initialChildren) }, op)

fun EventTarget.gridpane(op: GridPane.()->Unit = {}) = opcr(this.wrapped(), GridPaneWrapper(), op)
fun EventTarget.pane(op: Pane.()->Unit = {}) = opcr(this.wrapped(), Pane(), op)
fun EventTarget.flowpane(op: FlowPane.()->Unit = {}) = opcr(this.wrapped(), FlowPaneWrapper(), op)
fun NodeWrapper<*>.flowpane(op: FlowPane.()->Unit = {}) = node.flowpane(op)
fun EventTarget.tilepane(op: TilePane.()->Unit = {}) = opcr(this.wrapped(), TilePaneWrapper(), op)
fun EventTarget.borderpane(op: BorderPane.()->Unit = {}) = opcr(this.wrapped(), BorderPaneWrapper(), op)


@Deprecated("Use top = node {} instead")
fun <T: Node> BorderPaneWrapper.top(topNode: T, op: T.()->Unit = {}): T {
  top = topNode
  return opcr(this.wrapped(), topNode, op)
}


@Deprecated("Use bottom = node {} instead")
fun <T: Node> BorderPaneWrapper.bottom(bottomNode: T, op: T.()->Unit = {}): T {
  bottom = bottomNode
  return opcr(this.wrapped(), bottomNode, op)
}

@Deprecated("Use left = node {} instead")
fun <T: Node> BorderPaneWrapper.left(leftNode: T, op: T.()->Unit = {}): T {
  left = leftNode
  return opcr(this.wrapped(), leftNode, op)
}

@Deprecated("Use right = node {} instead")
fun <T: Node> BorderPaneWrapper.right(rightNode: T, op: T.()->Unit = {}): T {
  right = rightNode
  return opcr(this.wrapped(), rightNode, op)
}

@Deprecated("Use center = node {} instead")
fun <T: Node> BorderPaneWrapper.center(centerNode: T, op: T.()->Unit = {}): T {
  center = centerNode
  return opcr(this.wrapped(), centerNode, op)
}

fun EventTarget.titledpane(
  title: String? = null,
  node: Node? = null,
  collapsible: Boolean = true,
  op: (TitledPaneWrapper).()->Unit = {}
): TitledPaneWrapper {
  val titledPane = TitledPaneWrapper { text = title; graphic = node }
  titledPane.isCollapsible = collapsible
  opcr(this.wrapped(), titledPane, op)
  return titledPane
}

fun EventTarget.titledpane(
  title: ObservableValue<String>,
  node: Node? = null,
  collapsible: Boolean = true,
  op: (TitledPaneWrapper).()->Unit = {}
): TitledPaneWrapper {
  val titledPane = TitledPaneWrapper{ text = ""; graphic = node }
  titledPane.textProperty().bind(title)
  titledPane.isCollapsible = collapsible
  opcr(this.wrapped(), titledPane, op)
  return titledPane
}

fun EventTarget.pagination(pageCount: Int? = null, pageIndex: Int? = null, op: PaginationWrapper.()->Unit = {}): PaginationWrapper {
  val pagination = PaginationWrapper()
  if (pageCount != null) pagination.pageCount = pageCount
  if (pageIndex != null) pagination.currentPageIndex = pageIndex
  return opcr(this.wrapped(), pagination, op)
}

open class DummyClassYesIuse() {
  val v by lazy { Random.nextDouble() }
}

fun EventTarget.scrollpane(
  fitToWidth: Boolean = false,
  fitToHeight: Boolean = false,
  op: ScrollPaneWrapper.()->Unit = {}
): ScrollPaneWrapper {
  val pane = ScrollPaneWrapper()
  pane.isFitToWidth = fitToWidth
  pane.isFitToHeight = fitToHeight
  opcr(this.wrapped(), pane, op)
  return pane
}


fun EventTarget.splitpane(
  orientation: Orientation = Orientation.HORIZONTAL,
  vararg nodes: Node,
  op: SplitPaneWrapper.()->Unit = {}
): SplitPaneWrapper {
  val splitpane = SplitPaneWrapper()
  splitpane.orientation = orientation
  if (nodes.isNotEmpty())
	splitpane.items.addAll(nodes)
  opcr(this.wrapped(), splitpane, op)
  return splitpane
}

@Deprecated(
  "No need to wrap splitpane items in items{} anymore. Remove the wrapper and all builder items will still be added as before.",
  ReplaceWith("no items{} wrapper"),
  DeprecationLevel.WARNING
)
fun SplitPaneWrapper.items(op: (SplitPaneWrapper.()->Unit)) = op(this)

fun EventTarget.canvas(width: Double = 0.0, height: Double = 0.0, op: CanvasWrapper.()->Unit = {}) =
  opcr(this.wrapped(), CanvasWrapper(width, height), op)

fun EventTarget.anchorpane(vararg nodes: Node, op: AnchorPaneWrapper.()->Unit = {}): AnchorPaneWrapper {
  val anchorpane = AnchorPaneWrapper()
  if (nodes.isNotEmpty()) anchorpane.children.addAll(nodes)
  opcr(this.wrapped(), anchorpane, op)
  return anchorpane
}

fun EventTarget.accordion(vararg panes: TitledPane, op: AccordionWrapper.()->Unit = {}): AccordionWrapper {
  val accordion = AccordionWrapper()
  if (panes.isNotEmpty()) accordion.panes.addAll(panes)
  opcr(this.wrapped(), accordion, op)
  return accordion
}

fun <T: Node> AccordionWrapper.fold(
  title: String? = null,
  node: T,
  expanded: Boolean = false,
  op: T.()->Unit = {}
): TitledPaneWrapper {
  val fold = TitledPaneWrapper(title, node)
  fold.isExpanded = expanded
  panes += fold
  op(node)
  return fold
}

@Deprecated(
  "Properties added to the container will be lost if you add only a single child Node",
  ReplaceWith("Accordion.fold(title, node, op)"),
  DeprecationLevel.WARNING
)
fun AccordionWrapper.fold(title: String? = null, op: Pane.()->Unit = {}): TitledPaneWrapper {
  val vbox = VBox().also(op)
  val fold = TitledPaneWrapper(title, if (vbox.children.size == 1) vbox.children[0] else vbox)
  panes += fold
  return fold
}

fun EventTarget.region(op: Region.()->Unit = {}) = opcr(this.wrapped(), RegionWrapper(), op)


@Deprecated("Use the paddingRight property instead", ReplaceWith("paddingRight = p"))
fun Region.paddingRight(p: Double) {
  paddingRight = p
}

var Region.paddingRight: Number
  get() = padding.right
  set(value) {
	padding = padding.copy(right = value.toDouble())
  }

@Deprecated("Use the paddingLeft property instead", ReplaceWith("paddingLeft = p"))
fun Region.paddingLeft(p: Double) {
  paddingLeft = p
}

var Region.paddingLeft: Number
  get() = padding.left
  set(value) {
	padding = padding.copy(left = value)
  }

@Deprecated("Use the paddingTop property instead", ReplaceWith("paddingTop = p"))
fun Region.paddingTop(p: Double) {
  paddingTop = p
}

var Region.paddingTop: Number
  get() = padding.top
  set(value) {
	padding = padding.copy(top = value)
  }

@Deprecated("Use the paddingBottom property instead", ReplaceWith("paddingBottom = p"))
fun Region.paddingBottom(p: Double) {
  paddingBottom = p
}

var Region.paddingBottom: Number
  get() = padding.bottom
  set(value) {
	padding = padding.copy(bottom = value)
  }

@Deprecated("Use the paddingVertical property instead", ReplaceWith("paddingVertical = p"))
fun Region.paddingVertical(p: Double) {
  paddingVertical = p
}

var Region.paddingVertical: Number
  get() = padding.vertical*2
  set(value) {
	val half = value.toDouble()/2.0
	padding = padding.copy(vertical = half)
  }

@Deprecated("Use the paddingHorizontal property instead", ReplaceWith("paddingHorizontal = p"))
fun Region.paddingHorizontal(p: Double) {
  paddingHorizontal = p
}

var Region.paddingHorizontal: Number
  get() = padding.horizontal*2
  set(value) {
	val half = value.toDouble()/2.0
	padding = padding.copy(horizontal = half)
  }

@Deprecated("Use the paddingAll property instead", ReplaceWith("paddingAll = p"))
fun Region.paddingAll(p: Double) {
  paddingAll = p
}

var Region.paddingAll: Number
  get() = padding.all
  set(value) {
	padding = insets(value)
  }

fun Region.fitToParentHeight() {
  val parent = this.parent
  if (parent != null && parent is Region) {
	fitToHeight(parent)
  }
}

fun Region.fitToParentWidth() {
  val parent = this.parent
  if (parent != null && parent is Region) {
	fitToWidth(parent)
  }
}

fun Region.fitToParentSize() {
  fitToParentHeight()
  fitToParentWidth()
}

fun Region.fitToHeight(region: Region) {
  prefHeightProperty().bind(region.heightProperty())
}

fun Region.fitToWidth(region: Region) {
  prefWidthProperty().bind(region.widthProperty())
}

fun Region.fitToSize(region: Region) {
  fitToHeight(region)
  fitToWidth(region)
}


val Region.paddingVerticalProperty: DoubleProperty
  get() = properties.getOrPut("paddingVerticalProperty") {
	proxypropDouble(paddingProperty(), { paddingVertical.toDouble() }) {
	  val half = it/2.0
	  Insets(half, value.right, half, value.left)
	}
  } as DoubleProperty

val Region.paddingHorizontalProperty: DoubleProperty
  get() = properties.getOrPut("paddingHorizontalProperty") {
	proxypropDouble(paddingProperty(), { paddingHorizontal.toDouble() }) {
	  val half = it/2.0
	  Insets(value.top, half, value.bottom, half)
	}
  } as DoubleProperty

val Region.paddingAllProperty: DoubleProperty
  get() = properties.getOrPut("paddingAllProperty") {
	proxypropDouble(paddingProperty(), { paddingAll.toDouble() }) {
	  Insets(it, it, it, it)
	}
  } as DoubleProperty


