package matt.fx.graphics.wrapper.pane.grid

import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.RowConstraints
import matt.fx.graphics.tfx.nodes.GridPaneConstraint
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.grid.GridPaneWrapper.GridDSLType.COL
import matt.fx.graphics.wrapper.pane.grid.GridPaneWrapper.GridDSLType.ROW
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp


fun <T: NodeWrapper> T.gridpaneConstraints(op: (GridPaneConstraint.()->Unit)): T {
  val gpc = GridPaneConstraint(this.node)
  gpc.op()
  return gpc.applyToNode(this)
}


fun <C: NodeWrapper> ET.gridpane(op: GridPaneWrapper<C>.()->Unit = {}) = attach(GridPaneWrapper(), op)
open class GridPaneWrapper<C: NodeWrapper>(node: GridPane = GridPane()): PaneWrapperImpl<GridPane, C>(node) {
  companion object {
	fun setConstraints(child: Node, columnIndex: Int, rowIndex: Int) =
	  GridPane.setConstraints(child, columnIndex, rowIndex)
  }

  val columnConstraints: ObservableList<ColumnConstraints> = node.columnConstraints
  val rowConstraints: ObservableList<RowConstraints> = node.rowConstraints

  enum class GridDSLType {
	ROW, COL
  }

  private var dslType: GridDSLType? = null

  private var globalRowIndex = 0
  fun row(op: GridPaneWrapper<C>.()->Unit) {
	require(dslType != COL)
	dslType = ROW
	val newChildren = stupidChildAdderDSL(op)
	var localColIndex = 0
	newChildren.forEach {
	  it.gridpaneConstraints {
		rowIndex = this@GridPaneWrapper.globalRowIndex
		columnIndex = localColIndex++
	  }
	}
	while (columnConstraints.size < localColIndex) {
	  columnConstraints += ColumnConstraints()
	}
	globalRowIndex++
	while (rowConstraints.size < globalRowIndex) {
	  rowConstraints += RowConstraints()
	}
  }

  private var globalColIndex = 0
  fun column(op: GridPaneWrapper<C>.()->Unit) {
	require(dslType != ROW)
	dslType = COL
	val newChildren = stupidChildAdderDSL(op)
	var localRowIndex = 0
	newChildren.forEach {
	  it.gridpaneConstraints {
		columnIndex = this@GridPaneWrapper.globalColIndex
		rowIndex = localRowIndex++
	  }
	}
	while (rowConstraints.size < localRowIndex) {
	  rowConstraints += RowConstraints()
	}
	globalColIndex++
	while (columnConstraints.size < globalColIndex) {
	  columnConstraints += ColumnConstraints()
	}
  }

  private fun stupidChildAdderDSL(op: GridPaneWrapper<C>.()->Unit): List<NW> {
	val oldChildren = children.toList()
	@Suppress("UNUSED_EXPRESSION") op()
	return children.filter { it !in oldChildren }
  }

  val hgapProperty by lazy {node.hgapProperty().toNonNullableProp().cast<Double>()}
  var hGap by hgapProperty
  val vgapProperty by lazy {node.hgapProperty().toNonNullableProp().cast<Double>()}
  var vGap by vgapProperty


}


