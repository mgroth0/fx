package matt.fx.control.popup.test


import matt.fx.control.popup.dialog.DialogWrapper
import matt.mbuild.mtest.fx.FXTester
import matt.mbuild.mtest.fx.FxTests
import kotlin.test.Test

class PopupTests() : FxTests() {
    @Test
    fun instantiateClasses() {
        FXTester.runFXHeadlessApp {
            DialogWrapper<Int>()
        }
    }
}