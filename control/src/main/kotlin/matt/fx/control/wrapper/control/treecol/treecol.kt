package matt.fx.control.wrapper.control.treecol

import javafx.beans.property.ObjectProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableCell
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.TreeTableColumn.CellDataFeatures
import javafx.util.Callback
import matt.hurricanefx.wrapper.cellfact.TreeTableCellFactory
import matt.hurricanefx.wrapper.cellfact.cellvalfact.CellValueFactory
import matt.hurricanefx.wrapper.control.colbase.TableColumnBaseWrapper
import matt.hurricanefx.wrapper.control.hascols.HasCols
import matt.hurricanefx.wrapper.control.treetable.TreeTableViewWrapper
import matt.hurricanefx.wrapper.wrapped


class TreeTableColumnWrapper<E, P>(
  override val node: TreeTableColumn<E, P>
): TableColumnBaseWrapper<TreeItem<E>, P, TreeTableColumn<E, P>>(node),
   TreeTableCellFactory<TreeTableColumn<E, P>, E, P>,
   CellValueFactory<CellDataFeatures<E, P>, P>,
   HasCols<TreeItem<E>> {


  constructor(name: String): this(TreeTableColumn(name))

  override val cellFactoryProperty: ObjectProperty<Callback<TreeTableColumn<E, P>, TreeTableCell<E, P>>> get() = node.cellFactoryProperty()
  override var cellValueFactory: Callback<CellDataFeatures<E, P>, ObservableValue<P>>? by node::cellValueFactory

  override val columns: ObservableList<TreeTableColumn<E, *>> = node.columns

  override val tableView: TreeTableViewWrapper<E>? get() = node.treeTableView?.wrapped()


}