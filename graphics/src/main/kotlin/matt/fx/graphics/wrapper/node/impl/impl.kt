package matt.fx.graphics.wrapper.node.impl

import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.Scene
import matt.fx.base.wrapper.obs.obsval.prop.NullableFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.base.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.base.wrapper.obs.obsval.toNullableROProp
import matt.fx.graphics.service.uncheckedNullableWrapperConverter
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.fx.graphics.wrapper.style.StyleableWrapper
import matt.fx.graphics.wrapper.style.StyleableWrapperImpl2
import matt.lang.delegation.lazyDelegate
import matt.model.flowlogic.recursionblocker.RecursionBlocker
import matt.obs.bind.binding
import matt.obs.prop.BindableProperty

//private operator fun <T, V> ReadOnlyProperty<T, V>.getValue(t: T, property: KProperty<*>): V {
//	return error("abc")
//}
abstract class NodeWrapperImpl<out N : Node>(
    node: N
) : SingularEventTargetWrapper<N>(node), StyleableWrapper by StyleableWrapperImpl2(node), NodeWrapper {



    final override val cursorProperty: NullableFXBackedBindableProp<Cursor> by lazy {
        node.cursorProperty().toNullableProp()
    }

    final override val layoutYProperty: BindableProperty<Double> by lazy {
        node.layoutYProperty().toNonNullableProp().cast()
    }
    final override val layoutXProperty: BindableProperty<Double> by lazy {
        node.layoutXProperty().toNonNullableProp().cast()
    }
    final override val scaleXProperty: BindableProperty<Double> by lazy {
        node.scaleXProperty().toNonNullableProp().cast()
    }
    final override val scaleYProperty: BindableProperty<Double> by lazy {
        node.scaleYProperty().toNonNullableProp().cast()
    }

    final override val sceneProperty by lazy {
        node.sceneProperty().toNullableROProp().binding(
            converter = uncheckedNullableWrapperConverter<Scene, SceneWrapper<*>>()
        )
    }

    final override val scene by lazyDelegate {    /*lazy because there is an issue where the inner mechanics of this property causes the wrong scene wrapper to be built during the SceneWrapper's initialization*/
        sceneProperty
    }


    final override val focusedProperty by lazy { node.focusedProperty().toNonNullableROProp() }
    final override val isFocused by lazyDelegate {
        focusedProperty
    }


    final override val layoutBoundsProperty by lazy { node.layoutBoundsProperty().toNonNullableROProp() }
    final override val hoverProperty by lazy { node.hoverProperty().toNonNullableROProp() }
    final override val styleProperty by lazy {
        node.styleProperty().toNonNullableROProp()
    }
    final override val effectProperty by lazy { node.effectProperty().toNullableProp() }
    final override val disabledProperty by lazy { node.disabledProperty().toNonNullableROProp() }
    final override val disableProperty by lazy { node.disableProperty().toNonNullableProp() }
    final override val enableProperty by lazy {
        val r = BindableProperty(!disableProperty.value)
        val rBlocker = RecursionBlocker()
        disableProperty.onChange {
            rBlocker.with {
                r.value = !it
            }
        }
        r.onChange {
            rBlocker.with {
                disableProperty.value = !it
            }
        }
        r
    }
    final override val managedProperty by lazy { node.managedProperty().toNonNullableProp() }
    final override val visibleProperty by lazy { node.visibleProperty().toNonNullableProp() }


    final override val layoutProxies = mutableSetOf<NodeWrapper>()

    final override fun setTheStyle(value: String) {
        node.style = value
    }

    private val _visibleAndManagedProp by lazy {
        val r = BindableProperty(isVisible && isManaged)
        var changing = false
        r.onChange {
            changing = true
            isVisible = it
            isManaged = it
            changing = false
        }
        visibleProperty.onChange {
            if (!changing) r.value = isVisible && isManaged
        }
        managedProperty.onChange {
            if (!changing) r.value = isVisible && isManaged
        }
        r
    }

    final override val visibleAndManagedProp by lazy {
        _visibleAndManagedProp
    }


}
