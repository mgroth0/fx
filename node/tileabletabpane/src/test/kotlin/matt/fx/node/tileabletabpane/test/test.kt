package matt.fx.node.tileabletabpane.test


import matt.fx.node.tileabletabpane.TileableTabPane
import matt.mbuild.mtest.fx.FXTester
import matt.mbuild.mtest.fx.FxTests
import kotlin.test.Test

class TileabletabpaneTests : FxTests() {
    @Test
    fun instantiateClasses() {
        FXTester.runFXHeadlessApp {
            TileableTabPane()
        }
    }
}