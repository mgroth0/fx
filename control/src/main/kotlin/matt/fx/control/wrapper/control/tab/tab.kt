package matt.fx.control.wrapper.control.tab

import javafx.collections.ObservableMap
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.base.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.lang.assertions.require.requireNull
import matt.obs.bindings.bool.ObsB
import matt.obs.bindings.bool.not

open class TabWrapper<C : NodeWrapper?>(
    node: Tab = Tab()
) : SingularEventTargetWrapper<Tab>(node) {

    final override fun isInsideRow() = false

    constructor(
        text: String?,
        content: C? = null
    ) : this(Tab(text, content?.node))

    val closableProp: NonNullFXBackedBindableProp<Boolean> by lazy { node.closableProperty().toNonNullableProp() }
    var isClosable by closableProp

    final override val properties: ObservableMap<Any, Any?> get() = node.properties

    final override fun removeFromParent() {
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
    val isSelected: Boolean by selectedProperty
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
            if (predicate.value.not()) localTabPane!!.tabs.remove(node)
            else if (node !in tabPane!!.tabs) localTabPane!!.tabs.add(node)
        }
        updateState()
        predicate.onChange { updateState() }
    }

    fun close() = removeFromParent()


    fun whenSelected(op: () -> Unit) {
        selectedProperty.onChange { if (it) op() }
    }

    fun select() = apply { tabPane!!.selectionModel.select(this.node) }

    final override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        requireNull(index)
        contentProperty v (child.node)
    }
}
