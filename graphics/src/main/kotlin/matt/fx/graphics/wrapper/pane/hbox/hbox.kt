package matt.fx.graphics.wrapper.pane.hbox

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.graphics.style.inset.MarginableConstraints
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.SimplePaneWrapper
import matt.fx.graphics.wrapper.pane.box.BoxWrapper
import matt.fx.graphics.wrapper.pane.box.BoxWrapperImpl
import matt.lang.common.B
import matt.lang.delegation.lazyVarDelegate
import matt.obs.prop.writable.Var
import kotlin.reflect.KClass

fun ET.h(
    spacing: Number? = null,
    alignment: Pos? = null,
    op: HBoxWrapper<NW>.() -> Unit = {}
) = hbox(spacing, alignment, op)

inline fun <reified C: NodeWrapper> ET.hbox(
    spacing: Number? = null,
    alignment: Pos? = null,
    op: HBoxWrapper<C>.() -> Unit = {}
): HBoxWrapper<C> {
    val hbox = HBoxWrapperImpl<C>(HBox(), C::class)
    if (alignment != null) hbox.alignment = alignment
    if (spacing != null) hbox.spacing = spacing.toDouble()
    return attach(hbox, op)
}

typealias HBoxW = HBoxWrapperImpl<NW>

interface HBoxWrapper<C: NodeWrapper>: BoxWrapper<C> {
    val fillHeightProperty: Var<B>
    var isFillHeight: B
}

open class HBoxWrapperImpl<C: NodeWrapper>(node: HBox = HBox(), childClass: KClass<C>): BoxWrapperImpl<HBox, C>(node, childClass = childClass), HBoxWrapper<C> {
    companion object {
        operator fun invoke(vararg nodes: NodeWrapper) = HBoxWrapperImpl(HBox(*nodes.map { it.node }.toTypedArray()), NodeWrapper::class)
    }


    final override val fillHeightProperty: NonNullFXBackedBindableProp<Boolean> by lazy {
        node.fillHeightProperty().toNonNullableProp()
    }
    final override var isFillHeight by lazyVarDelegate { fillHeightProperty }
}

fun HBoxWrapperImpl<NodeWrapper>.spacer(prio: Priority = Priority.ALWAYS, op: PaneWrapperImpl<*, *>.() -> Unit = {}) =
    attach(SimplePaneWrapper<NodeWrapper>().apply { hGrow = prio }, op)


inline fun <T: Node> T.hboxConstraints(op: (HBoxConstraint.() -> Unit)): T {
    val c = HBoxConstraint(this)
    c.op()
    return c.applyToNode(this)
}

class HBoxConstraint(
    node: Node,
    override var margin: Insets? = HBox.getMargin(node),
    var hGrow: Priority? = null
): MarginableConstraints() {

    fun <T: Node> applyToNode(node: T): T {
        margin?.let { HBox.setMargin(node, it) }
        hGrow?.let { HBox.setHgrow(node, it) }
        return node
    }
}

