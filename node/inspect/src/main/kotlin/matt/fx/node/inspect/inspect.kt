package matt.fx.node.inspect

import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority.ALWAYS
import javafx.scene.layout.VBox
import matt.fx.control.wrapper.control.list.ListViewWrapper
import matt.fx.control.wrapper.control.table.TableViewWrapper
import matt.fx.control.wrapper.label.label
import matt.fx.control.wrapper.selects.SelectingControl
import matt.fx.graphics.node.Inspectable
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.box.BoxWrapperImpl
import matt.fx.graphics.wrapper.pane.hbox.HBoxWrapperImpl
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.fx.base.collect.toObservable


class InspectionView<N: PaneWrapperImpl<*, *>, T: Inspectable<N>>(
  items: List<T>,
  dir: Orientation = Orientation.HORIZONTAL,
  table: Boolean = false,
  wrap_lv: PaneWrapperImpl<*, *>? = null
): BoxWrapperImpl<Pane, NodeWrapper>(if (dir == Orientation.HORIZONTAL) HBox() else VBox()) {

  private val oitems = if (items is ObservableList) items else items.toObservable()
  val lv: SelectingControl<T> = if (table) TableViewWrapper(oitems) else ListViewWrapper(oitems)

  init {
	add((wrap_lv?.also { it.add(lv) } ?: lv).also {
	  lv.vgrow = ALWAYS
	  lv.hgrow = ALWAYS
	})
	val inspectHolder: BoxWrapperImpl<*, *> =
	  if (dir == Orientation.HORIZONTAL) HBoxWrapperImpl<NodeWrapper>() else VBoxWrapperImpl<NodeWrapper>()
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