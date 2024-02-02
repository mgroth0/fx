package matt.fx.graphics.wrapper.pane.anchor

import javafx.scene.Node
import javafx.scene.layout.AnchorPane
import matt.collect.itr.mapToArray
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.lang.NOT_IMPLEMENTED
import matt.lang.anno.Open

fun <C: NodeWrapper> ET.anchorpane(
    vararg nodes: C,
    op: AnchorPaneWrapperImpl<C>.()->Unit = {}
): AnchorPaneWrapperImpl<C> {
    val anchorpane = AnchorPaneWrapperImpl<C>()
    if (nodes.isNotEmpty()) anchorpane.children.addAll(nodes)
    attach(anchorpane, op)
    return anchorpane
}

interface AnchorPaneWrapper<C: NodeWrapper>: PaneWrapper<C> {
    override val node: AnchorPane
    @Open
    var left: NodeWrapper
        get() = NOT_IMPLEMENTED
        set(value) {
            /*be very careful changing this line to use wrappers... or just never do it. There used to be horrible issue here and I never found the root cause. Switching it away from using wrappers to using `value.node` and `node.children` completely solved it. Yes, maybe some internal improvement I made since then fixed the issue and maybe switching to wrappers now would fix it. But is it really worth it? If I feel the need to change it, only do so when I have extensive testing set up.*/
            if (value.node !in node.children) add(value)
            value.setAsLeftAnchor(0.0)
        }
    @Open   var right: NodeWrapper
        get() = NOT_IMPLEMENTED
        set(value) {
            /*be very careful changing this line to use wrappers... or just never do it. There used to be horrible issue here and I never found the root cause. Switching it away from using wrappers to using `value.node` and `node.children` completely solved it. Yes, maybe some internal improvement I made since then fixed the issue and maybe switching to wrappers now would fix it. But is it really worth it? If I feel the need to change it, only do so when I have extensive testing set up.*/
            if (value.node !in node.children) add(value)
            value.setAsRightAnchor(0.0)
        }
    @Open  var bottom: NodeWrapper
        get() = NOT_IMPLEMENTED
        set(value) {
            /*be very careful changing this line to use wrappers... or just never do it. There used to be horrible issue here and I never found the root cause. Switching it away from using wrappers to using `value.node` and `node.children` completely solved it. Yes, maybe some internal improvement I made since then fixed the issue and maybe switching to wrappers now would fix it. But is it really worth it? If I feel the need to change it, only do so when I have extensive testing set up.*/
            if (value.node !in node.children) add(value)
            value.setAsBottomAnchor(0.0)
        }
    @Open   var top: NodeWrapper
        get() = NOT_IMPLEMENTED
        set(value) {
            /*be very careful changing this line to use wrappers... or just never do it. There used to be horrible issue here and I never found the root cause. Switching it away from using wrappers to using `value.node` and `node.children` completely solved it. Yes, maybe some internal improvement I made since then fixed the issue and maybe switching to wrappers now would fix it. But is it really worth it? If I feel the need to change it, only do so when I have extensive testing set up.*/
            if (value.node !in node.children) add(value)
            value.setAsTopAnchor(0.0)
        }
    @Open  var allSides: NodeWrapper
        get() = NOT_IMPLEMENTED
        set(value) {
            left = value
            right = value
            bottom = value
            top = value
        }
}

//class UnRegisteredAnchorPaneWrapper<C: NodeWrapper>(node: AnchorPane): AnchorPaneWrapper<C>

open class AnchorPaneWrapperImpl<C: NodeWrapper>(node: AnchorPane = AnchorPane()): PaneWrapperImpl<AnchorPane, C>(node),
    AnchorPaneWrapper<C> {

    constructor (vararg children: NodeWrapper): this(AnchorPane(*children.mapToArray { it.node }))


}



inline fun <T: Node> T.anchorpaneConstraints(op: AnchorPaneConstraint.()->Unit): T {
    val c = AnchorPaneConstraint()
    c.op()
    return c.applyToNode(this)
}

class AnchorPaneConstraint(
    var topAnchor: Number? = null,
    var rightAnchor: Number? = null,
    var bottomAnchor: Number? = null,
    var leftAnchor: Number? = null
) {
    fun <T: Node> applyToNode(node: T): T {
        topAnchor?.let { AnchorPane.setTopAnchor(node, it.toDouble()) }
        rightAnchor?.let { AnchorPane.setRightAnchor(node, it.toDouble()) }
        bottomAnchor?.let { AnchorPane.setBottomAnchor(node, it.toDouble()) }
        leftAnchor?.let { AnchorPane.setLeftAnchor(node, it.toDouble()) }
        return node
    }
}
