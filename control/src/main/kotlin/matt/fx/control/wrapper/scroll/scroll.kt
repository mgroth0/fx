package matt.fx.control.wrapper.scroll

import javafx.geometry.Bounds
import javafx.scene.control.ScrollPane
import javafx.scene.control.ScrollPane.ScrollBarPolicy
import matt.collect.itr.recurse.chain.chain
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.maxYRelativeTo
import matt.fx.graphics.wrapper.node.minYRelativeTo
import matt.fx.graphics.wrapper.node.parent.parent
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.lang.assertions.require.requireNull
import matt.lang.assertions.require.requireOne
import matt.lang.assertions.require.requireZero
import kotlin.reflect.KClass
import kotlin.reflect.cast


fun NW.isFullyVisibleIn(sp: ScrollPaneWrapper<*>): Boolean {
    val content = sp.content!!
    requireZero(sp.vmin)
    requireOne(sp.vmax)
    if (parent!!.chain { it.parent }.none { it == sp }) return false
    if (!isVisible) return false
    if (!isManaged) return false
    val minY = minYRelativeTo(content)
    val maxY =
        maxYRelativeTo(
            content
        )
    require(minY != null && maxY != null)
    return minY >= sp.vValueConverted && maxY <= sp.vValueConvertedMax
}


infix fun RegionWrapper<*>.wrappedIn(sp: ScrollPaneWrapper<in NodeWrapper>): ScrollPaneWrapper<out NodeWrapper> {
    this minBind sp
    sp.backgroundProperty.bindBidirectional(backgroundProperty)
    return sp.apply {
        content = this@wrappedIn
    }
}




inline fun <reified C : NodeWrapper> ET.scrollpane(
    content: C? = null,
    fitToWidth: Boolean = false,
    fitToHeight: Boolean = false,
    op: ScrollPaneWrapper<C>.() -> Unit = {}
): ScrollPaneWrapper<C> {
    val pane = if (content != null) ScrollPaneWrapper(content) else ScrollPaneWrapper(contentCls = C::class)
    pane.isFitToWidth = fitToWidth
    pane.isFitToHeight = fitToHeight
    attach(pane, op)
    return pane
}


open class ScrollPaneWrapper<C : NodeWrapper>(node: ScrollPane = ScrollPane(), private val contentCls: KClass<C>) : ControlWrapperImpl<ScrollPane>(node) {
    companion object {
        inline operator fun <reified C: NodeWrapper> invoke(content: C) = ScrollPaneWrapper(ScrollPane(content.node), C::class)
    }


    var viewportBounds: Bounds
        get() = node.viewportBounds
        set(value) {
            node.viewportBounds = value
        }

    var vmin
        get() = node.vmin
        set(value) {
            node.vmin = value
        }

    var vmax
        get() = node.vmax
        set(value) {
            node.vmax = value
        }


    var hmin
        get() = node.hmin
        set(value) {
            node.hmin = value
        }
    var hmax
        get() = node.hmax
        set(value) {
            node.hmax = value
        }


    val vbarPolicyProperty: NonNullFXBackedBindableProp<ScrollBarPolicy> by lazy {
        node.vbarPolicyProperty().toNonNullableProp()
    }

    var vbarPolicy by vbarPolicyProperty

    val hbarPolicyProp: NonNullFXBackedBindableProp<ScrollBarPolicy> by lazy {
        node.hbarPolicyProperty().toNonNullableProp()
    }

    var hbarPolicy by hbarPolicyProp


    val fitToWidthProperty: NonNullFXBackedBindableProp<Boolean> by lazy { node.fitToWidthProperty().toNonNullableProp() }
    var isFitToWidth by fitToWidthProperty

    val fitToHeightProperty: NonNullFXBackedBindableProp<Boolean> by lazy { node.fitToHeightProperty().toNonNullableProp() }
    var isFitToHeight by fitToHeightProperty

    var prefViewportWidth
        get() = node.prefViewportWidth
        set(value) {
            node.prefViewportWidth = value
        }
    var prefViewportHeight
        get() = node.prefViewportHeight
        set(value) {
            node.prefViewportHeight = value
        }

    val hValueProp by lazy { node.hvalueProperty().toNonNullableProp().cast({ it as Double }, { it }) }
    val vValueProp by lazy {



        node.vvalueProperty().toNonNullableProp().cast<Double>(Double::class)
    }

    var hvalue by hValueProp
    var vvalue: Double by vValueProp

    var content: C?
        get() = node.content?.wrapped()?.let { contentCls.cast(it) }
        set(value) {
            node.content = value?.node
        }

    fun scrollToMinYOf(node: NodeWrapperImpl<*>): Boolean {
        /*scrolling values range from 0 to 1*/
        minYRelativeTo(node)?.let {
            /*there was an issue with this code. no idea if in "it/content" below, "content" is supposed to be "content" or "node"*/
            vvalue =
                (it / content!!.boundsInLocal.height) * 1.1 /*IDK why, but y is always coming up a bit short, but this fixes it*/
            return true
        }
        return false
    }

    val vValueConverted
        get() = vvalue * ((content!!.boundsInParent.height - viewportBounds.height).takeIf { it > 0 } ?: 0.0)

    val vValueConvertedMax get() = vValueConverted + viewportBounds.height
    final override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        requireNull(index)

        /*content = node // TORNADOFX DEFAULT*/
        content!!.addChild(child) /*MATT'S WAY*/
    }
}
