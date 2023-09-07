package matt.fx.node.perfdiagram.test


import matt.fx.node.perfdiagram.analysisNodeIr
import matt.log.profile.stopwatch.tic
import matt.test.Tests
import kotlin.test.Test

class PerfdiagramTests() : Tests() {
    @Test
    fun runThroughWholeModule() {
        tic().analysisNodeIr()
    }
}