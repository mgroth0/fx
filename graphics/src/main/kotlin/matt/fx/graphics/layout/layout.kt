/*a lot of this was from tornadofx*/

package matt.fx.graphics.layout

import javafx.beans.property.DoubleProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.geometry.Bounds
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.geometry.Rectangle2D
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.TitledPane
import javafx.scene.control.ToolBar
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.ConstraintsBase
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
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
import matt.hurricanefx.wrapper.EventTargetWrapper
import matt.hurricanefx.wrapper.FlowPaneWrapper
import matt.hurricanefx.wrapper.GridPaneWrapper
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
import matt.hurricanefx.wrapper.TilePaneWrapper
import matt.hurricanefx.wrapper.TitledPaneWrapper
import matt.hurricanefx.wrapper.ToolBarWrapper
import matt.hurricanefx.wrapper.VBoxWrapper
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


fun ToolBarWrapper.spacer(prio: Priority = Priority.ALWAYS, op: PaneWrapper.()->Unit = {}): PaneWrapper {
  val pane = PaneWrapper().apply {
	hgrow = prio
  }
  op(pane)
  add(pane)
  return pane
}

fun HBoxWrapper.spacer(prio: Priority = Priority.ALWAYS, op: PaneWrapper.()->Unit = {}) =
  opcr(this, PaneWrapper().apply { hGrow = prio }, op)

fun VBoxWrapper.spacer(prio: Priority = Priority.ALWAYS, op: PaneWrapper.()->Unit = {}) =
  opcr(this, PaneWrapper().apply { vGrow = prio }, op)


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


fun EventTargetWrapper<*>.toolbar(vararg nodes: Node, op: ToolBarWrapper.()->Unit = {}): ToolBarWrapper {
  val toolbar = ToolBarWrapper()
  if (nodes.isNotEmpty()) toolbar.items.addAll(nodes)
  opcr(this, toolbar, op)
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

fun ToolBarWrapper.separator(
  orientation: Orientation = Orientation.HORIZONTAL,
  op: SeparatorWrapper.()->Unit = {}
): SeparatorWrapper {
  val separator = SeparatorWrapper(orientation).also(op)
  add(separator)
  return separator
}

fun EventTargetWrapper<*>.separator(
  orientation: Orientation = Orientation.HORIZONTAL,
  op: SeparatorWrapper.()->Unit = {}
) =
  opcr(this, SeparatorWrapper(orientation), op)

fun EventTargetWrapper<*>.group(initialChildren: Iterable<Node>? = null, op: GroupWrapper.()->Unit = {}) =
  opcr(this, GroupWrapper().apply { if (initialChildren != null) children.addAll(initialChildren) }, op)

fun EventTargetWrapper<*>.stackpane(initialChildren: Iterable<Node>? = null, op: StackPaneWrapper.()->Unit = {}) =
  opcr(this, StackPaneWrapper().apply { if (initialChildren != null) children.addAll(initialChildren) }, op)

fun EventTargetWrapper<*>.gridpane(op: GridPaneWrapper.()->Unit = {}) = opcr(this, GridPaneWrapper(), op)
fun EventTargetWrapper<*>.pane(op: PaneWrapper.()->Unit = {}) = opcr(this, PaneWrapper(), op)
fun EventTargetWrapper<*>.flowpane(op: FlowPaneWrapper.()->Unit = {}) = opcr(this, FlowPaneWrapper(), op)
fun EventTargetWrapper<*>.tilepane(op: TilePaneWrapper.()->Unit = {}) = opcr(this, TilePaneWrapper(), op)
fun EventTargetWrapper<*>.borderpane(op: BorderPaneWrapper.()->Unit = {}) = opcr(this, BorderPaneWrapper(), op)


@Deprecated("Use top = node {} instead")
fun <T: NodeWrapper<*>> BorderPaneWrapper.top(topNode: T, op: T.()->Unit = {}): T {
  top = topNode.node
  return opcr(this, topNode, op)
}


@Deprecated("Use bottom = node {} instead")
fun <T: NodeWrapper<*>> BorderPaneWrapper.bottom(bottomNode: T, op: T.()->Unit = {}): T {
  bottom = bottomNode.node
  return opcr(this, bottomNode, op)
}

@Deprecated("Use left = node {} instead")
fun <T: NodeWrapper<*>> BorderPaneWrapper.left(leftNode: T, op: T.()->Unit = {}): T {
  left = leftNode.node
  return opcr(this, leftNode, op)
}

@Deprecated("Use right = node {} instead")
fun <T: NodeWrapper<*>> BorderPaneWrapper.right(rightNode: T, op: T.()->Unit = {}): T {
  right = rightNode.node
  return opcr(this, rightNode, op)
}

@Deprecated("Use center = node {} instead")
fun <T: NodeWrapper<*>> BorderPaneWrapper.center(centerNode: T, op: T.()->Unit = {}): T {
  center = centerNode.node
  return opcr(this, centerNode, op)
}

fun EventTargetWrapper<*>.titledpane(
  title: String? = null,
  node: Node? = null,
  collapsible: Boolean = true,
  op: (TitledPaneWrapper).()->Unit = {}
): TitledPaneWrapper {
  val titledPane = TitledPaneWrapper { text = title; graphic = node }
  titledPane.isCollapsible = collapsible
  opcr(this, titledPane, op)
  return titledPane
}

fun EventTargetWrapper<*>.titledpane(
  title: ObservableValue<String>,
  node: Node? = null,
  collapsible: Boolean = true,
  op: (TitledPaneWrapper).()->Unit = {}
): TitledPaneWrapper {
  val titledPane = TitledPaneWrapper { text = ""; graphic = node }
  titledPane.textProperty().bind(title)
  titledPane.isCollapsible = collapsible
  opcr(this, titledPane, op)
  return titledPane
}

fun EventTargetWrapper<*>.pagination(
  pageCount: Int? = null,
  pageIndex: Int? = null,
  op: PaginationWrapper.()->Unit = {}
): PaginationWrapper {
  val pagination = PaginationWrapper()
  if (pageCount != null) pagination.pageCount = pageCount
  if (pageIndex != null) pagination.currentPageIndex = pageIndex
  return opcr(this, pagination, op)
}

open class DummyClassYesIuse() {
  val v by lazy { Random.nextDouble() }
}

fun EventTargetWrapper<*>.scrollpane(
  fitToWidth: Boolean = false,
  fitToHeight: Boolean = false,
  op: ScrollPaneWrapper.()->Unit = {}
): ScrollPaneWrapper {
  val pane = ScrollPaneWrapper()
  pane.isFitToWidth = fitToWidth
  pane.isFitToHeight = fitToHeight
  opcr(this, pane, op)
  return pane
}


fun EventTargetWrapper<*>.splitpane(
  orientation: Orientation = Orientation.HORIZONTAL,
  vararg nodes: Node,
  op: SplitPaneWrapper.()->Unit = {}
): SplitPaneWrapper {
  val splitpane = SplitPaneWrapper()
  splitpane.orientation = orientation
  if (nodes.isNotEmpty())
	splitpane.items.addAll(nodes)
  opcr(this, splitpane, op)
  return splitpane
}

@Deprecated(
  "No need to wrap splitpane items in items{} anymore. Remove the wrapper and all builder items will still be added as before.",
  ReplaceWith("no items{} wrapper"),
  DeprecationLevel.WARNING
)
fun SplitPaneWrapper.items(op: (SplitPaneWrapper.()->Unit)) = op(this)

fun EventTargetWrapper<*>.canvas(width: Double = 0.0, height: Double = 0.0, op: CanvasWrapper.()->Unit = {}) =
  opcr(this, CanvasWrapper(width, height), op)

fun EventTargetWrapper<*>.anchorpane(vararg nodes: Node, op: AnchorPaneWrapper.()->Unit = {}): AnchorPaneWrapper {
  val anchorpane = AnchorPaneWrapper()
  if (nodes.isNotEmpty()) anchorpane.children.addAll(nodes)
  opcr(this, anchorpane, op)
  return anchorpane
}

fun EventTargetWrapper<*>.accordion(vararg panes: TitledPane, op: AccordionWrapper.()->Unit = {}): AccordionWrapper {
  val accordion = AccordionWrapper()
  if (panes.isNotEmpty()) accordion.panes.addAll(panes)
  opcr(this, accordion, op)
  return accordion
}

fun <T: Node> AccordionWrapper.fold(
  title: String? = null,
  node: T,
  expanded: Boolean = false,
  op: T.()->Unit = {}
): TitledPaneWrapper {
  val fold = TitledPaneWrapper { text = title;graphic = node }
  fold.isExpanded = expanded
  panes += fold.node
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
  val fold = TitledPaneWrapper { text = title; graphic = if (vbox.children.size == 1) vbox.children[0] else vbox }
  panes += fold.node
  return fold
}

fun EventTargetWrapper<*>.region(op: RegionWrapper.()->Unit = {}) = opcr(this, RegionWrapper(), op)


@Deprecated("Use the paddingRight property instead", ReplaceWith("paddingRight = p"))
fun RegionWrapper.paddingRight(p: Double) {
  paddingRight = p
}

var RegionWrapper.paddingRight: Number
  get() = padding.right
  set(value) {
	padding = padding.copy(right = value.toDouble())
  }

@Deprecated("Use the paddingLeft property instead", ReplaceWith("paddingLeft = p"))
fun RegionWrapper.paddingLeft(p: Double) {
  paddingLeft = p
}

var RegionWrapper.paddingLeft: Number
  get() = padding.left
  set(value) {
	padding = padding.copy(left = value)
  }

@Deprecated("Use the paddingTop property instead", ReplaceWith("paddingTop = p"))
fun RegionWrapper.paddingTop(p: Double) {
  paddingTop = p
}

var RegionWrapper.paddingTop: Number
  get() = padding.top
  set(value) {
	padding = padding.copy(top = value)
  }

@Deprecated("Use the paddingBottom property instead", ReplaceWith("paddingBottom = p"))
fun RegionWrapper.paddingBottom(p: Double) {
  paddingBottom = p
}

var RegionWrapper.paddingBottom: Number
  get() = padding.bottom
  set(value) {
	padding = padding.copy(bottom = value)
  }

@Deprecated("Use the paddingVertical property instead", ReplaceWith("paddingVertical = p"))
fun RegionWrapper.paddingVertical(p: Double) {
  paddingVertical = p
}

var RegionWrapper.paddingVertical: Number
  get() = padding.vertical*2
  set(value) {
	val half = value.toDouble()/2.0
	padding = padding.copy(vertical = half)
  }

@Deprecated("Use the paddingHorizontal property instead", ReplaceWith("paddingHorizontal = p"))
fun RegionWrapper.paddingHorizontal(p: Double) {
  paddingHorizontal = p
}

var RegionWrapper.paddingHorizontal: Number
  get() = padding.horizontal*2
  set(value) {
	val half = value.toDouble()/2.0
	padding = padding.copy(horizontal = half)
  }

@Deprecated("Use the paddingAll property instead", ReplaceWith("paddingAll = p"))
fun RegionWrapper.paddingAll(p: Double) {
  paddingAll = p
}

var RegionWrapper.paddingAll: Number
  get() = padding.all
  set(value) {
	padding = insets(value)
  }

fun RegionWrapper.fitToParentHeight() {
  (parent?.node as? Region)?.let { fitToHeight(it) }
}

fun RegionWrapper.fitToParentWidth() {
  (parent?.node as? Region)?.let { fitToWidth(it) }
}

fun RegionWrapper.fitToParentSize() {
  fitToParentHeight()
  fitToParentWidth()
}

fun RegionWrapper.fitToHeight(region: Region) {
  prefHeightProperty.bind(region.heightProperty())
}

fun RegionWrapper.fitToWidth(region: Region) {
  prefWidthProperty.bind(region.widthProperty())
}

fun RegionWrapper.fitToSize(region: Region) {
  fitToHeight(region)
  fitToWidth(region)
}


val RegionWrapper.paddingVerticalProperty: DoubleProperty
  get() = node.properties.getOrPut("paddingVerticalProperty") {
	proxypropDouble(paddingProperty, { paddingVertical.toDouble() }) {

	  val half = it/2.0
	  Insets(half, value.right, half, value.left)
	}
  } as DoubleProperty

val RegionWrapper.paddingHorizontalProperty: DoubleProperty
  get() = node.properties.getOrPut("paddingHorizontalProperty") {
	proxypropDouble(paddingProperty, { paddingHorizontal.toDouble() }) {
	  val half = it/2.0
	  Insets(value.top, half, value.bottom, half)
	}
  } as DoubleProperty

val RegionWrapper.paddingAllProperty: DoubleProperty
  get() = node.properties.getOrPut("paddingAllProperty") {
	proxypropDouble(paddingProperty, { paddingAll.toDouble() }) {
	  Insets(it, it, it, it)
	}
  } as DoubleProperty


