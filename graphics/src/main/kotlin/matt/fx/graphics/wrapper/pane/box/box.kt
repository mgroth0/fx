package matt.fx.graphics.wrapper.pane.box

import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import kotlin.reflect.KClass

interface BoxWrapper<C: NodeWrapper>: PaneWrapper<C> {
    var alignment: Pos
    var spacing: Double
}

abstract class BoxWrapperImpl<N: Pane, C: NodeWrapper>(node: N, childClass: KClass<C>): PaneWrapperImpl<N, C>(node, childClass), BoxWrapper<C> {

    final override var alignment: Pos
        get() = (node as? HBox)?.alignment ?: (node as VBox).alignment
        set(value) {
            if (node is HBox) (node as HBox).alignment = value
            else (node as VBox).alignment = value
        }
    final override var spacing: Double
        get() = (node as? HBox)?.spacing ?: (node as VBox).spacing
        set(value) {
            if (node is HBox) (node as HBox).spacing = value
            else (node as VBox).spacing = value
        }
}
