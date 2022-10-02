@file:Suppress("UNCHECKED_CAST")

/*slightly modified code I stole from tornadofx*/

package matt.fx.graphics.tfx.nodes

import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Point2D
import javafx.geometry.Point3D
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import matt.fx.graphics.wrapper.node.NodeWrapper


fun point(x: Number, y: Number) = Point2D(x.toDouble(), y.toDouble())
fun point(x: Number, y: Number, z: Number) = Point3D(x.toDouble(), y.toDouble(), z.toDouble())
infix fun Number.xy(y: Number) = Point2D(toDouble(), y.toDouble())



/**
 * Access BorderPane constraints to manipulate and apply on this control
 */
inline fun <T: Node> T.borderpaneConstraints(op: (BorderPaneConstraint.()->Unit)): T {
  val bpc = BorderPaneConstraint(this)
  bpc.op()
  return bpc.applyToNode(this)
}

class BorderPaneConstraint(
  node: Node,
  override var margin: Insets? = BorderPane.getMargin(node),
  var alignment: Pos? = null
): MarginableConstraints() {
  fun <T: Node> applyToNode(node: T): T {
	margin.let { BorderPane.setMargin(node, it) }
	alignment?.let { BorderPane.setAlignment(node, it) }
	return node
  }
}


class GridPaneConstraint(
  node: Node,
  var columnIndex: Int? = null,
  var rowIndex: Int? = null,
  var hGrow: Priority? = null,
  var vGrow: Priority? = null,
  override var margin: Insets? = GridPane.getMargin(node),
  var fillHeight: Boolean? = null,
  var fillWidth: Boolean? = null,
  var hAlignment: HPos? = null,
  var vAlignment: VPos? = null,
  var columnSpan: Int? = null,
  var rowSpan: Int? = null

): MarginableConstraints() {
  var vhGrow: Priority? = null
	set(value) {
	  vGrow = value
	  hGrow = value
	  field = value
	}

  var fillHeightWidth: Boolean? = null
	set(value) {
	  fillHeight = value
	  fillWidth = value
	  field = value
	}

  fun columnRowIndex(columnIndex: Int, rowIndex: Int) {
	this.columnIndex = columnIndex
	this.rowIndex = rowIndex
  }

  fun fillHeightWidth(fill: Boolean) {
	fillHeight = fill
	fillWidth = fill
  }

  fun <T: NodeWrapper> applyToNode(node: T): T {
	columnIndex?.let { GridPane.setColumnIndex(node.node, it) }
	rowIndex?.let { GridPane.setRowIndex(node.node, it) }
	hGrow?.let { GridPane.setHgrow(node.node, it) }
	vGrow?.let { GridPane.setVgrow(node.node, it) }
	margin.let { GridPane.setMargin(node.node, it) }
	fillHeight?.let { GridPane.setFillHeight(node.node, it) }
	fillWidth?.let { GridPane.setFillWidth(node.node, it) }
	hAlignment?.let { GridPane.setHalignment(node.node, it) }
	vAlignment?.let { GridPane.setValignment(node.node, it) }
	columnSpan?.let { GridPane.setColumnSpan(node.node, it) }
	rowSpan?.let { GridPane.setRowSpan(node.node, it) }
	return node
  }
}

inline fun <T: Node> T.vboxConstraints(op: (VBoxConstraint.()->Unit)): T {
  val c = VBoxConstraint(this)
  c.op()
  return c.applyToNode(this)
}

inline fun <T: Node> T.stackpaneConstraints(op: (StackpaneConstraint.()->Unit)): T {
  val c = StackpaneConstraint(this)
  c.op()
  return c.applyToNode(this)
}

class VBoxConstraint(
  node: Node,
  override var margin: Insets? = VBox.getMargin(node),
  var vGrow: Priority? = null

): MarginableConstraints() {
  fun <T: Node> applyToNode(node: T): T {
	margin?.let { VBox.setMargin(node, it) }
	vGrow?.let { VBox.setVgrow(node, it) }
	return node
  }
}

class StackpaneConstraint(
  node: Node,
  override var margin: Insets? = StackPane.getMargin(node),
  var alignment: Pos? = null

): MarginableConstraints() {
  fun <T: Node> applyToNode(node: T): T {
	margin?.let { StackPane.setMargin(node, it) }
	alignment?.let { StackPane.setAlignment(node, it) }
	return node
  }
}

inline fun <T: Node> T.hboxConstraints(op: (HBoxConstraint.()->Unit)): T {
  val c = HBoxConstraint(this)
  c.op()
  return c.applyToNode(this)
}

class HBoxConstraint(
  node: Node,
  override var margin: Insets? = HBox.getMargin(node),
  var hGrow: Priority? = null
): MarginableConstraints() {

  fun <T: Node> applyToNode(node: T): T {
	margin?.let { HBox.setMargin(node, it) }
	hGrow?.let { HBox.setHgrow(node, it) }
	return node
  }
}


inline fun <T: Node> T.anchorpaneConstraints(op: AnchorPaneConstraint.()->Unit): T {
  val c = AnchorPaneConstraint()
  c.op()
  return c.applyToNode(this)
}

class AnchorPaneConstraint(
  var topAnchor: Number? = null,
  var rightAnchor: Number? = null,
  var bottomAnchor: Number? = null,
  var leftAnchor: Number? = null
) {
  fun <T: Node> applyToNode(node: T): T {
	topAnchor?.let { AnchorPane.setTopAnchor(node, it.toDouble()) }
	rightAnchor?.let { AnchorPane.setRightAnchor(node, it.toDouble()) }
	bottomAnchor?.let { AnchorPane.setBottomAnchor(node, it.toDouble()) }
	leftAnchor?.let { AnchorPane.setLeftAnchor(node, it.toDouble()) }
	return node
  }
}



abstract class MarginableConstraints {
  abstract var margin: Insets?
  var marginTop: Double
	get() = margin?.top ?: 0.0
	set(value) {
	  margin = Insets(value, margin?.right ?: 0.0, margin?.bottom ?: 0.0, margin?.left ?: 0.0)
	}

  var marginRight: Double
	get() = margin?.right ?: 0.0
	set(value) {
	  margin = Insets(margin?.top ?: 0.0, value, margin?.bottom ?: 0.0, margin?.left ?: 0.0)
	}

  var marginBottom: Double
	get() = margin?.bottom ?: 0.0
	set(value) {
	  margin = Insets(margin?.top ?: 0.0, margin?.right ?: 0.0, value, margin?.left ?: 0.0)
	}

  var marginLeft: Double
	get() = margin?.left ?: 0.0
	set(value) {
	  margin = Insets(margin?.top ?: 0.0, margin?.right ?: 0.0, margin?.bottom ?: 0.0, value)
	}

  fun marginTopBottom(value: Double) {
	marginTop = value
	marginBottom = value
  }

  fun marginLeftRight(value: Double) {
	marginLeft = value
	marginRight = value
  }
}










