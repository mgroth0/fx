package matt.fx.graphics.wrapper.text

import javafx.beans.property.ObjectProperty
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper
import matt.fx.graphics.wrapper.text.textlike.TextLike
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.obs.bindings.str.ObsS

fun ET.text(op: TextWrapper.()->Unit = {}) = TextWrapper().attachTo(this, op)

fun ET.text(initialValue: String? = null, op: TextWrapper.()->Unit = {}) = TextWrapper().attachTo(this, op) {
  if (initialValue != null) it.text = initialValue
}


fun ET.text(observable: ObsS, op: TextWrapper.()->Unit = {}) = text().apply {
  textProperty.bind(observable)
  op(this)
}

open class TextWrapper(
  node: Text = Text(),
): ShapeWrapper<Text>(node), TextLike {

  constructor(text: String): this(Text(text))


  override val textProperty by lazy { node.textProperty().toNonNullableProp() }
  override val fontProperty by lazy { node.fontProperty().toNonNullableProp() }
  override val textFillProperty by lazy { node.fillProperty().toNonNullableProp() }

  val wrappingWidthProperty get() = node.wrappingWidthProperty()

  var textAlignment: TextAlignment
	get() = node.textAlignment
	set(value) {
	  node.textAlignment = value
	}

  fun textAlignmentProperty(): ObjectProperty<TextAlignment> = node.textAlignmentProperty()

}