package matt.fx.control.wrapper.control.colorpick

import javafx.scene.control.ColorPicker
import javafx.scene.paint.Color
import matt.fx.control.wrapper.control.combo.ComboBoxBaseWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.bind.smartBind
import matt.obs.prop.ObsVal
import matt.obs.prop.VarProp


fun ET.colorpicker(
  color: Color? = null, op: ColorPickerWrapper.()->Unit = {}
) = ColorPickerWrapper().attachTo(this, op) {
  if (color != null) it.value = color
}

fun ET.colorpicker(
  colorProperty: VarProp<Color?>, op: ColorPickerWrapper.()->Unit = {}
) = ColorPickerWrapper().apply { bind(colorProperty) }.attachTo(this, op) {}

class ColorPickerWrapper(
   node: ColorPicker = ColorPicker(),
): ComboBoxBaseWrapper<Color, ColorPicker>(node) {
  companion object {
	fun ColorPicker.wrapped() = ColorPickerWrapper(this)
  }

}

fun ColorPickerWrapper.bind(property: ObsVal<Color?>, readonly: Boolean = false) =
  valueProperty.smartBind(property, readonly)