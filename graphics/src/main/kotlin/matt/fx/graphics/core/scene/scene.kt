package matt.fx.graphics.core.scene

import javafx.event.Event
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.input.ContextMenuEvent
import javafx.scene.paint.Color
import javafx.scene.paint.Color.BLUE
import javafx.scene.paint.Color.GREEN
import javafx.scene.paint.Color.ORANGE
import javafx.scene.paint.Color.PURPLE
import javafx.scene.paint.Color.RED
import javafx.scene.paint.Color.WHITE
import javafx.scene.paint.Color.YELLOW
import javafx.stage.Stage
import matt.auto.openInIntelliJ
import matt.color.name.ColorUtils
import matt.file.MFile
import matt.file.commons.ICON_FOLDER
import matt.file.construct.mFile
import matt.fx.graphics.hotkeys.addDefaultHotkeys
import matt.fx.graphics.icon.ICON_HEIGHT
import matt.fx.graphics.icon.ICON_WIDTH
import matt.fx.graphics.icon.Icon
import matt.fx.graphics.mag.reversed_displays
import matt.fx.graphics.menu.context.mcontextmenu
import matt.fx.graphics.menu.context.showMContextMenu
import matt.fx.graphics.style.CUSTOM_CSS
import matt.fx.graphics.style.DARK_MODENA_CSS
import matt.fx.graphics.style.DarkModeController.darkModeProp
import matt.fx.graphics.toAwtColor
import matt.fx.graphics.win.interact.WinGeom
import matt.fx.graphics.win.interact.WinOwn
import matt.fx.graphics.win.interact.openInNewWindow
import matt.fx.graphics.win.winfun.noDocking
import matt.hurricanefx.wrapper.node.setOnDoubleClick
import matt.hurricanefx.wrapper.pane.vbox.VBoxWrapper
import matt.hurricanefx.wrapper.parent.ParentWrapper
import matt.hurricanefx.wrapper.region.RegionWrapper
import matt.hurricanefx.wrapper.region.RegionWrapperImpl
import matt.hurricanefx.wrapper.scene.SceneWrapper
import matt.hurricanefx.wrapper.stage.StageWrapper
import matt.hurricanefx.wrapper.wrapped
import matt.klib.str.tab
import matt.stream.recurse.recurse
import java.net.URL
import kotlin.reflect.KClass

/*this is for when I have two monitors that are the scame brand and therefore mac is mixing up which side each is on I think*/
const val NEED_REVERSED_DISPLAYS_FEATURE = false

fun SceneWrapper<*>.reloadStyle(darkMode: Boolean) {
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
  root: ParentWrapper, val icon: MFile
): SceneWrapper<ParentWrapper>(root) {
  constructor(
	root: ParentWrapper, icon: String
  ): this(root, ICON_FOLDER["white/$icon.png"])

  constructor(
	root: ParentWrapper
  ): this(root, "chunk")

  private fun handleContextMenuReq(e: Event) {
	println("context menu requested from e=${e.hashCode()}")
	tab("target=${e.target}")
	tab("source=${e.source}")
	if (e is ContextMenuEvent) {
	  (e.target as? Node)?.let {
		showMContextMenu(it, e.screenX to e.screenY)
	  }
	  e.consume()
	}
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
		  mFile(URL(DARK_MODENA_CSS).file).openInIntelliJ()
		}
		actionitem("open custom.css") {
		  mFile(URL(CUSTOM_CSS).file).openInIntelliJ()
		}
		actionitem("print style info samples") {
		  val classesPrinted = mutableListOf<KClass<*>>()
		  /*  (root.node as Node).recurse {
			  (it as? Parent)?.childrenUnmodifiable ?: listOf()
			}.forEach {
			  if (it::class !in classesPrinted) {
				println(it.wrapped().styleInfo())
				classesPrinted += it::class
			  }
			}*/

		  (root.node as Node).recurse {
			(it as? Parent)?.childrenUnmodifiable ?: listOf()
		  }.forEach {
			if (it::class !in classesPrinted) {
			  println(it.wrapped().styleInfo())
			  classesPrinted += it::class
			}
		  }

		}        /*need this*/
		this.menu("set border") {        /*specify this here explicitly at least once
		  * or else it will use the `matt.fx.graphics.menu.actionitem` above without import*/
		  this.actionitem("none") {
			(root as RegionWrapper).borderFill = null
			/*(root.node as? Region)?.wrapped()?.borderFill = null*/
		  }
		  listOf(YELLOW, BLUE, RED, GREEN, ORANGE, PURPLE, WHITE).forEach {
			actionitem(ColorUtils().getColorNameFromColor(it.toAwtColor())) {
			  (root as RegionWrapper).borderFill = it
			  /*(root.node as? Region)?.wrapped()?.borderFill = it*/
			}
		  }
		}
	  }


	  if (NEED_REVERSED_DISPLAYS_FEATURE) actionitem("reverse displays") {
		reversed_displays = !reversed_displays
	  }

	}
	addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED) { e ->
	  handleContextMenuReq(e)
	}
  }

  fun iconify() = iconify(icon)
}


fun SceneWrapper<*>.iconify(icon: MFile) {
  var iconWindow: StageWrapper? = null
  println("making icon with $icon")
  VBoxWrapper(Icon(icon)).apply {
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
	setOnDoubleClick {
	  (this@iconify.window as Stage).show()
	  (scene!!.window as Stage).close()
	}
  }.openInNewWindow(own = WinOwn.None, geom = WinGeom.ManualOr0(
	width = ICON_WIDTH,
	height = ICON_HEIGHT,
	x = this@iconify.window.x + (this@iconify.window.width/2) - (ICON_WIDTH/2),
	y = this@iconify.window.y + (this@iconify.window.height/2) - (ICON_HEIGHT/2),
  ), mScene = false, border = false, beforeShowing = {
	scene.wrapped().reloadStyle(darkModeProp.value)
	darkModeProp.onChangeWithWeak(this) { scene.wrapped().reloadStyle(darkModeProp.value) }
  }).apply {
	iconWindow = this
	isAlwaysOnTop = true
	noDocking()
  }
  window.hide()
}