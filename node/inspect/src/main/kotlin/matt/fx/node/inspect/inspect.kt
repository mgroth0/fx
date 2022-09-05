package matt.fx.node.inspect

import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority.ALWAYS
import javafx.scene.layout.VBox
import matt.fx.graphics.Inspectable
import matt.hurricanefx.eye.collect.toObservable
import matt.hurricanefx.wrapper.control.list.ListViewWrapper
import matt.hurricanefx.wrapper.control.table.TableViewWrapper
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.hurricanefx.wrapper.pane.PaneWrapperImpl
import matt.hurricanefx.wrapper.pane.box.BoxWrapper
import matt.hurricanefx.wrapper.pane.hbox.HBoxWrapper
import matt.hurricanefx.wrapper.pane.vbox.VBoxWrapper
import matt.hurricanefx.wrapper.selects.SelectingControl


class InspectionView<N: PaneWrapperImpl<*, *>, T: Inspectable<N>>(
  items: List<T>,
  dir: Orientation = Orientation.HORIZONTAL,
  table: Boolean = false,
  wrap_lv: PaneWrapperImpl<*, *>? = null
): BoxWrapper<Pane, NodeWrapper>(if (dir == Orientation.HORIZONTAL) HBox() else VBox()) {

  private val oitems = if (items is ObservableList) items else items.toObservable()
  val lv: SelectingControl<T> = if (table) TableViewWrapper(oitems) else ListViewWrapper(oitems)

  init {
	add((wrap_lv?.also { it.add(lv) } ?: lv).also {
	  lv.vgrow = ALWAYS
	  lv.hgrow = ALWAYS
	})
	val inspectHolder: BoxWrapper<*, *> =
	  if (dir == Orientation.HORIZONTAL) HBoxWrapper<NodeWrapper>() else VBoxWrapper<NodeWrapper>()
	inspectHolder.vgrow = ALWAYS
	inspectHolder.hgrow = ALWAYS
	add(inspectHolder)

	val onSelectBlock = { it: T? ->
	  inspectHolder.clear()
	  val noSelectionLabel = inspectHolder.label("no selection")
	  it?.let {
		noSelectionLabel.removeFromParent()
		inspectHolder.add(it.inspect().apply {
		  vgrow = ALWAYS
		  hgrow = ALWAYS
		})
	  }
	  Unit
	}
	lv.onSelect(onSelectBlock)

  }

}

fun <N: PaneWrapperImpl<*, *>, T: Inspectable<N>> PaneWrapperImpl<*, *>.inspectionview(
  items: List<T>,
  dir: Orientation = Orientation.HORIZONTAL,
  table: Boolean = false,
  op: (PaneWrapperImpl<*, *>.()->Unit)? = null,
  wrap_lv: PaneWrapperImpl<*, *>? = null
): InspectionView<*, T> {
  val iv = InspectionView(items, dir, table, wrap_lv).apply {
	op?.invoke(this)
  }
  add(iv)
  return iv
}