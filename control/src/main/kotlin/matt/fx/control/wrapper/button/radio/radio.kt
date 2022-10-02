package matt.fx.control.wrapper.button.radio

import javafx.scene.control.RadioButton
import javafx.scene.control.ToggleGroup
import matt.fx.control.toggle.getToggleGroup
import matt.fx.control.wrapper.button.toggle.ToggleButtonWrapper
import matt.fx.control.wrapper.control.value.HasWritableValue
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.obs.prop.BindableProperty



/**
 * Create a radiobutton inside the current or given toggle group. The optional value parameter will be matched against
 * the extension property `selectedValueProperty()` on Toggle Group. If the #ToggleGroup.selectedValueProperty is used,
 * it's value will be updated to reflect the value for this radio button when it's selected.
 *
 * Likewise, if the `selectedValueProperty` of the ToggleGroup is updated to a value that matches the value for this
 * radiobutton, it will be automatically selected.
 */
fun <V> ET.radiobutton(
  text: String? = null,
  group: ToggleGroup? = getToggleGroup(),
  value: V,
  op: ValuedRadioButton<V>.()->Unit = {}
) = ValuedRadioButton(value).attachTo(this, op) {
  it.text = if (value != null && text == null) value.toString() else text ?: ""
  if (group != null) it.node.toggleGroup = group
}


class ValuedRadioButton<V>(value: V): RadioButtonWrapper(), HasWritableValue<V> {
  override val valueProperty = BindableProperty(value)
}

open class RadioButtonWrapper(
  node: RadioButton = RadioButton(),
): ToggleButtonWrapper(node) {

  constructor(text: String): this(RadioButton(text))

}