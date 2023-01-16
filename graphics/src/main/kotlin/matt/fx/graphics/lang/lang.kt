package matt.fx.graphics.lang

import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.lang.err
import matt.lang.setall.setAll

fun NodeWrapper.setOnFocusLost(op: ()->Unit) {
  focusedProperty.onChange { it: Boolean? ->
	if (it == null) err("here it is")
	if (!it) {
	  op()
	}
  }
}

fun NodeWrapper.setOnFocusGained(op: ()->Unit) {
  focusedProperty.onChange { it: Boolean? ->
	if (it == null) err("here it is")
	if (it) {
	  op()
	}
  }
}


fun <T> MutableList<T>.setToSublist(start: Int, Stop: Int) {
  setAll(subList(start, Stop).toList())
}

fun <T> MutableList<T>.removeAllButLastN(num: Int) {
  val siz = size
  setToSublist(siz - num, siz)
}


@Suppress("unused")
fun SceneWrapper<*>.onDoubleClickConsume(action: ()->Unit) {
  node.setOnMouseClicked {
	if (it.clickCount == 2) {
	  action()
	  it.consume()
	}
  }
}
