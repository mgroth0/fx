package matt.fx.control.wrapper.selects

import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.control.SelectionModel
import matt.fx.control.wrapper.control.ControlWrapper
import matt.fx.graphics.wrapper.EventTargetWrapperImpl
import matt.hurricanefx.eye.lib.onChange
import matt.model.convert.Converter

interface Selects<T> {
  val selectionModel: SelectionModel<T>
  val selectedItem: T? get() = selectionModel.selectedItem
  fun select(item: T?) = selectionModel.select(item)
  fun select(index: Int) = selectionModel.select(index) /*what if T is Int? weird...*/
  val selectedItemProperty: ReadOnlyObjectProperty<T> get() = selectionModel.selectedItemProperty()
  fun setOnSelectionChange(listener: (T?)->Unit): ObservableValue<T> = selectedItemProperty.onChange(listener)

  fun onSelect(op: (T?)->Unit) {
	selectionModel.selectedItemProperty().onChange {
	  op(it)
	}
  }
}

interface SelectingControl<T>: Selects<T>, ControlWrapper


interface SelectsWrapper<N, W>: Selects<W> {
  val selectionModelWrapper: SelectionModelWrapper<N, W>
  override val selectionModel get() = selectionModelWrapper
}

interface ETWrapperConverter<N: EventTarget, W: EventTargetWrapperImpl<N>>: Converter<N, W> {
  override fun convertToA(b: W) = toNode(b)
  override fun convertToB(a: N) = toWrapper(a)

  fun toNode(w: W): N
  fun toWrapper(n: N): W
}

class SelectionModelWrapper<N, W>(
  private val sm: SelectionModel<N>,
  private val converter: Converter<N, W>
): SelectionModel<W>() {
  override fun clearAndSelect(index: Int) = sm.clearAndSelect(index)
  override fun select(index: Int) = sm.select(index)
  override fun clearSelection(index: Int) = sm.clearSelection(index)
  override fun clearSelection() = sm.clearSelection()
  override fun isSelected(index: Int) = sm.isSelected(index)
  override fun isEmpty() = sm.isEmpty
  override fun selectPrevious() = sm.selectPrevious()
  override fun selectNext() = sm.selectNext()
  override fun selectFirst() = sm.selectFirst()
  override fun selectLast() = sm.selectLast()
  override fun select(obj: W) = sm.select(converter.convertToA(obj))

  init {
	selectedIndex = sm.selectedIndex
	sm.selectedIndexProperty().onChange {
	  selectedIndex = it
	}
	selectedItem = sm.selectedItem?.let { converter.convertToB(it) }
	sm.selectedItemProperty().onChange {
	  selectedItem = it?.let { converter.convertToB(it) }
	}
  }
}



