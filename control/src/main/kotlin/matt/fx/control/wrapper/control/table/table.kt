package matt.fx.control.wrapper.control.table

import javafx.application.Platform
import javafx.application.Platform.runLater
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TablePosition
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.input.InputEvent
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.util.Callback
import matt.async.thread.daemon
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.table.cols.ColumnsDSL
import matt.fx.control.wrapper.control.table.cols.ColumnsDSLImpl
import matt.fx.control.wrapper.control.tablelike.TableLikeWrapper
import matt.fx.control.wrapper.selects.wrap
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.fxWidth
import matt.fx.graphics.service.uncheckedWrapperConverter
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.wrapper.obs.collect.list.createMutableWrapper
import matt.hurricanefx.eye.wrapper.obs.collect.list.mfxMutableListConverter
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNullableROProp
import matt.lang.function.Op
import matt.lang.go
import matt.lang.setall.setAll
import matt.obs.bind.binding
import matt.obs.bindings.bool.ObsB
import matt.obs.col.olist.ImmutableObsList
import matt.obs.col.olist.MutableObsList
import matt.obs.col.olist.sync.toSyncedList
import matt.obs.col.olist.toMutableObsList
import matt.obs.prop.ObsVal
import matt.obs.prop.VarProp
import matt.time.dur.sleep
import kotlin.time.Duration.Companion.milliseconds

fun <T: Any> ET.tableview(items: ImmutableObsList<T>? = null, op: TableViewWrapper<T>.()->Unit = {}) =
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
): ControlWrapperImpl<TableView<E>>(node), TableLikeWrapper<E>, ColumnsDSL<E> by ColumnsDSLImpl<E>(node.columns) {

  constructor(items: ObservableList<E>): this(TableView(items))

  fun setRowFactory(value: Callback<TableView<E>, TableRow<E>>) = node.setRowFactory(value)


  override fun isInsideRow() = true

  val editableProperty by lazy { node.editableProperty().toNonNullableProp() }

  var isEditable by editableProperty

  fun refresh() = node.refresh()


  val sortOrder by lazy {
	node.sortOrder.createMutableWrapper().toSyncedList(
	  uncheckedWrapperConverter()
	)
  }

  val columnResizePolicyProperty by lazy { node.columnResizePolicyProperty().toNonNullableProp() }
  var columnResizePolicy by columnResizePolicyProperty


  val itemsProperty by lazy { node.itemsProperty().toNullableProp().proxy(mfxMutableListConverter<E>().nullable()) }
  var items by itemsProperty

  val comparatorProperty by lazy { node.comparatorProperty().toNullableROProp() }
  val comparator by comparatorProperty

  override val columns: ObservableList<TableColumn<E, *>> get() = node.columns

  override val selectionModel by lazy { node.selectionModel.wrap() }
  fun scrollTo(i: Int) = node.scrollTo(i)
  fun scrollToWithWeirdDirtyFix(
	i: Int,
	recursionLevel: Int = 5,
	sleepTime: kotlin.time.Duration = 100.milliseconds,
	callback: Op = {}
  ) {
	scrollTo(i) /*scrollTo is not working well... hope this helps*/
	if (recursionLevel > 0) {
	  daemon {
		sleep(sleepTime)
		runLater {
		  scrollToWithWeirdDirtyFix(i, recursionLevel = recursionLevel - 1, sleepTime = sleepTime, callback = callback)
		}
	  }
	} else {
	  callback()
	}
  }

  fun scrollTo(e: E) = node.scrollTo(e)
  val focusModel get() = node.focusModel
  fun sort() = node.sort()
  val editingCellProperty by lazy { node.editingCellProperty().toNullableROProp() }
  fun edit(row: Int, col: TableColumn<E, *>) = node.edit(row, col)


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
	columns.associateWith { column ->

	  val dataList = (0 ..< items!!.size).map {
		column.getCellData(it)
	  }
	  if (dataList.any { it is NW }) {
		null /*prevent resizing of nodeColumn which is managed separately. Trying to resize those here leads to issues because getCellData() returns a different node then the one being displayed*/
	  } else {
		val textWidths = dataList.mapNotNull {
		  it?.toString()?.fxWidth ?: 0.0
		}.toTypedArray()

		val widths = listOf(
		  *textWidths,
		  column.text.fxWidth
		)
		val bareMinW = widths.maxOrNull() ?: 0.0
		bareMinW + 10.0
	  }

	}.forEach { (column, w) ->
	  w?.go(column::setPrefWidth)
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



