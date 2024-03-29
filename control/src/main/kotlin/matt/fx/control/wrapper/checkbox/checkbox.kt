package matt.fx.control.wrapper.checkbox

import javafx.scene.control.CheckBox
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.control.wrapper.control.button.base.ButtonBaseWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.lang.assertions.require.requireNull
import matt.obs.bind.smartBind
import matt.obs.prop.writable.Var

fun ET.checkbox(
    text: String = "",
    property: Var<Boolean>? = null,
    weakBothWays: Boolean? = null,
    op: CheckBoxWrapper.() -> Unit = {}
) = CheckBoxWrapper().apply { this.text = text }.attachTo(this, op) {
    if (property != null) it.bind(property, weakBothWays = weakBothWays ?: false)
    else {
        requireNull(weakBothWays) {
            "setting weakBothWays does not make sense if property is not set"
        }
    }
}


class CheckBoxWrapper(
    node: CheckBox = CheckBox()
) : ButtonBaseWrapper<CheckBox>(node) {

    constructor(text: String?) : this(CheckBox(text))


    var isSelected
        get() = node.isSelected
        set(value) {
            node.isSelected = value
        }

    val selectedProperty: NonNullFXBackedBindableProp<Boolean> get() = node.selectedProperty().toNonNullableProp()


    var isAllowIndeterminate
        get() = node.isAllowIndeterminate
        set(value) {
            node.isAllowIndeterminate = value
        }

    val allowIndeterminateProperty: NonNullFXBackedBindableProp<Boolean> get() = node.allowIndeterminateProperty().toNonNullableProp()

    var isIndeterminate
        get() = node.isIndeterminate
        set(value) {
            node.isIndeterminate = value
        }

    val indeterminateProperty: NonNullFXBackedBindableProp<Boolean> get() = node.indeterminateProperty().toNonNullableProp()
}

fun CheckBoxWrapper.bind(
    property: Var<Boolean>,
    readonly: Boolean = false,
    weakBothWays: Boolean = false
) =
    selectedProperty.smartBind(property, readonly, weak = weakBothWays)
