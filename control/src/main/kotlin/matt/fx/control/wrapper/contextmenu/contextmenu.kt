package matt.fx.control.wrapper.contextmenu

import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.ContextMenu
import javafx.scene.control.Control
import javafx.scene.control.CustomMenuItem
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.ToggleGroup
import javafx.scene.input.KeyCombination
import matt.fx.control.wrapper.menu.item.MenuItemWrapper
import matt.fx.control.wrapper.menu.item.SimpleMenuItem
import matt.fx.control.wrapper.menu.item.action
import matt.fx.control.wrapper.menu.radioitem.ValuedRadioMenuItem
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.window.WindowWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.obs.bindings.str.ObsS
import kotlin.concurrent.thread


fun ET.contextmenu(op: ContextMenu.()->Unit = {}): ContextMenu {
  val menu = (this as? Control)?.contextMenu ?: ContextMenu()
  op(menu)
  if (this is Control) {
	contextMenu = menu
  } else (this as? Node)?.apply {
	setOnContextMenuRequested { event ->
	  menu.show(this, event.screenX, event.screenY)
	  event.consume()
	}
  }
  return menu
}


/**
 * Add a context menu to the target which will be created on demand.
 */
fun ET.lazyContextmenu(op: ContextMenu.()->Unit = {}) = apply {
  var currentMenu: ContextMenu? = null
  (this as? Node)?.setOnContextMenuRequested { event ->
	currentMenu?.hide()
	currentMenu = ContextMenu().also {
	  it.setOnCloseRequest { currentMenu = null }
	  op(it)
	  it.show(this, event.screenX, event.screenY)
	}
	event.consume()
  }
}


class ContextMenuWrapper(node: ContextMenu = ContextMenu()): WindowWrapper<ContextMenu>(node) {
  val items: ObservableList<MenuItem> get() = node.items


  val autoHideProperty by lazy { node.autoHideProperty().toNonNullableProp() }
  var isAutoHide by autoHideProperty
  var isAutoFix by autoHideProperty


  operator fun <T: MenuItem> plusAssign(menuItem: T) {
	this.items += menuItem
  }

  //ContextMenu extensions
  fun menu(name: String? = null, op: Menu.()->Unit = {}) = Menu(name).also {
	op(it)
	this += it
  }


  /**
   * Create a MenuItem. The op block will be configured as the `setOnAction`. This will be deprecated in favor of the `item` call, where the
   * op block operates on the MenuItem. This deprecation was made to align the menuitem builder with the other builders.
   */
  @Deprecated(
	"Use the item builder instead, which expects an action parameter",
	ReplaceWith("item(name, KeyCombination.valueOf(keyCombination), matt.fx.control.inter.getGraphic).action(onAction)")
  )
  fun menuitem(
	name: String, keyCombination: String, graphic: Node? = null, onAction: ()->Unit = {}
  ): MenuItemWrapper<*> = item(name, KeyCombination.valueOf(keyCombination), graphic).apply {
	action(onAction)
  }

  fun checkmenuitem(
	name: String, keyCombination: KeyCombination? = null, graphic: Node? = null, op: CheckMenuItem.()->Unit = {}
  ) = CheckMenuItem(name, graphic).also {
	keyCombination?.apply { it.accelerator = this }
	graphic?.apply { it.graphic = graphic }
	op(it)
	this += it
  }

  /**
   * Create a MenuItem. The op block operates on the MenuItem where you can call `setOnAction` to provide the menu item action. Notice that this differs
   * from the deprecated `menuitem` builder where the op is configured as the `setOnAction` directly.
   */
  fun item(
	name: String, keyCombination: KeyCombination? = null, graphic: Node? = null, op: MenuItemWrapper<*>.()->Unit = {}
  ) = SimpleMenuItem().apply { this.text = name; this.graphic = graphic }.also {
	keyCombination?.apply { it.accelerator = this }
	graphic?.apply { it.graphic = this }
	op(it)
	this += it.node
  }

  /**
   * Create a MenuItem with the name property bound to the given observable string. The op block operates on the MenuItem where you can
   * call `setOnAction` to provide the menu item action. Notice that this differs from the deprecated `menuitem` builder where the op
   * is configured as the `setOnAction` directly.
   */
  fun item(
	name: ObsS,
	keyCombination: KeyCombination? = null,
	graphic: Node? = null,
	op: MenuItemWrapper<*>.()->Unit = {}
  ) = SimpleMenuItem().apply { this.graphic = graphic }.also {
	it.textProperty.bind(name)
	keyCombination?.apply { it.accelerator = this }
	graphic?.apply { it.graphic = this }
	op(it)
	this += it.node
  }


  /**
   * Add a separator to the contextmenu
   */
  fun separator(op: SeparatorMenuItem.()->Unit = {}) {
	this += SeparatorMenuItem().also(op)
  }


  /**
   * Create a CustomMenuItem. You must provide a builder inside the `CustomMenuItem` or assign to the `content` property
   * of the item. The item action is configured with the `action` builder.
   */
  fun customitem(
	keyCombination: KeyCombination? = null, hideOnClick: Boolean = true, op: CustomMenuItem.()->Unit = {}
  ) = CustomMenuItem().also {
	it.isHideOnClick = hideOnClick
	keyCombination?.apply { it.accelerator = this }
	op(it)
	this += it
  }

  fun <V> radiomenuitem(
	name: String, toggleGroup: ToggleGroup? = null, keyCombination: KeyCombination? = null,
	graphic: Node? = null, value: V, op: ValuedRadioMenuItem<V>.()->Unit = {}
  ) = ValuedRadioMenuItem<V>(value, name, graphic).also {
	toggleGroup?.apply { it.node.toggleGroup = this }
	keyCombination?.apply { it.accelerator = this }
	graphic?.apply { it.graphic = graphic }
	op(it)
	this += it.node
  }


  fun actionitem(s: String, threaded: Boolean = false, op: ()->Unit) {
	item(s) {
	  setOnAction {
		if (threaded) thread { op() }
		else op()
	  }
	}
  }

  // because when in listcells, "item" is taken
  @Suppress("unused")
  fun menuitem(
	name: String, keyCombination: KeyCombination? = null, graphic: Node? = null, op: MenuItemWrapper<*>.()->Unit = {}
  ) = item(name, keyCombination, graphic, op)

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }


}