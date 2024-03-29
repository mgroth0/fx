package matt.fx.control.test
/*TEST FILE PATHS MUST BE DISTINGUISHABLE FROM MAIN FILE PATHS OR INTELLIJ WONT NAV TO THEM*/
import javafx.application.Platform
import javafx.event.EventTarget
import matt.fx.control.wrapper.wrapped.util.CannotFindWrapperException
import matt.fx.control.wrapper.wrapped.wrapped
import matt.mbuild.mtest.fx.FXTester
import matt.mbuild.mtest.fx.FxTests
import matt.reflect.j.access
import matt.reflect.j.noArgConstructor
import matt.reflect.pack.Pack
import matt.reflect.scan.jcommon.systemScope
import matt.reflect.scan.jcommon.usingClassGraph
import kotlin.contracts.ExperimentalContracts
import kotlin.reflect.KClass
import kotlin.test.Test


class FXControlTests : FxTests() {

    @Test
    @ExperimentalContracts
    fun testWrappers() {



        FXTester.runFXHeadlessApp {
            val failedToWrap =
                with(systemScope(includePlatformClassloader = false).usingClassGraph()) {
                    (EventTarget::class as KClass<out EventTarget>).subClasses(setOf(Pack("javafx")))
                }.filter {
                    /*must test for anonymous first because trying to see if anonymous is abstract leads to error*/
                    !it.java.isAnonymousClass
                        && !it.isAbstract

                    /*	  && it.qualifiedName !in listOf(
                            "javafx.embed.swing.SwingNode",

                    // not sure how this keeps getting in even though I'm not depending on fx-swing



		                    )*/
                }.mapNotNull {
                    it.noArgConstructor
                }.mapNotNull {
                    try {
                        it.access {
                            (it.call() as EventTarget).wrapped()
                        }
                        null
                    } catch (e: CannotFindWrapperException) {
                        e.cls
                    }
                }
            assert(failedToWrap.isEmpty()) {
                "failed to wrap the following:\n\t${failedToWrap.joinToString("\n\t") { it.qualifiedName!! }}"
            }
            Platform.exit()
        }
    }
}
