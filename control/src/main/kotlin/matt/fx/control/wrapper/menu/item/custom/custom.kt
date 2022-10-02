package matt.fx.control.wrapper.menu.item.custom

import javafx.scene.Node
import javafx.scene.control.CustomMenuItem
import matt.fx.control.wrapper.menu.item.MenuItemWrapper

open class CustomMenuItemWrapper(
  node: CustomMenuItem = CustomMenuItem(),
): MenuItemWrapper<CustomMenuItem>(node) {
  constructor(g: Node, hideOnClick: Boolean): this(CustomMenuItem(g, hideOnClick))

  var content: Node?
	get() = node.content
	set(value) {
	  node.content = value
	}

  override fun addChild(child: NodeWrapper, index: Int?) {
	require(index == null)
	content = child.node
  }

}