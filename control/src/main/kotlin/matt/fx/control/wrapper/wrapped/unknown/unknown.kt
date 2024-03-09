package matt.fx.control.wrapper.wrapped.unknown

import javafx.collections.ObservableMap
import javafx.event.EventTarget
import javafx.scene.Node
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl

fun Node.unknownWrapper() = UnknownNodeWrapper(this)
fun EventTarget.unknownWrapper() = UnknownEventTargetWrapper(this)

/*

todo: The bottom line is that currently, JavaFX Node classes may not ship with my wrapper classes. There is simply no way I can guarantee that I get the correct wrapper without creating some sort of complex external registry. Not happening. So let's just do this, and change our understanding of how the `wrapped` function works. It does NOT currently guarantee that I will get the "correct" (most specific possible) wrapper. This is unfortunate, but the best solution currently available. Maybe with some sort of external registry or ServiceLoaders I can ensure wrapper specificity in the future, but that is not a super high priority. For now, don't make any log depend on which class is outputted from the `wrapped` function.

Still need CannotFindWrapperException for specific `wrapped` functions to maintain their type checking, like Labelled.wrapped().

* */
class UnknownEventTargetWrapper(et: EventTarget) : SingularEventTargetWrapper<EventTarget>(et) {
    override val properties: ObservableMap<Any, Any?> get() = TODO()

    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }

    override fun removeFromParent() {
        TODO()
    }

    override fun isInsideRow(): Boolean {
        TODO()
    }
}

/*At least I can be slightly specific*/
class UnknownNodeWrapper(node: Node) : NodeWrapperImpl<Node>(node) {
    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }
}

