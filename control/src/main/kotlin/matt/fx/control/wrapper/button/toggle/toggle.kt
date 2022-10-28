package matt.fx.control.wrapper.button.toggle

import javafx.scene.control.ToggleButton
import matt.fx.control.inter.select.Selectable
import matt.fx.control.inter.select.SelectableValue
import matt.fx.control.tfx.control.ToggleMechanism
import matt.fx.control.wrapper.control.button.base.ButtonBaseWrapper
import matt.fx.control.wrapper.control.value.HasWritableValue
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.lang.go
import matt.obs.listen.OldAndNewListener
import matt.obs.prop.BindableProperty
import matt.obs.prop.VarProp

/**
 * Create a togglebutton inside the current or given toggle group. The optional value parameter will be matched against
 * the extension property `selectedValueProperty()` on Toggle Group. If the #ToggleGroup.selectedValueProperty is used,
 * it's value will be updated to reflect the value for this radio button when it's selected.
 *
 * Likewise, if the `selectedValueProperty` of the ToggleGroup is updated to a value that matches the value for this
 * togglebutton, it will be automatically selected.
 */
fun <V: Any> ET.togglebutton(
  text: String? = null,
  /*group: ToggleGroup? = getToggleGroup(),*/
  group: ToggleMechanism<V>? = null,
  selectFirst: Boolean = false,
  value: V,
  op: ValuedToggleButton<V>.()->Unit = {}
) = ValuedToggleButton(value).attachTo(this, op) {
  it.text = if (value != null && text == null) value.toString() else text ?: ""
  if (group != null) it.toggleMechanism.value = group
  if (it.node.toggleGroup?.selectedToggle == null && selectFirst) it.isSelected = true
}

fun <V: Any> ET.togglebutton(
  text: VarProp<String>? = null,
  group: ToggleMechanism<V>? = null,
  selectFirst: Boolean = false,
  value: V,
  op: ValuedToggleButton<V>.()->Unit = {}
) = ValuedToggleButton(value).attachTo(this, op) {
  val thing = it
  text?.go { thing.textProperty.bind(it) }
  if (group != null) it.toggleMechanism.value = group
  if (it.node.toggleGroup?.selectedToggle == null && selectFirst) it.isSelected = true
}

fun <V: Any> ET.togglebutton(
  group: ToggleMechanism<V>? = null,
  selectFirst: Boolean = false,
  value: V,
  op: ValuedToggleButton<V>.()->Unit = {}
) = ValuedToggleButton(value).attachTo(this, op) {
  if (group != null) it.toggleMechanism.value = group
  if (it.node.toggleGroup?.selectedToggle == null && selectFirst) it.isSelected = true
}


class ValuedToggleButton<V: Any>(value: V): ToggleButtonWrapper(ToggleButton()),
											HasWritableValue<V>,
											SelectableValue<V> {

  override val valueProperty = BindableProperty(value)

  val toggleMechanism = BindableProperty<ToggleMechanism<V>?>(null).apply {
	addListener(OldAndNewListener { old, new ->
	  old?.toggles?.remove(this@ValuedToggleButton)
	  new?.toggles?.add(this@ValuedToggleButton)
	})
  }

}

open class ToggleButtonWrapper(
  node: ToggleButton = ToggleButton(),
): ButtonBaseWrapper<ToggleButton>(node), Selectable {

  override val selectedProperty by lazy { node.selectedProperty().toNonNullableProp() }

  fun whenSelected(op: ()->Unit) {
	selectedProperty.onChange { if (it) op() }
  }

}