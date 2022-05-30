package matt.fx.web

import javafx.beans.property.SimpleDoubleProperty
import javafx.event.EventTarget
import javafx.scene.web.HTMLEditor
import javafx.scene.web.WebView
import matt.hurricanefx.eye.lang.DProp
import matt.hurricanefx.tornadofx.fx.attachTo
import matt.klib.lang.NEVER

fun WebView.exactWidthProperty() = SimpleDoubleProperty().also {
  minWidthProperty().bind(it)
  maxWidthProperty().bind(it)
}

fun WebView.exactHeightProperty() = SimpleDoubleProperty().also {
  minHeightProperty().bind(it)
  maxHeightProperty().bind(it)
}
var WebView.exactWidth: Number
  set(value) {
    exactWidthProperty().bind(DProp(value.toDouble()))
  }
  get() = NEVER
var WebView.exactHeight: Number
  set(value) {
    exactHeightProperty().bind(DProp(value.toDouble()))
  }
  get() = NEVER

fun EventTarget.webview(op: WebView.() -> Unit = {}) = WebView().attachTo(this, op)

fun EventTarget.htmleditor(html: String? = null, op: HTMLEditor.() -> Unit = {}) = HTMLEditor().attachTo(this, op) {
  if (html != null) it.htmlText = html
}