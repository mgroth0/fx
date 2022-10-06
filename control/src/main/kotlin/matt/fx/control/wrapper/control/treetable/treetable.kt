package matt.fx.control.wrapper.control.treetable

import javafx.beans.property.BooleanProperty
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.scene.control.TableColumn
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.TreeTablePosition
import javafx.scene.control.TreeTableRow
import javafx.scene.control.TreeTableView
import javafx.scene.control.TreeTableView.ResizeFeatures
import javafx.scene.control.TreeTableView.TreeTableViewSelectionModel
import javafx.scene.control.cell.TreeItemPropertyValueFactory
import javafx.scene.input.InputEvent
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.util.Callback
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.tablelike.TableLikeWrapper
import matt.fx.control.wrapper.control.tree.like.TreeLikeWrapper
import matt.fx.control.wrapper.control.tree.like.populateTree
import matt.fx.control.wrapper.control.treecol.TreeTableColumnWrapper
import matt.fx.control.wrapper.selects.wrap
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.eye.prop.observable
import matt.prim.str.decap
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

val <T> TreeTableViewWrapper<T>.selectedCell: TreeTablePosition<T, *>?
  get() = selectionModel.selectedCells.firstOrNull()

val <T> TreeTableViewWrapper<T>.selectedColumn: TreeTableColumn<T, *>?
  get() = selectedCell?.tableColumn

//val <T> TreeTableViewWrapper<T>.selectedValue: Any?
//  get() = selectedColumn?.getCellObservableValue(selectionModel.selectedItem)?.value

fun <T> ET.treetableview(root: TreeItem<T>? = null, op: TreeTableViewWrapper<T>.()->Unit = {}) =
  TreeTableViewWrapper<T>().attachTo(this, op) {
	if (root != null) it.root = root
  }

class TreeTableViewWrapper<E>(
  node: TreeTableView<E> = TreeTableView(),
): ControlWrapperImpl<TreeTableView<E>>(node),
   TreeLikeWrapper<TreeTableView<E>, E>,
   TableLikeWrapper<TreeItem<E>> {
  override fun isInsideRow() = true

  fun editableProperty(): BooleanProperty = node.editableProperty()

  val sortOrder: ObservableList<TreeTableColumn<E, *>> get() = node.sortOrder

  override var root: TreeItem<E>
	get() = node.root
	set(value) {
	  node.root = value
	}
  override var isShowRoot: Boolean
	get() = node.isShowRoot
	set(value) {
	  node.isShowRoot = value
	}

  var columnResizePolicy: Callback<ResizeFeatures<*>, Boolean>
	get() = node.columnResizePolicy
	set(value) {
	  node.columnResizePolicy = value
	}


  override val columns: ObservableList<TreeTableColumn<E, *>> get() = node.columns


  override fun getRow(ti: TreeItem<E>) = node.getRow(ti)

  override val selectionModel by lazy { node.selectionModel.wrap() }
  override fun scrollTo(i: Int) = node.scrollTo(i)
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }

  fun setRowFactory(value: Callback<TreeTableView<E>, TreeTableRow<E>>) = node.setRowFactory(value)

  fun sort() = node.sort()

  inline fun <reified P> column(
	title: String,
	prop: KMutableProperty1<E, P>,
	noinline op: TreeTableColumnWrapper<E, P>.()->Unit = {}
  ): TreeTableColumnWrapper<E, P> {
	val column = TreeTableColumnWrapper<E, P>(title)
	column.cellValueFactory = Callback {
	  it.value.value?.let {
		observable(
		  it, prop
		)
	  }
	} /*Matt: added null safety here way later because I ran into a NPE here... thought I went years without this null safety first so maybe the null was my fault?*/
	addColumnInternal(column)
	return column.also(op)
  }


  inline fun <reified P> column(
	title: String,
	prop: KProperty1<E, P>,
	noinline op: TreeTableColumnWrapper<E, P>.()->Unit = {}
  ): TreeTableColumnWrapper<E, P> {
	val column = TreeTableColumnWrapper<E, P>(title)
	column.cellValueFactory = Callback {
	  it.value.value?.let {
		observable(
		  it, prop
		)
	  }
	} /*Matt: added null safety here way later because I ran into a NPE here... thought I went years without this null safety first so maybe the null was my fault?*/
	addColumnInternal(column)
	return column.also(op)
  }


  @JvmName(name = "columnForObservableProperty")
  fun <P> column(
	title: String,
	prop: KProperty1<E, ObservableValue<P>>
  ): TreeTableColumnWrapper<E, P> {
	val column = TreeTableColumnWrapper<E, P>(title)
	column.cellValueFactory = Callback { prop.call(it.value.value) }
	addColumnInternal(column)
	return column
  }


  inline fun <reified P> column(
	title: String,
	observableFn: KFunction<ObservableValue<P>>
  ): TreeTableColumnWrapper<E, P> {
	val column = TreeTableColumnWrapper<E, P>(title)
	column.cellValueFactory = Callback { observableFn.call(it.value) }
	addColumnInternal(column)
	return column
  }


  /**
   * Create a matt.hurricanefx.tableview.coolColumn with a value factory that extracts the value from the given callback.
   */
  fun <P> column(
	title: String,
	valueProvider: (TreeTableColumn.CellDataFeatures<E, P>)->ObservableValue<P>
  ): TreeTableColumnWrapper<E, P> {
	val column = TreeTableColumnWrapper<E, P>(title)
	column.cellValueFactory = Callback { valueProvider(it) }
	addColumnInternal(column)
	return column
  }


  /**
   * Create a matt.hurricanefx.tableview.coolColumn holding matt.fx.control.layout.children columns
   */
  @Suppress("UNCHECKED_CAST")
  fun nestedColumn(
	title: String,
	op: TreeTableViewWrapper<E>.()->Unit = {}
  ): TreeTableColumnWrapper<E, Any?> {
	val column = TreeTableColumnWrapper<E, Any?>(title)
	addColumnInternal(column)
	val previousColumnTarget = node.properties["tornadofx.columnTarget"] as? ObservableList<TableColumn<E, *>>
	node.properties["tornadofx.columnTarget"] = column.columns
	op(this)
	node.properties["tornadofx.columnTarget"] = previousColumnTarget
	return column
  }


  /**
   * Create a matt.hurricanefx.tableview.coolColumn using the propertyName of the attribute you want shown.
   */
  fun <P> column(
	title: String,
	propertyName: String,
	op: TreeTableColumnWrapper<E, P>.()->Unit = {}
  ): TreeTableColumnWrapper<E, P> {
	val column = TreeTableColumnWrapper<E, P>(title)
	column.cellValueFactory = TreeItemPropertyValueFactory<E, P>(propertyName)
	addColumnInternal(column)
	return column.also(op)
  }


  /**
   * Create a matt.hurricanefx.tableview.coolColumn using the getter of the attribute you want shown.
   */
  @JvmName("pojoColumn")
  fun <P> column(title: String, getter: KFunction<P>): TreeTableColumnWrapper<E, P> {
	val startIndex = if (getter.name.startsWith("is") && getter.name[2].isUpperCase()) 2 else 3
	val propName = getter.name.substring(startIndex).decap()
	return this.column(title, propName)
  }


  fun <P> addColumnInternal(column: TreeTableColumnWrapper<E, P>, index: Int? = null) {
	@Suppress("UNCHECKED_CAST")
	val columnTarget = node.properties["tornadofx.columnTarget"] as? ObservableList<TreeTableColumn<E, *>> ?: columns
	if (index == null) columnTarget.add(column.node) else columnTarget.add(index, column.node)
  }


  /**
   * Matt was here!
   */
  @JvmName("coolColumn")
  inline fun <P> column(
	getter: KFunction<P>,
	op: TreeTableColumnWrapper<E, P>.()->Unit = {}
  ): TreeTableColumnWrapper<E, P> {
	return column(getter.name) {
	  SimpleObjectProperty(getter.call(it.value.value))
	}.apply(op)
  }


  /**
   * Matt was here!
   */
  @JvmName("coolColumn2")
  inline fun <P> column(
	getter: KProperty1<E, P>,
	op: TreeTableColumnWrapper<E, P>.()->Unit = {}
  ): TreeTableColumnWrapper<E, P> {
	return column(getter.name) {
	  SimpleObjectProperty(getter.call(it.value.value))
	}.apply(op)
  }

}

fun <T> TreeTableViewWrapper<T>.selectFirst() = selectionModel.selectFirst()


fun <T> TreeTableViewWrapper<T>.bindSelected(property: Property<T>) {
  selectionModel.selectedItemProperty.onChange {
	property.value = it?.value
  }
}


/**
 * Execute action when the enter key is pressed or the mouse is clicked

 * @param clickCount The number of mouse clicks to trigger the action
 * *
 * @param action The action to execute on select
 */
fun <T> TreeTableViewWrapper<T>.onUserSelect(clickCount: Int = 2, action: (T)->Unit) {
  val isSelected = { event: InputEvent ->
	event.target.wrapped().isInsideRow() && !selectionModel.isEmpty
  }

  addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
	if (event.clickCount == clickCount && isSelected(event))
	  action(selectedItem!!.value)
  }

  addEventFilter(KeyEvent.KEY_PRESSED) { event ->
	if (event.code == KeyCode.ENTER && !event.isMetaDown && isSelected(event))
	  action(selectedItem!!.value)
  }
}


fun <T> TreeTableViewWrapper<T>.populate(
  itemFactory: (T)->TreeItem<T> = { TreeItem(it) },
  childFactory: (TreeItem<T>)->Iterable<T>?
) =
  populateTree(root, itemFactory, childFactory)


fun TreeTableViewWrapper<*>.editableWhen(predicate: ObservableValue<Boolean>) = apply {
  editableProperty().bind(predicate)
}