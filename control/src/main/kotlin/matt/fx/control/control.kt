package matt.fx.control

import javafx.scene.Node
import javafx.scene.control.Alert.AlertType.CONFIRMATION
import javafx.scene.control.ButtonType
import javafx.scene.control.TreeTableView
import javafx.scene.input.ContextMenuEvent
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Pane
import javafx.scene.layout.RowConstraints
import matt.fx.control.tfx.dialog.alert
import matt.fx.control.win.interact.dialog
import matt.fx.control.win.interact.popupTextInput
import matt.fx.control.wrapper.checkbox.CheckBoxWrapper
import matt.fx.control.wrapper.control.tab.TabWrapper
import matt.fx.control.wrapper.control.text.field.textfield
import matt.fx.control.wrapper.scroll.ScrollPaneWrapper
import matt.fx.graphics.fxthread.ensureInFXThreadInPlace
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.NodeWrapperImpl
import matt.fx.graphics.wrapper.text.text
import matt.hurricanefx.eye.mtofx.createROFXPropWrapper
import matt.lang.NEVER
import matt.log.warn.warn
import matt.obs.prop.BindableProperty
import matt.obs.prop.Var
import matt.service.action.ActionAbilitiesService


interface Scrolls {
  val scrollPane: ScrollPaneWrapper<*>
}

fun Scrolls.scrollToMinYOf(node: NodeWrapperImpl<*>) {
  scrollPane.scrollToMinYOf(node)
}


fun ColumnConstraints.exactWidthProperty() = BindableProperty<Double?>(null).also {
  minWidthProperty().bind(it.createROFXPropWrapper())
  maxWidthProperty().bind(it.createROFXPropWrapper())
}

fun RowConstraints.exactHeightProperty() = BindableProperty<Double?>(null).also {
  minHeightProperty().bind(it.createROFXPropWrapper())
  maxHeightProperty().bind(it.createROFXPropWrapper())
}


var ColumnConstraints.exactWidth: Number
  set(value) {
	exactWidthProperty().bind(BindableProperty(value.toDouble()))
  }
  get() = NEVER
var RowConstraints.exactHeight: Number
  set(value) {
	exactHeightProperty().bind(BindableProperty(value.toDouble()))
  }
  get() = NEVER


fun lazyTab(name: String, nodeOp: ()->NodeWrapper) = TabWrapper<NodeWrapper>(name).apply {
  if (isSelected) {
	content = nodeOp()
  } else {
	selectedProperty.onChangeUntilInclusive({ true }) {
	  if (it == true) {
		content = nodeOp()
	  }
	}
  }
}


fun Node.disableContextMenu() {
  addEventFilter(ContextMenuEvent.ANY) {
	it.consume()
  }
}


//fun BooleanProperty.checkbox() = CheckBoxWrapper(name).also {
//  it.selectedProperty.bindBidirectional(this)
//}
fun Var<Boolean>.checkbox(name: String? = null) = CheckBoxWrapper(name).also {
  warn("need auto name name")
  it.selectedProperty.bindBidirectional(this)
}


class TreeTableTreeView<T>(val table: Boolean): TreeTableView<T>() {
  override fun resize(width: Double, height: Double) {
	super.resize(width, height)
	if (!table) {
	  val header = lookup("TableHeaderRow") as Pane
	  header.minHeight = 0.0
	  header.prefHeight = 0.0
	  header.maxHeight = 0.0
	  header.isVisible = false
	}
  }
}


object FXActionAbilitiesService: ActionAbilitiesService {
  override fun confirm(s: String): Boolean {

	return ensureInFXThreadInPlace {

	  var ok = false

	  alert(CONFIRMATION, s) {
		ok = it == ButtonType.OK
	  }

	  ok

	}


  }

  override fun input(prompt: String) = popupTextInput(prompt) ?: NEVER
}