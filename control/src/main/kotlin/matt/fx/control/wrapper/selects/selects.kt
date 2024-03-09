package matt.fx.control.wrapper.selects

import javafx.collections.ObservableList
import javafx.scene.control.MultipleSelectionModel
import javafx.scene.control.SelectionMode
import javafx.scene.control.SelectionModel
import javafx.scene.control.TablePosition
import javafx.scene.control.TableSelectionModel
import javafx.scene.control.TableView.TableViewSelectionModel
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTablePosition
import javafx.scene.control.TreeTableView.TreeTableViewSelectionModel
import matt.fx.base.wrapper.obs.collect.list.createImmutableWrapper
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.base.wrapper.obs.obsval.toNullableROProp
import matt.fx.control.wrapper.control.ControlWrapper
import matt.fx.control.wrapper.control.colbase.TableColumnBaseWrapper
import matt.fx.control.wrapper.control.column.TableColumnWrapper
import matt.lang.anno.Open
import matt.lang.convert.BiConverter
import matt.obs.bind.binding
import matt.obs.col.olist.ImmutableObsList
import matt.obs.col.olist.MutableObsList
import matt.obs.col.olist.mappedlist.toMappedList
import matt.obs.prop.ObsVal
import matt.obs.prop.writable.Var

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
    val selectedItemProperty: ObsVal<out T?>
    @Open
    fun setOnSelectionChange(listener: (T?) -> Unit) = selectedItemProperty.onChange(listener)
    @Open fun onSelect(op: (T?) -> Unit) {
        selectedItemProperty.onChange {
            op(it)
        }
    }

    @Open val selectedItem: T? get() = selectedItemProperty.value
}

interface Selects<T: Any>: SelectionControls<T> {
    val selectionModel: SelectionControls<T>
    @Open override val selectedItem: T? get() = selectionModel.selectedItem
    @Open override fun selectNext() = selectionModel.selectNext()
    @Open override fun selectFirst() = selectionModel.selectFirst()
    @Open override fun selectLast() = selectionModel.selectLast()
    @Open override fun selectionIsEmpty(): Boolean = selectionModel.selectionIsEmpty()
    @Open override fun selectPrevious() = selectionModel.selectPrevious()
    @Open override fun clearAndSelect(index: Int) = selectionModel.clearAndSelect(index)
    @Open override fun selectIndex(index: Int) = selectionModel.selectIndex(index)
    @Open override fun clearSelection(index: Int) = selectionModel.clearSelection(index)
    @Open override fun clearSelection() = selectionModel.clearSelection()
    @Open override fun isSelected(index: Int): Boolean = selectionModel.isSelected(index)
    @Open override val selectedIndex: Int? get() = selectionModel.selectedIndex
    @Open override fun select(obj: T?) = selectionModel.select(obj)
    @Open override val selectedItemProperty: ObsVal<out T?> get() = selectionModel.selectedItemProperty
    @Open override val selectedIndexProperty: ObsVal<Int?> get() = selectionModel.selectedIndexProperty
}

interface MultiSelects<T: Any>: Selects<T>, MultiSelectControls<T> {
    override val selectionModel: MultiSelectControls<T>
    @Open override val selectionModeProperty get() = selectionModel.selectionModeProperty
    @Open override fun selectedIndices() = selectionModel.selectedIndices()
    @Open override fun selectIndices(index: Int, vararg indices: Int) = selectionModel.selectIndices(index, *indices)
    @Open override fun selectRange(start: Int, end: Int) = selectionModel.selectRange(start, end)
    @Open override fun selectAll() = selectionModel.selectAll()
    @Open override val selectedItems get() = selectionModel.selectedItems
}


interface SelectingControl<T: Any>: Selects<T>, ControlWrapper


typealias SelectModWrap<T> = SelectionModelWrapperBase<*, T>

abstract class SelectionModelWrapperBase<T: Any, W: Any>(
    protected open val sm: SelectionModel<T>
): SelectionControls<W> {
    @Open override fun selectNext() = sm.selectNext()
    @Open override fun selectFirst() = sm.selectFirst()
    @Open override fun selectLast() = sm.selectLast()
    @Open override fun selectionIsEmpty() = sm.isEmpty
    @Open override fun selectPrevious() = sm.selectPrevious()
    @Open override fun clearAndSelect(index: Int) {
        sm.clearAndSelect(index)
    }
    @Open override fun selectIndex(index: Int) = sm.select(index)
    @Open override fun clearSelection(index: Int) = sm.clearSelection(index)
    @Open override fun clearSelection() = sm.clearSelection()
    @Open override fun isSelected(index: Int) = sm.isSelected(index)
    @Open override val selectedIndex: Int? get() = selectedIndexProperty.value
    @Open override val selectedIndexProperty by lazy {
        sm.selectedIndexProperty().toNonNullableROProp().cast<Int>().binding {
            it.takeIf { it >= 0 }
        }
    }
}

fun <T: Any> SelectionModel<T>.wrap() = SelectionModelWrapperImpl(this)
open class SelectionModelWrapperImpl<T: Any>(sm: SelectionModel<T>): SelectionModelWrapperBase<T, T>(sm) {
    final override fun select(obj: T?) = sm.select(obj) /*what if T is Int? Would the index be selected? weird...*/
    final override val selectedItemProperty by lazy {
        sm.selectedItemProperty().toNullableROProp()
    }
}

fun <T: Any, W: Any> SelectionModel<T>.wrap(converter: BiConverter<T, W>) = SelectionModelProxy(this, converter)
class SelectionModelProxy<T: Any, W: Any>(
    sm: SelectionModel<T>,
    private val converter: BiConverter<T, W>
): SelectionModelWrapperBase<T, W>(sm) {
    override fun select(obj: W?) = sm.select(obj?.let(converter::convertToA))
    override val selectedItemProperty by lazy {
        sm.selectedItemProperty().toNullableROProp().binding { it?.let(converter::convertToB) }
    }
}


interface MultiSelectControls<W: Any>: SelectionControls<W> {
    val selectionModeProperty: Var<SelectionMode>
    @Open var selectionMode
        get() = selectionModeProperty.value
        set(value) {
            selectionModeProperty.value = value
        }

    fun selectedIndices(): MutableObsList<Int>
    fun selectIndices(index: Int, vararg indices: Int)
    fun selectRange(start: Int, end: Int)
    fun selectAll()
    val selectedItems: ImmutableObsList<W>
}

typealias MultiSelectWrap<T> = MultipleSelectionModelWrapperBase<*, T>

abstract class MultipleSelectionModelWrapperBase<T: Any, W: Any>(
    protected open val sm: MultipleSelectionModel<T>
): MultiSelectControls<W> {
    @Open override val selectionModeProperty by lazy { sm.selectionModeProperty().toNonNullableProp() }
    @Open override fun selectedIndices() = sm.selectedIndices.createImmutableWrapper()
    @Open override fun selectIndices(index: Int, vararg indices: Int) = sm.selectIndices(index, *indices)
    @Open override fun selectRange(start: Int, end: Int) = sm.selectRange(start, end)
    @Open override fun selectAll() = sm.selectAll()
    abstract override val selectedItems: ImmutableObsList<W>
}

fun <T: Any> MultipleSelectionModel<T>.wrap() = MultipleSelectionModelWrapperImpl(this)
open class MultipleSelectionModelWrapperImpl<T: Any>(
    sm: MultipleSelectionModel<T>
): MultipleSelectionModelWrapperBase<T, T>(sm), SelectionControls<T> by SelectionModelWrapperImpl(sm)  {
    final override val selectedItems by lazy { sm.selectedItems.createImmutableWrapper() }
}

fun <T: Any, W: Any> MultipleSelectionModel<T>.wrap(converter: BiConverter<T, W>) =
    MultipleSelectionModelWrapperProxy(this, converter)

class MultipleSelectionModelWrapperProxy<T: Any, W: Any>(
    sm: MultipleSelectionModel<T>,
    private val converter: BiConverter<T, W>
): MultipleSelectionModelWrapperBase<T, W>(sm), SelectionControls<W> by SelectionModelProxy<T, W>(sm, converter) {
    override val selectedItems by lazy {
        sm.selectedItems.createImmutableWrapper().toMappedList {
            converter.convertToB(it)
        }
    }
}

/*fun <T: Any> TableSelectionModel<T>.wrap() = TableSelectionModelWrapper<T>(this)*/

abstract class TableSelectionModelWrapper<T: Any>(
    @Open override val sm: TableSelectionModel<T>
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
    final override val sm: TableViewSelectionModel<T>
): TableSelectionModelWrapper<T>(sm) {
    val selectedCells: ObservableList<TablePosition<Any, Any>> get() = sm.selectedCells
}

fun <T: Any> TreeTableViewSelectionModel<T>.wrap() = TreeTableViewSelectionModelWrapper(this)

class TreeTableViewSelectionModelWrapper<T: Any>(
    override val sm: TreeTableViewSelectionModel<T>
): TableSelectionModelWrapper<TreeItem<T>>(sm) {
    val selectedCells: ObservableList<TreeTablePosition<T, *>> get() = sm.selectedCells
}
