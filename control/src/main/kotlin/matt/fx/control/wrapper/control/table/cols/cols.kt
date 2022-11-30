package matt.fx.control.wrapper.control.table.cols

import javafx.collections.ObservableList
import javafx.scene.control.TableColumn
import javafx.util.Callback
import matt.fx.control.wrapper.cellfact.SimpleFactory
import matt.fx.control.wrapper.control.column.TableColumnWrapper
import matt.fx.graphics.wrapper.FXNodeWrapperDSL
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.obs.prop.BindableProperty
import matt.obs.prop.ObsVal
import matt.obs.prop.toVarProp
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

@FXNodeWrapperDSL
interface ColumnsDSL<E: Any> {
  fun <P> column(
	title: String,
	prop: KMutableProperty1<E, P>,
	op: TableColumnWrapper<E, P>.()->Unit = {}
  ): TableColumnWrapper<E, P>

  fun <P> column(
	title: String,
	prop: KProperty1<E, ObsVal<P>>,
	op: TableColumnWrapper<E, P>.()->Unit = {}
  ): TableColumnWrapper<E, P>

  fun <P> column(
	title: String,
	prefWidth: Double? = null,
	valueProvider: (TableColumn.CellDataFeatures<E, P>)->ObsVal<P>,
  ): TableColumnWrapper<E, P>

  fun nodeColumn(
	title: String,
	prefWidth: Double? = null,
	nodeProvider: (E)->NodeWrapper,
  ): TableColumnWrapper<E, NodeWrapper>

  fun <P> column(
	title: String,
	observableFn: KFunction<ObsVal<P>>,
	op: TableColumnWrapper<E, P>.()->Unit
  ): TableColumnWrapper<E, P>

  fun <P> column(
	getter: KFunction<P>,
	op: TableColumnWrapper<E, P>.()->Unit = {}
  ): TableColumnWrapper<E, P>

  fun <P> column(
	getter: KProperty1<E, P>,
	op: TableColumnWrapper<E, P>.()->Unit = {}
  ): TableColumnWrapper<E, P>

}

class ColumnsDSLImpl<E: Any>(private val columns: ObservableList<TableColumn<E, *>>): ColumnsDSL<E> {

  override fun <P> column(
	title: String,
	prop: KMutableProperty1<E, P>,
	op: TableColumnWrapper<E, P>.()->Unit
  ) = column(title) {
	prop.call(it.value).toVarProp()
  }.also(op)

  override fun <P> column(
	title: String,
	observableFn: KFunction<ObsVal<P>>,
	op: TableColumnWrapper<E, P>.()->Unit
  ) = column(title) {
	observableFn.call(it.value)
  }.also(op)

  override fun <P> column(
	getter: KFunction<P>,
	op: TableColumnWrapper<E, P>.()->Unit
  ) = column(getter.name) {
	getter.call(it.value).toVarProp()
  }.apply(op)


  override fun <P> column(
	title: String,
	prop: KProperty1<E, ObsVal<P>>,
	op: TableColumnWrapper<E, P>.()->Unit
  ) = column(title) {
	prop.call(it.value)
  }.also(op)


  override fun nodeColumn(
	title: String,
	prefWidth: Double?,
	nodeProvider: (E)->NodeWrapper,
  ) = column(title, prefWidth = prefWidth) {
	BindableProperty(nodeProvider(it.value))
  }.apply {
	simpleCellFactory(SimpleFactory {
	  "" to it
	})
  }

  override fun <P> column(
	getter: KProperty1<E, P>,
	op: TableColumnWrapper<E, P>.()->Unit
  ) = column(getter.name) {
	getter.call(it.value).toVarProp()
  }.apply(op)

  override fun <P> column(
	title: String,
	prefWidth: Double?,
	valueProvider: (TableColumn.CellDataFeatures<E, P>)->ObsVal<P>,
  ): TableColumnWrapper<E, P> {
	val column = TableColumnWrapper<E, P>(title)
	column.cellValueFactory = Callback { valueProvider(it) }
	prefWidth?.let { column.prefWidth = it }
	columns.add(column.node)
	return column
  }


}



