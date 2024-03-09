package matt.fx.graphics.wrapper

import com.google.common.collect.MapMaker
import javafx.collections.ObservableMap
import javafx.event.EventTarget
import javafx.scene.Node
import matt.fx.graphics.hotkey.HotKeyEventHandler
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.lang.anno.Open
import matt.lang.assertions.require.requireDoesNotContain
import matt.lang.assertions.require.requireNull
import matt.reflect.tostring.PropReflectingStringableClass
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

    @Open
    val childList: MutableList<Node>? get() = null


    fun addChild(
        child: NodeWrapper,
        index: Int? = null
    )


    @Open operator fun plusAssign(node: NodeWrapper) {
        addChild(node)
    }

    @Open operator fun NW.unaryPlus() {
        this@EventTargetWrapper.add(this)
    }

    @Open fun add(nw: NodeWrapper) = plusAssign(nw)

    @Open fun replaceChildren(vararg node: Node) {
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


sealed class EventTargetWrapperImpl<out N : EventTarget> : PropReflectingStringableClass(), EventTargetWrapper {
    abstract override val node: N


    final override var hotKeyHandler: HotKeyEventHandler? = null
    final override var hotKeyFilter: HotKeyEventHandler? = null
}

abstract class SingularEventTargetWrapper<out N : EventTarget>(
    @Open final override val node: N
) : EventTargetWrapperImpl<N>() {

    companion object {
        private val wrappers: ConcurrentMap<EventTarget, EventTargetWrapper> =
            MapMaker().weakKeys().weakValues().makeMap<EventTarget, EventTargetWrapper>()

        /*WeakMap<EventTarget, EventTargetWrapper>()*/
        operator fun get(e: EventTarget) = wrappers[e]
    }

    init {


        /*println("checking for ${this.toStringBasic()} with ${superNode.toStringBasic()}")



        if ("Scene@" in superNode.toStringBasic()) {
          Thread.dumpStack()
        }*/


        requireDoesNotContain(wrappers, node) {
            """
            
            This is $this
            
            A second ${this::class.simpleName} was created for $node
            
            The first one is ${wrappers[node]}
            
            
            """.trimMargin()
        } /*println("putting ${superNode.toStringBasic()} in wrappers for ${this.toStringBasic()}")*/
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
