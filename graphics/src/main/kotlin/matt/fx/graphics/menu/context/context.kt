package matt.fx.graphics.menu.context

import javafx.application.Platform.runLater
import javafx.beans.property.Property
import javafx.collections.ListChangeListener.Change
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.ContextMenu
import javafx.scene.shape.Shape
import matt.async.date.tic
import matt.auto.jumpToKotlinSourceString
import matt.auto.openInIntelliJ
import matt.fx.graphics.hotkey.filters
import matt.fx.graphics.hotkey.handlers
import matt.fx.graphics.menu.context.EventHandlerType.Filter
import matt.fx.graphics.menu.context.EventHandlerType.Handler
import matt.hurricanefx.wrapper.contextmenu.ContextMenuWrapper
import matt.hurricanefx.wrapper.menu.MenuWrapper
import matt.hurricanefx.wrapper.menu.checkitem.CheckMenuItemWrapper
import matt.hurricanefx.wrapper.menu.item.MenuItemWrapper
import matt.hurricanefx.wrapper.menu.item.SimpleMenuItem
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.hurricanefx.wrapper.node.NodeWrapperImpl
import matt.hurricanefx.wrapper.parent.parent
import matt.hurricanefx.wrapper.scene.SceneWrapper
import matt.hurricanefx.wrapper.target.EventTargetWrapper
import matt.hurricanefx.wrapper.wrapped
import matt.klib.dmap.DefaultStoringMap
import matt.klib.dmap.withStoringDefault
import matt.klib.str.tab
import matt.stream.map.lazyMap
import matt.stream.recurse.chain
import java.lang.Thread.sleep
import java.util.WeakHashMap
import kotlin.concurrent.thread
import kotlin.reflect.KClass

fun EventTargetWrapper.mcontextmenu(op: MContextMenuBuilder.()->Unit) = MContextMenuBuilder(this.node).apply(op)

class MContextMenuBuilder(
  val node: EventTarget,
  private val isGen: Boolean = false
) {


  val genList = mutableListOf<MenuItemWrapper<*>>()

  infix fun String.does(op: ()->Unit) = actionitem(this, op)
  infix fun String.doesInThread(op: ()->Unit) = actionitem(this) {
	thread {
	  op()
	}
  }

  fun actionitem(s: String, op: ()->Unit) = SimpleMenuItem(s).apply {
	isMnemonicParsing = false
	setOnAction {
	  op()
	  it.consume()
	}
  }.also { add(it) }

  fun item(s: String, g: NodeWrapperImpl<*>? = null, op: MenuItemWrapper<*>.()->Unit = {}) =
	SimpleMenuItem(s, g?.node).apply {
	  isMnemonicParsing = false
	  op()
	}.also { add(it) }


  infix fun String.toggles(b: Property<Boolean>) = checkitem(this, b)

  fun checkitem(s: String, b: Property<Boolean>, op: CheckMenuItemWrapper.()->Unit = {}) =
	CheckMenuItemWrapper(s).apply {
	  isMnemonicParsing = false
	  selectedProperty().bindBidirectional(b)
	  op()
	}.also { add(it) }

  fun menu(s: String, op: MenuWrapper.()->Unit) = MenuWrapper(s).apply {
	isMnemonicParsing = false
	op()
  }.also { add(it) }

  fun add(item: MenuItemWrapper<*>) {
	if (isGen) {
	  genList.add(item)
	} else {
	  contextMenuItems[node].add(item)
	}
  }


  fun onRequest(op: MContextMenuBuilder.()->Unit) {
	require(!isGen)
	contextMenuItemGens[node].add(op)

  }
  //
  //  fun <T: Any?> onRequest(keyGetter: ()->T, op: MContextMenuBuilder.(T)->Unit) {
  //	require(!isGen)
  //	contextMenuItemsByKey[node]!![key]!!.add(op(keyGetter()))
  //  }


}

private fun getCMItems(node: EventTarget): List<MenuItemWrapper<*>>? {
  val normal = contextMenuItems[node]
  val gen = contextMenuItemGens[node].flatMap {
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

val contextMenus = lazyMap<Scene, ContextMenuWrapper> {
  ContextMenuWrapper().apply {
	isAutoHide = true
	isAutoFix = true
  }
}


/**
 * see [here](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ContextMenu.html) for info on how to propertly use a context menu
 * KDoc test: [NodeWrapperImpl]
 * [inline markup](https://kotlinlang.org/docs/kotlin-doc.html#inline-markup)
 *
 * @param target must be a [Node] and not a [NodeWrapperImpl] because event targets dont carry a wrapper reference
 *
 */
fun SceneWrapper<*>.showMContextMenu(
  target: Node, /*cannot be [NodeWrapper] because event targets are not wrappers?*/
  xy: Pair<Double, Double>
) {

  sequenceOf(1, 2, 3)

  CmFix()

  val t = tic(prefix = "showMContextMenu", enabled = false)
  t.toc("start")

  val devMenu = MenuWrapper("dev")

  t.toc("made devMenu")

  devMenu.actionitem("test exception") {
	throw Exception("test exception")
  }

  devMenu.actionitem("print nodes") {
	println("NODES:")
	tab("TARGET:\t${target::class.simpleName}")
	var parent: Node? = target.parent
	while (parent != null) {
	  tab("PARENT:\t${parent::class.simpleName}")
	  parent = parent.parent
	}
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
	  //	  println("1looking for parent of $node (parent=${(node as? Parent)?.parent}) (scene=${(node as? Parent)?.scene})")
	  //	  println("1node=${(node as? Node)}")
	  //	  println("1node.scene=${(node as? Node)?.scene}")
	  //	  println("1node.scene.root=${(node as? Node)?.scene?.root}")
	  //	  println("1node.scene.root.scene=${(node as? Node)?.scene?.root?.scene}")
	  t.toc("starting loop block for $node")
	  getCMItems(node)?.let {
		if (items.isNotEmpty()) separator()
		items += it.map { it.node }
	  }
	  t.toc("got CmItems")
	  //	  println("2looking for parent of $node (parent=${(node as? Parent)?.parent}) (scene=${(node as? Parent)?.scene})")
	  //	  println("2node=${(node as? Node)}")
	  //	  println("2node.scene=${(node as? Node)?.scene}")
	  //	  println("2node.scene.root=${(node as? Node)?.scene?.root}")
	  //	  println("2node.scene.root.scene=${(node as? Node)?.scene?.root?.scene}")
	  node::class.qualifiedName
		?.takeIf { "matt" in it && it !in added }
		?.let {
		  reflectMenu.actionitem(node::class.simpleName!!, threaded = true) {
			node::class.jumpToSource()
		  }
		  added += it
		}
	  t.toc("something with q name done")
	  //	  println("3looking for parent of $node (parent=${(node as? Parent)?.parent}) (scene=${(node as? Parent)?.scene})")
	  //	  println("3node=${(node as? Node)}")
	  //	  println("3node.scene=${(node as? Node)?.scene}")
	  //	  println("3node.scene.root=${(node as? Node)?.scene?.root}")
	  //	  println("3node.scene.root.scene=${(node as? Node)?.scene?.root?.scene}")
	  node = when (node) {
		is Parent -> node.parent ?: node.scene
		is Shape  -> node.parent
		is Canvas -> node.parent
		is Scene  -> node.window
		else      -> break
	  }
	  t.toc("finished loop block")
	}
	if (items.isNotEmpty()) separator()
	t.toc("made spe")
	items += target.wrapped().hotkeyInfoMenu().node
	t.toc("added hotkey info menu")
	items += devMenu.node
	t.toc("added devMeny")
  }.node.show(target, xy.first, xy.second)
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

private fun NodeWrapper.hotkeyInfoMenu() = MenuWrapper("Click For Hotkey Info").apply {
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


private val contextMenuItems: DefaultStoringMap<EventTarget, MutableList<MenuItemWrapper<*>>> =
  WeakHashMap<EventTarget, MutableList<MenuItemWrapper<*>>>().withStoringDefault { mutableListOf() }

private val contextMenuItemGens: DefaultStoringMap<EventTarget, MutableList<MContextMenuBuilder.() -> Unit>> =
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