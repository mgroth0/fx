package matt.fx.control.wrapper.selects

import javafx.scene.control.MultipleSelectionModel
import javafx.scene.control.SelectionMode
import javafx.scene.control.SelectionModel
import javafx.scene.control.TableSelectionModel
import javafx.scene.control.TableView.TableViewSelectionModel
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableView.TreeTableViewSelectionModel
import matt.fx.control.wrapper.control.ControlWrapper
import matt.fx.control.wrapper.control.colbase.TableColumnBaseWrapper
import matt.fx.control.wrapper.control.column.TableColumnWrapper
import matt.hurricanefx.eye.wrapper.obs.collect.createImmutableWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNonNullableROProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNullableROProp
import matt.model.convert.Converter
import matt.obs.bind.binding
import matt.obs.col.olist.ObsList
import matt.obs.col.olist.mappedlist.toMappedList
import matt.obs.prop.ObsVal
import matt.obs.prop.Var

interface SelectionControls<T: Any> {
  fun selectNext()
  fun selectFirst()
  fun selectLast()
  fun selectionIsEmpty(): Boolean
  fun selectPrevious()
  fun clearAndSelect(index: Int)
  fun selectIndex(index: Int)
  fun clearSelection(index: Int)
  fun clearSelection()
  fun isSelected(index: Int): Boolean
  val selectedIndexProperty: ObsVal<Int?>
  val selectedIndex: Int?
  fun select(obj: T?)
  val selectedItemProperty: ObsVal<T?>
  fun setOnSelectionChange(listener: (T?)->Unit) = selectedItemProperty.onChange(listener)
  fun onSelect(op: (T?)->Unit) {
	selectedItemProperty.onChange {
	  op(it)
	}
  }

  val selectedItem: T? get() = selectedItemProperty.value
}

interface Selects<T: Any>: SelectionControls<T> {
  val selectionModel: SelectionControls<T>
  override val selectedItem: T? get() = selectionModel.selectedItem
  override fun selectNext() = selectionModel.selectNext()
  override fun selectFirst() = selectionModel.selectFirst()
  override fun selectLast() = selectionModel.selectLast()
  override fun selectionIsEmpty(): Boolean = selectionModel.selectionIsEmpty()
  override fun selectPrevious() = selectionModel.selectPrevious()
  override fun clearAndSelect(index: Int) = selectionModel.clearAndSelect(index)
  override fun selectIndex(index: Int) = selectionModel.selectIndex(index)
  override fun clearSelection(index: Int) = selectionModel.clearSelection(index)
  override fun clearSelection() = selectionModel.clearSelection()
  override fun isSelected(index: Int): Boolean = selectionModel.isSelected(index)
  override val selectedIndex: Int? get() = selectionModel.selectedIndex
  override fun select(obj: T?) = selectionModel.select(obj)
  override val selectedItemProperty: ObsVal<T?> get() = selectionModel.selectedItemProperty
  override val selectedIndexProperty: ObsVal<Int?> get() = selectionModel.selectedIndexProperty
}

interface MultiSelects<T: Any>: Selects<T>, MultiSelectControls<T> {
  override val selectionModel: MultiSelectControls<T>
  override val selectionModeProperty get() = selectionModel.selectionModeProperty
  override fun selectedIndices() = selectionModel.selectedIndices()
  override fun selectIndices(index: Int, vararg indices: Int) = selectionModel.selectIndices(index, *indices)
  override fun selectRange(start: Int, end: Int) = selectionModel.selectRange(start, end)
  override fun selectAll() = selectionModel.selectAll()
  override val selectedItems get() = selectionModel.selectedItems
}


interface SelectingControl<T: Any>: Selects<T>, ControlWrapper


typealias SelectModWrap<T> = SelectionModelWrapperBase<*, T>

abstract class SelectionModelWrapperBase<T: Any, W: Any>(
  protected open val sm: SelectionModel<T>
): SelectionControls<W> {
  override fun selectNext() = sm.selectNext()
  override fun selectFirst() = sm.selectFirst()
  override fun selectLast() = sm.selectLast()
  override fun selectionIsEmpty() = sm.isEmpty
  override fun selectPrevious() = sm.selectPrevious()
  override fun clearAndSelect(index: Int) = sm.clearAndSelect(index)
  override fun selectIndex(index: Int) = sm.select(index)
  override fun clearSelection(index: Int) = sm.clearSelection(index)
  override fun clearSelection() = sm.clearSelection()
  override fun isSelected(index: Int) = sm.isSelected(index)
  override val selectedIndex: Int? get() = selectedIndexProperty.value
  override val selectedIndexProperty by lazy {
	sm.selectedIndexProperty().toNonNullableROProp().cast<Int>().binding {
	  it.takeIf { it >= 0 }
	}
  }
}

fun <T: Any> SelectionModel<T>.wrap() = SelectionModelWrapperImpl(this)
open class SelectionModelWrapperImpl<T: Any>(sm: SelectionModel<T>): SelectionModelWrapperBase<T, T>(sm) {
  override fun select(obj: T?) = sm.select(obj) /*what if T is Int? Would the index be selected? weird...*/
  override val selectedItemProperty by lazy {
	sm.selectedItemProperty().toNullableROProp()
  }
}

fun <T: Any, W: Any> SelectionModel<T>.wrap(converter: Converter<T, W>) = SelectionModelProxy(this, converter)
class SelectionModelProxy<T: Any, W: Any>(
  sm: SelectionModel<T>, private val converter: Converter<T, W>
): SelectionModelWrapperBase<T, W>(sm) {
  override fun select(obj: W?) = sm.select(obj?.let(converter::convertToA))
  override val selectedItemProperty by lazy {
	sm.selectedItemProperty().toNullableROProp().binding { it?.let(converter::convertToB) }
  }
}


interface MultiSelectControls<W: Any>: SelectionControls<W> {
  val selectionModeProperty: Var<SelectionMode>
  var selectionMode
	get() = selectionModeProperty.value
	set(value) {
	  selectionModeProperty.value = value
	}

  fun selectedIndices(): ObsList<Int>
  fun selectIndices(index: Int, vararg indices: Int)
  fun selectRange(start: Int, end: Int)
  fun selectAll()
  val selectedItems: ObsList<W>
}

typealias MultiSelectWrap<T> = MultipleSelectionModelWrapperBase<*, T>

abstract class MultipleSelectionModelWrapperBase<T: Any, W: Any>(
  protected open val sm: MultipleSelectionModel<T>
): MultiSelectControls<W> {
  override val selectionModeProperty by lazy { sm.selectionModeProperty().toNonNullableProp() }
  override fun selectedIndices() = sm.selectedIndices.createImmutableWrapper()
  override fun selectIndices(index: Int, vararg indices: Int) = sm.selectIndices(index, *indices)
  override fun selectRange(start: Int, end: Int) = sm.selectRange(start, end)
  override fun selectAll() = sm.selectAll()
  abstract override val selectedItems: ObsList<W>
}

fun <T: Any> MultipleSelectionModel<T>.wrap() = MultipleSelectionModelWrapperImpl(this)
open class MultipleSelectionModelWrapperImpl<T: Any>(
  sm: MultipleSelectionModel<T>
): MultipleSelectionModelWrapperBase<T, T>(sm), SelectionControls<T> by SelectionModelWrapperImpl(sm) {
  override val selectedItems by lazy { sm.selectedItems.createImmutableWrapper() }
}

fun <T: Any, W: Any> MultipleSelectionModel<T>.wrap(converter: Converter<T, W>) =
  MultipleSelectionModelWrapperProxy(this, converter)

class MultipleSelectionModelWrapperProxy<T: Any, W: Any>(
  sm: MultipleSelectionModel<T>, private val converter: Converter<T, W>
): MultipleSelectionModelWrapperBase<T, W>(sm), SelectionControls<W> by SelectionModelProxy<T, W>(sm, converter) {
  override val selectedItems by lazy {
	sm.selectedItems.createImmutableWrapper().toMappedList {
	  converter.convertToB(it)
	}
  }
}

/*fun <T: Any> TableSelectionModel<T>.wrap() = TableSelectionModelWrapper<T>(this)*/

abstract class TableSelectionModelWrapper<T: Any>(
  override val sm: TableSelectionModel<T>
): MultipleSelectionModelWrapperImpl<T>(sm) {
  var isCellSelectionEnabled
	get() = sm.isCellSelectionEnabled
	set(value) {
	  sm.isCellSelectionEnabled = value
	}

  fun clearAndSelect(row: Int, col: TableColumnWrapper<T, *>) = sm.clearAndSelect(row, col.node)
  fun selectRange(
	min: Int,
	minCol: TableColumnBaseWrapper<T, *, *>,
	max: Int,
	maxCol: TableColumnBaseWrapper<T, *, *>
  ) = sm.selectRange(min, minCol.node, max, maxCol.node)
}

fun <T: Any> TableViewSelectionModel<T>.wrap() = TableViewSelectionModelWrapper(this)

open class TableViewSelectionModelWrapper<T: Any>(
  override val sm: TableViewSelectionModel<T>
): TableSelectionModelWrapper<T>(sm) {
  val selectedCells get() = sm.selectedCells
}

fun <T: Any> TreeTableViewSelectionModel<T>.wrap() = TreeTableViewSelectionModelWrapper(this)

class TreeTableViewSelectionModelWrapper<T: Any>(
  override val sm: TreeTableViewSelectionModel<T>
): TableSelectionModelWrapper<TreeItem<T>>(sm) {
  val selectedCells get() = sm.selectedCells
}