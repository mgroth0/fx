package matt.fx.control.wrapper.toolbar

import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.ToolBar
import javafx.scene.layout.Priority
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.SimplePaneWrapper


fun ET.toolbar(vararg nodes: Node, op: ToolBarWrapper.()->Unit = {}): ToolBarWrapper {
    val toolbar = ToolBarWrapper()
    if (nodes.isNotEmpty()) toolbar.items.addAll(nodes)
    toolbar.attachTo(this, op)
    return toolbar
}


class ToolBarWrapper(
    node: ToolBar = ToolBar(),
): ControlWrapperImpl<ToolBar>(node) {

    override val childList get() = items


    val items: ObservableList<Node> get() = node.items


    fun spacer(prio: Priority = Priority.ALWAYS, op: PaneWrapperImpl<*, *>.()->Unit = {}): PaneWrapperImpl<*, *> {
        val pane = SimplePaneWrapper<NodeWrapper>().apply {
            hgrow = prio
        }
        op(pane)
        add(pane)
        return pane
    }

    override fun addChild(child: NodeWrapper, index: Int?) {
        if (index != null) {
            items.add(index, child.node)
        } else {
            items.add(child.node)
        }
    }

}
