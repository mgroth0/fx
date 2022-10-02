package matt.fx.control.wrapper.control.text.area

import javafx.beans.property.BooleanProperty
import javafx.scene.control.TextArea
import matt.hurricanefx.eye.prop.getValue
import matt.hurricanefx.eye.prop.setValue
import matt.hurricanefx.wrapper.control.text.input.TextInputControlWrapper
import matt.hurricanefx.wrapper.node.NodeWrapper

open class TextAreaWrapper(
  node: TextArea = TextArea(),
): TextInputControlWrapper<TextArea>(node) {

  constructor(text: String): this(TextArea(text))

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }


  val wrapTextProperty: BooleanProperty get() = node.wrapTextProperty()
  var isWrapText by node.wrapTextProperty()

}