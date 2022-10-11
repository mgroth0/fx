package matt.fx.control

import javafx.scene.Node
import javafx.scene.control.ScrollPane
import matt.reflect.subclasses
import matt.test.yesIUseTestLibs
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts

class SomeTests {

  @Test
  @ExperimentalContracts
  fun testWrappers() {

	yesIUseTestLibs()

	listOf<Node>(
	  ScrollPane()
	)

	Node::class.subclasses().forEach {
	  println("node subclass: ${it}")
	}

	//	assert(false)

  }
}
