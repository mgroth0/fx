package matt.fx.graphics.wrapper

import javafx.collections.ObservableMap
import javafx.event.EventTarget
import javafx.scene.Node
import matt.collect.weak.WeakMap
import matt.fx.graphics.wrapper.node.NodeWrapper

@DslMarker annotation class FXNodeWrapperDSL


@FXNodeWrapperDSL
interface EventTargetWrapper {


  val node: EventTarget


  val properties: ObservableMap<Any, Any>

  val childList: MutableList<Node>? get() = null


  fun addChild(child: NodeWrapper, index: Int? = null)


  operator fun plusAssign(node: NodeWrapper) {
	addChild(node)
  }

  fun add(nw: NodeWrapper) = plusAssign(nw)

  fun replaceChildren(vararg node: Node) {
	val children = requireNotNull(childList) { "This node doesn't have a child list" }
	children.clear()
	children.addAll(node)
  }



  fun removeFromParent() {
	val n = node
	when (n) {
	  is Tab         -> n.tabPane?.tabs?.remove(n)
	  is Node        -> {
		(n.parent?.parent as? ToolBar)?.items?.remove(n) ?: n.parent?.wrapped()?.childList?.remove(n)
	  }

	  is TreeItem<*> -> n.parent.children.remove(n)
	}
  }


}

abstract class EventTargetWrapperImpl<out N: EventTarget>: EventTargetWrapper {
  abstract override val node: N


}

abstract class SingularEventTargetWrapper<out N: EventTarget>(
  /*TODO: node must be made internal...? then protected...*/
  final override val node: N
): EventTargetWrapperImpl<N>() {

  companion object {
	val wrappers = WeakMap<EventTarget, EventTargetWrapper>()
  }

  init {

	require(node !in wrappers) {
	  "A second ${SingularEventTargetWrapper::class.simpleName} was created for $node"
	}
	wrappers[node] = this
  }
}
