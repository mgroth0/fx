package matt.fx.control.wrapper.checkbox

import javafx.scene.control.CheckBox
import matt.fx.control.wrapper.control.button.base.ButtonBaseWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.bind.smartBind
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.obs.prop.Var
import matt.fx.graphics.wrapper.ET
fun ET.checkbox(
  text: String? = null, property: Var<Boolean>? = null, op: CheckBoxWrapper.()->Unit = {}
) = CheckBoxWrapper().apply { this.text = text!! }.attachTo(this, op) {
  if (property != null) it.bind(property)
}


class CheckBoxWrapper(
   node: CheckBox = CheckBox(),
): ButtonBaseWrapper<CheckBox>(node) {
  companion object {
	fun CheckBox.wrapped() = CheckBoxWrapper(this)
  }

  constructor(text: String?): this(CheckBox(text))


  var isSelected
	get() = node.isSelected
	set(value) {
	  node.isSelected = value
	}

  val selectedProperty: NonNullFXBackedBindableProp<Boolean> get() = node.selectedProperty().toNonNullableProp()
}

fun CheckBoxWrapper.bind(property: Var<Boolean>, readonly: Boolean = false) =
  selectedProperty.smartBind(property, readonly)