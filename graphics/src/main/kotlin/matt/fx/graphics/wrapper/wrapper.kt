package matt.fx.graphics.wrapper

import javafx.event.EventTarget
import javafx.scene.Node
import matt.hurricanefx.wrapper.node.attachTo

@DslMarker annotation class FXNodeWrapperDSL


@FXNodeWrapperDSL
interface EventTargetWrapper {
  val node: EventTarget

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


  /**
   * Did the event occur inside a TableRow, TreeTableRow or ListCell?
   */
  fun isInsideRow(): Boolean {
	val n = node
	if (n !is Node) return false

	if (n is TableColumnHeader) return false

	if (n is TableRow<*> || n is TableView<*> || n is TreeTableRow<*> || n is TreeTableView<*> || n is ListCell<*>) return true

	if (n.parent != null) return n.parent.wrapped().isInsideRow()

	return false
  }


  fun removeFromParent() {
	val n = node
	when (n) {
	  is Tab  -> n.tabPane?.tabs?.remove(n)
	  is Node -> {
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
  init {

	require(WrapperKey !in node.properties) {
	  "A second ${SingularEventTargetWrapper::class.simpleName} was created for $node"
	}
	node.properties[WrapperKey] = this
  }
}

object WrapperKey