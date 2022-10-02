package matt.fx.control.wrapper.button.toggle

import javafx.beans.property.BooleanProperty
import javafx.scene.control.ToggleButton
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.wrapper.control.button.base.ButtonBaseWrapper
import matt.hurricanefx.wrapper.control.value.HasWritableValue
import matt.obs.prop.BindableProperty

class ValuedToggleButton<V>(value: V): matt.fx.control.wrapper.button.toggle.ToggleButtonWrapper(ToggleButton()), HasWritableValue<V> {
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