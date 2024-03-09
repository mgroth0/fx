package matt.fx.graphics.wrapper.node.shape

import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Shape
import javafx.scene.shape.StrokeType
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.graphics.style.sty
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.lang.delegation.lazyVarDelegate

abstract class ShapeWrapper<N : Shape>(node: N) : NodeWrapperImpl<N>(node) {

    val strokeProperty by lazy {
        node.strokeProperty().toNullableProp()
    }
    var stroke by lazyVarDelegate { strokeProperty }


    var strokeWidth
        get() = node.strokeWidth
        set(value) {
            node.strokeWidth = value
        }

    val strokeWidthProperty by lazy { node.strokeWidthProperty().toNonNullableProp() }

    var strokeType: StrokeType
        get() = node.strokeType
        set(value) {
            node.strokeType = value
        }

    val strokeTypeProperty by lazy { node.strokeTypeProperty().toNonNullableProp() }


    var fill: Paint?
        get() = node.fill
        set(value) {
            node.fill = value
        }

    val fillProperty: NonNullFXBackedBindableProp<Paint> by lazy { node.fillProperty().toNonNullableProp() }


    /*filling via normal fill is just not working. its like getting overriden by something. what a mess. I've seen this problem everywhere. definitely will need to reproduce it and fix it via a test.*/
    var fillViaStyleSinceThereIsSomeBug: Color
        get() = TODO()
        set(value) {
            sty {
                fxFill = value
            }
        }

    final override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }
}
