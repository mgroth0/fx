package matt.fx.graphics.menu.context

import javafx.application.Platform.runLater
import javafx.beans.property.BooleanProperty
import javafx.collections.ListChangeListener.Change
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
import javafx.scene.layout.Region
import javafx.scene.shape.Shape
import matt.async.date.tic
import matt.auto.jumpToKotlinSourceString
import matt.auto.openInIntelliJ
import matt.fx.graphics.hotkey.filters
import matt.fx.graphics.hotkey.handlers
import matt.fx.graphics.menu.actionitem
import matt.fx.graphics.menu.context.EventHandlerType.Filter
import matt.fx.graphics.menu.context.EventHandlerType.Handler
import matt.hurricanefx.tornadofx.menu.item
import matt.hurricanefx.tornadofx.menu.menu
import matt.hurricanefx.tornadofx.menu.separator
import matt.hurricanefx.wrapper.EventTargetWrapper
import matt.hurricanefx.wrapper.MenuItemWrapper
import matt.hurricanefx.wrapper.NodeW
import matt.hurricanefx.wrapper.NodeWrapper
import matt.hurricanefx.wrapper.NodeWrapper.Companion.wrapped
import matt.hurricanefx.wrapper.SceneWrapper
import matt.hurricanefx.wrapper.parent
import matt.klib.dmap.withStoringDefault
import matt.stream.map.lazyMap
import matt.stream.recurse.chain
import java.lang.Thread.sleep
import java.util.WeakHashMap
import kotlin.concurrent.thread
import kotlin.reflect.KClass


fun EventTarget.mcontextmenu(op: MContextMenuBuilder.()->Unit) {
  MContextMenuBuilder(this).apply(op)
}

fun EventTargetWrapper<*>.mcontextmenu(op: MContextMenuBuilder.()->Unit) = node.mcontextmenu(op)

class MContextMenuBuilder(
  val node: EventTarget,
  private val isGen: Boolean = false
) {


  val genList = mutableListOf<MenuItem>()

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

  fun item(s: String, g: NodeWrapper<*>? = null, op: MenuItemWrapper.()->Unit = {}) =
	MenuItemWrapper(s, g?.node).apply {
	  isMnemonicParsing = false
	  op()
	}.also { add(it.node) }


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
	require(!isGen)
	contextMenuItemGens[node]!!.add(op)

  }
  //
  //  fun <T: Any?> onRequest(keyGetter: ()->T, op: MContextMenuBuilder.(T)->Unit) {
  //	require(!isGen)
  //	contextMenuItemsByKey[node]!![key]!!.add(op(keyGetter()))
  //  }


}

private fun getCMItems(node: EventTarget): List<MenuItem>? {
  val normal = contextMenuItems[node]!!
  val gen = contextMenuItemGens[node]!!.flatMap {
	MContextMenuBuilder(node, isGen = true).apply(it).genList
  }
  //  val keyGen = contextMenuItemsByKey[node]!!
  return (normal + gen).takeIf { it.isNotEmpty() }
}


abstract class RunOnce {
  companion object {
	var ranOnce = mutableSetOf<KClass<out RunOnce>>()
  }


  protected abstract fun run()

  init {
	if (this::class !in ranOnce) {
	  run()
	  ranOnce += this::class
	}
  }

}


class CmFix: RunOnce() {
  override fun run() {
	/*https://bugs.openjdk.org/browse/JDK-8198497*/
	ContextMenu.getWindows().addListener { change: Change<*> ->
	  while (change.next()) {
		change.addedSubList.filterIsInstance<ContextMenu>().forEach { cm ->
		  cm.setOnShown {
			/* I added the thread and runLater, since this still isn't working */
			thread {
			  sleep(100)
			  runLater {
				cm.sizeToScene()
				/*IT FINALLY WORKS*/
			  }
			}
		  }
		}
	  }
	}
  }
}

val contextMenus = lazyMap<Scene, ContextMenu> {
  ContextMenu().apply {
	isAutoHide = true
	isAutoFix = true
  }
}





/**
 * see [here](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ContextMenu.html) for info on how to propertly use a context menu
 * KDoc test: [NodeWrapper]
 * [inline markup](https://kotlinlang.org/docs/kotlin-doc.html#inline-markup)
 *
 * @param target must be a [Node] and not a [NodeWrapper] because event targets dont carry a wrapper reference
 *
 */
fun SceneWrapper.showMContextMenu(
  target: Node, /*cannot be [NodeWrapper] because event targets are not wrappers?*/
  xy: Pair<Double, Double>
) {

  sequenceOf(1,2,3)

  CmFix()

  val t = tic(prefix = "showMContextMenu", enabled = false)
  t.toc("start")

  val devMenu = Menu("dev")

  t.toc("made devMenu")

  devMenu.actionitem("test exception") {
	throw Exception("test exception")
  }

  t.toc("made first actionitem")

  val reflectMenu = devMenu.menu("reflect")
  t.toc("made reflect menu")
  contextMenus[this.node].apply {
	items.clear()
	t.toc("cleared items")
	var node: EventTarget = target
	val added = mutableListOf<String>()
	t.toc("starting loop")
	while (true) {
	  t.toc("starting loop block for $node")
	  getCMItems(node)?.let {
		if (items.isNotEmpty()) separator()
		items += it
	  }
	  t.toc("got CmItems")
	  node::class.qualifiedName
		?.takeIf { "matt" in it && it !in added }
		?.let {
		  reflectMenu.actionitem(node::class.simpleName!!, threaded = true) {
			node::class.jumpToSource()
		  }
		  added += it
		}
	  t.toc("something with q name done")
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
	  t.toc("finished loop block")
	}
	if (items.isNotEmpty()) separator()
	t.toc("made spe")
	items += target.wrapped().hotkeyInfoMenu()
	t.toc("added hotkey info menu")
	items += devMenu
	t.toc("added devMeny")
  }.show(target, xy.first, xy.second)
  t.toc("showed cm")
}

enum class EventHandlerType {
  Handler, Filter
}

private fun KClass<*>.jumpToSource() {
  val pack = this.java.`package`.name
  jumpToKotlinSourceString(
	matt.file.commons.IdeProject.all.folder,
	this.simpleName!!,
	packageFilter = pack
  )?.let { fl ->
	openInIntelliJ(fl.first.absolutePath, fl.second)
  }
}

private fun NodeW.hotkeyInfoMenu() = Menu("Click For Hotkey Info").apply {
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

  setOnMouseClicked {
	items.clear()
	addInfo(Handler)
	addInfo(Filter)
  }

}


private val contextMenuItems = WeakHashMap<EventTarget, MutableList<MenuItem>>().withStoringDefault { mutableListOf() }

private val contextMenuItemGens =
  WeakHashMap<EventTarget, MutableList<MContextMenuBuilder.()->Unit>>().withStoringDefault { mutableListOf() }
//
//
//private val contextMenuItemsByKey = WeakHashMap<
//	EventTarget,
//	Map<
//		Any?,
//		MutableList<MenuItem>
//		>
//	>().withStoringDefault {
//  mutableMapOf<Any?, MutableList<MenuItem>>().withStoringDefault { mutableListOf() }
//}