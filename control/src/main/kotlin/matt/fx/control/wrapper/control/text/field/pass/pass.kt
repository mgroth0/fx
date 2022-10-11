package matt.fx.control.wrapper.control.text.field.pass

import javafx.scene.control.PasswordField
import matt.fx.control.wrapper.control.text.field.TextFieldWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.obs.prop.VarProp

fun ET.passwordfield(value: String? = null, op: PasswordFieldWrapper.()->Unit = {}) =
  PasswordFieldWrapper().attachTo(this, op) {
	if (value != null) it.text = value
  }

fun ET.passwordfield(property: VarProp<String>, op: PasswordFieldWrapper.()->Unit = {}) = passwordfield().apply {
  textProperty.bindBidirectional(property)
  op(this)
}

class PasswordFieldWrapper(
  node: PasswordField = PasswordField(),
): TextFieldWrapper(node) {
}