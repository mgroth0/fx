package matt.fx.control.wrapper.control.table

import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TablePosition
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.control.TableView.ResizeFeatures
import javafx.scene.input.InputEvent
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.util.Callback
import matt.fx.control.wrapper.cellfact.SimpleFactory
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.column.TableColumnWrapper
import matt.fx.control.wrapper.control.tablelike.TableLikeWrapper
import matt.fx.control.wrapper.selects.wrap
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.fxWidth
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.wrapper.obs.collect.mfxMutableListConverter
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNullableROProp
import matt.lang.setAll
import matt.obs.bind.binding
import matt.obs.bindings.bool.ObsB
import matt.obs.col.olist.MutableObsList
import matt.obs.col.olist.ObsList
import matt.obs.col.olist.toMutableObsList
import matt.obs.prop.BindableProperty
import matt.obs.prop.ObsVal
import matt.obs.prop.VarProp
import matt.obs.prop.toVarProp
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


fun <T: Any> ET.tableview(items: ObsList<T>? = null, op: TableViewWrapper<T>.()->Unit = {}) =
  TableViewWrapper<T>().attachTo(this, op) {
	if (items != null) {
	  if (items is MutableObsList<T>) {
		it.items = items
	  } else {
		it.items = items.toMutableObsList().apply {
		  items.onChange {
			setAll(items)
		  }
		}
	  }

	}
  }

fun <T: Any> ET.tableview(
  items: ObsVal<out MutableObsList<T>>,
  op: TableViewWrapper<T>.()->Unit = {}
) =
  TableViewWrapper<T>().attachTo(this, op) {
	it.itemsProperty.bind(items.binding { it })
  }

open class TableViewWrapper<E: Any>(
  node: TableView<E> = TableView<E>(),
): ControlWrapperImpl<TableView<E>>(node), TableLikeWrapper<E> {

  constructor(items: ObservableList<E>): this(TableView(items))

  fun setRowFactory(value: Callback<TableView<E>, TableRow<E>>) = node.setRowFactory(value)


  override fun isInsideRow() = true

  val editableProperty by lazy { node.editableProperty().toNonNullableProp() }

  var isEditable by editableProperty

  val sortOrder: ObservableList<TableColumn<E, *>> get() = node.sortOrder

  var columnResizePolicy: Callback<ResizeFeatures<Any>, Boolean>
	get() = node.columnResizePolicy
	set(value) {
	  node.columnResizePolicy = value
	}

  fun columnResizePolicyProperty(): ObjectProperty<Callback<ResizeFeatures<Any>, Boolean>> =
	node.columnResizePolicyProperty()


  val itemsProperty by lazy { node.itemsProperty().toNullableProp().proxy(mfxMutableListConverter<E>().nullable()) }
  var items by itemsProperty

  val comparatorProperty by lazy { node.comparatorProperty().toNullableROProp() }

  override val columns: ObservableList<TableColumn<E, *>> get() = node.columns

  override val selectionModel by lazy { node.selectionModel.wrap() }
  fun scrollTo(i: Int) = node.scrollTo(i)
  fun scrollTo(e: E) = node.scrollTo(e)
  fun sort() = node.sort()
  val editingCellProperty by lazy { node.editingCellProperty().toNullableROProp() }


  inline fun <reified P> column(
	title: String,
	prop: KMutableProperty1<E, P>,
	noinline op: TableColumnWrapper<E, P>.()->Unit = {}
  ): TableColumnWrapper<E, P> {
	val column = TableColumnWrapper<E, P>(title)
	column.cellValueFactory = Callback {
	  prop.call(it.value).toVarProp()
	  /*observable(it.value, prop)*/
	}
	addColumnInternal(column)
	return column.also(op)
  }


  inline fun <reified P> column(
	title: String,
	prop: KProperty1<E, ObsVal<P>>,
	noinline op: TableColumnWrapper<E, P>.()->Unit = {}
  ): TableColumnWrapper<E, P> {
	val column = TableColumnWrapper<E, P>(title)
	column.cellValueFactory = Callback { prop.call(it.value) }
	addColumnInternal(column)
	return column.also(op)
  }

  fun <P> column(
	title: String,
	prefWidth: Double? = null,
	valueProvider: (TableColumn.CellDataFeatures<E, P>)->ObsVal<P>,
  ): TableColumnWrapper<E, P> {
	val column = TableColumnWrapper<E, P>(title)
	column.cellValueFactory = Callback { valueProvider(it) }
	prefWidth?.let { column.prefWidth = it }
	addColumnInternal(column)
	return column
  }

  fun nodeColumn(
	title: String,
	prefWidth: Double? = null,
	nodeProvider: (TableColumn.CellDataFeatures<E, NodeWrapper>)->NodeWrapper,
  ): TableColumnWrapper<E, NodeWrapper> {
	val column = TableColumnWrapper<E, NodeWrapper>(title)
	column.cellValueFactory = Callback { BindableProperty(nodeProvider(it)) }
	column.simpleCellFactory(SimpleFactory {
	  "" to it
	})
	prefWidth?.let { column.prefWidth = it }
	addColumnInternal(column)
	return column
  }


  inline fun <reified P> column(
	title: String,
	observableFn: KFunction<ObsVal<P>>
  ): TableColumnWrapper<E, P> {
	val column = TableColumnWrapper<E, P>(title)
	column.cellValueFactory = Callback { observableFn.call(it.value) }
	addColumnInternal(column)
	return column
  }


  fun <P> column(
	getter: KFunction<P>,
	op: TableColumnWrapper<E, P>.()->Unit = {}
  ): TableColumnWrapper<E, P> {
	return column(getter.name) {
	  BindableProperty(getter.call(it.value))
	}.apply(op)
  }


  fun <P> column(
	getter: KProperty1<E, P>,
	op: TableColumnWrapper<E, P>.()->Unit = {}
  ): TableColumnWrapper<E, P> {
	return column(getter.name) {
	  BindableProperty(getter.call(it.value))
	}.apply(op)
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
	  setCellValueFactory { ReadOnlyObjectWrapper(items!!.indexOf(it.value) + startNumber) }
	}
  }


  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }


  fun selectOnDrag() {
	var startRow = 0
	var startColumn = columns.first()

	// Record start position and clear selection unless Control is down
	addEventFilter(MouseEvent.MOUSE_PRESSED) {
	  startRow = 0

	  (it.pickResult.intersectedNode as? TableCell<*, *>)?.apply {
		startRow = index
		@Suppress("UNCHECKED_CAST")
		startColumn = tableColumn as TableColumn<E, *>?

		if (selectionModel.isCellSelectionEnabled) {
		  selectionModel.clearAndSelect(startRow, startColumn.wrapped())
		} else {
		  selectionModel.clearAndSelect(startRow)
		}
	  }
	}

	// Select items while dragging
	addEventFilter(MouseEvent.MOUSE_DRAGGED) {
	  (it.pickResult.intersectedNode as? TableCell<*, *>)?.apply {
		if (items!!.size > index) {
		  if (selectionModel.isCellSelectionEnabled) {
			@Suppress("UNCHECKED_CAST")
			selectionModel.selectRange(
			  startRow, startColumn.wrapped(), index, (tableColumn as TableColumn<E, *>).wrapped()
			)
		  } else {
			selectionModel.selectRange(startRow, index)
		  }
		}
	  }
	}
  }


  //  call the method after inserting the data into table
  fun autoResizeColumns() {
	columnResizePolicy = TableView.UNCONSTRAINED_RESIZE_POLICY
	columns.forEach { column ->
	  column.setPrefWidth(
		(((0 until items!!.size).mapNotNull {
		  column.getCellData(it)
		}.map {
		  it.toString().fxWidth
		}.toMutableList() + listOf(
		  column.text.fxWidth
		)).maxOrNull() ?: 0.0) + 10.0
	  )
	}
  }


  fun bindSelected(property: VarProp<E?>) {
	selectionModel.selectedItemProperty.onChange {
	  property.value = it
	}
  }

  fun selectWhere(scrollTo: Boolean = true, condition: (E)->Boolean) {
	items!!.asSequence().filter(condition).forEach {
	  selectionModel.select(it)
	  if (scrollTo) scrollTo(it)
	}
  }


  fun moveToTopWhere(
	backingList: MutableObsList<E> = items!!,
	select: Boolean = true,
	predicate: (E)->Boolean
  ) {
	if (select) selectionModel.clearSelection()
	backingList.filter(predicate).forEach {
	  backingList.remove(it)
	  backingList.add(0, it)
	  if (select) selectionModel.select(it)
	}
  }


  fun moveToBottomWhere(
	backingList: MutableObsList<E> = items!!,
	select: Boolean = true,
	predicate: (E)->Boolean
  ) {
	val end = backingList.size - 1
	if (select) selectionModel.clearSelection()
	backingList.filter(predicate).forEach {
	  backingList.remove(it)
	  backingList.add(end, it)
	  if (select) selectionModel.select(it)

	}
  }

  /**
   * Execute action when the enter key is pressed or the mouse is clicked

   * @param clickCount The number of mouse clicks to trigger the action
   * *
   * @param action The action to execute on select
   */
  fun onUserSelect(clickCount: Int = 2, action: (E)->Unit) {
	val isSelected = { event: InputEvent ->
	  event.target.wrapped().isInsideRow() && !selectionModel.selectionIsEmpty()
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


  fun onUserDelete(action: (E)->Unit) {
	addEventFilter(KeyEvent.KEY_PRESSED) { event ->
	  if (event.code == KeyCode.BACK_SPACE && selectedItem != null)
		action(selectedItem!!)
	}
  }


  fun regainFocusAfterEdit() = apply {
	editingCellProperty.onChange {
	  if (it == null)
		requestFocus()
	}
  }


  fun editableWhen(predicate: ObsB) = apply {
	editableProperty.bind(predicate)
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

	columns.addListener { change: ListChangeListener.Change<out TableColumn<E, *>> ->
	  while (change.next()) {
		if (change.wasAdded())
		  change.addedSubList.forEach(::addEventHandlerForColumn)
	  }
	}
  }


  fun enableCellEditing() {
	selectionModel.isCellSelectionEnabled = true
	isEditable = true
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


}






