package matt.fx.control.wrapper.control.datepick

import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.scene.control.DatePicker
import matt.fx.control.wrapper.control.combo.ComboBoxBaseWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.bind.smartBind
import java.time.LocalDate

import matt.fx.graphics.wrapper.ET

fun ET.datepicker(op: DatePickerWrapper.()->Unit = {}) = DatePickerWrapper().attachTo(this, op)
fun ET.datepicker(property: Property<LocalDate>, op: DatePickerWrapper.()->Unit = {}) = datepicker().apply {
  bind(property)
  op(this)
}

class DatePickerWrapper(
   node: DatePicker = DatePicker(),
): ComboBoxBaseWrapper<LocalDate, DatePicker>(node) {

  companion object {
	fun DatePicker.wrapped() = DatePickerWrapper(this)
  }



}

fun DatePickerWrapper.bind(property: ObservableValue<LocalDate>, readonly: Boolean = false) =
  valueProperty().smartBind(property, readonly)