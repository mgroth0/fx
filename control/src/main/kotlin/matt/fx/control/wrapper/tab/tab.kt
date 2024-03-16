package matt.fx.control.wrapper.tab

import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import matt.fx.base.wrapper.obs.collect.list.createMutableWrapper
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.tab.TabWrapper
import matt.fx.control.wrapper.selects.SelectModWrap
import matt.fx.control.wrapper.selects.Selects
import matt.fx.control.wrapper.selects.wrap
import matt.fx.graphics.service.wrapperConverter
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.obs.col.olist.sync.toSyncedList
import kotlin.reflect.KClass
import kotlin.reflect.cast


inline fun <reified T : TabWrapper<*>> ET.tabpane(op: TabPaneWrapper<T>.() -> Unit = {}) = TabPaneWrapper<T>().attachTo(this, op)


open class TabPaneWrapper<T : TabWrapper<*>>(
    node: TabPane = TabPane(),
    private val tabClass: KClass<T>
) : ControlWrapperImpl<TabPane>(node), Selects<T> {
    companion object {
        inline operator fun <reified T: TabWrapper<*>> invoke(vararg tabs: T): TabPaneWrapper<T> =
            TabPaneWrapper(
                TabPane(
                    *tabs.map { it.node }.toTypedArray()
                ),
                T::class
            )
    }



    val tabs get() = node.tabs.createMutableWrapper().toSyncedList<Tab, T>(wrapperConverter(Tab::class, tabClass))

    final override val selectionModel: SelectModWrap<T> by lazy {
        println("actually selection model is a property...")
        node.selectionModel.wrap(wrapperConverter(Tab::class, tabClass))
    }

    fun add(tab: T) = tabs.add(tab)
    fun add(
        index: Int,
        tab: T
    ) = tabs.add(index, tab)

    fun remove(tab: T) = tabs.remove(tab)
    operator fun contains(tab: T) = tab in tabs

    final override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        val tab = TabWrapper(child.toString(), child)
        if (index == null) add(tabClass.cast(tab))
        else add(index, tabClass.cast(tab))
    }
}


inline fun <reified W : NodeWrapper> TabPaneWrapper<TabWrapper<W>>.tab(
    text: String,
    content: W,
    index: Int? = null,
    closable: Boolean = true,
    op: W.() -> Unit = {}
): TabWrapper<W> {
    val tab = TabWrapper(text, content)
    tab.isClosable = closable
    tabs.add(index ?: tabs.size, tab)
    op(content)
    return tab
}

inline fun <reified C : NodeWrapper> TabPaneWrapper<TabWrapper<VBoxWrapper<C>>>.vtab(
    s: String = "",
    crossinline op: VBoxWrapper<C>.() -> Unit = {}
): TabWrapper<VBoxWrapper<C>> =
    tab(s, VBoxWrapperImpl(), closable = false) {
        op()
    }
