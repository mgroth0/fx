package matt.fx.control.wrapper.menu.button

import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.MenuButton
import javafx.scene.control.MenuItem
import javafx.scene.control.ToggleGroup
import javafx.scene.input.KeyCombination
import matt.fx.control.wrapper.control.button.base.ButtonBaseWrapper
import matt.fx.control.wrapper.menu.MenuWrapper
import matt.fx.control.wrapper.menu.checkitem.CheckMenuItemWrapper
import matt.fx.control.wrapper.menu.item.MenuItemWrapper
import matt.fx.control.wrapper.menu.item.custom.CustomMenuItemWrapper
import matt.fx.control.wrapper.menu.radioitem.ValuedRadioMenuItem

open class MenuButtonWrapper(
  node: MenuButton = MenuButton(),
): ButtonBaseWrapper<MenuButton>(node) {
  companion object {
	fun MenuButton.wrapped() = MenuButtonWrapper(this)
  }

  val items: ObservableList<MenuItem> get() = node.items

  fun menu(
	name: String? = null, keyCombination: KeyCombination? = null, graphic: Node? = null, op: MenuWrapper.()->Unit = {}
  ) = MenuWrapper(name, graphic).also {
	keyCombination?.apply { it.accelerator = this }
	op(it)
	items += it.node
  }


  fun menu(name: String? = null, keyCombination: String, graphic: Node? = null, op: MenuWrapper.()->Unit = {}) =
	menu(name, KeyCombination.valueOf(keyCombination), graphic, op)


  /**
   * Create a CustomMenuItem. You must provide a builder inside the `CustomMenuItem` or assign to the `content` property
   * of the item. The item action is configured with the `action` builder.
   */
  fun customitem(
	keyCombination: KeyCombination? = null, hideOnClick: Boolean = true, op: CustomMenuItemWrapper.()->Unit = {}
  ) = CustomMenuItemWrapper().also {
	it.node.isHideOnClick = hideOnClick
	keyCombination?.apply { it.accelerator = this }
	op(it)
	items.add(it.node)
  }


  fun MenuButtonWrapper.item(
	name: String,
	keyCombination: KeyCombination? = null,
	graphic: Node? = null,
	op: MenuItemWrapper<MenuItem>.()->Unit = {}
  ) = MenuItemWrapper.construct(name, graphic).also {
	keyCombination?.apply { it.accelerator = this }
	graphic?.apply { it.graphic = graphic }
	op(it)
	items += it.node
  }


  fun <V> radiomenuitem(
	name: String,
	toggleGroup: ToggleGroup? = null,
	keyCombination: KeyCombination? = null,
	graphic: Node? = null,
	value: V,
	op: ValuedRadioMenuItem<V>.()->Unit = {}
  ) = ValuedRadioMenuItem(value, name, graphic).also {
	toggleGroup?.apply { it.node.toggleGroup = this }
	keyCombination?.apply { it.accelerator = this }
	graphic?.apply { it.graphic = graphic }
	op(it)
	items += it.node
  }

  fun checkmenuitem(
	name: String, keyCombination: KeyCombination? = null, graphic: Node? = null, op: CheckMenuItemWrapper.()->Unit = {}
  ) = CheckMenuItemWrapper(name, graphic).also {
	keyCombination?.apply { it.accelerator = this }
	graphic?.apply { it.graphic = graphic }
	op(it)
	items += it.node
  }


}