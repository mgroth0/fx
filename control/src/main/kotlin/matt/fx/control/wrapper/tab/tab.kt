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
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.fx.base.wrapper.obs.collect.list.createMutableWrapper
import matt.obs.col.olist.sync.toSyncedList


fun <T: TabWrapper<*>> ET.tabpane(op: TabPaneWrapper<T>.()->Unit = {}) = TabPaneWrapper<T>().attachTo(this, op)


open class TabPaneWrapper<T: TabWrapper<*>>(
  node: TabPane = TabPane(),
): ControlWrapperImpl<TabPane>(node), Selects<T> {
  constructor(vararg tabs: T): this(
	TabPane(
	  *tabs.map { it.node }.toTypedArray()
	)
  )

  //  /*non null*/final override val selectedItemProperty: ObsVal<T> by lazy { selectionModel.selectedItemProperty.cast() }
  //  /*non null*/override val selectedItem: T by selectedItemProperty

  val tabs get() = node.tabs.createMutableWrapper().toSyncedList<Tab, T>(uncheckedWrapperConverter())

  override val selectionModel: SelectModWrap<T> by lazy {
	/*TODO: actually selection model is a property...*/
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


fun <W: NodeWrapper> TabPaneWrapper<TabWrapper<W>>.tab(
  text: String,
  content: W,
  index: Int? = null,
  closable: Boolean = true,
  op: W.()->Unit = {}
): TabWrapper<W> {
  val tab = TabWrapper(text, content)
  tab.isClosable = closable
  tabs.add(index ?: tabs.size, tab)
  op(content)
  return tab
}

fun <C: NodeWrapper> TabPaneWrapper<TabWrapper<VBoxWrapper<C>>>.vtab(
  s: String = "",
  op: VBoxWrapper<C>.()->Unit = {}
): TabWrapper<VBoxWrapper<C>> {
  return tab(s, VBoxWrapperImpl(), closable = false) {
	op()
  }
}
