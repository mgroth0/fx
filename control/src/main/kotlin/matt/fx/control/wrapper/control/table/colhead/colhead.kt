package matt.fx.control.wrapper.control.table.colhead

import javafx.scene.control.skin.TableColumnHeader
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl

class TableColumnHeaderWrapper(node: TableColumnHeader): RegionWrapperImpl<TableColumnHeader, NodeWrapper>(node) {

    override fun isInsideRow() = false

    override fun addChild(child: NodeWrapper, index: Int?) {
        TODO()
    }
}
