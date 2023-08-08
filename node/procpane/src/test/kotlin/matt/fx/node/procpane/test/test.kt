package matt.fx.node.procpane.test


import matt.fx.node.procpane.inspect.ProcessInspectPane
import matt.mbuild.mtest.fx.FXTester
import matt.mbuild.mtest.fx.FxTests
import matt.shell.context.DefaultMacExecutionContext
import matt.shell.spawner.ExecProcessSpawner
import kotlin.test.Test

class ProcpaneTests() : FxTests() {
    @Test
    fun instantiateClasses() {
        FXTester.runFXHeadlessApp {
            with(DefaultMacExecutionContext) {
                ProcessInspectPane(ExecProcessSpawner(this).sendCommand("echo", "hi"))
            }
        }
    }
}