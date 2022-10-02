package matt.fx.control

import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.Node
import javafx.scene.control.TreeTableView
import javafx.scene.input.ContextMenuEvent
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Pane
import javafx.scene.layout.RowConstraints
import matt.fx.control.wrapper.checkbox.CheckBoxWrapper
import matt.fx.control.wrapper.control.tab.TabWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.NodeWrapperImpl
import matt.hurricanefx.eye.lang.DProp
import matt.fx.control.wrapper.scroll.ScrollPaneWrapper
import matt.hurricanefx.eye.lib.onChangeUntilAfterFirst
import matt.lang.NEVER
import matt.log.warn
import matt.obs.prop.Var


interface Scrolls {
  val scrollPane: ScrollPaneWrapper<*>
}

fun Scrolls.scrollToMinYOf(node: NodeWrapperImpl<*>) {
  scrollPane.scrollToMinYOf(node)
}


fun ColumnConstraints.exactWidthProperty() = SimpleDoubleProperty().also {
  minWidthProperty().bind(it)
  maxWidthProperty().bind(it)
}

fun RowConstraints.exactHeightProperty() = SimpleDoubleProperty().also {
  minHeightProperty().bind(it)
  maxHeightProperty().bind(it)
}


var ColumnConstraints.exactWidth: Number
  set(value) {
	exactWidthProperty().bind(DProp(value.toDouble()))
  }
  get() = NEVER
var RowConstraints.exactHeight: Number
  set(value) {
	exactHeightProperty().bind(DProp(value.toDouble()))
  }
  get() = NEVER



fun lazyTab(name: String, nodeOp: ()->NodeWrapper) = TabWrapper<NodeWrapper>(name).apply {
  if (isSelected) {
    content = nodeOp()
  } else {
    selectedProperty.onChangeUntilAfterFirst(true) {
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
