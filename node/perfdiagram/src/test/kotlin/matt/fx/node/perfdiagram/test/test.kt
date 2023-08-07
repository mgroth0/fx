package matt.fx.node.perfdiagram.test


import matt.fx.node.perfdiagram.analysisNode
import matt.log.profile.stopwatch.tic
import matt.mbuild.mtest.fx.FXTester
import matt.mbuild.mtest.fx.FxTests
import kotlin.test.Test

class PerfdiagramTests() : FxTests() {
    @Test
    fun instantiateClasses() {
        FXTester.runFXHeadlessApp {
            tic().analysisNode()
        }
    }
}