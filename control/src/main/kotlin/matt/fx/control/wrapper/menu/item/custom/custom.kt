package matt.fx.control.wrapper.menu.item.custom

import javafx.scene.Node
import javafx.scene.control.CustomMenuItem
import matt.fx.control.wrapper.menu.item.MenuItemWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.lang.assertions.require.requireNull

open class CustomMenuItemWrapper(
    node: CustomMenuItem = CustomMenuItem(),
) : MenuItemWrapper<CustomMenuItem>(node) {
    constructor(
        g: Node,
        hideOnClick: Boolean
    ) : this(CustomMenuItem(g, hideOnClick))

    var content: Node?
        get() = node.content
        set(value) {
            node.content = value
        }

    final override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        requireNull(index)
        content = child.node
    }

}
