package matt.fx.control.wrapper.menu

import javafx.beans.property.Property
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.CustomMenuItem
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.ToggleGroup
import javafx.scene.input.KeyCombination
import matt.fx.control.wrapper.menu.item.MenuItemWrapper
import matt.fx.control.wrapper.menu.item.SimpleMenuItem
import matt.fx.control.wrapper.menu.radioitem.ValuedRadioMenuItem
import matt.obs.bindings.str.ObsS

interface ContextMenuBuilder {
    fun actionitem(
        s: String,
        op: () -> Unit
    ): MenuItemWrapper<*>
    infix fun String.does(op: () -> Unit) = actionitem(this, op)
}

class MenuWrapper(node: Menu) : MenuItemWrapper<Menu>(node), ContextMenuBuilder {

    constructor(
        text: String? = null,
        g: Node? = null
    ) : this(Menu(text, g))

    val items: ObservableList<MenuItem> get() = node.items

    //Menu-related operator functions
    operator fun <T : MenuItem> plusAssign(menuItem: T) {
        this.items += menuItem
    }


    fun menu(
        name: String? = null,
        keyCombination: KeyCombination? = null,
        graphic: Node? = null,
        op: MenuWrapper.() -> Unit = {}
    ) = MenuWrapper(name, graphic).also {
        keyCombination?.apply { it.accelerator = this }
        op(it)
        this += it.node
    }


    /**
     * Create a MenuItem. The op block operates on the MenuItem where you can call `action` to provide the menu item action.
     * Notice that this differs from the deprecated `menuitem` builder where the op
     * is configured as the `setOnAction` directly.
     */
    fun item(
        name: String,
        keyCombination: KeyCombination? = null,
        graphic: Node? = null,
        op: MenuItemWrapper<*>.() -> Unit = {}
    ) = SimpleMenuItem().apply { this.text = name; this.graphic = graphic }.also {
        keyCombination?.apply { it.accelerator = this }
        graphic?.apply { it.graphic = graphic }
        op(it)
        this += it.node
    }


    /**
     * Create a CustomMenuItem. You must provide a builder inside the `CustomMenuItem` or assign to the `content` property
     * of the item. The item action is configured with the `action` builder.
     */
    fun customitem(
        keyCombination: KeyCombination? = null,
        hideOnClick: Boolean = true,
        op: CustomMenuItem.() -> Unit = {}
    ) = CustomMenuItem().also {
        it.isHideOnClick = hideOnClick
        keyCombination?.apply { it.accelerator = this }
        op(it)
        this += it
    }


    /**
     * Create a MenuItem. The op block operates on the MenuItem where you can call `setOnAction` to provide the menu item action.
     */
    fun item(
        name: String,
        keyCombination: String,
        graphic: Node? = null,
        op: MenuItemWrapper<*>.() -> Unit = {}
    ) =
        item(name, KeyCombination.valueOf(keyCombination), graphic, op)

    /**
     * Create a MenuItem with the name property bound to the given observable string. The op block operates on the MenuItem where you can
     * call `setOnAction` to provide the menu item action. Notice that this differs from the deprecated `menuitem` builder where the op
     * is configured as the `setOnAction` directly.
     */
    fun item(
        name: ObsS,
        keyCombination: KeyCombination? = null,
        graphic: Node? = null,
        op: MenuItemWrapper<MenuItem>.() -> Unit = {}
    ) = MenuItemWrapper(MenuItem()).also {
        it.graphic = graphic
        it.textProperty.bind(name)
        keyCombination?.apply { it.accelerator = this }
        graphic?.apply { it.graphic = graphic }
        op(it)
        this += it.node
    }

    fun separator() {
        this += SeparatorMenuItem()
    }

    fun <V> radiomenuitem(
        name: String,
        toggleGroup: ToggleGroup? = null,
        keyCombination: KeyCombination? = null,
        graphic: Node? = null,
        value: V,
        op: ValuedRadioMenuItem<V>.() -> Unit = {}
    ) = ValuedRadioMenuItem(value, name, graphic).also {
        toggleGroup?.apply { it.node.toggleGroup = this }
        keyCombination?.apply { it.accelerator = this }
        graphic?.apply { it.graphic = graphic }
        op(it)
        this += it.node
    }

    fun checkmenuitem(
        name: String,
        keyCombination: String,
        graphic: Node? = null,
        selected: Property<Boolean>? = null,
        op: CheckMenuItem.() -> Unit = {}
    ) =
        checkmenuitem(name, KeyCombination.valueOf(keyCombination), graphic, selected, op)

    fun checkmenuitem(
        name: String,
        keyCombination: KeyCombination? = null,
        graphic: Node? = null,
        selected: Property<Boolean>? = null,
        op: CheckMenuItem.() -> Unit = {}
    ) = CheckMenuItem(name, graphic).also {
        keyCombination?.apply { it.accelerator = this }
        graphic?.apply { it.graphic = graphic }
        selected?.apply { it.selectedProperty().bindBidirectional(this) }
        op(it)
        this += it
    }


    override fun actionitem(
        s: String,
        op: () -> Unit
    ) = item(s) {
        setOnAction {
            op()
        }
    }

}