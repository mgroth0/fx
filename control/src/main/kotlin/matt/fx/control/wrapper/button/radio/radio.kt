package matt.fx.control.wrapper.button.radio

import javafx.scene.control.RadioButton
import matt.obs.prop.BindableProperty

class ValuedRadioButton<V>(value: V): matt.fx.control.wrapper.button.radio.RadioButtonWrapper(), HasWritableValue<V> {
  override val valueProperty = BindableProperty(value)
}

open class RadioButtonWrapper(
  node: RadioButton = RadioButton(),
): ToggleButtonWrapper(node) {

  constructor(text: String): this(RadioButton(text))

}