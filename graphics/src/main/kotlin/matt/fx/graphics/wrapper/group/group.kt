package matt.fx.graphics.wrapper.group

import javafx.collections.ObservableList
import javafx.scene.Group
import javafx.scene.Node
import matt.fx.base.wrapper.obs.collect.list.createMutableWrapper
import matt.fx.graphics.service.wrapperConverter
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.node.parent.ParentWrapperImpl
import matt.lang.assertions.require.requireNull
import matt.log.warn.common.warn
import matt.obs.col.olist.sync.toSyncedList
import kotlin.reflect.KClass
import kotlin.reflect.cast

inline fun <reified C: NodeWrapper> ET.group(initialChildren: Iterable<C>? = null, op: GroupWrapper<C>.() -> Unit = {}) =
    attach(
        GroupWrapper<C>(
            childClass = C::class
        ).apply { if (initialChildren != null) children.addAll(initialChildren) },
        op
    )

class GroupWrapper<C: NodeWrapper>(node: Group = Group(), childClass: KClass<C>): ParentWrapperImpl<Group, C>(node, childClass) {
    val children by lazy {
        node.children.createMutableWrapper().toSyncedList(
            wrapperConverter(
                Node::class,
                NodeWrapper::class
            )
        )
    }
    override fun addChild(child: NodeWrapper, index: Int?) {
        requireNull(index)
        warn("unchecked add C")
        children.add(childClass.cast(child))
    }

    override val childList: ObservableList<Node> get() = node.children
}

