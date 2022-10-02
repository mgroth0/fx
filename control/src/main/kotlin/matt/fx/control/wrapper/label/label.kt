package matt.fx.control.wrapper.label

import javafx.scene.Node
import javafx.scene.control.Label
import matt.fx.control.wrapper.labeled.LabeledWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper

open class LabelWrapper(
  node: Label = Label(),
): LabeledWrapper<Label>(node) {

  constructor(
	text: String?,
	graphic: Node? = null,
	wrap: Boolean? = null
  ): this(Label(text, graphic).apply {
	if (wrap != null) isWrapText = wrap
  })

  override fun addChild(child: NodeWrapper, index: Int?) {
	require(index == null)
	graphic = child
  }


}