package matt.fx.control.wrapper.control.tab

import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.base.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.lang.require.requireNull
import matt.obs.bindings.bool.ObsB
import matt.obs.bindings.bool.not

open class TabWrapper<C : NodeWrapper?>(
    node: Tab = Tab()
) : SingularEventTargetWrapper<Tab>(node) {

    override fun isInsideRow() = false

    constructor(
        text: String?,
        content: C? = null
    ) : this(Tab(text, content?.node))

    val closableProp by lazy { node.closableProperty().toNonNullableProp() }
    var isClosable by closableProp

    override val properties get() = node.properties

    override fun removeFromParent() {
        node.tabPane?.tabs?.remove(node)
    }

    val contentProperty by lazy { node.contentProperty().toNullableProp() }

    open var content: C
        @Suppress("UNCHECKED_CAST")
        get() = node.content?.wrapped() as C
        set(value) = contentProperty v (value?.node)

    val graphicProperty by lazy { node.graphicProperty().toNullableProp() }
    var graphic by graphicProperty
    val disabledProperty by lazy { node.disabledProperty().toNonNullableROProp() }
    val disableProperty by lazy { node.disableProperty().toNonNullableProp() }
    val closableProperty by lazy { node.closableProperty().toNonNullableProp() }
    val selectedProperty by lazy { node.selectedProperty().toNonNullableROProp() }
    val isSelected by selectedProperty
    val tabPane: TabPane? get() = node.tabPane


    fun disableWhen(predicate: ObsB) = disableProperty.bind(predicate)

    fun enableWhen(predicate: ObsB) {
        disableProperty.bind(predicate.not())
    }

    fun closeableWhen(predicate: ObsB) {
        closableProperty.bind(predicate)
    }

    fun visibleWhen(predicate: ObsB) {
        val localTabPane = tabPane
        fun updateState() {
            if (predicate.value.not()) localTabPane!!.tabs.remove(this.node)
            else if (this.node !in tabPane!!.tabs) localTabPane!!.tabs.add(this.node)
        }
        updateState()
        predicate.onChange { updateState() }
    }

    fun close() = removeFromParent()

    //fun TabPane.matt.prim.str.build.tab(text: String? = null, node: Node? = null, op: Tab.() -> Unit = {}): Tab {
    //    val matt.prim.str.build.tab = Tab(text,node)
    ////    matt.prim.str.build.tab.tag = tag
    //    tabs.add(matt.prim.str.build.tab)
    //    return matt.prim.str.build.tab.also(op)
    //}

    fun whenSelected(op: () -> Unit) {
        selectedProperty.onChange { if (it) op() }
    }

    fun select() = apply { tabPane!!.selectionModel.select(this.node) }

    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        requireNull(index)
        contentProperty v (child.node)
    }
}