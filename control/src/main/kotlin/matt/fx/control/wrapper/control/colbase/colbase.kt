package matt.fx.control.wrapper.control.colbase

import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.collections.ObservableMap
import javafx.scene.control.TableColumn
import javafx.scene.control.TableColumnBase
import matt.fx.control.wrapper.control.tablelike.TableLikeWrapper
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.sizeman.WidthManaged
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNonNullableROProp


abstract class TableColumnBaseWrapper<E: Any, P, F: TableColumnBase<E, P>>(
  override val node: TableColumnBase<E, P>
):
  SingularEventTargetWrapper<TableColumnBase<E, P>>(node),
  WidthManaged {
  fun widthProperty(): ReadOnlyDoubleProperty = node.widthProperty()

  override fun isInsideRow() = false

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
 * Used cancel an edit event, typically from `onEditStart`
 */
fun <S, T> TableColumn.CellEditEvent<S, T>.cancel() {

  tableView.edit(-1, tableColumn)
}
