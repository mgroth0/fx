package matt.fx.control.wrapper.menubar

import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import matt.hurricanefx.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.menu.MenuWrapper
import matt.hurricanefx.wrapper.node.NodeWrapper

open class MenuBarWrapper(
  node: MenuBar = MenuBar(),
): ControlWrapperImpl<MenuBar>(node) {
  companion object {
	fun MenuBar.wrapped() = MenuBarWrapper(this)
  }

  val menus: ObservableList<Menu> get() = node.menus

  operator fun plusAssign(menu: MenuWrapper) {
	this.menus += menu.node
  }


  //MenuBar extensions
  fun menu(name: String? = null, graphic: Node? = null, op: MenuWrapper.()->Unit = {}) =
	MenuWrapper(name, graphic).also {
	  op(it)
	  this += it
	}

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }


}