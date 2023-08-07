package matt.fx.threedemo.test


import matt.fx.threedemo.demoBox
import matt.mbuild.mtest.fx.FxTests
import kotlin.test.Test

class ThreedemoTests : FxTests() {
    @Test
    fun createDemoBox() {
        demoBox
    }
}