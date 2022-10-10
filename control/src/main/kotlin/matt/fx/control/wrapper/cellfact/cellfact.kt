package matt.fx.control.wrapper.cellfact

import javafx.scene.control.Cell
import javafx.scene.control.ListCell
import javafx.scene.control.TableCell
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeTableCell
import javafx.scene.control.cell.CheckBoxListCell
import javafx.util.Callback
import javafx.util.StringConverter
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.hurricanefx.eye.mtofx.createROFXPropWrapper
import matt.obs.bind.binding
import matt.obs.bindings.bool.ObsB
import matt.obs.bindings.str.ObsS
import matt.obs.prop.BindableProperty
import matt.obs.prop.ObsVal
import matt.obs.prop.Var
import kotlin.reflect.KProperty1

interface CellFactory<N, T, C: Cell<T>> {


  val cellFactoryProperty: Var<Callback<N, C>>
  var cellFactory: Callback<N, C>
	get() = cellFactoryProperty.value
	set(value) {
	  cellFactoryProperty v value
	}

  fun setCellFact(value: (N)->C) {
	cellFactoryProperty v value
  }


  fun simpleCellFactoryFromProps(op: (T)->Pair<ObsS, ObsVal<NodeWrapper?>>)

  fun simpleCellFactory(prop: KProperty1<T, ObsS>) {
	simpleCellFactoryFromProps { prop.get(it) to BindableProperty(null) }
  }

  fun simpleCellFactory(op: SimpleFactory<T>) {
	val op2: (T)->Pair<ObsS, ObsVal<NodeWrapper?>> = {
	  val pair = op.call(it)
	  BindableProperty(pair.first) to BindableProperty(pair.second)
	}
	simpleCellFactoryFromProps(op2)
  }

  fun simpleCellFactory(op: FirstPropFactory<T>) {
	val op2: (T)->Pair<ObsS, ObsVal<NodeWrapper?>> = {
	  val pair = op.call(it)
	  pair.first to BindableProperty(pair.second)
	}
	simpleCellFactoryFromProps(op2)
  }

  fun simpleCellFactory(op: SecondPropFactory<T>) {
	val op2: (T)->Pair<ObsS, ObsVal<NodeWrapper?>> = {
	  val pair = op.call(it)
	  BindableProperty(pair.first) to pair.second
	}
	simpleCellFactoryFromProps(op2)
  }

  fun simpleCellFactory(op: BothPropFactory<T>) {
	val op2: (T)->Pair<ObsS, ObsVal<NodeWrapper?>> = {
	  val pair = op.call(it)
	  pair.first to pair.second
	}
	simpleCellFactoryFromProps(op2)
  }
}

fun interface SimpleFactory<P> {
  fun call(p: P): Pair<String, NodeWrapper?>
}

fun interface FirstPropFactory<P> {
  fun call(p: P): Pair<ObsS, NodeWrapper?>
}

fun interface SecondPropFactory<P> {
  fun call(p: P): Pair<String, ObsVal<NodeWrapper?>>
}

fun interface BothPropFactory<P> {
  fun call(p: P): Pair<ObsS, ObsVal<NodeWrapper?>>
}


interface ListCellFactory<N, T>: CellFactory<N, T, ListCell<T>> {
  override fun simpleCellFactoryFromProps(op: (T)->Pair<ObsS, ObsVal<NodeWrapper?>>) {
	setCellFact {
	  object: ListCell<T>() {
		override fun updateItem(item: T, empty: Boolean) {
		  super.updateItem(item, empty)
		  simpleUpdateLogic(item, empty, op)
		}
	  }
	}
  }

  fun useCheckbox(converter: StringConverter<T>? = null, getter: (T)->ObsB) {
	setCellFact { CheckBoxListCell({ getter(it).createROFXPropWrapper() }, converter) }
  }
}

interface TreeCellFactory<N, T>: CellFactory<N, T, TreeCell<T>> {
  override fun simpleCellFactoryFromProps(op: (T)->Pair<ObsS, ObsVal<NodeWrapper?>>) {
	setCellFact {
	  object: TreeCell<T>() {
		override fun updateItem(item: T, empty: Boolean) {
		  super.updateItem(item, empty)
		  simpleUpdateLogic(item, empty, op)
		}
	  }
	}
  }
}


interface TreeTableCellFactory<N, E, P>: CellFactory<N, P, TreeTableCell<E, P>> {
  override fun simpleCellFactoryFromProps(op: (P)->Pair<ObsS, ObsVal<NodeWrapper?>>) {
	setCellFact {
	  object: TreeTableCell<E, P>() {
		override fun updateItem(item: P, empty: Boolean) {
		  super.updateItem(item, empty)
		  simpleUpdateLogic(item, empty, op)
		}
	  }
	}
  }
}

interface TableCellFactory<N, E, P>: CellFactory<N, P, TableCell<E, P>> {
  override fun simpleCellFactoryFromProps(op: (P)->Pair<ObsS, ObsVal<NodeWrapper?>>) {
	setCellFact {
	  object: TableCell<E, P>() {
		override fun updateItem(item: P, empty: Boolean) {
		  super.updateItem(item, empty)
		  simpleUpdateLogic(item, empty, op)
		}
	  }
	}
  }
}

private fun Cell<*>.clear() {
  textProperty().unbind()
  text = null
  graphicProperty().unbind()
  graphic = null
}

//private fun <T> Cell<T>.simpleUpdateLogic(
//  item: T,
//  empty: Boolean,
//  op: (T)->Pair<String?, Node?>
//) {
//  if (empty || item == null) clear()
//  else {
//	op(item).let {
//	  text = it.first
//	  graphic = it.second
//	}
//  }
//}

//@JvmName("simpleUpdateLogicT")
private fun <T> Cell<T>.simpleUpdateLogic(
  item: T,
  empty: Boolean,
  op: (T)->Pair<ObsS, ObsVal<NodeWrapper?>>
) {
  if (empty || item == null) clear()
  else {
	op(item).let {
	  textProperty().bind(it.first.createROFXPropWrapper())
	  graphicProperty().bind(it.second.binding { it?.node }.createROFXPropWrapper())
	}
  }
}

//sealed class StringNode
//class StringNodeValues(val t: String, val g: Node)
//class TextPropertyGraphicValue(val t: Text, val g: Node)