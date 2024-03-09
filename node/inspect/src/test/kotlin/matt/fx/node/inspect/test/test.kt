package matt.fx.node.inspect.test


import matt.fx.graphics.node.Inspectable
import matt.fx.graphics.wrapper.pane.vbox.VBoxW
import matt.fx.node.inspect.InspectionView
import matt.mbuild.mtest.fx.FXTester
import matt.mbuild.mtest.fx.FxTests
import kotlin.test.Test

class InspectTests : FxTests() {
    @Test
    fun instantiateClasses() {
        FXTester.runFXHeadlessApp {
            InspectionView(listOf(TestInspectable))
        }
    }
}

object TestInspectable : Inspectable<VBoxW> {
    override fun inspect(): VBoxW = VBoxW()
}
