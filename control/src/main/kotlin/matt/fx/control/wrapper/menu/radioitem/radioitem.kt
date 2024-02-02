package matt.fx.control.wrapper.menu.radioitem

import javafx.beans.property.BooleanProperty
import javafx.scene.Node
import javafx.scene.control.RadioMenuItem
import matt.fx.control.wrapper.control.value.HasWritableValue
import matt.fx.control.wrapper.menu.item.MenuItemWrapper
import matt.obs.prop.BindableProperty

class ValuedRadioMenuItem<V>(value: V, text: String, graphic: Node? = null): RadioMenuItemWrapper(text, graphic),
    HasWritableValue<V> {
    override val valueProperty = BindableProperty(value)
}

open class RadioMenuItemWrapper(
    node: RadioMenuItem = RadioMenuItem(),
): MenuItemWrapper<RadioMenuItem>(node) {

    constructor(text: String, graphic: Node? = null): this(RadioMenuItem(text, graphic))


    var isSelected
        get() = node.isSelected
        set(value) {
            node.isSelected = value
        }

    fun selectedProperty(): BooleanProperty = node.selectedProperty()
}
