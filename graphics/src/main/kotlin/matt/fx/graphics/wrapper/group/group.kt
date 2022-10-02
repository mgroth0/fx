package matt.fx.graphics.wrapper.group

import javafx.scene.Group
import matt.hurricanefx.eye.wrapper.obs.collect.createMutableWrapper
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapperImpl
import matt.hurricanefx.wrapper.uncheckedWrapperConverter
import matt.log.warn
import matt.obs.col.olist.mappedlist.toSyncedList

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

