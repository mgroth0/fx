/*a lot of this was from tornadofx*/

package matt.fx.graphics.layout

import javafx.collections.ObservableList
import javafx.geometry.Bounds
import javafx.geometry.Insets
import javafx.geometry.Rectangle2D
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.ToolBar
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.ConstraintsBase
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import matt.hurricanefx.tornadofx.fx.getChildList


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




@Deprecated(
  "No need to wrap ToolBar children in children{} anymore. Remove the wrapper and all builder items will still be added as before.",
  ReplaceWith("no children{} wrapper"),
  DeprecationLevel.WARNING
)
fun ToolBar.children(op: ToolBar.()->Unit) = apply { op() }




