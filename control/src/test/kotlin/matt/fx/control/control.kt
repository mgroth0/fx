package matt.fx.control

import javafx.application.Platform
import javafx.event.EventTarget
import matt.fx.control.fxapp.runFXAppBlocking
import matt.fx.control.wrapper.wrapped.CannotFindWrapperException
import matt.fx.control.wrapper.wrapped.wrapped
import matt.reflect.noArgConstructor
import matt.reflect.reflections.subclasses
import matt.test.yesIUseTestLibs
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts
import kotlin.reflect.KClass
import kotlin.reflect.jvm.isAccessible

class SomeTests {

  /*DO NOT DELETE THIS TEST. DON'T BE LAZY ABOUT TESTS. ITS CRITICAL AS A SOLO-DEV TO TEST WELL SO MY RELEASES ARE BUG-FREE.*/
  @Test @ExperimentalContracts fun testWrappers() {

	yesIUseTestLibs()

	runFXAppBlocking(args = arrayOf(), usePreloaderApp = false, t = null, fxOp = {
	  val failedToWrap = (EventTarget::class as KClass<out EventTarget>).subclasses("javafx")
		.filter {		/*must test for anonymous first because trying to see if anonymous is abstract leads to error*/
		  !it.java.isAnonymousClass && !it.isAbstract
		}.mapNotNull {
		it.noArgConstructor
	  }.filter {
		it.isAccessible
	  }.mapNotNull {
		try {
		  (it.call() as EventTarget).wrapped()
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