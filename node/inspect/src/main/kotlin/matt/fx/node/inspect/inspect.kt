package matt.fx.node.inspect

import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import matt.fx.graphics.Inspectable
import matt.fx.graphics.layout.hgrow
import matt.fx.graphics.layout.vgrow
import matt.gui.fxlang.onSelect
import matt.hurricanefx.eye.collect.toObservable
import matt.hurricanefx.tornadofx.control.label
import matt.hurricanefx.tornadofx.nodes.add
import matt.hurricanefx.tornadofx.nodes.clear
import matt.hurricanefx.tornadofx.nodes.removeFromParent
import matt.hurricanefx.wrapper.pane.box.BoxWrapper
import matt.hurricanefx.wrapper.pane.hbox.HBoxWrapper
import matt.hurricanefx.wrapper.ListViewWrapper
import matt.hurricanefx.wrapper.pane.PaneWrapper
import matt.hurricanefx.wrapper.TableViewWrapper
import matt.hurricanefx.wrapper.pane.vbox.VBoxWrapper


fun <T: Inspectable> InspectionView(
  items: List<T>,
  dir: Orientation = Orientation.HORIZONTAL,
  table: Boolean = false,
  wrap_lv: PaneWrapper? = null
): PaneWrapper {
  val root = if (dir == Orientation.HORIZONTAL) HBoxWrapper() else VBoxWrapper()
  val oitems = if (items is ObservableList) items else items.toObservable()
  val lv = if (table) TableViewWrapper(oitems) else ListViewWrapper(oitems)
  root.add((wrap_lv?.apply { add(lv) } ?: lv).apply {
	lv.vgrow = Priority.ALWAYS
	lv.hgrow = Priority.ALWAYS
  })
  val inspectHolder: BoxWrapper<*> = if (dir == Orientation.HORIZONTAL) HBoxWrapper() else VBoxWrapper()
  inspectHolder.vgrow = Priority.ALWAYS
  inspectHolder.hgrow = Priority.ALWAYS
  root.add(inspectHolder)

  val onSelectBlock = { it: T? ->
	inspectHolder.clear()
	val noSelectionLabel = inspectHolder.label("no selection")
	it?.let {
	  noSelectionLabel.removeFromParent()
	  inspectHolder.add(it.inspect().apply {
		this.vgrow = Priority.ALWAYS
		this.hgrow = Priority.ALWAYS
	  })
	}
	Unit
  }
  @Suppress("UNCHECKED_CAST")
  (lv as? ListViewWrapper<T>)?.onSelect(onSelectBlock)
  @Suppress("UNCHECKED_CAST")
  (lv as? TableViewWrapper<T>)?.onSelect(onSelectBlock)
  return root
}

fun <T: Inspectable> PaneWrapper.inspectionview(
  items: List<T>,
  dir: Orientation = Orientation.HORIZONTAL,
  table: Boolean = false,
  op: (PaneWrapper.()->Unit)? = null,
  wrap_lv: PaneWrapper? = null
): PaneWrapper {
  val iv = InspectionView(items, dir, table, wrap_lv).apply {
	if (op != null) {
	  op()
	}
  }
  add(iv)
  return iv
}