package matt.fx.control.wrapper.control.slider

import javafx.geometry.Orientation
import javafx.scene.control.Slider
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.lang.delegation.lazyVarDelegate
import matt.obs.bind.smartBind
import matt.obs.prop.ObsVal

fun ET.slider(
    min: Number? = null,
    max: Number? = null,
    value: Number? = null,
    orientation: Orientation? = null,
    op: SliderWrapper.() -> Unit = {}
) = SliderWrapper().attachTo(this, op) {
    if (min != null) it.min = min.toDouble()
    if (max != null) it.max = max.toDouble()
    if (value != null) it.value = value.toDouble()
    if (orientation != null) it.orientation = orientation
}

fun <T> ET.slider(
    range: ClosedRange<T>,
    value: Number? = null,
    orientation: Orientation? = null,
    op: SliderWrapper.() -> Unit = {}
): SliderWrapper where T: Comparable<T>, T: Number = slider(range.start, range.endInclusive, value, orientation, op)

class SliderWrapper(
    node: Slider = Slider()
): ControlWrapperImpl<Slider>(node) {


    var max
        get() = node.max
        set(value) {
            node.max = value
        }

    var min
        get() = node.min
        set(value) {
            node.min = value
        }

    var orientation: Orientation
        get() = node.orientation
        set(value) {
            node.orientation = value
        }

    var value
        get() = node.value
        set(value) {
            node.value = value
        }

    val valueProperty by lazy { node.valueProperty().toNonNullableProp().cast<Double>(Double::class) }


    val valueChangingProperty: NonNullFXBackedBindableProp<Boolean> by lazy { node.valueChangingProperty().toNonNullableProp() }
    var valueChanging by lazyVarDelegate { valueChangingProperty }


    val snapToTicksProperty: NonNullFXBackedBindableProp<Boolean> by lazy { node.snapToTicksProperty().toNonNullableProp() }
    var isSnapToTicks by lazyVarDelegate { snapToTicksProperty }


    val showTickMarksProperty: NonNullFXBackedBindableProp<Boolean> by lazy { node.showTickMarksProperty().toNonNullableProp() }
    var isShowTickMarks by lazyVarDelegate { showTickMarksProperty }


    val showTickLabelsProperty: NonNullFXBackedBindableProp<Boolean> by lazy { node.showTickLabelsProperty().toNonNullableProp() }
    var isShowTickLabels by lazyVarDelegate { showTickLabelsProperty }

    val majorTickUnitProperty: NonNullFXBackedBindableProp<Number> by lazy { node.majorTickUnitProperty().toNonNullableProp() }
    var majorTickUnit by lazyVarDelegate { majorTickUnitProperty }

    val minorTickCountProperty: NonNullFXBackedBindableProp<Number> by lazy { node.minorTickCountProperty().toNonNullableProp() }
    var minorTickCount by lazyVarDelegate { minorTickCountProperty }
    override fun addChild(child: NodeWrapper, index: Int?) {
        TODO()
    }
}

fun SliderWrapper.bind(property: ObsVal<Double>, readonly: Boolean = false) =
    valueProperty.smartBind(property, readonly)
