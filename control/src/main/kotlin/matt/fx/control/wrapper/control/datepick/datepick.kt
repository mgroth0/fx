package matt.fx.control.wrapper.control.datepick

import javafx.scene.control.DatePicker
import matt.fx.control.wrapper.control.combo.ComboBoxBaseWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.obs.bind.smartBind
import matt.obs.prop.ObsVal
import java.time.LocalDate

fun ET.datepicker(op: DatePickerWrapper.() -> Unit = {}) = DatePickerWrapper().attachTo(this, op)

class DatePickerWrapper(
    node: DatePicker = DatePicker()
): ComboBoxBaseWrapper<LocalDate, DatePicker>(node)

fun DatePickerWrapper.bind(property: ObsVal<LocalDate?>, readonly: Boolean = false) =
    valueProperty.smartBind(property, readonly)
