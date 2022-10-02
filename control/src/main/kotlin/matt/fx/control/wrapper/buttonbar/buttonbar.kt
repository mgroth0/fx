package matt.fx.control.wrapper.buttonbar

import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.ButtonBar
import matt.hurricanefx.wrapper.control.ControlWrapperImpl
import matt.hurricanefx.wrapper.control.button.ButtonWrapper
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.obs.bindings.str.ObsS
import matt.obs.prop.ValProp

class ButtonBarWrapper(
  node: ButtonBar = ButtonBar(),
): ControlWrapperImpl<ButtonBar>(node) {
  companion object {
	fun ButtonBar.wrapped() = matt.fx.control.wrapper.buttonbar.ButtonBarWrapper(this)
  }


  val buttons: ObservableList<Node> get() = node.buttons

  var buttonOrder: String?
	get() = node.buttonOrder
	set(value) {
	  node.buttonOrder = value
	}

  override fun addChild(child: NodeWrapper, index: Int?) {
	require(index == null)
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