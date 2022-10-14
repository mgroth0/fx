@file:Suppress("UNUSED_PARAMETER")

package matt.fx.control.wrapper.control.text.input

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ReadOnlyIntegerProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.scene.control.TextInputControl
import javafx.scene.paint.Paint
import matt.fx.control.tfx.control.mutateOnChange
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.graphics.wrapper.style.FXStyle
import matt.fx.graphics.wrapper.style.parseFXStyle
import matt.fx.graphics.wrapper.style.toStyleString
import matt.fx.graphics.wrapper.text.textlike.ColoredText
import matt.lang.err
import matt.log.warn.warn
import matt.obs.prop.BindableProperty

open class TextInputControlWrapper<N: TextInputControl>(node: N): ControlWrapperImpl<N>(node), ColoredText {

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }


  fun editableProperty(): BooleanProperty = node.editableProperty()

  override val textProperty by lazy { node.textProperty().toNonNullableProp() }

  override val textFillProperty by lazy {
	BindableProperty<Paint?>(null).apply {
	  warn("broken paint property...")
	  onChange {
		if (it == null) {
		  node.style = node.style.parseFXStyle().minus(FXStyle.`text-fill`).toStyleString()
		} else {
		  node.style = node.style.parseFXStyle().plus(FXStyle.`text-fill` to it.toString()).toStyleString()
		}

	  }
	}
  }


  var promptText: String
	get() = node.promptText
	set(value) {
	  node.promptText = value
	}

  fun promptTextProperty(): StringProperty = node.promptTextProperty()


  override val fontProperty by lazy { node.fontProperty().toNonNullableProp() }


  val caretPosition get() = node.caretPosition
  fun caretPositionProperty(): ReadOnlyIntegerProperty = node.caretPositionProperty()

  fun end() = node.end()
  fun home() = node.home()


  /**
   * Remove leading or trailing whitespace from a Text Input Control.
   */
  fun trimWhitespace() = focusedProperty.onChange { focused: Boolean? ->
	if (focused == null) err("here it is")
	if (!focused) text = text.trim()
  }

  /**
   * Remove any whitespace from a Text Input Control.
   */
  fun stripWhitespace() = textProperty.mutateOnChange { it?.replace(Regex("\\s*"), "") }

  /**
   * Remove any non integer values from a Text Input Control.
   */
  fun stripNonInteger() = textProperty.mutateOnChange { it?.replace(Regex("[^0-9-]"), "") }

  /**
   * Remove any non integer values from a Text Input Control.
   */
  fun stripNonNumeric(vararg allowedChars: String = arrayOf(".", ",", "-")) =
	textProperty.mutateOnChange { it?.replace(Regex("[^0-9${allowedChars.joinToString("")}]"), "") }


  fun editableWhen(predicate: ObservableValue<Boolean>) = apply {
	editableProperty().bind(predicate)
  }

}