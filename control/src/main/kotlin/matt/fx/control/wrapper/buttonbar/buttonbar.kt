package matt.fx.control.wrapper.buttonbar

import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.ButtonBar
import matt.fx.control.inter.graphic
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.button.ButtonWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.lang.assertions.require.requireNull
import matt.obs.bindings.str.ObsS

fun ET.buttonbar(buttonOrder: String? = null, op: (ButtonBarWrapper.()->Unit)) = ButtonBarWrapper().attachTo(this, op) {
  if (buttonOrder != null) it.buttonOrder = buttonOrder
}

class ButtonBarWrapper(
  node: ButtonBar = ButtonBar(),
): ControlWrapperImpl<ButtonBar>(node) {


  val buttons: ObservableList<Node> get() = node.buttons

  var buttonOrder: String?
	get() = node.buttonOrder
	set(value) {
	  node.buttonOrder = value
	}

  override fun addChild(child: NodeWrapper, index: Int?) {
	requireNull(index)
	buttons.add(child.node)
  }

  //  fun setButtonData() = node.butt


  fun button(
	text: String = "",
	type: ButtonBar.ButtonData? = null,
	graphic: NodeWrapper? = null,
	op: ButtonWrapper.()->Unit = {}
  ) = ButtonWrapper().apply { this.text = text }.also {
	if (type != null) ButtonBar.setButtonData(it.node, type)
	if (graphic != null) it.graphic = graphic
	buttons += it.node
	op(it)
  }

  fun button(
	text: ObsS,
	type: ButtonBar.ButtonData? = null,
	graphic: NodeWrapper? = null,
	op: ButtonWrapper.()->Unit = {}
  ) = ButtonWrapper().also {
	it.textProperty.bind(text)
	if (type != null) ButtonBar.setButtonData(it.node, type)
	if (graphic != null) it.graphic = graphic
	buttons += it.node
	op(it)
  }

}