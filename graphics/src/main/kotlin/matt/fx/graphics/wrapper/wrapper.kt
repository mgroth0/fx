package matt.fx.graphics.wrapper

import com.google.common.collect.MapMaker
import javafx.collections.ObservableMap
import javafx.event.EventTarget
import javafx.scene.Node
import matt.fx.graphics.hotkey.HotKeyEventHandler
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.lang.assertions.require.requireDoesNotContain
import matt.lang.assertions.require.requireNull
import matt.lang.toStringBasic
import java.util.concurrent.ConcurrentMap

@DslMarker
annotation class FXNodeWrapperDSL

typealias ET = EventTargetWrapper

@FXNodeWrapperDSL
interface EventTargetWrapper {


    var hotKeyHandler: HotKeyEventHandler?
    var hotKeyFilter: HotKeyEventHandler?

    val node: EventTarget


    val properties: ObservableMap<Any, Any?>

    val childList: MutableList<Node>? get() = null


    fun addChild(
        child: NodeWrapper,
        index: Int? = null
    )


    operator fun plusAssign(node: NodeWrapper) {
        addChild(node)
    }

    operator fun NW.unaryPlus() {
        this@EventTargetWrapper.add(this)
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


sealed class EventTargetWrapperImpl<out N : EventTarget> : EventTargetWrapper {
    abstract override val node: N


    override var hotKeyHandler: HotKeyEventHandler? = null
    override var hotKeyFilter: HotKeyEventHandler? = null

    override fun toString() = super.toString().substringAfterLast(".")

}

abstract class SingularEventTargetWrapper<out N : EventTarget>(
    node: N
) : EventTargetWrapperImpl<N>() {

    override val node = node

    companion object {
        private val wrappers: ConcurrentMap<EventTarget, EventTargetWrapper> = MapMaker()
            .weakKeys()
            .weakValues()
            .makeMap<EventTarget, EventTargetWrapper>()

        /*WeakMap<EventTarget, EventTargetWrapper>()*/
        operator fun get(e: EventTarget) = wrappers[e]
    }

    init {

//        sun.security.util.MemoryCache(true,1)

        /*println("checking for ${this.toStringBasic()} with ${superNode.toStringBasic()}")*/
        /*if ("Scene@" in superNode.toStringBasic()) {
          Thread.dumpStack()
        }*/


        requireDoesNotContain(wrappers, node) {
            """
		
		This is ${this.toStringBasic()}
		
		A second ${this::class.simpleName} was created for ${node.toStringBasic()}
		
		The first one is ${wrappers[node]?.toStringBasic()}
		
		
	  """.trimMargin()
        }
        /*println("putting ${superNode.toStringBasic()} in wrappers for ${this.toStringBasic()}")*/
        wrappers[node] = this
    }
}


@Suppress("UNUSED_PARAMETER")
class ProxyEventTargetWrapper(private val addOp: (NW) -> Unit) : EventTargetWrapper {
    override var hotKeyHandler: HotKeyEventHandler?
        get() = TODO()
        set(value) {}
    override var hotKeyFilter: HotKeyEventHandler?
        get() = TODO()
        set(value) {}
    override val node: EventTarget
        get() = TODO()
    override val properties: ObservableMap<Any, Any?>
        get() = TODO()

    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        requireNull(index)
        addOp(child)
    }

    override fun removeFromParent() {
        TODO()
    }

    override fun isInsideRow(): Boolean {
        TODO()
    }

}