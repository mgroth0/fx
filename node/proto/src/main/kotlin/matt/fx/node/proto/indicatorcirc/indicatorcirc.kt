package matt.fx.node.proto.indicatorcirc

import javafx.scene.paint.Color
import matt.fx.graphics.wrapper.node.shape.circle.CircleWrapper
import matt.obs.bind.binding
import matt.obs.prop.ValProp

fun indicatorCircle(booleanProperty: ValProp<Boolean>) = CircleWrapper(8.0).apply {
    fillProperty.bind(booleanProperty.binding {
        if (it) Color.LIGHTGREEN else Color.DARKRED
    })
}
