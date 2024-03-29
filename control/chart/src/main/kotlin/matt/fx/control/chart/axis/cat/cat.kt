package matt.fx.control.chart.axis.cat

import javafx.collections.ObservableList
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.control.chart.axis.AxisWrapper
import matt.fx.control.chart.axis.cat.cat.CategoryAxisForCatAxisWrapper

class CategoryAxisWrapper(node: CategoryAxisForCatAxisWrapper = CategoryAxisForCatAxisWrapper()) :
    AxisWrapper<String, CategoryAxisForCatAxisWrapper>(node) {
    val categoriesProp: NonNullFXBackedBindableProp<ObservableList<String>> = node.categories.toNonNullableProp()
    var categories by categoriesProp
}
