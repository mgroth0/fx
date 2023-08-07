package matt.fx.node.tex.test


import matt.fx.node.tex.dsl.TeXDSL
import matt.mbuild.mtest.fx.FxTests
import kotlin.test.Test

class TexTests : FxTests() {
    @Test
    fun instantiateClasses() {
        TeXDSL()
    }
}