package matt.fx.control

import javafx.application.Platform
import javafx.scene.Node
import matt.fx.control.fxapp.runFXAppBlocking
import matt.reflect.reflections.subclasses
import matt.test.yesIUseTestLibs
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts
import kotlin.reflect.KClass

class SomeTests {

  /*DO NOT DELETE THIS TEST. DON'T BE LAZY ABOUT TESTS. ITS CRITICAL AS A SOLO-DEV TO TEST WELL SO MY RELEASES ARE BUG-FREE.*/
  @Test @ExperimentalContracts fun testWrappers() {


	yesIUseTestLibs()

	runFXAppBlocking(
	  args = arrayOf(),
	  usePreloaderApp = false,
	  t = null,
	  fxOp = {
		(Node::class as KClass<out Node>).subclasses("javafx").filter {
		  !it.isAbstract
		}.forEach {
		  println("node subclass: $it")
		}
		Platform.exit()
	  }
	)


  }
}