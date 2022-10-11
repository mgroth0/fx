package matt.fx.control

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import kotlin.contracts.ExperimentalContracts
import kotlin.test.junit5.JUnit5Asserter

@Suppress("UNUSED_VARIABLE")
class SomeTests {


  @Test
  @ExperimentalContracts
  fun testWrappers() {


	val yesIUseJUnitHereIsSomethingNotInlined = AfterTestExecutionCallback { TODO("Not yet implemented") }
	val hereIsAnother = JUnit5Asserter

//	assert(false)

  }
}
