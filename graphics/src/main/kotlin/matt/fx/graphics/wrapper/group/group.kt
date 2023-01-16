package matt.fx.graphics.wrapper.group

import javafx.scene.Group
import matt.fx.graphics.service.uncheckedWrapperConverter
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.node.parent.ParentWrapperImpl
import matt.hurricanefx.eye.wrapper.obs.collect.list.createMutableWrapper
import matt.log.warn.warn
import matt.obs.col.olist.sync.toSyncedList

fun <C: NodeWrapper> ET.group(initialChildren: Iterable<C>? = null, op: GroupWrapper<C>.()->Unit = {}) =
  attach(GroupWrapper<C>().apply { if (initialChildren != null) children.addAll(initialChildren) }, op)

class GroupWrapper<C: NodeWrapper>(node: Group = Group()): ParentWrapperImpl<Group, C>(node) {
  val children by lazy { node.children.createMutableWrapper().toSyncedList(uncheckedWrapperConverter()) }
  override fun addChild(child: NodeWrapper, index: Int?) {
	require(index == null)
	warn("unchecked add C")
	@Suppress("UNCHECKED_CAST")
	children.add(child as C)
  }

  override val childList get() = node.children
}

