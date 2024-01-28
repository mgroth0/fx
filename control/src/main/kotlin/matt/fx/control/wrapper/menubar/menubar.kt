package matt.fx.control.wrapper.menubar

import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.menu.MenuWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.ET

fun ET.menubar(op: MenuBarWrapper.()->Unit = {}) = MenuBarWrapper().attachTo(this, op)

open class MenuBarWrapper(
  node: MenuBar = MenuBar(),
): ControlWrapperImpl<MenuBar>(node) {

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

  final override fun addChild(child: NodeWrapper, index: Int?) {
	TODO()
  }


}