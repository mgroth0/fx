package matt.fx.node.file.test


import matt.file.commons.reg.TEMP_DIR
import matt.fx.node.file.draggableIcon
import matt.mbuild.mtest.fx.FXTester
import matt.mbuild.mtest.fx.FxTests
import kotlin.test.Test

class FileTests : FxTests() {
    @Test
    fun getAFileNode() {
        FXTester.runFXHeadlessApp {
            TEMP_DIR.draggableIcon()
        }
    }
}
