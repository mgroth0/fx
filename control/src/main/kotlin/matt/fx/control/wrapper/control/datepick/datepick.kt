package matt.fx.control.wrapper.control.datepick

import javafx.beans.value.ObservableValue
import javafx.scene.control.DatePicker
import matt.hurricanefx.eye.bind.smartBind
import matt.hurricanefx.wrapper.control.combo.ComboBoxBaseWrapper
import java.time.LocalDate

class DatePickerWrapper(
   node: DatePicker = DatePicker(),
): ComboBoxBaseWrapper<LocalDate,DatePicker>(node) {

  companion object {
	fun DatePicker.wrapped() = DatePickerWrapper(this)
  }



}

fun DatePickerWrapper.bind(property: ObservableValue<LocalDate>, readonly: Boolean = false) =
  valueProperty().smartBind(property, readonly)