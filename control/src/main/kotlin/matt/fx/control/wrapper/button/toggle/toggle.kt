package matt.fx.control.wrapper.button.toggle

import javafx.beans.property.BooleanProperty
import javafx.scene.control.ToggleButton
import matt.fx.control.wrapper.control.button.base.ButtonBaseWrapper
import matt.fx.control.wrapper.control.value.HasWritableValue
import matt.hurricanefx.eye.lib.onChange
import matt.obs.prop.BindableProperty

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