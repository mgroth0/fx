package matt.fx.control.wrapper.control.table

import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TablePosition
import javafx.scene.control.TableView
import javafx.scene.control.TableView.ResizeFeatures
import javafx.scene.control.TableView.TableViewSelectionModel
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.input.InputEvent
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.util.Callback
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.column.TableColumnWrapper
import matt.fx.control.wrapper.control.tablelike.TableLikeWrapper
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.eye.prop.getValue
import matt.hurricanefx.eye.prop.observable
import matt.hurricanefx.eye.prop.setValue
import matt.lang.decap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


open class TableViewWrapper<E>(
  node: TableView<E> = TableView<E>(),
): ControlWrapperImpl<TableView<E>>(node), TableLikeWrapper<E> {

  constructor(items: ObservableList<E>): this(TableView(items))


  fun editableProperty(): BooleanProperty = node.editableProperty()

  var isEditable by editableProperty()

  val sortOrder: ObservableList<TableColumn<E, *>> get() = node.sortOrder

  var columnResizePolicy: Callback<ResizeFeatures<Any>, Boolean>
	get() = node.columnResizePolicy
	set(value) {
	  node.columnResizePolicy = value
	}

  fun columnResizePolicyProperty(): ObjectProperty<Callback<ResizeFeatures<Any>, Boolean>> =
	node.columnResizePolicyProperty()

  var items: ObservableList<E>
	get() = node.items
	set(value) {
	  node.items = value
	}

  fun itemsProperty(): ObjectProperty<ObservableList<E>> = node.itemsProperty()

  fun comparatorProperty(): ReadOnlyObjectProperty<Comparator<E>> = node.comparatorProperty()

  override val columns: ObservableList<TableColumn<E, *>> get() = node.columns

  override val selectionModel: TableViewSelectionModel<E> get() = node.selectionModel
  fun scrollTo(i: Int) = node.scrollTo(i)
  fun scrollTo(e: E) = node.scrollTo(e)
  fun sort() = node.sort()
  fun editingCellProperty(): ReadOnlyObjectProperty<TablePosition<E, *>> = node.editingCellProperty()

  /**
   * Create a matt.hurricanefx.tableview.coolColumn with a value factory that extracts the value from the given mutable
   * property and converts the property to an observable value.
   */
  inline fun <reified P> column(
	title: String,
	prop: KMutableProperty1<E, P>,
	noinline op: TableColumnWrapper<E, P>.()->Unit = {}
  ): TableColumnWrapper<E, P> {
	val column = TableColumnWrapper<E, P>(title)
	column.cellValueFactory = Callback { observable(it.value, prop) }
	addColumnInternal(column)
	return column.also(op)
  }

  /**
   * Create a matt.hurricanefx.tableview.coolColumn with a value factory that extracts the value from the given property and
   * converts the property to an observable value.
   *
   * ATTENTION: This function was renamed to `readonlyColumn` to avoid shadowing the version for
   * observable properties.
   */
  inline fun <reified P> readonlyColumn(
	title: String,
	prop: KProperty1<E, P>,
	noinline op: TableColumnWrapper<E, P>.()->Unit = {}
  ): TableColumnWrapper<E, P> {
	val column = TableColumnWrapper<E, P>(title)
	column.cellValueFactory = Callback { observable(it.value, prop) }
	addColumnInternal(column)
	return column.also(op)
  }


  /**
   * Create a matt.hurricanefx.tableview.coolColumn with a value factory that extracts the value from the given ObservableValue property.
   */
  inline fun <reified P> column(
	title: String,
	prop: KProperty1<E, ObservableValue<P>>,
	noinline op: TableColumnWrapper<E, P>.()->Unit = {}
  ): TableColumnWrapper<E, P> {
	val column = TableColumnWrapper<E, P>(title)
	column.cellValueFactory = Callback { prop.call(it.value) }
	addColumnInternal(column)
	return column.also(op)
  }


  /**
   * Add a global edit commit handler to the TableView. You avoid assuming the responsibility
   * for writing back the data into your domain object and can consentrate on the actual
   * response you want to happen when a matt.hurricanefx.tableview.coolColumn commits and edit.
   */
  fun onEditCommit(onCommit: TableColumn.CellEditEvent<E, Any>.(E)->Unit) {
	fun addEventHandlerForColumn(column: TableColumn<E, *>) {
	  column.addEventHandler(TableColumn.editCommitEvent<E, Any>()) { event ->
		// Make sure the domain object gets the new value before we notify our handler
		Platform.runLater {
		  onCommit(event, event.rowValue)
		}
	  }
	  column.columns.forEach(::addEventHandlerForColumn)
	}

	columns.forEach(::addEventHandlerForColumn)

	columns.addListener({ change: ListChangeListener.Change<out TableColumn<E, *>> ->
	  while (change.next()) {
		if (change.wasAdded())
		  change.addedSubList.forEach(::addEventHandlerForColumn)
	  }
	})
  }


  /**
   * Add a global edit start handler to the TableView. You can use this callback
   * to matt.hurricanefx.matt.fx.control.wrapper.wrapped.getWrapper.control.column.matt.hurricanefx.matt.fx.control.wrapper.wrapped.getWrapper.control.colbase.cancel the edit request by calling matt.hurricanefx.matt.fx.control.wrapper.wrapped.getWrapper.control.column.matt.hurricanefx.matt.fx.control.wrapper.wrapped.getWrapper.control.colbase.cancel()
   */
  fun onEditStart(onEditStart: TableColumn.CellEditEvent<E, Any?>.(E)->Unit) {
	fun addEventHandlerForColumn(column: TableColumn<E, *>) {
	  column.addEventHandler(TableColumn.editStartEvent<E, Any?>()) { event ->
		onEditStart(event, event.rowValue)
	  }
	  column.columns.forEach(::addEventHandlerForColumn)
	}

	columns.forEach(::addEventHandlerForColumn)

	columns.addListener({ change: ListChangeListener.Change<out TableColumn<E, *>> ->
	  while (change.next()) {
		if (change.wasAdded())
		  change.addedSubList.forEach(::addEventHandlerForColumn)
	  }
	})
  }


  /**
   * Create a matt.hurricanefx.tableview.coolColumn with a title specified cell type and operate on it. Inside the code block you can call
   * `value { it.value.someProperty }` to set up a cellValueFactory that must return T or ObservableValue<T>
   */
  @Suppress("UNUSED_PARAMETER")
  fun <P: Any> column(
	title: String,
	cellType: KClass<P>,
	op: TableColumnWrapper<E, P>.()->Unit = {}
  ): TableColumnWrapper<E, P> {
	val column = TableColumnWrapper<E, P>(title)
	addColumnInternal(column)
	return column.also(op)
  }


  /**
   * Create a matt.hurricanefx.tableview.coolColumn with a value factory that extracts the value from the given callback.
   */
  fun <P> column(
	title: String,
	prefWidth: Double? = null,
	valueProvider: (TableColumn.CellDataFeatures<E, P>)->ObservableValue<P>,
  ): TableColumnWrapper<E, P> {
	val column = TableColumnWrapper<E, P>(title)
	column.cellValueFactory = Callback { valueProvider(it) }
	prefWidth?.let { column.prefWidth = it }
	addColumnInternal(column)
	return column
  }


  /**
   * Create a matt.hurricanefx.tableview.coolColumn with a value factory that extracts the observable value from the given function reference.
   * This method requires that you have kotlin-reflect on your classpath.
   */
  inline fun <reified P> column(
	title: String,
	observableFn: KFunction<ObservableValue<P>>
  ): TableColumnWrapper<E, P> {
	val column = TableColumnWrapper<E, P>(title)
	column.cellValueFactory = Callback { observableFn.call(it.value) }
	addColumnInternal(column)
	return column
  }

  fun enableCellEditing() {
	selectionModel.isCellSelectionEnabled = true
	isEditable = true
  }

  /**
   * Create a matt.hurricanefx.tableview.coolColumn holding matt.fx.control.layout.children columns
   */
  @Suppress("UNCHECKED_CAST")
  fun nestedColumn(
	title: String,
	op: TableViewWrapper<E>.(TableColumn<E, Any?>)->Unit = {}
  ): TableColumnWrapper<E, Any?> {
	val column = TableColumnWrapper<E, Any?>(title)
	addColumnInternal(column)
	val previousColumnTarget = node.properties["tornadofx.columnTarget"] as? ObservableList<TableColumn<E, *>>
	node.properties["tornadofx.columnTarget"] = column.node.columns
	op(this, column.node)
	node.properties["tornadofx.columnTarget"] = previousColumnTarget
	return column
  }


  /**
   * Create a matt.hurricanefx.tableview.coolColumn using the propertyName of the attribute you want shown.
   */
  fun <P> column(
	title: String,
	propertyName: String,
	op: TableColumnWrapper<E, P>.()->Unit = {}
  ): TableColumnWrapper<E, P> {
	val column = TableColumnWrapper<E, P>(title)
	column.cellValueFactory = PropertyValueFactory<E, P>(propertyName)
	addColumnInternal(column)
	return column.also(op)
  }


  /**
   * Create a matt.hurricanefx.tableview.coolColumn using the getter of the attribute you want shown.
   */
  @JvmName("pojoColumn")
  fun <P> column(title: String, getter: KFunction<P>): TableColumnWrapper<E, P> {
	val startIndex = if (getter.name.startsWith("is") && getter.name[2].isUpperCase()) 2 else 3
	val propName = getter.name.substring(startIndex).decap()
	return this.column(title, propName)
  }

  @Suppress("UNCHECKED_CAST")
  fun addColumnInternal(column: TableColumnWrapper<E, *>, index: Int? = null) {
	val columnTarget = node.properties["tornadofx.columnTarget"] as? ObservableList<TableColumn<E, *>> ?: columns
	if (index == null) columnTarget.add(column.node) else columnTarget.add(index, column.node)
  }

  fun makeIndexColumn(name: String = "#", startNumber: Int = 1): TableColumn<E, Number> {
	return TableColumn<E, Number>(name).apply {
	  isSortable = false
	  prefWidth = width

	  this@TableViewWrapper.columns += this
	  setCellValueFactory { ReadOnlyObjectWrapper(items.indexOf(it.value) + startNumber) }
	}
  }


  @Suppress("UNCHECKED_CAST")
  val selectedCell: TablePosition<E, *>?
	get() = selectionModel.selectedCells.firstOrNull() as TablePosition<E, *>?

  val selectedColumn: TableColumn<E, *>?
	get() = selectedCell?.tableColumn

  val selectedValue: Any?
	get() = selectedColumn?.getCellObservableValue(selectedItem)?.value

  fun multiSelect(enable: Boolean = true) {
	selectionModel.selectionMode = if (enable) SelectionMode.MULTIPLE else SelectionMode.SINGLE
  }


  /**
   * Matt was here!
   */
  @JvmName("coolColumn")
  fun <P> column(getter: KFunction<P>, op: TableColumnWrapper<E, P>.()->Unit = {}): TableColumnWrapper<E, P> {
	return column(getter.name) {
	  SimpleObjectProperty(getter.call(it.value))
	}.apply(op)
  }


  /**
   * Matt was here!
   */
  @JvmName("coolColumn2")
  fun <P> column(
	getter: KProperty1<E, P>,
	op: TableColumnWrapper<E, P>.()->Unit = {}
  ): TableColumnWrapper<E, P> {
	return column(getter.name) {
	  SimpleObjectProperty(getter.call(it.value))
	}.apply(op)
  }

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }

}


fun <T> TableViewWrapper<T>.selectWhere(scrollTo: Boolean = true, condition: (T)->Boolean) {
  items.asSequence().filter(condition).forEach {
	selectionModel.select(it)
	if (scrollTo) scrollTo(it)
  }
}


fun <T> TableViewWrapper<T>.moveToTopWhere(
  backingList: ObservableList<T> = items,
  select: Boolean = true,
  predicate: (T)->Boolean
) {
  if (select) selectionModel.clearSelection()
  backingList.filter(predicate).forEach {
	backingList.remove(it)
	backingList.add(0, it)
	if (select) selectionModel.select(it)
  }
}

fun <T> TableViewWrapper<T>.moveToBottomWhere(
  backingList: ObservableList<T> = items,
  select: Boolean = true,
  predicate: (T)->Boolean
) {
  val end = backingList.size - 1
  if (select) selectionModel.clearSelection()
  backingList.filter(predicate).forEach {
	backingList.remove(it)
	backingList.add(end, it)
	if (select) selectionModel.select(it)

  }
}

fun <T> TableViewWrapper<T>.selectFirst() = selectionModel.selectFirst()


fun <S> TableViewWrapper<S>.onSelectionChange(func: (S?)->Unit) =
  selectionModel.selectedItemProperty().addListener({ _, _, newValue -> func(newValue) })


/**
 * Execute action when the enter key is pressed or the mouse is clicked

 * @param clickCount The number of mouse clicks to trigger the action
 * *
 * @param action The action to execute on select
 */
fun <T> TableViewWrapper<T>.onUserSelect(clickCount: Int = 2, action: (T)->Unit) {
  val isSelected = { event: InputEvent ->
	event.target.wrapped().isInsideRow() && !selectionModel.isEmpty
  }

  addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
	if (event.clickCount == clickCount && isSelected(event))
	  action(selectedItem!!)
  }

  addEventFilter(KeyEvent.KEY_PRESSED) { event ->
	if (event.code == KeyCode.ENTER && !event.isMetaDown && isSelected(event))
	  action(selectedItem!!)
  }
}

fun <T> TableViewWrapper<T>.onUserDelete(action: (T)->Unit) {
  addEventFilter(KeyEvent.KEY_PRESSED, { event ->
	if (event.code == KeyCode.BACK_SPACE && selectedItem != null)
	  action(selectedItem!!)
  })
}


fun <T> TableViewWrapper<T>.regainFocusAfterEdit() = apply {
  editingCellProperty().onChange {
	if (it == null)
	  requestFocus()
  }
}


fun TableViewWrapper<*>.editableWhen(predicate: ObservableValue<Boolean>) = apply {
  editableProperty().bind(predicate)
}