package matt.fx.control.wrapper.control.colbase

import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.collections.ObservableMap
import javafx.scene.control.TableColumn
import javafx.scene.control.TableColumnBase
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.control.wrapper.control.tablelike.TableLikeWrapper
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.sizeman.WidthManaged
import matt.lang.anno.Open


abstract class TableColumnBaseWrapper<E: Any, P, F: TableColumnBase<E, P>>(
    @Open override val node: TableColumnBase<E, P>
):
    SingularEventTargetWrapper<TableColumnBase<E, P>>(node),
        WidthManaged {
    fun widthProperty(): ReadOnlyDoubleProperty = node.widthProperty()

    final override fun isInsideRow() = false

    final override val widthProperty get() = node.widthProperty().toNonNullableROProp().cast<Double>()
    final override val prefWidthProperty get() = node.prefWidthProperty().toNonNullableProp().cast<Double>()
    final override val minWidthProperty get() = node.minWidthProperty().toNonNullableProp().cast<Double>()
    final override val maxWidthProperty get() = node.maxWidthProperty().toNonNullableProp().cast<Double>()

    final override val properties: ObservableMap<Any, Any?>
        get() = node.properties

    abstract val tableView: TableLikeWrapper<E>?

    @Open override fun addChild(child: NodeWrapper, index: Int?) {
        TODO()
    }


}

/**
 * Used cancel an edit event, typically from `onEditStart`
 */
fun <S, T> TableColumn.CellEditEvent<S, T>.cancel() {

    tableView.edit(-1, tableColumn)
}
