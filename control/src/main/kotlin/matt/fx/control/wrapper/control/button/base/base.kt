package matt.fx.control.wrapper.control.button.base

import javafx.event.ActionEvent
import javafx.scene.control.ButtonBase
import matt.fx.control.inter.graphic
import matt.fx.control.wrapper.labeled.LabeledWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.http.url.MURL
import matt.hurricanefx.eye.wrapper.obs.obsval.toNonNullableROProp
import matt.lang.NOT_IMPLEMENTED
import java.awt.Desktop
import java.net.URI
import java.net.URL

open class ButtonBaseWrapper<N: ButtonBase>(node: N): LabeledWrapper<N>(node) {

  val armedProp by lazy {
	node.armedProperty().toNonNullableROProp()
  }

  fun fire() = node.fire()
  fun setOnAction(op: (ActionEvent)->Unit) {
	node.setOnAction(op)
  }
  //  fun setOnAction(op: () -> Unit) {
  //	node.setOnAction { op() }
  //  }

  var onAction: ()->Unit
	get() = NOT_IMPLEMENTED
	set(value) {
	  setOnAction {
		value()
	  }
	}

  override fun addChild(child: NodeWrapper, index: Int?) {
	require(index == null)
	graphic = child
  }

  fun opens(uri: URI) {
	setOnAction {
	  Desktop.getDesktop().browse(uri)
	}
  }

  fun opens(url: URL) = opens(url.toURI())
  fun opens(url: MURL) = opens(url.jURL.toURI())

}

fun ButtonBaseWrapper<*>.action(op: ()->Unit) = setOnAction { op() }