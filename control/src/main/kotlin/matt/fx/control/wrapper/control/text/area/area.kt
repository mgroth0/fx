package matt.fx.control.wrapper.control.text.area

import javafx.beans.property.BooleanProperty
import javafx.scene.control.TextArea
import matt.fx.control.wrapper.control.text.input.TextInputControlWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.model.convert.StringConverter
import matt.obs.prop.VarProp

fun ET.textarea(value: String? = null, op: TextAreaWrapper.()->Unit = {}) = TextAreaWrapper().attachTo(this, op) {
  if (value != null) it.text = value
}


fun ET.textarea(property: VarProp<String>, op: TextAreaWrapper.()->Unit = {}) = textarea().apply {
  textProperty.bindBidirectional(property)
  op(this)
}


fun <T> ET.textarea(
  property: VarProp<T>, converter: StringConverter<T>, op: TextAreaWrapper.()->Unit = {}
) = textarea().apply {
  textProperty.bindBidirectional(property, converter)
  op(this)
}


open class TextAreaWrapper(
  node: TextArea = TextArea(),
): TextInputControlWrapper<TextArea>(node) {

  constructor(text: String): this(TextArea(text))

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }


  val wrapTextProperty by lazy { node.wrapTextProperty().toNonNullableProp() }
  var isWrapText by wrapTextProperty

}