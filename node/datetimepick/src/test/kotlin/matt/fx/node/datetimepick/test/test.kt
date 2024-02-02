package matt.fx.node.datetimepick.test


import matt.fx.node.datetimepick.DateTimePicker
import matt.mbuild.mtest.fx.FXTester
import matt.mbuild.mtest.fx.FxTests
import kotlin.test.Test

class DatetimepickTests : FxTests() {
    @Test
    fun instantiateClasses() {
        FXTester.runFXHeadlessApp {
            DateTimePicker()
        }
    }
}
