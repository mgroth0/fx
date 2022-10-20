package matt.fx.graphics.wrapper

import javafx.collections.ObservableMap
import javafx.event.EventTarget
import javafx.scene.Node
import matt.collect.weak.WeakMap
import matt.fx.graphics.hotkey.HotKeyEventHandler
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.lang.toStringBasic

@DslMarker annotation class FXNodeWrapperDSL

typealias ET = EventTargetWrapper

@FXNodeWrapperDSL interface EventTargetWrapper {


  var hotKeyHandler: HotKeyEventHandler?
  var hotKeyFilter: HotKeyEventHandler?

  val node: EventTarget


  val properties: ObservableMap<Any, Any?>

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


  fun removeFromParent()


  /**
   * Did the event occur inside a TableRow, TreeTableRow or ListCell?
   */
  fun isInsideRow(): Boolean


}

sealed class EventTargetWrapperImpl<out N: EventTarget>: EventTargetWrapper {
  abstract override val node: N


  override var hotKeyHandler: HotKeyEventHandler? = null
  override var hotKeyFilter: HotKeyEventHandler? = null

}

abstract class SingularEventTargetWrapper<out N: EventTarget>(/*TODO: node must be made internal...? then protected...*/
															  node: N
): EventTargetWrapperImpl<N>() {

  //  private var superNode: N? = null
  //  init {
  //	node
  //  }

  private val superNode = node

  @Suppress("CanBePrimaryConstructorProperty") override val node = node

  companion object {
	private val wrappers = WeakMap<EventTarget, EventTargetWrapper>()
	operator fun get(e: EventTarget) = wrappers[e]
  }

  init {

	/*println("checking for ${this.toStringBasic()} with ${superNode.toStringBasic()}")*/
	/*if ("Scene@" in superNode.toStringBasic()) {
	  Thread.dumpStack()
	}*/

	require(superNode !in wrappers) {
	  """
		
		This is ${this.toStringBasic()}
		
		A second ${this::class.simpleName} was created for ${superNode.toStringBasic()}
		
		The first one is ${wrappers[superNode]?.toStringBasic()}
		
		
	  """.trimMargin()
	}
	/*println("putting ${superNode.toStringBasic()} in wrappers for ${this.toStringBasic()}")*/
	wrappers[superNode] = this
  }
}
