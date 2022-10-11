package matt.fx.control

import javafx.application.Application
import javafx.scene.Node
import javafx.stage.Stage
import matt.reflect.subclasses
import matt.test.yesIUseTestLibs
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts

class SomeTests {

  @Test @ExperimentalContracts fun testWrappers() {

	yesIUseTestLibs()

	Application.launch(TestApp::class.java)

  }
}


class TestApp(): Application() {

  override fun start(primaryStage: Stage?) {
	Node::class.subclasses().forEach {
	  println("node subclass: $it")
	}
  }

}
