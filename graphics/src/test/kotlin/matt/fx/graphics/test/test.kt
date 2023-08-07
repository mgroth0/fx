package matt.fx.graphics.test


import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.grid.GridPaneWrapper
import matt.mbuild.mtest.fx.FxTests
import kotlin.test.Test

class GraphicsTests : FxTests() {
    @Test
    fun createGridPane() {
        GridPaneWrapper<NodeWrapper>()
    }
}