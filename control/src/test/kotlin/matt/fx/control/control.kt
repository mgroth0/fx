package matt.fx.control

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Node
import javafx.stage.Stage
import matt.collect.itr.recurse.recurse
import matt.reflect.subclasses
import matt.test.yesIUseTestLibs
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts
import kotlin.reflect.KClass

class SomeTests {

  @Test @ExperimentalContracts fun testWrappers() {

	yesIUseTestLibs()

	Platform.setImplicitExit(true)
	Application.launch(TestApp::class.java)


  }
}


class TestApp: Application() {

  override fun start(primaryStage: Stage?) {
	(Node::class as KClass<out Node>).recurse { it.subclasses() }.filter {
	  !it.isAbstract
	}.forEach {
	  println("node subclass: $it")
	}
	primaryStage!!.close()
	Platform.exit()
  }

}
