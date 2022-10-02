package matt.fx.control.wrapper.menu.checkitem

import javafx.scene.Node
import javafx.scene.control.CheckMenuItem
import matt.fx.control.wrapper.menu.item.MenuItemWrapper
import matt.hurricanefx.eye.bind.smartBind
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.obs.prop.ValProp

class CheckMenuItemWrapper(
  node: CheckMenuItem = CheckMenuItem(),
): MenuItemWrapper<CheckMenuItem>(node) {



  constructor(text: String, graphic: Node? = null): this(CheckMenuItem(text, graphic))


  var isSelected
	get() = node.isSelected
	set(value) {
	  node.isSelected = value
	}

  val selectedProperty: NonNullFXBackedBindableProp<Boolean> = node.selectedProperty().toNonNullableProp()
}

fun CheckMenuItemWrapper.bind(property: ValProp<Boolean>, readonly: Boolean = false) =
  selectedProperty.smartBind(property, readonly)