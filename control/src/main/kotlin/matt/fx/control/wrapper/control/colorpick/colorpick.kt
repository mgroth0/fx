package matt.fx.control.wrapper.control.colorpick

import javafx.beans.value.ObservableValue
import javafx.scene.control.ColorPicker
import javafx.scene.paint.Color
import matt.fx.control.wrapper.control.combo.ComboBoxBaseWrapper
import matt.hurricanefx.eye.bind.smartBind

class ColorPickerWrapper(
   node: ColorPicker = ColorPicker(),
): ComboBoxBaseWrapper<Color, ColorPicker>(node) {
  companion object {
	fun ColorPicker.wrapped() = ColorPickerWrapper(this)
  }

}

fun ColorPickerWrapper.bind(property: ObservableValue<Color>, readonly: Boolean = false) =
  valueProperty().smartBind(property, readonly)