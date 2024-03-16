package matt.fx.node.chart.test


import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxW
import matt.fx.node.chart.annochart.annopane.legend.MyLegend
import matt.fx.node.chart.annochart.annopane.legend.MyLegend.LegendItem
import matt.mbuild.mtest.fx.FXTester
import matt.obs.col.olist.toBasicObservableList
import matt.test.Tests
import kotlin.test.Test

class ChartTests : Tests() {
    @Test
    fun instantiateClasses() {
        FXTester.runFXHeadlessApp {
            val items = listOf(LegendItem({ VBoxW(childClass = NodeWrapper::class) }, "a"))
            MyLegend(items.toBasicObservableList())
        }
    }
}
