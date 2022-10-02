package matt.fx.control.wrapper.control.colbase

import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.scene.control.TableColumn
import javafx.scene.control.TableColumnBase
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNonNullableROProp
import matt.hurricanefx.wrapper.control.tablelike.TableLikeWrapper
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.sizeman.WidthManaged
import matt.hurricanefx.wrapper.target.EventTargetWrapperImpl


abstract class TableColumnBaseWrapper<E, P, F: TableColumnBase<E, P>>(
  override val node: TableColumnBase<E, P>
):
  EventTargetWrapperImpl<TableColumnBase<E, P>>(),
  WidthManaged {
  fun widthProperty(): ReadOnlyDoubleProperty = node.widthProperty()


  override val widthProperty get() = node.widthProperty().toNonNullableROProp().cast<Double>()
  override val prefWidthProperty get() = node.prefWidthProperty().toNonNullableProp().cast<Double>()
  override val minWidthProperty get() = node.minWidthProperty().toNonNullableProp().cast<Double>()
  override val maxWidthProperty get() = node.maxWidthProperty().toNonNullableProp().cast<Double>()


  abstract val tableView: TableLikeWrapper<E>?

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }


}

/**
 * Used to matt.hurricanefx.wrapper.control.column.matt.hurricanefx.wrapper.control.colbase.cancel an edit event, typically from `onEditStart`
 */
fun <S, T> TableColumn.CellEditEvent<S, T>.cancel() {
  tableView.edit(-1, tableColumn)
}
