package matt.fx.graphics.wrapper.pane.vbox

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.graphics.style.inset.MarginableConstraints
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.SimplePaneWrapper
import matt.fx.graphics.wrapper.pane.box.BoxWrapper
import matt.fx.graphics.wrapper.pane.box.BoxWrapperImpl
import matt.lang.common.B
import matt.lang.delegation.lazyVarDelegate
import matt.obs.prop.writable.Var
import kotlin.reflect.KClass

fun ET.v(
    spacing: Number? = null,
    alignment: Pos? = null,
    op: VBoxWrapper<NW>.() -> Unit = {}
) = vbox(spacing, alignment, op)

inline fun <reified C: NodeWrapper> ET.vbox(
    spacing: Number? = null,
    alignment: Pos? = null,
    op: VBoxWrapper<C>.() -> Unit = {}
): VBoxWrapper<C> {
    val vbox = VBoxWrapperImpl<C>(VBox(), childClass = C::class)
    if (alignment != null) vbox.alignment = alignment
    if (spacing != null) vbox.spacing = spacing.toDouble()
    return attach(vbox, op)
}

class VBoxSimple: VBoxWrapperImpl<NodeWrapper>(VBox(), NodeWrapper::class)

typealias VBoxW = VBoxWrapperImpl<NodeWrapper>

interface VBoxWrapper<C: NodeWrapper>: BoxWrapper<C> {
    val fillWidthProperty: Var<B>
    var isFillWidth: B
}

open class VBoxWrapperImpl<C: NodeWrapper>(node: VBox = VBox(), childClass: KClass<C>): BoxWrapperImpl<VBox, C>(node, childClass = childClass), VBoxWrapper<C> {
    companion object {
        inline operator fun <reified C:NodeWrapper> invoke(
            vararg nodes: C
        ) = VBoxWrapperImpl(VBox(*nodes.map { it.node }.toTypedArray()), C::class)
    }



    final override val fillWidthProperty: NonNullFXBackedBindableProp<Boolean> by lazy {
        node.fillWidthProperty().toNonNullableProp()
    }
    final override var isFillWidth by lazyVarDelegate { fillWidthProperty }
}

fun VBoxWrapperImpl<PaneWrapper<*>>.spacer(prio: Priority = Priority.ALWAYS, op: PaneWrapperImpl<*, *>.() -> Unit = {}) =
    attach(SimplePaneWrapper<NodeWrapper>().apply { vGrow = prio }, op)


class VBoxConstraint(
    node: Node,
    override var margin: Insets? = VBox.getMargin(node),
    var vGrow: Priority? = null

): MarginableConstraints() {
    fun <T: Node> applyToNode(node: T): T {
        margin?.let { VBox.setMargin(node, it) }
        vGrow?.let { VBox.setVgrow(node, it) }
        return node
    }
}


inline fun <T: Node> T.vboxConstraints(op: (VBoxConstraint.() -> Unit)): T {
    val c = VBoxConstraint(this)
    c.op()
    return c.applyToNode(this)
}

