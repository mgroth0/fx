package matt.fx.node.tex.test


import matt.fx.node.tex.EquationNotReallyIrTex
import matt.mbuild.mtest.fx.FxTests
import matt.model.code.tex.TexCode
import kotlin.test.Test

class TexTests : FxTests() {
    @Test
    fun instantiateClasses() {
        EquationNotReallyIrTex(TexCode(""))
    }
}