package matt.fx.control.wrapper.control.text.field

import com.sun.javafx.scene.control.FakeFocusTextField
import javafx.event.ActionEvent
import javafx.scene.control.TextField
import matt.hurricanefx.wrapper.control.text.input.TextInputControlWrapper
import matt.hurricanefx.wrapper.control.value.HasWritableValue


open class TextFieldWrapper(
  node: TextField = TextField(),
): TextInputControlWrapper<TextField>(node), HasWritableValue<String> {
  constructor(text: String?): this(TextField(text))

  fun setOnAction(op: (ActionEvent)->Unit) {
	node.setOnAction(op)
  }

  override val valueProperty by lazy { textProperty }

  infix fun withPrompt(s: String): TextFieldWrapper {
    promptText = s
    return this
  }
}

fun TextFieldWrapper.action(op: ()->Unit) = setOnAction { op() }


class FakeFocusTextFieldWrapper(
  node: FakeFocusTextField
): TextFieldWrapper(node)