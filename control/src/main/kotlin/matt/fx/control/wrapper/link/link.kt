package matt.fx.control.wrapper.link

import javafx.scene.control.Hyperlink
import matt.fx.control.wrapper.control.button.base.ButtonBaseWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.obs.bindings.str.ObsS

fun ET.hyperlink(text: String = "", graphic: NodeWrapper? = null, op: HyperlinkWrapper.()->Unit = {}) =
  HyperlinkWrapper().apply { this.text = text;this.graphic = graphic }.attachTo(this, op)

fun ET.hyperlink(
  observable: ObsS,
  graphic: NodeWrapper? = null,
  op: HyperlinkWrapper.()->Unit = {}
) = hyperlink(graphic = graphic).apply {
  textProperty.bind(observable)
  op(this)
}

class HyperlinkWrapper(
  node: Hyperlink = Hyperlink(),
): ButtonBaseWrapper<Hyperlink>(node)