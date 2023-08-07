package matt.fx.web.test


import matt.fx.web.refreshImages
import matt.mbuild.mtest.fx.FxTests
import kotlin.test.Test

class WebTests : FxTests() {
    @Test
    fun initVals() {
        refreshImages
    }
}