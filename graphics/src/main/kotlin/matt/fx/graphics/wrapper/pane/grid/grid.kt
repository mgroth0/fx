package matt.fx.graphics.wrapper.pane.grid

import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.RowConstraints
import matt.fx.graphics.tfx.nodes.GridPaneConstraint
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl


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

}