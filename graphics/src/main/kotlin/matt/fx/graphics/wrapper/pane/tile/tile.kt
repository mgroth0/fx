package matt.fx.graphics.wrapper.pane.tile

import javafx.scene.layout.TilePane
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import kotlin.reflect.KClass

inline fun <reified C: NodeWrapper> ET.tilepane(op: TilePaneWrapper<C>.() -> Unit = {}) = attach(TilePaneWrapper(childClass = C::class), op)
open class TilePaneWrapper<C: NodeWrapper>(
    node: TilePane = TilePane(),
    childClass: KClass<C>
): PaneWrapperImpl<TilePane, C>(node, childClass)
