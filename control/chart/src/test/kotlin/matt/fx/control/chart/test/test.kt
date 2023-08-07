package matt.fx.control.chart.test


import matt.fx.control.chart.axis.value.moregenval.DoubleAxisConverter
import matt.fx.control.chart.axis.value.number.moregennum.MoreGenericNumberAxis
import matt.mbuild.mtest.fx.FXTester
import matt.mbuild.mtest.fx.FxTests
import kotlin.test.Test

class ChartTests() : FxTests() {
    @Test
    fun instantiateClasses() {
        FXTester.runFXHeadlessApp {
            MoreGenericNumberAxis(DoubleAxisConverter)
        }
    }
}