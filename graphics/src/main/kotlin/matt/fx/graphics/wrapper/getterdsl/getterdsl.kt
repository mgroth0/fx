package matt.fx.graphics.wrapper.getterdsl

import javafx.collections.ObservableMap
import javafx.event.EventTarget
import matt.fx.graphics.hotkey.HotKeyEventHandler
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.lang.assertions.require.requireNull

fun buildNodes(op: EventTargetWrapper.() -> Unit): List<NodeWrapper> {
    val built = NodeBuilder().apply(op).nodesThatHaveBeenBuilt()
    return built
}

fun <R : NodeWrapper> buildNode(op: EventTargetWrapper.() -> R): R {
    val builder = NodeBuilder()
    val result = builder.run(op)
    val built = builder.nodesThatHaveBeenBuilt()
    require(result == built.single())
    return result
}

internal class NodeBuilder() : EventTargetWrapper {
    @Suppress("UNUSED_PARAMETER")
    override var hotKeyHandler: HotKeyEventHandler?
        get() = TODO()
        set(value) {
            TODO()
        }
    @Suppress("UNUSED_PARAMETER")
    override var hotKeyFilter: HotKeyEventHandler?
        get() = TODO()
        set(value) {
            TODO()
        }
    override val node: EventTarget
        get() = TODO()
    override val properties: ObservableMap<Any, Any?>
        get() = TODO()

    private val builtNodes = mutableListOf<NodeWrapper>()

    fun nodesThatHaveBeenBuilt() = builtNodes.toList()

    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        requireNull(index)
        builtNodes += child
    }

    override fun removeFromParent() {
        TODO()
    }

    override fun isInsideRow(): Boolean {
        TODO()
    }
}
