package matt.fx.control.wrapper.control.text.field

import com.sun.javafx.scene.control.FakeFocusTextField
import javafx.event.ActionEvent
import javafx.scene.control.TextField
import matt.fx.control.wrapper.control.text.input.TextInputControlWrapper
import matt.fx.control.wrapper.control.value.HasWritableValue
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.model.op.convert.Converter
import matt.model.op.convert.MyNumberStringConverter
import matt.model.op.convert.StringConverter
import matt.obs.prop.Var
import kotlin.reflect.KClass


val converters = mapOf<KClass<*>, StringConverter<*>>(
  Number::class to MyNumberStringConverter
)


inline fun <reified T: Any> ET.textfield(property: Var<T>, op: TextFieldWrapper.()->Unit = {}) =
  textfield().apply {
	if (T::class == String::class) {
	  @Suppress("UNCHECKED_CAST")
	  textProperty.bindBidirectional(property as Var<String>)
	} else {
	  @Suppress("UNCHECKED_CAST")
	  textProperty.bindBidirectional(property, converters[T::class]!! as Converter<String, T>)
	}

	op(this)
  }

///*@JvmName("textfieldNumber") */inline fun ET.textfield(
//  property: Var<Number>,
//  op: TextFieldWrapper.()->Unit = {}
//): TextFieldWrapper = textfield().apply {
//  textProperty.bindBidirectional(property, MyNumberStringConverter)
//  op(this)
//}

//@JvmName("textfieldInt") fun EventTargetWrapper.textfield(
//  property: ValProp<Int>,
//  op: TextFieldWrapper.()->Unit = {}
//) = textfield().apply {
//  bind(property)
//  op(this)
//}

fun ET.textfield(value: String? = null, op: TextFieldWrapper.()->Unit = {}) = TextFieldWrapper().attachTo(this, op) {
  if (value != null) it.text = value
}

fun <T> ET.textfield(
  property: Var<T>, converter: StringConverter<T>, op: TextFieldWrapper.()->Unit = {}
) = textfield().apply {
  textProperty.bindBidirectional(property, converter)
  op(this)
}


open class TextFieldWrapper(
  node: TextField = TextField(),
): TextInputControlWrapper<TextField>(node), HasWritableValue<String> {
  constructor(text: String?): this(TextField(text))

  fun setOnAction(op: (ActionEvent)->Unit) {
	node.setOnAction(op)
  }

  override val valueProperty by lazy { textProperty }

  infix fun withPrompt(s: String): TextFieldWrapper {
	promptText = s
	return this
  }
}

fun TextFieldWrapper.action(op: ()->Unit) = setOnAction { op() }


class FakeFocusTextFieldWrapper(
  node: FakeFocusTextField
): TextFieldWrapper(node)


