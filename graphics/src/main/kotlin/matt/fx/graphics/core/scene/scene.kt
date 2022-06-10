package matt.fx.graphics.core.scene

import javafx.event.Event
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.input.ContextMenuEvent
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import matt.auto.openInIntelliJ
import matt.fx.graphics.menu.context.mcontextmenu
import matt.fx.graphics.menu.context.showMContextMenu
import matt.fx.graphics.hotkeys.addDefaultHotkeys
import matt.fx.graphics.icon.ICON_HEIGHT
import matt.fx.graphics.icon.ICON_WIDTH
import matt.fx.graphics.icon.Icon
import matt.fx.graphics.icon.IconFolder
import matt.fx.graphics.menu.actionitem
import matt.fx.graphics.lang.onDoubleClickConsume
import matt.fx.graphics.mag.reversed_displays
import matt.fx.graphics.style.CUSTOM_CSS
import matt.fx.graphics.style.DARK_MODENA_CSS
import matt.fx.graphics.style.DarkModeController.darkModeProp
import matt.fx.graphics.style.borderFill
import matt.fx.graphics.style.styleInfo
import matt.fx.graphics.win.interact.WinGeom
import matt.fx.graphics.win.interact.WinOwn
import matt.fx.graphics.win.interact.openInNewWindow
import matt.fx.graphics.win.winfun.noDocking
import matt.hurricanefx.tornadofx.async.runLater
import matt.hurricanefx.tornadofx.menu.item
import matt.hurricanefx.tornadofx.menu.menu
import matt.stream.recurse.recurse
import matt.kjlib.byte.MemReport
import matt.klib.commons.get
import matt.klib.file.MFile

import java.net.URL
import kotlin.reflect.KClass

//val noContextMenu = WeakSet<Node>()


fun Scene.reloadStyle(darkMode: Boolean) {
  stylesheets.clear()
  fill = null
  if (darkMode) {
	stylesheets.add(DARK_MODENA_CSS)
	stylesheets.add(CUSTOM_CSS)

	//     ensure that even while the screen is loading it is black. So not white flashes or flickering while refreshing
	fill =
	  Color.BLACK    //        maybe also possible by styleing .root in css, but if I remember correctly that also affects other nodes
  }
}

open class MScene(
  root: Parent, val icon: MFile
): Scene(root) {
  constructor(
	root: Parent, icon: String
  ): this(root, IconFolder["white/$icon.png"])

  constructor(
	root: Parent
  ): this(root, "chunk")

  fun handleContextMenuReq(e: Event) {
	if (e is ContextMenuEvent) {
	  (e.target as? Node)?.let {        //                    if (it !in noContextMenu) {
		showMContextMenu(it, e.screenX to e.screenY)        //                    }
	  }
	  e.consume()
	}

	/*this doesnt work. lets try insets for right clicking*/    //	  else if (matt.kjlib.jmath.e is MouseEvent) {
	//		(matt.kjlib.jmath.e.target as? Node)?.let { showMContextMenu(it, matt.kjlib.jmath.e.screenX to matt.kjlib.jmath.e.screenY) }
	//		/*dont consume. maybe if I'm lucky I'll get both my context menu and the web one*/
	//	  }

  }


  init {
	addDefaultHotkeys()




	reloadStyle(darkModeProp.value)
	var old = darkModeProp.value
	darkModeProp.onChangeWithWeak(this) {
	  if (it != old) {
		reloadStyle(darkModeProp.value)
		old = it
	  }
	}




	mcontextmenu {
	  menu("style") {
		actionitem("reload style") {
		  reloadStyle(darkModeProp.value)
		}

		/*this is controlled from the OS from now on*/        /*matt.fx.graphics.menu.actionitem("toggle darkMode") {
		  darkMode = !darkMode
		  if (darkMode) {
			stylesheets.setAll(DARK_MODENA_CSS, CUSTOM_CSS)
		  } else {
			stylesheets.setAll()
		  }
		  darkModeListeners.forEach { it() }
		}*/


		actionitem("open darkModena.css") {
		  MFile(URL(DARK_MODENA_CSS).file).openInIntelliJ()
		}
		actionitem("open custom.css") {
		  MFile(URL(CUSTOM_CSS).file).openInIntelliJ()
		}
		actionitem("print style info samples") {
		  val classesPrinted = mutableListOf<KClass<*>>()
		  (root as Node).recurse {
			(it as? Parent)?.childrenUnmodifiable ?: listOf()
		  }.forEach {
			if (it::class !in classesPrinted) {
			  println(it.styleInfo())
			  classesPrinted += it::class
			}
		  }
		}        /*need this*/
		this.menu("set border") {        /*specify this here explicitly at least once
		  * or else it will use the `matt.fx.graphics.menu.actionitem` above without import*/
		  this.actionitem("none") {
			(root as? Region)?.borderFill = null
		  }
		  actionitem("yellow") {
			(root as? Region)?.borderFill = Color.YELLOW
		  }
		  actionitem("blue") {
			(root as? Region)?.borderFill = Color.BLUE
		  }
		  actionitem("red") {
			(root as? Region)?.borderFill = Color.RED
		  }
		  actionitem("green") {
			(root as? Region)?.borderFill = Color.GREEN
		  }
		  actionitem("orange") {
			(root as? Region)?.borderFill = Color.ORANGE
		  }
		  actionitem("purple") {
			(root as? Region)?.borderFill = Color.PURPLE
		  }
		  actionitem("white") {
			(root as? Region)?.borderFill = Color.WHITE
		  }
		}
	  }

	  actionitem("reverse displays") {
		reversed_displays = !reversed_displays
	  }

	  actionitem("test exception") {
		throw Exception("test exception")
	  }

	  actionitem("iconify", ::iconify)

	  onRequest {
		val mreport = MemReport()
		menu("MemReport") {        /*need one this to enforce THIS*/
		  this.item("total:${mreport.total}") {}
		  item("max:${mreport.max}") {}
		  item("free:${mreport.free}") {}
		}
	  }
	}


	/*dont know why I was using a filter here, but it prevented me from blocking context menus on certain nodes*/    //        addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED) { e ->
	//            handleContextMenuReq(e)
	//        }
	addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED) { e ->
	  handleContextMenuReq(e)
	}

	/*this doesnt work.  lets try insets for right clicking*/    //	/*for web view. plese work*/
	//	addEventFilter(MouseEvent.MOUSE_CLICKED) { matt.kjlib.jmath.e ->
	//	  if (matt.kjlib.jmath.e.isSecondaryButtonDown) {
	//		handleContextMenuReq(matt.kjlib.jmath.e)
	//	  }

	//	}
  }


  fun iconify() {
	var iconWindow: Stage? = null
	VBox(Icon(icon)).apply {
	  var xOffset: Double? = null
	  var yOffset: Double? = null
	  setOnMousePressed { e ->
		iconWindow?.let {
		  xOffset = it.x - e.screenX
		  yOffset = it.y - e.screenY
		}
	  }
	  setOnMouseDragged {
		iconWindow?.x = it.screenX + (xOffset ?: 0.0)
		iconWindow?.y = it.screenY + (yOffset ?: 0.0)
	  }
	  onDoubleClickConsume {
		runLater {
		  (this@MScene.window as Stage).show()
		  (scene.window as Stage).close()
		}
	  }
	}.openInNewWindow(own = WinOwn.None, geom = WinGeom.ManualOr0(
	  width = ICON_WIDTH,
	  height = ICON_HEIGHT,
	  x = this@MScene.window.x + (this@MScene.window.width/2) - (ICON_WIDTH/2),
	  y = this@MScene.window.y + (this@MScene.window.height/2) - (ICON_HEIGHT/2),
	), mScene = false, border = false, beforeShowing = {
	  scene.reloadStyle(darkModeProp.value)
	  darkModeProp.onChangeWithWeak(this) { scene.reloadStyle(darkModeProp.value) }
	}).apply {
	  iconWindow = this
	  isAlwaysOnTop = true
	  noDocking()
	}
	window.hide()
  }


}