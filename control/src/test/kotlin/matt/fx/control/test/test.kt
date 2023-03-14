package matt.fx.control.test
/*TEST FILE PATHS MUST BE DISTINGUISHABLE FROM MAIN FILE PATHS OR INTELLIJ WONT NAV TO THEM*/
import javafx.application.Platform
import javafx.event.EventTarget
import matt.fx.control.fxapp.runFXAppBlocking
import matt.fx.control.wrapper.wrapped.CannotFindWrapperException
import matt.fx.control.wrapper.wrapped.wrapped
import matt.reflect.access
import matt.reflect.noArgConstructor
import matt.reflect.reflections.subclasses
import matt.test.yesIUseTestLibs
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts
import kotlin.reflect.KClass



class FXControlTests {

  @Test @ExperimentalContracts fun testWrappers() {

	yesIUseTestLibs()


	runFXAppBlocking(args = arrayOf(), usePreloaderApp = false, reporter = null, fxOp = {
	  //	  @Suppress("UNUSED_VARIABLE")
	  val failedToWrap = (EventTarget::class as KClass<out EventTarget>).subclasses("javafx")
		.filter {
		  /*must test for anonymous first because trying to see if anonymous is abstract leads to error*/
		  !it.java.isAnonymousClass
		  && !it.isAbstract
		  /*	  && it.qualifiedName !in listOf(
				  "javafx.embed.swing.SwingNode", *//*not sure how this keeps getting in even though I'm not depending on fx-swing*//*
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
	})


  }
}