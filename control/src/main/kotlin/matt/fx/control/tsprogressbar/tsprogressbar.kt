package matt.fx.control.tsprogressbar

import javafx.application.Platform.runLater
import javafx.scene.Node
import javafx.scene.control.ProgressBar
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.lang.NOT_IMPLEMENTED
import matt.log.warn.warn


sealed class ThreadSafeNodeWrapper<N: Node>(
  node: N
): NodeWrapperImpl<N>(node) {
  init {
	warn("this is pointless in the current implementation because other props are accessible in non thread safe way")
  }
}


class TSProgressBar: ThreadSafeNodeWrapper<ProgressBar>(ProgressBar()) {
  var progress: Double
	get() = node.progress
	set(value) = runLater { node.progress = value }

  override fun addChild(child: NodeWrapper, index: Int?) = NOT_IMPLEMENTED
}