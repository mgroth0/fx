package matt.fx.control.wrapper.control.colbase

import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.collections.ObservableMap
import javafx.scene.control.TableColumn
import javafx.scene.control.TableColumnBase
import matt.fx.control.wrapper.control.tablelike.TableLikeWrapper
import matt.fx.graphics.wrapper.EventTargetWrapperImpl
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.graphics.wrapper.sizeman.WidthManaged


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

  override val properties: ObservableMap<Any, Any?>
    get() = node.properties

  abstract val tableView: TableLikeWrapper<E>?

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }


}

/**
 * Used to matt.hurricanefx.matt.fx.control.wrapper.wrapped.getWrapper.control.column.matt.hurricanefx.matt.fx.control.wrapper.wrapped.getWrapper.control.colbase.cancel an edit event, typically from `onEditStart`
 */
fun <S, T> TableColumn.CellEditEvent<S, T>.cancel() {
  tableView.edit(-1, tableColumn)
}
