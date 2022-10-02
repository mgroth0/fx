package matt.fx.graphics.wrapper.pane.grid

import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.RowConstraints
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl

open class GridPaneWrapper<C: NodeWrapper>(node: GridPane = GridPane()): PaneWrapperImpl<GridPane, C>(node) {
  companion object {
	fun setConstraints(child: Node, columnIndex: Int, rowIndex: Int) =
	  GridPane.setConstraints(child, columnIndex, rowIndex)
  }

  val columnConstraints: ObservableList<ColumnConstraints> = node.columnConstraints
  val rowConstraints: ObservableList<RowConstraints> = node.rowConstraints

}