package matt.fx.control.wrapper.tab

import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.tab.TabWrapper
import matt.fx.control.wrapper.selects.SelectModWrap
import matt.fx.control.wrapper.selects.Selects
import matt.fx.control.wrapper.selects.wrap
import matt.fx.graphics.service.uncheckedWrapperConverter
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.wrapper.obs.collect.createMutableWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.obs.col.olist.mappedlist.toSyncedList


fun <T: TabWrapper<*>> ET.tabpane(op: TabPaneWrapper<T>.()->Unit = {}) = TabPaneWrapper<T>().attachTo(this, op)


open class TabPaneWrapper<T: TabWrapper<*>>(
  node: TabPane = TabPane(),
): ControlWrapperImpl<TabPane>(node), Selects<T> {
  constructor(vararg tabs: T): this(
	TabPane(
	  *tabs.map { it.node }.toTypedArray()
	)
  )

  val tabs get() = node.tabs.createMutableWrapper().toSyncedList<Tab, T>(uncheckedWrapperConverter())

  override val selectionModel: SelectModWrap<T> by lazy {
	node.selectionModel.wrap(uncheckedWrapperConverter())
  }

  fun add(tab: T) = tabs.add(tab)
  fun add(index: Int, tab: T) = tabs.add(index, tab)
  fun remove(tab: T) = tabs.remove(tab)
  operator fun contains(tab: T) = tab in tabs

  override fun addChild(child: NodeWrapper, index: Int?) {
	val tab = TabWrapper<NodeWrapper>(child.toString(), child)
	@Suppress("UNCHECKED_CAST")
	if (index == null) (this as TabPaneWrapper<TabWrapper<NodeWrapper>>).add(tab)
	else (this as TabPaneWrapper<TabWrapper<NodeWrapper>>).add(index, tab)
  }


}


fun <W: NodeWrapper> TabPaneWrapper<TabWrapper<*>>.tab(

  text: String,
  content: W,
  index: Int? = null,
  op: W.()->Unit = {}
): TabWrapper<*> {
  val tab = TabWrapper(text, content)
  tabs.add(index ?: tabs.size, tab)
  op(content)
  return tab
}

/*matt was here*/
fun <W: NodeWrapper> TabPaneWrapper<TabWrapper<*>>.staticTab(
  text: String,
  content: W,
  index: Int? = null,
  op: W.()->Unit = {}
): TabWrapper<*> {
  val tab = TabWrapper(text, content).apply {
	isClosable = false
  }
  tabs.add(index ?: tabs.size, tab)
  op(content)
  return tab
}


fun <C: NodeWrapper> TabPaneWrapper<TabWrapper<*>>.vtab(
  s: String = "",
  op: VBoxWrapperImpl<C>.()->Unit = {}
): TabWrapper<*> {
  return staticTab(s, VBoxWrapperImpl<C>()) {
	op()
  }
}
