package matt.fx.control.wrapper.button.toggle

import javafx.beans.property.BooleanProperty
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import matt.fx.control.toggle.getToggleGroup
import matt.fx.control.wrapper.control.button.base.ButtonBaseWrapper
import matt.fx.control.wrapper.control.value.HasWritableValue
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.lib.onChange
import matt.lang.go
import matt.obs.prop.BindableProperty
import matt.fx.graphics.wrapper.ET
import matt.obs.prop.VarProp

/**
 * Create a togglebutton inside the current or given toggle group. The optional value parameter will be matched against
 * the extension property `selectedValueProperty()` on Toggle Group. If the #ToggleGroup.selectedValueProperty is used,
 * it's value will be updated to reflect the value for this radio button when it's selected.
 *
 * Likewise, if the `selectedValueProperty` of the ToggleGroup is updated to a value that matches the value for this
 * togglebutton, it will be automatically selected.
 */
fun <V> ET.togglebutton(
  text: String? = null,
  group: ToggleGroup? = getToggleGroup(),
  selectFirst: Boolean = false,
  value: V,
  op: ValuedToggleButton<V>.()->Unit = {}
) = ValuedToggleButton(value).attachTo(this, op) {
  it.text = if (value != null && text == null) value.toString() else text ?: ""
  if (group != null) it.node.toggleGroup = group
  if (it.node.toggleGroup?.selectedToggle == null && selectFirst) it.isSelected = true
}

fun <V> ET.togglebutton(
  text: VarProp<String>? = null,
  group: ToggleGroup? = getToggleGroup(),
  selectFirst: Boolean = false,
  value: V,
  op: ValuedToggleButton<V>.()->Unit = {}
) = ValuedToggleButton(value).attachTo(this, op) {
  val thing = it
  text?.go { thing.textProperty.bind(it) }
  if (group != null) it.node.toggleGroup = group
  if (it.node.toggleGroup?.selectedToggle == null && selectFirst) it.isSelected = true
}

fun <V> ET.togglebutton(
  group: ToggleGroup? = getToggleGroup(),
  selectFirst: Boolean = false,
  value: V,
  op: ValuedToggleButton<V>.()->Unit = {}
) = ValuedToggleButton(value).attachTo(this, op) {
  if (group != null) it.node.toggleGroup = group
  if (it.node.toggleGroup?.selectedToggle == null && selectFirst) it.isSelected = true
}



class ValuedToggleButton<V>(value: V): ToggleButtonWrapper(ToggleButton()),
									   HasWritableValue<V> {
  override val valueProperty = BindableProperty(value)
}

open class ToggleButtonWrapper(
  node: ToggleButton = ToggleButton(),
): ButtonBaseWrapper<ToggleButton>(node) {

  var isSelected
	get() = node.isSelected
	set(value) {
	  node.isSelected = value
	}


  //    var toggleGroup: ToggleGroup
  //  	get() = node.toggleGroup
  //  	set(value) {
  //  	  node.toggleGroup = value
  //  	}

  val selectedProperty: BooleanProperty get() = node.selectedProperty()

  fun whenSelected(op: ()->Unit) {
	selectedProperty.onChange { if (it) op() }
  }




}