package matt.fx.control.wrapper.virtualflow.clip

import javafx.scene.layout.Region
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.lang.assertions.require.requireEquals
import matt.lang.classname.common.JvmQualifiedClassName
import matt.lang.classname.j.jvmQualifiedClassName

val CLIPPED_CONTAINER_QNAME = JvmQualifiedClassName("javafx.scene.control.skin.VirtualFlow.ClippedContainer")

class ClippedContainerWrapper(node: Region) : RegionWrapperImpl<Region, NodeWrapper>(node) {
    init {
        requireEquals(node::class.jvmQualifiedClassName, CLIPPED_CONTAINER_QNAME) {
            "this class is reserved for ClippedContainer, but was created with a ${node::class.qualifiedName}"
        }
    }

    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }
}
