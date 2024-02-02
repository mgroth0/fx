package matt.fx.control.wrapper.split

import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.SplitPane
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach

fun ET.splitpane(
    orientation: Orientation = javafx.geometry.Orientation.HORIZONTAL,
    vararg nodes: Node,
    op: SplitPaneWrapper.() -> Unit = {}
): SplitPaneWrapper {
    val splitpane = SplitPaneWrapper()
    splitpane.orientation = orientation
    if (nodes.isNotEmpty()) splitpane.items.addAll(nodes)
    attach(splitpane, op)
    return splitpane
}

open class SplitPaneWrapper(node: SplitPane = SplitPane()) : ControlWrapperImpl<SplitPane>(node) {
    var orientation: Orientation
        get() = node.orientation
        set(value) {
            node.orientation = value
        }
    val items: ObservableList<Node> get() = node.items

    final override val childList get() = items

    final override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        if (index != null) {
            items.add(index, child.node)
        } else {
            items.add(child.node)
        }

    }
}
