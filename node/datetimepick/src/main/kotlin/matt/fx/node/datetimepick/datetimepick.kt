/*ORIGINALLY FROM TORNADOFX*/
@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package matt.fx.node.datetimepick


import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.scene.control.DatePicker
import javafx.util.StringConverter
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import matt.time.currentTime
import matt.time.nowLocal
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.LocalDate as JLocalDate
import java.time.LocalDateTime as JLocalDateTime


/**
 * A DateTimePicker with configurable datetime format where both date and time can be changed
 * via the text field and the date can additionally be changed via the JavaFX default date picker.
 */
class DateTimePicker : DatePicker() {
    private var formatter: DateTimeFormatter? = null
    private val dateTimeValue: ObjectProperty<LocalDateTime?> = SimpleObjectProperty(nowLocal())
    private val format: ObjectProperty<String> =
        object : SimpleObjectProperty<String>() {
            override fun set(newValue: String) {
                super.set(newValue)
                formatter = DateTimeFormatter.ofPattern(newValue)
            }
        }

    fun alignColumnCountWithFormat() {
        editor.prefColumnCount = getFormat().length
    }

    fun pleasework(): LocalDateTime? {
        converter.fromString(editor.text)
        return dateTimeValue.get()
    }


    fun simulateEnterPressed() {
        editor.commitValue()
    }

    fun getDateTimeValue(): LocalDateTime? = dateTimeValue.get()

    fun setDateTimeValue(dateTimeValue: LocalDateTime?) {
        this.dateTimeValue.set(dateTimeValue)
    }

    fun dateTimeValueProperty(): ObjectProperty<LocalDateTime?> = dateTimeValue

    fun getFormat(): String = format.get()

    fun formatProperty(): ObjectProperty<String> = format

    fun setFormat(format: String) {
        this.format.set(format)
        alignColumnCountWithFormat()
    }

    internal inner class InternalConverter : StringConverter<JLocalDate?>() {
        override fun toString(`object`: JLocalDate?): String {
            val value = getDateTimeValue()
            return if (value != null) value.toJavaLocalDateTime().format(formatter) else ""
        }

        override fun fromString(value: String): JLocalDate? {
            if (value.isEmpty()) {
                dateTimeValue.set(null)
                return null
            }
            return try {
                dateTimeValue.set(JLocalDateTime.parse(value, formatter).toKotlinLocalDateTime())
                dateTimeValue.get()!!.toJavaLocalDateTime().toLocalDate()
            } catch (e: DateTimeParseException) {
                dateTimeValue.set(null)
                null
            }
        }
    }

    companion object {
        const val DefaultFormat = "yyyy-MM-dd HH:mm"
    }

    init {
        styleClass.add("datetime-picker")
        setFormat(DefaultFormat)
        converter = InternalConverter()
        alignColumnCountWithFormat()

        /* Synchronize changes to the underlying date value back to the dateTimeValue */
        valueProperty().addListener { _: ObservableValue<out JLocalDate?>?, _: JLocalDate?, newValue: JLocalDate? ->
            if (newValue == null) {
                dateTimeValue.set(null)
            } else {
                if (dateTimeValue.get() == null) {
                    dateTimeValue.set(
                        LocalDateTime(newValue.toKotlinLocalDate(), currentTime())
                    )
                } else {
                    val time = dateTimeValue.get()!!.time
                    dateTimeValue.set(LocalDateTime(newValue.toKotlinLocalDate(), time))
                }
            }
        }

        /* Synchronize changes to dateTimeValue back to the underlying date value */
        dateTimeValue.addListener { _: ObservableValue<out LocalDateTime?>?, _: LocalDateTime?, newValue: LocalDateTime? ->
            if (newValue != null) {
                val dateValue = newValue.date
                val forceUpdate = dateValue == valueProperty().get().toKotlinLocalDate()
                /* Make sure the display is updated even when the date itself wasn't changed */
                value = dateValue.toJavaLocalDate()
                if (forceUpdate) converter = InternalConverter()
            } else {
                value = null
            }
        }

        /* Persist changes onblur */
        editor.focusedProperty()
            .addListener { _: ObservableValue<out Boolean?>?, _: Boolean?, newValue: Boolean? -> if (!newValue!!) simulateEnterPressed() }
    }
}
