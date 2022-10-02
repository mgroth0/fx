package matt.fx.control.wrapper.control.button.base

import javafx.event.ActionEvent
import javafx.scene.control.ButtonBase
import matt.http.url.MURL
import matt.hurricanefx.wrapper.labeled.LabeledWrapper
import matt.hurricanefx.wrapper.node.NodeWrapper
import java.awt.Desktop
import java.net.URI
import java.net.URL

open class ButtonBaseWrapper<N: ButtonBase>(node: N): LabeledWrapper<N>(node) {

  fun fire() = node.fire()
  fun setOnAction(op: (ActionEvent)->Unit) {
	node.setOnAction(op)
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