package matt.fx.graphics.wrapper.pane.tile

import javafx.scene.layout.TilePane
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl

open class TilePaneWrapper<C: NodeWrapper>(node: TilePane = TilePane()): PaneWrapperImpl<TilePane, C>(node)