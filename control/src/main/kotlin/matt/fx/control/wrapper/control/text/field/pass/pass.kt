package matt.fx.control.wrapper.control.text.field.pass

import javafx.scene.control.PasswordField
import matt.fx.control.wrapper.control.text.field.TextFieldWrapper

class PasswordFieldWrapper(
   node: PasswordField = PasswordField(),
): TextFieldWrapper(node) {
  companion object {
	fun PasswordField.wrapped() = PasswordFieldWrapper(this)
  }
}