package matt.fx.graphics.wrapper.pane.tile

import javafx.scene.layout.TilePane
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl

fun <C: NodeWrapper> ET.tilepane(op: TilePaneWrapper<C>.() -> Unit = {}) = attach(TilePaneWrapper(), op)
open class TilePaneWrapper<C: NodeWrapper>(node: TilePane = TilePane()): PaneWrapperImpl<TilePane, C>(node)
