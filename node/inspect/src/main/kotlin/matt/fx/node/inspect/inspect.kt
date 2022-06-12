package matt.fx.node.inspect

import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.control.ListView
import javafx.scene.control.TableView
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import matt.fx.graphics.Inspectable
import matt.fx.graphics.layout.hgrow
import matt.fx.graphics.layout.vgrow
import matt.gui.fxlang.onSelect
import matt.hurricanefx.eye.collect.toObservable
import matt.hurricanefx.tornadofx.control.label
import matt.hurricanefx.tornadofx.nodes.add
import matt.hurricanefx.tornadofx.nodes.clear
import matt.hurricanefx.tornadofx.nodes.removeFromParent



fun <T: Inspectable> InspectionView(
  items: List<T>,
  dir: Orientation = Orientation.HORIZONTAL,
  table: Boolean = false,
  wrap_lv: Pane? = null
): Pane {
  val root = if (dir == Orientation.HORIZONTAL) HBox() else VBox()
  val oitems = if (items is ObservableList) items else items.toObservable()
  val lv = if (table) TableView(oitems) else ListView(oitems)
  root.add((wrap_lv?.apply { add(lv) } ?: lv).apply {
	lv.vgrow = Priority.ALWAYS
	lv.hgrow = Priority.ALWAYS
  })
  val inspectHolder: Pane = if (dir == Orientation.HORIZONTAL) HBox() else VBox()
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
  (lv as? ListView<T>)?.onSelect(onSelectBlock)
  @Suppress("UNCHECKED_CAST")
  (lv as? TableView<T>)?.onSelect(onSelectBlock)
  return root
}

fun <T: Inspectable> Pane.inspectionview(
  items: List<T>,
  dir: Orientation = Orientation.HORIZONTAL,
  table: Boolean = false,
  op: (Pane.()->Unit)? = null,
  wrap_lv: Pane? = null
): Pane {
  val iv = InspectionView(items, dir, table, wrap_lv).apply {
	if (op != null) {
	  op()
	}
  }
  add(iv)
  return iv
}