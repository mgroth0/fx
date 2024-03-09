package matt.fx.control.chart.axis.value

import javafx.util.StringConverter
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.NullableFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.control.chart.axis.AxisWrapper
import matt.fx.control.chart.axis.value.moregenval.MoreGenericValueAxis


abstract class ValueAxisWrapper<T: Any>(node: MoreGenericValueAxis<T>): AxisWrapper<T, MoreGenericValueAxis<T>>(node) {
    private val superNode = node
    val minorTickCountProperty: NonNullFXBackedBindableProp<Number> by lazy { node.minorTickCountProperty().toNonNullableProp() }
    var minorTickCount by minorTickCountProperty
    val lowerBoundProperty get() = superNode.lowerBound
    var lowerBound by lowerBoundProperty
    val upperBoundProperty get() = superNode.upperBound
    var upperBound by upperBoundProperty
    val tickLabelFormatterProperty: NullableFXBackedBindableProp<StringConverter<in T>> by lazy {
        node.tickLabelFormatterProperty().toNullableProp()
    }
    var tickLabelFormatter by tickLabelFormatterProperty
    val minorTickVisibleProperty: NonNullFXBackedBindableProp<Boolean> by lazy { node.minorTickVisibleProperty().toNonNullableProp() }
    var isMinorTickVisible by minorTickVisibleProperty
}







