package matt.fx.node.procpane.test


import matt.fx.node.procpane.inspect.ProcessInspectPane
import matt.mbuild.mtest.fx.FXTester
import matt.mbuild.mtest.fx.FxTests
import matt.shell.spawner.ExecProcessSpawner
import kotlin.contracts.ExperimentalContracts
import kotlin.test.Test

class ProcpaneTests() : FxTests() {
    @OptIn(ExperimentalContracts::class)
    @Test
    fun instantiateClasses() {
        FXTester.runFXHeadlessApp {
            ProcessInspectPane(ExecProcessSpawner(this).sendCommand("echo", "hi").process)
        }
    }
}
