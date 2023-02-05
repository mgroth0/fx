package matt.fx.control.wrapper.control.column

import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableValue
import javafx.beans.value.WritableValue
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableColumn.CellDataFeatures
import javafx.scene.control.TableColumn.CellEditEvent
import javafx.scene.control.cell.ChoiceBoxTableCell
import javafx.scene.control.cell.ComboBoxTableCell
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.text.Text
import javafx.util.Callback
import javafx.util.StringConverter
import matt.fx.control.wrapper.cellfact.TableCellFactory
import matt.fx.control.wrapper.cellfact.cellvalfact.CellValueFactory
import matt.fx.control.wrapper.control.colbase.TableColumnBaseWrapper
import matt.fx.control.wrapper.control.hascols.HasCols
import matt.fx.control.wrapper.control.table.TableViewWrapper
import matt.fx.control.wrapper.control.table.cols.ColumnsDSL
import matt.fx.control.wrapper.control.table.cols.ColumnsDSLImpl
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.base.converter.callbackConverter
import matt.hurricanefx.eye.prop.stringBinding
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNullableROProp
import matt.obs.prop.BindableProperty
import matt.obs.prop.ObsVal
import matt.obs.prop.Var

private typealias CellValFact<E, P> = Callback<CellDataFeatures<E, P>, ObservableValue<P>?>


class TableColumnWrapper<E: Any, P>(
  override val node: TableColumn<E, P>
): TableColumnBaseWrapper<E, P, TableColumn<E, P>>(node),
   TableCellFactory<TableColumn<E, P>, E, P>,
   CellValueFactory<CellDataFeatures<E, P>, P>,
   HasCols<E>,
   ColumnsDSL<E> by ColumnsDSLImpl(node.columns) {



  constructor(name: String): this(TableColumn<E, P>(name))

  val sortTypeProp by lazy {
	node.sortTypeProperty().toNonNullableProp()
  }
  var sortType by sortTypeProp

  override val tableView: TableViewWrapper<E>? get() = node.tableView?.wrapped()
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }

  fun getCellObservableValue(index: Int) = node.getCellObservableValue(index).toNullableROProp()
  fun getCellObservableValue(item: E) = node.getCellObservableValue(item).toNullableROProp()

  override fun removeFromParent() {
	node.tableView.columns.remove(this.node)
  }


  override val columns: ObservableList<TableColumn<E, *>> = node.columns

  override val cellFactoryProperty by lazy { node.cellFactoryProperty().toNonNullableProp() }


  val cellValueFactoryProperty by lazy {
	node.cellValueFactoryProperty().toNullableProp().proxy(
	  callbackConverter<CellDataFeatures<E, P>, P>().nullable()
	)
  }


  override var cellValueFactory by cellValueFactoryProperty

  infix fun value(cvf: (CellDataFeatures<E, P>)->P) {
	this.cellValueFactory = Callback { it: CellDataFeatures<E, P> ->
	  val createdValue = cvf(it)
	  BindableProperty(createdValue)
	}
  }

  @JvmName("value1")
  infix fun value(cvf: (CellDataFeatures<E, P>)->ObsVal<P>) {
	this.cellValueFactory = Callback { it: CellDataFeatures<E, P> ->
	  cvf(it)
	}
  }

  fun enableTextWrap() {
	setCellFact {
	  TableCell<E, P>().apply {
		val text = Text()
		graphic = text
		prefHeight = Control.USE_COMPUTED_SIZE
		text.wrappingWidthProperty()
		  .bind(this.widthProperty().subtract(Bindings.multiply(2.0, graphicTextGapProperty())))
		text.textProperty().bind(itemProperty().stringBinding {
		  it?.toString() ?: ""
		})
	  }
	}
  }

  val comparatorProp by lazy {
	node.comparatorProperty().toNonNullableProp()
  }
  var comparator by comparatorProp

  fun setOnEditCommit(value: EventHandler<CellEditEvent<E, P?>>) = node.setOnEditCommit(value)

  fun useComboBox(
	items: ObservableList<P>,
	afterCommit: (CellEditEvent<E, P?>)->Unit = {}
  ) = apply {
	cellFactory = ComboBoxTableCell.forTableColumn(items)
	setOnEditCommit {
	  @Suppress("UNCHECKED_CAST") val property = it.tableColumn.wrapped().getCellObservableValue(it.rowValue) as Var<P?>
	  property.value = it.newValue
	  afterCommit(it)
	}
  }


  fun useChoiceBox(
	items: ObservableList<P>,
	afterCommit: (CellEditEvent<E, P?>)->Unit = {}
  ) = apply {
	cellFactory = ChoiceBoxTableCell.forTableColumn(items)
	setOnEditCommit {
	  @Suppress("UNCHECKED_CAST") val property = it.tableColumn.wrapped().getCellObservableValue(it.rowValue) as Var<P?>
	  property.value = it.newValue
	  afterCommit(it)
	}
  }


  /**
   * Write a value into the property representing this TableColumn, provided
   * the property is writable.
   */
  @Suppress("UNCHECKED_CAST")
  fun setValue(item: E, value: P?) {
	val property = getTableColumnProperty(item)
	(property as? WritableValue<P>)?.value = value
  }

  /**
   * Get the value from the property representing this TableColumn.
   */
  fun getValue(item: E): P? = getTableColumnProperty(item).value

  /**
   * Get the property representing this TableColumn for the given item.
   */
  fun getTableColumnProperty(item: E): ObsVal<P> {
	val param = CellDataFeatures(node.tableView, node, item)
	val property = cellValueFactory!!.call(param)
	return property!!
  }


  inline fun <E, reified P> TableColumn<E, P?>.useTextField(
	converter: StringConverter<P>? = null,
	noinline afterCommit: (CellEditEvent<E, P?>)->Unit = {}
  ) = apply {
	when (P::class) {
	  String::class -> {
		@Suppress("UNCHECKED_CAST")
		val stringColumn = this as TableColumn<E, String?>
		stringColumn.cellFactory = TextFieldTableCell.forTableColumn()
	  }

	  else          -> {
		requireNotNull(converter) { "You must supply a converter for non String columns" }
		cellFactory = TextFieldTableCell.forTableColumn(converter)
	  }
	}

	setOnEditCommit {
	  @Suppress("UNCHECKED_CAST")
	  val property = it.tableColumn.wrapped().getCellObservableValue(it.rowValue!!) as Var<P?>
	  property.value = it.newValue
	  afterCommit(it)
	}


  }

  fun makeEditable(converter: StringConverter<P>): TableColumnWrapper<E, P> = apply {
	tableView!!.isEditable = true
	cellFactory = TextFieldTableCell.forTableColumn(converter)
  }


}


fun <E: Any> TableColumnWrapper<E, String>.makeEditable(): TableColumnWrapper<E, String> = apply {
  tableView!!.isEditable = true
  cellFactory = TextFieldTableCell.forTableColumn()
}
