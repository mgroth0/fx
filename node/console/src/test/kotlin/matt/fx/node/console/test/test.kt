package matt.fx.node.console.test


import matt.fx.node.console.text.ConsoleTextFlow
import matt.mbuild.mtest.fx.FXTester
import matt.test.Tests
import kotlin.test.Test

class ConsoleTests : Tests() {
    @Test
    fun instantiateClasses() {
        FXTester.runFXHeadlessApp {
            ConsoleTextFlow()
        }
    }
}