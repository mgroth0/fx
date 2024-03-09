package matt.fx.swing

import javafx.embed.swing.SwingNode
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import javax.swing.JComponent

class SwingNodeWrapper(node: SwingNode = SwingNode()) : NodeWrapperImpl<SwingNode>(node) {
    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }

    var content: JComponent by node::content
    fun prefHeight(d: Double) = node.prefHeight(d)
    fun prefWidth(d: Double) = node.prefWidth(d)
}
