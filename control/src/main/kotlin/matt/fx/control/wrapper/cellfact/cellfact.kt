package matt.fx.control.wrapper.cellfact

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableObjectValue
import javafx.beans.value.ObservableStringValue
import javafx.beans.value.ObservableValue
import javafx.scene.control.Cell
import javafx.scene.control.ListCell
import javafx.scene.control.TableCell
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeTableCell
import javafx.scene.control.cell.CheckBoxListCell
import javafx.util.Callback
import javafx.util.StringConverter
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.hurricanefx.eye.lang.Prop
import matt.hurricanefx.eye.prop.objectBindingN
import kotlin.reflect.KProperty1

interface CellFactory<N, T, C: Cell<T>> {


  val cellFactoryProperty: ObjectProperty<Callback<N, C>>
  var cellFactory: Callback<N, C>
	get() = cellFactoryProperty.get()
	set(value) {
	  cellFactoryProperty.set(value)
	}

  fun setCellFact(value: Callback<N, C>) {
	cellFactoryProperty.set(value)
  }


  fun simpleCellFactoryFromProps(op: (T)->Pair<ObservableStringValue, ObservableObjectValue<NodeWrapper>>)

  fun simpleCellFactory(prop: KProperty1<T, ObservableStringValue>) {
	simpleCellFactoryFromProps { prop.get(it) to Prop<NodeWrapper>() }
  }

  fun simpleCellFactory(op: SimpleFactory<T>) {
	val op2: (T)->Pair<ObservableStringValue, ObservableObjectValue<NodeWrapper>> = {
	  val pair = op.call(it)
	  SimpleStringProperty(pair.first) to Prop(pair.second)
	}
	simpleCellFactoryFromProps(op2)
  }

  fun simpleCellFactory(op: FirstPropFactory<T>) {
	val op2: (T)->Pair<ObservableStringValue, ObservableObjectValue<NodeWrapper>> = {
	  val pair = op.call(it)
	  pair.first to Prop(pair.second)
	}
	simpleCellFactoryFromProps(op2)
  }

  fun simpleCellFactory(op: SecondPropFactory<T>) {
	val op2: (T)->Pair<ObservableStringValue, ObservableObjectValue<NodeWrapper>> = {
	  val pair = op.call(it)
	  SimpleStringProperty(pair.first) to pair.second
	}
	simpleCellFactoryFromProps(op2)
  }

  fun simpleCellFactory(op: BothPropFactory<T>) {
	val op2: (T)->Pair<ObservableStringValue, ObservableObjectValue<NodeWrapper>> = {
	  val pair = op.call(it)
	  pair.first to pair.second
	}
	simpleCellFactoryFromProps(op2)
  }
}

fun interface SimpleFactory<P> {
  fun call(p: P): Pair<String?, NodeWrapper?>
}

fun interface FirstPropFactory<P> {
  fun call(p: P): Pair<ObservableStringValue, NodeWrapper?>
}

fun interface SecondPropFactory<P> {
  fun call(p: P): Pair<String?, ObservableObjectValue<NodeWrapper>>
}

fun interface BothPropFactory<P> {
  fun call(p: P): Pair<ObservableStringValue, ObservableObjectValue<NodeWrapper>>
}


interface ListCellFactory<N, T>: CellFactory<N, T, ListCell<T>> {
  override fun simpleCellFactoryFromProps(op: (T)->Pair<ObservableStringValue, ObservableObjectValue<NodeWrapper>>) {
	setCellFact {
	  object: ListCell<T>() {
		override fun updateItem(item: T, empty: Boolean) {
		  super.updateItem(item, empty)
		  simpleUpdateLogic(item, empty, op)
		}
	  }
	}
  }

  fun useCheckbox(converter: StringConverter<T>? = null, getter: (T)->ObservableValue<Boolean>) {
	setCellFact { CheckBoxListCell(getter, converter) }
  }
}

interface TreeCellFactory<N, T>: CellFactory<N, T, TreeCell<T>> {
  override fun simpleCellFactoryFromProps(op: (T)->Pair<ObservableStringValue, ObservableObjectValue<NodeWrapper>>) {
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
  override fun simpleCellFactoryFromProps(op: (P)->Pair<ObservableStringValue, ObservableObjectValue<NodeWrapper>>) {
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
  override fun simpleCellFactoryFromProps(op: (P)->Pair<ObservableStringValue, ObservableObjectValue<NodeWrapper>>) {
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
  op: (T)->Pair<ObservableStringValue, ObservableObjectValue<NodeWrapper>>
) {
  if (empty || item == null) clear()
  else {
	op(item).let {
	  textProperty().bind(it.first)
	  graphicProperty().bind(it.second.objectBindingN { it?.node })
	}
  }
}

//sealed class StringNode
//class StringNodeValues(val t: String, val g: Node)
//class TextPropertyGraphicValue(val t: Text, val g: Node)