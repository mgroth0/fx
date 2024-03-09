package matt.fx.fxauto.test


import matt.file.commons.reg.REGISTERED_FOLDER
import matt.fx.fxauto.fxActions
import matt.mbuild.mtest.fx.FxTests
import kotlin.test.Test

class FxautoTests() : FxTests() {
    @Test
    fun getFileActions() {
        REGISTERED_FOLDER.fxActions()
    }
}
