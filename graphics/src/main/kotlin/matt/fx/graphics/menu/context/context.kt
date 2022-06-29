package matt.fx.graphics.menu.context

import javafx.beans.property.BooleanProperty
import javafx.event.EventTarget
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.ContextMenu
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.layout.Region
import javafx.scene.shape.Shape
import matt.auto.openInIntelliJ
import matt.file.commons.RootProjects.flow
import matt.fx.graphics.hotkey.filters
import matt.fx.graphics.hotkey.handlers
import matt.fx.graphics.menu.actionitem
import matt.fx.graphics.menu.context.EventHandlerType.Filter
import matt.fx.graphics.menu.context.EventHandlerType.Handler
import matt.hurricanefx.stage
import matt.hurricanefx.tornadofx.menu.item
import matt.hurricanefx.tornadofx.menu.lazyContextmenu
import matt.hurricanefx.tornadofx.menu.menu
import matt.hurricanefx.tornadofx.menu.separator
import matt.kjlib.byte.MemReport
import matt.kjlib.reflect.jumpToKotlinSourceString
import matt.klib.lang.NEVER
import matt.stream.recurse.chain
import java.util.WeakHashMap
import kotlin.collections.set
import kotlin.concurrent.thread
import kotlin.reflect.KClass

val contextMenuItems = WeakHashMap<EventTarget, MutableList<MenuItem>>()
val contextMenuItemGens = WeakHashMap<EventTarget, MutableList<MContextMenuBuilder.()->Unit>>()

fun EventTarget.mcontextmenu(op: MContextMenuBuilder.()->Unit) {
  MContextMenuBuilder(this).apply(op)
}

class MContextMenuBuilder(
  val node: EventTarget,
  private val isGen: Boolean = false
) {
  val genList = mutableListOf<MenuItem>()

  init {
	if (!isGen && contextMenuItems[node] == null) {
	  contextMenuItems[node] = mutableListOf()
	}
  }

  infix fun String.does(op: ()->Unit) = actionitem(this, op)
  infix fun String.doesInThread(op: ()->Unit) = actionitem(this) {
	thread {
	  op()
	}
  }

  fun actionitem(s: String, op: ()->Unit) = MenuItem(s).apply {
	isMnemonicParsing = false
	setOnAction {
	  op()
	  it.consume()
	}
  }.also { add(it) }

  fun item(s: String, g: Node? = null, op: MenuItem.()->Unit = {}) = MenuItem(s, g).apply {
	isMnemonicParsing = false
	op()
  }.also { add(it) }


  infix fun String.toggles(b: BooleanProperty) = checkitem(this, b)
  fun checkitem(s: String, b: BooleanProperty, op: CheckMenuItem.()->Unit = {}) = CheckMenuItem(s).apply {
	isMnemonicParsing = false
	selectedProperty().bindBidirectional(b)
	op()
  }.also { add(it) }

  fun menu(s: String, op: Menu.()->Unit) = Menu(s).apply {
	isMnemonicParsing = false
	op()
  }.also { add(it) }

  fun add(item: MenuItem) {
	if (isGen) {
	  genList.add(item)
	} else {
	  contextMenuItems[node]!!.add(item)
	}
  }


  fun onRequest(op: MContextMenuBuilder.()->Unit) {
	if (isGen) NEVER
	if (contextMenuItemGens[node] == null) {
	  contextMenuItemGens[node] = mutableListOf()
	}
	contextMenuItemGens[node]!!.add(op)
  }


}

private fun getCMItems(node: EventTarget): List<MenuItem>? {
  val normal = contextMenuItems[node]
  val gen = contextMenuItemGens[node]?.flatMap {
	MContextMenuBuilder(node, isGen = true).apply(it).genList
  }
  return ((normal ?: listOf()) + (gen ?: listOf())).takeIf { it.isNotEmpty() }
}

fun showMContextMenu(
  target: Node,
  xy: Pair<Double, Double>
) {
  val devMenu = Menu("dev")

  devMenu.actionitem("test exception") {
	throw Exception("test exception")
  }

  val reflectMenu = devMenu.menu("reflect")
  ContextMenu().apply {
	isAutoHide = true; isAutoFix = true
	var node: EventTarget = target
	val added = mutableListOf<String>()
	while (true) {
	  getCMItems(node)?.let {
		if (items.isNotEmpty()) separator()
		items += it
	  }
	  node::class.qualifiedName
		?.takeIf { "matt" in it && it !in added }
		?.let {
		  reflectMenu.actionitem(node::class.simpleName!!, threaded = true) {
			node::class.jumpToSource()
		  }
		  added += it
		}
	  node = when (node) {
		is Region, is Group -> when ((node as Parent).parent) {
		  null -> node.scene
		  else -> node.parent
		}

		is Shape            -> node.parent
		is Canvas           -> node.parent
		is Scene            -> node.window
		else                -> break
	  }
	}
	if (items.isNotEmpty()) separator()
	items += target.hotkeyInfoMenu()
	items += devMenu
  }.show(target, xy.first, xy.second)
}

enum class EventHandlerType {
  Handler, Filter
}

private fun KClass<*>.jumpToSource() {
  val pack = this.java.`package`.name
  jumpToKotlinSourceString(
	flow.folder,
	this.simpleName!!,
	packageFilter = pack
  )?.let { fl ->
	openInIntelliJ(fl.first.absolutePath, fl.second)
  }
}

private fun Node.hotkeyInfoMenu() = Menu("Hotkey Info").apply {
  val node = this@hotkeyInfoMenu
  fun addInfo(type: EventHandlerType) {
	menu(
	  when (type) {
		Handler -> "handlers"; Filter -> "filters"
	  }
	) {
	  (node.chain { it.parent } + node.scene + node.stage).forEach { node ->
		menu(node.toString()) {
		  val h = when (type) {
			Handler -> handlers[node]
			Filter  -> filters[node]
		  }
		  item("\tqp=${h?.quickPassForNormalTyping}")
		  handlers[node]?.hotkeys?.forEach { hkc ->
			item("\t${hkc.getHotkeys().joinToString { it.toString() }}")
		  }
		}
	  }
	}
  }

  items.clear()
  addInfo(Handler)
  addInfo(Filter)
}

