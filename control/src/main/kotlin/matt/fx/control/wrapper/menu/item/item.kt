package matt.fx.control.wrapper.menu.item

import javafx.beans.property.BooleanProperty
import javafx.collections.ObservableMap
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.MenuItem
import javafx.scene.input.KeyCombination
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.lang.anno.Open
import matt.lang.assertions.require.requireNull
import matt.model.op.convert.NullToBlankStringConverter
import matt.obs.bindings.bool.ObsB
import matt.obs.bindings.bool.not

fun SimpleMenuItem() = MenuItemWrapper(MenuItem())
fun SimpleMenuItem(
    text: String?,
    g: Node? = null
) = MenuItemWrapper(MenuItem(text, g))

open class MenuItemWrapper<N : MenuItem>(
    node: N
) : SingularEventTargetWrapper<N>(node) {

    companion object {
        fun construct(
            text: String,
            graphic: Node? = null
        ) = MenuItemWrapper(MenuItem(text, graphic))
    }


    final override val properties: ObservableMap<Any, Any?> get() = node.properties

    val visibleProperty by lazy { node.visibleProperty().toNonNullableProp() }
    val disableProperty by lazy { node.disableProperty().toNonNullableProp() }

    var isMnemonicParsing
        get() = node.isMnemonicParsing
        set(value) {
            node.isMnemonicParsing = value
        }

    fun mnemonicParsingProperty(): BooleanProperty = node.mnemonicParsingProperty()

    var onAction: EventHandler<ActionEvent>?
        get() = node.onAction
        set(value) {
            node.onAction = value
        }

    fun setOnAction(op: (ActionEvent) -> Unit) {
        node.setOnAction(op)
    }

    var text: String
        get() = node.text
        set(value) {
            node.text = value
        }

    val textProperty by lazy { node.textProperty().toNullableProp().proxyInv(NullToBlankStringConverter) }


    var graphic: Node?
        get() = node.graphic
        set(value) {
            node.graphic = value
        }
    var accelerator: KeyCombination?
        get() = node.accelerator
        set(value) {
            node.accelerator = value
        }


    fun visibleWhen(expr: () -> ObsB) = visibleWhen(expr())

    fun visibleWhen(predicate: ObsB) = visibleProperty.bind(predicate)
    fun disableWhen(expr: () -> ObsB) = disableWhen(expr())
    fun disableWhen(predicate: ObsB) = disableProperty.bind(predicate)
    fun enableWhen(expr: () -> ObsB) = enableWhen(expr())
    fun enableWhen(obs: ObsB) = disableProperty.bind(obs.not())

    @Open
    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        requireNull(index)
        graphic = child.node
    }

    final override fun removeFromParent() {
        node.parentMenu.items.remove(node)
    }

    final override fun isInsideRow() = false
}


fun MenuItemWrapper<*>.action(op: () -> Unit) = setOnAction { op() }
