package matt.fx.control

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Node
import javafx.stage.Stage
import matt.reflect.reflections.subclasses
import matt.test.yesIUseTestLibs
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts
import kotlin.reflect.KClass

class SomeTests {

  /*DO NOT DELETE THIS TEST. DON'T BE LAZY ABOUT TESTS. ITS CRITICAL AS A SOLO-DEV TO TEST WELL SO MY RELEASES ARE BUG-FREE.*/
  @Test @ExperimentalContracts fun testWrappers() {


	yesIUseTestLibs()

	Platform.setImplicitExit(true)
	Application.launch(TestApp::class.java)


  }
}


class TestApp: Application() {

  override fun start(primaryStage: Stage?) {
	(Node::class as KClass<out Node>).subclasses("javafx").filter {
	  !it.isAbstract
	}.forEach {
	  println("node subclass: $it")
	}
	primaryStage!!.close()
	Platform.exit()
  }

}
