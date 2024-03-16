package matt.fx.control.wrapper.virtualflow

import javafx.scene.control.IndexedCell
import javafx.scene.control.skin.VirtualFlow
import javafx.scene.layout.Region
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl

class VirtualFlowWrapper<T: IndexedCell<*>>(node: VirtualFlow<T>): RegionWrapperImpl<VirtualFlow<T>, NodeWrapper>(node, childClass = NodeWrapper::class) {
    override fun addChild(child: NodeWrapper, index: Int?) {
        TODO()
    }
}

/*FlowLess*/
class FlowLessVirtualFlowWrapper(val vflow: Region): RegionWrapperImpl<Region, NodeWrapper>(vflow, childClass = NodeWrapper::class) {
    override fun addChild(child: NodeWrapper, index: Int?) {
        TODO()
    }
}
