@file:Suppress("UNCHECKED_CAST")

/*slightly modified code I stole from tornadofx*/

package matt.fx.control.tfx.item

import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.TreeTablePosition
import javafx.scene.input.MouseEvent
import matt.hurricanefx.eye.collect.asObservable
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.eye.mtofx.createWritableFXPropWrapper
import matt.hurricanefx.eye.wrapper.obs.collect.createFXWrapper
import matt.hurricanefx.wrapper.control.choice.ChoiceBoxWrapper
import matt.hurricanefx.wrapper.control.choice.bind
import matt.hurricanefx.wrapper.control.combo.ComboBoxWrapper
import matt.hurricanefx.wrapper.control.combo.bind
import matt.hurricanefx.wrapper.control.list.ListViewWrapper
import matt.hurricanefx.wrapper.control.spinner.SpinnerWrapper
import matt.hurricanefx.wrapper.control.table.TableViewWrapper
import matt.hurricanefx.wrapper.control.tree.TreeViewWrapper
import matt.hurricanefx.wrapper.control.treetable.TreeTableViewWrapper
import matt.hurricanefx.wrapper.node.attachTo
import matt.hurricanefx.wrapper.target.EventTargetWrapper
import matt.hurricanefx.wrapper.target.EventTargetWrapperImpl
import matt.lang.err
import matt.obs.col.olist.ObsList
import matt.obs.prop.BindableProperty
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

/**
 * Create a spinner for an arbitrary type. This spinner requires you to configure a value factory, or it will throw an exception.
 */
fun <T> EventTargetWrapper.spinner(
  editable: Boolean = false,
  property: Property<T>? = null,
  enableScroll: Boolean = false,
  op: SpinnerWrapper<T>.()->Unit = {}
) = SpinnerWrapper<T>().also {
  it.isEditable = editable
  it.attachTo(this, op)

  if (property != null) requireNotNull(it.valueFactory) {
	"You must configure the value factory or use the Number based spinner builder " +
		"which configures a default value factory along with min, max and initialValue!"
  }.valueProperty().apply {
	bindBidirectional(property)
  }

  if (enableScroll) it.setOnScroll { event ->
	if (event.deltaY > 0) it.increment()
	if (event.deltaY < 0) it.decrement()
  }

  if (editable) it.focusedProperty().addListener { _, _, newValue: Boolean? ->
	if (newValue == null) err("here it is")
	if (!newValue) it.increment(0)
  }
}

inline fun <reified T: Number> EventTargetWrapperImpl<*>.spinner(
  min: T? = null,
  max: T? = null,
  initialValue: T? = null,
  amountToStepBy: T? = null,
  editable: Boolean = false,
  property: BindableProperty<T>? = null,
  enableScroll: Boolean = false,
  noinline op: SpinnerWrapper<T>.()->Unit = {}
): SpinnerWrapper<T> {
  val spinner: SpinnerWrapper<T>


  /*property is IntegerProperty && property !is DoubleProperty && property !is FloatProperty) ||*/
  val isInt = min is Int || max is Int || initialValue is Int ||
	  T::class == Int::class || T::class == Integer::class || T::class.javaPrimitiveType == Integer::class.java
  if (isInt) {
	spinner = SpinnerWrapper(
	  min?.toInt() ?: 0,
	  max?.toInt() ?: 100,
	  initialValue?.toInt() ?: 0,
	  amountToStepBy?.toInt() ?: 1
	)
  } else {
	spinner = SpinnerWrapper(
	  min?.toDouble() ?: 0.0, max?.toDouble() ?: 100.0, initialValue?.toDouble()
		?: 0.0, amountToStepBy?.toDouble() ?: 1.0
	)
  }
  if (property != null) {
	spinner.valueFactory!!.valueProperty().bindBidirectional(property.createWritableFXPropWrapper())
  }
  spinner.isEditable = editable

  if (enableScroll) {
	spinner.setOnScroll { event ->
	  if (event.deltaY > 0) spinner.increment()
	  if (event.deltaY < 0) spinner.decrement()
	}
  }

  if (editable) {
	spinner.focusedProperty().addListener { _, _, newValue: Boolean? ->
	  if (newValue == null) err("here it is")
	  if (!newValue) {
		spinner.increment(0)
	  }
	}
  }

  return spinner.attachTo(this, op)
}

fun <T> EventTargetWrapperImpl<*>.spinner(
  items: ObservableList<T>,
  editable: Boolean = false,
  property: Property<T>? = null,
  enableScroll: Boolean = false,
  op: SpinnerWrapper<T>.()->Unit = {}
) = SpinnerWrapper<T>(items).attachTo(this, op) {
  if (property != null) it.valueFactory!!.valueProperty().apply {
	bindBidirectional(property)
  }

  it.isEditable = editable

  if (enableScroll) it.setOnScroll { event ->
	if (event.deltaY > 0) it.increment()
	if (event.deltaY < 0) it.decrement()
  }

  if (editable) it.focusedProperty().addListener { _, _, newValue: Boolean? ->
	if (newValue == null) err("here it is")
	if (!newValue) it.increment(0)
  }
}

fun <T> EventTargetWrapperImpl<*>.spinner(
  valueFactory: SpinnerValueFactory<T>,
  editable: Boolean = false,
  property: Property<T>? = null,
  enableScroll: Boolean = false,
  op: SpinnerWrapper<T>.()->Unit = {}
) = SpinnerWrapper<T>(valueFactory).attachTo(this, op) {
  if (property != null) it.valueFactory!!.valueProperty().apply {
	bindBidirectional(property)
  }

  it.isEditable = editable

  if (enableScroll) it.setOnScroll { event ->
	if (event.deltaY > 0) it.increment()
	if (event.deltaY < 0) it.decrement()
  }

  if (editable) it.focusedProperty().addListener { _, _, newValue: Boolean? ->
	if (newValue == null) err("here it is")
	if (!newValue) it.increment(0)
  }
}

fun <T> EventTargetWrapperImpl<*>.combobox(
  property: Property<T>? = null,
  values: List<T>? = null,
  op: ComboBoxWrapper<T>.()->Unit = {}
) =
  ComboBoxWrapper<T>().attachTo(this, op) {
	if (values != null) it.items = values as? ObservableList<T> ?: values.asObservable()
	if (property != null) it.bind(property)
  }


inline fun <T> EventTargetWrapperImpl<*>.choicebox(
  property: BindableProperty<T?>? = null,
  values: List<T>? = null,
  op: ChoiceBoxWrapper<T>.()->Unit = {}
): ChoiceBoxWrapper<T> {
  contract {
	callsInPlace(op, EXACTLY_ONCE)
  }
  return ChoiceBoxWrapper<T>().attachTo(this, op) {
	if (values != null) it.items = (values as? ObservableList<T>) ?: values.asObservable()
	if (property != null) it.bind(property)
  }
}

inline fun <T> EventTargetWrapperImpl<*>.choicebox(
  property: BindableProperty<T?>? = null,
  values: Array<T>? = null,
  op: ChoiceBoxWrapper<T>.()->Unit = {}
) = choicebox(property, values?.toList(), op)


inline fun <T> choicebox(
  property: BindableProperty<T>? = null,
  values: List<T>? = null,
  op: ChoiceBoxWrapper<T>.()->Unit = {}
): ChoiceBoxWrapper<T> {
  contract {
	callsInPlace(op, EXACTLY_ONCE)
  }
  return ChoiceBoxWrapper<T>().also {
	it.op()
	if (values != null) it.items = (values as? ObservableList<T>) ?: values.asObservable()
	if (property != null) it.bind(property)
  }
}


fun <T> EventTargetWrapperImpl<*>.listview(values: ObservableList<T>? = null, op: ListViewWrapper<T>.()->Unit = {}) =
  ListViewWrapper<T>().attachTo(this, op) {
	if (values != null) {
	  it.items = values
	}
  }

fun <T> EventTargetWrapperImpl<*>.listview(values: ReadOnlyListProperty<T>, op: ListViewWrapper<T>.()->Unit = {}) =
  listview(values as ObservableValue<ObservableList<T>>, op)

fun <T> EventTargetWrapperImpl<*>.listview(
  values: ObservableValue<ObservableList<T>>,
  op: ListViewWrapper<T>.()->Unit = {}
) =
  ListViewWrapper<T>().attachTo(this, op) {
	it.itemsProperty().bind(values)
  }

fun <T> EventTargetWrapperImpl<*>.tableview(items: ObsList<T>? = null, op: TableViewWrapper<T>.()->Unit = {}) =
  TableViewWrapper<T>().attachTo(this, op) {
	if (items != null) {
	  it.items = items.createFXWrapper()
	}
  }

fun <T> EventTargetWrapperImpl<*>.tableview(items: ReadOnlyListProperty<T>, op: TableViewWrapper<T>.()->Unit = {}) =
  tableview(items as ObservableValue<ObservableList<T>>, op)

fun <T> EventTargetWrapperImpl<*>.tableview(
  items: ObservableValue<out ObservableList<T>>,
  op: TableViewWrapper<T>.()->Unit = {}
) =
  TableViewWrapper<T>().attachTo(this, op) {
	it.itemsProperty().bind(items)
  }

fun <T> EventTargetWrapperImpl<*>.treeview(root: TreeItem<T>? = null, op: TreeViewWrapper<T>.()->Unit = {}) =
  TreeViewWrapper<T>().attachTo(this, op) {
	if (root != null) it.root = root
  }

fun <T> EventTargetWrapperImpl<*>.treetableview(root: TreeItem<T>? = null, op: TreeTableViewWrapper<T>.()->Unit = {}) =
  TreeTableViewWrapper<T>().attachTo(this, op) {
	if (root != null) it.root = root
  }

fun <T> TreeItem<T>.treeitem(value: T? = null, op: TreeItem<T>.()->Unit = {}): TreeItem<T> {
  val treeItem = value?.let { TreeItem<T>(it) } ?: TreeItem<T>()
  treeItem.op()
  this += treeItem
  return treeItem
}

operator fun <T> TreeItem<T>.plusAssign(treeItem: TreeItem<T>) {
  this.children.add(treeItem)
}


fun <T> TableViewWrapper<T>.bindSelected(property: Property<T>) {
  selectionModel.selectedItemProperty().onChange {
	property.value = it
  }
}

fun <T> ComboBoxWrapper<T>.bindSelected(property: Property<T>) {
  selectionModel.selectedItemProperty().onChange {
	property.value = it
  }
}


val <T> TreeTableViewWrapper<T>.selectedCell: TreeTablePosition<T, *>?
  get() = selectionModel.selectedCells.firstOrNull()

val <T> TreeTableViewWrapper<T>.selectedColumn: TreeTableColumn<T, *>?
  get() = selectedCell?.tableColumn

//val <T> TreeTableViewWrapper<T>.selectedValue: Any?
//  get() = selectedColumn?.getCellObservableValue(selectionModel.selectedItem)?.value


fun <T> TableViewWrapper<T>.selectOnDrag() {
  var startRow = 0
  var startColumn = columns.first()

  // Record start position and clear selection unless Control is down
  addEventFilter(MouseEvent.MOUSE_PRESSED) {
	startRow = 0

	(it.pickResult.intersectedNode as? TableCell<*, *>)?.apply {
	  startRow = index
	  startColumn = tableColumn as TableColumn<T, *>?

	  if (selectionModel.isCellSelectionEnabled) {
		selectionModel.clearAndSelect(startRow, startColumn)
	  } else {
		selectionModel.clearAndSelect(startRow)
	  }
	}
  }

  // Select items while dragging
  addEventFilter(MouseEvent.MOUSE_DRAGGED) {
	(it.pickResult.intersectedNode as? TableCell<*, *>)?.apply {
	  if (items.size > index) {
		if (selectionModel.isCellSelectionEnabled) {
		  selectionModel.selectRange(startRow, startColumn, index, tableColumn as TableColumn<T, *>?)
		} else {
		  selectionModel.selectRange(startRow, index)
		}
	  }
	}
  }
}

