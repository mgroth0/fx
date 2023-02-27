package matt.fx.control

import javafx.scene.Node
import javafx.scene.control.TreeTableView
import javafx.scene.input.ContextMenuEvent
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Pane
import javafx.scene.layout.RowConstraints
import matt.fx.base.mtofx.createROFXPropWrapper
import matt.fx.control.wrapper.checkbox.CheckBoxWrapper
import matt.fx.control.wrapper.control.tab.TabWrapper
import matt.fx.control.wrapper.scroll.ScrollPaneWrapper
import matt.fx.control.wrapper.tab.TabPaneWrapper
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.http.url.query.buildQueryURL
import matt.lang.NEVER
import matt.lang.opt
import matt.log.warn.warn
import matt.obs.prop.BindableProperty
import matt.obs.prop.Var
import matt.prim.str.urlEncode
import java.awt.Desktop
import java.net.URI


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


fun TabPaneWrapper<TabWrapper<*>>.lazyTab(name: String, nodeOp: ()->NodeWrapper) = TabWrapper<NW>(name).apply {

  if (isSelected) {
	content = nodeOp()
  } else {
	selectedProperty.onChangeUntilInclusive({ true }) {
	  if (it == true) {
		content = nodeOp()
	  }
	}
  }
  this@lazyTab.tabs += this
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


/*does not belong in this file*/
fun mail(
  address: String,
  subject: String? = null,
  body: String? = null
) {
  val desktop = Desktop.getDesktop()
  val message = mailtoURL(address, subject = subject, body = body)
  val uri: URI = URI.create(message)
  desktop.mail(uri)
}

/*does not belong in this file*/
fun mailtoURL(
  address: String,
  subject: String? = null,
  body: String? = null
) = buildQueryURL(
  "mailto:$address",
  *opt(subject) { "subject" to this.urlEncode() },
  *opt(body) { "body" to this.urlEncode() }
)