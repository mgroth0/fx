package matt.fx.control.mscene

import javafx.event.Event
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.input.ContextMenuEvent
import javafx.scene.paint.Color
import matt.collect.itr.recurse.recurse
import matt.color.name.ColorUtils
import matt.file.MFile
import matt.file.commons.ICON_FOLDER
import matt.file.construct.mFile
import matt.fx.control.hotkeys.addDefaultHotkeys
import matt.fx.control.menu.context.mcontextmenu
import matt.fx.control.menu.context.showMContextMenu
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.core.scene.NEED_REVERSED_DISPLAYS_FEATURE
import matt.fx.graphics.core.scene.reloadStyle
import matt.fx.graphics.mag.VarJson
import matt.fx.graphics.style.CUSTOM_CSS
import matt.fx.graphics.style.DARK_MODENA_CSS
import matt.fx.graphics.style.DarkModeController
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.region.border.FXBorder
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.fx.graphics.wrapper.style.StyleableWrapper
import matt.fx.graphics.wrapper.style.toAwtColor
import matt.log.profile.tic
import matt.log.tab
import java.net.URL
import kotlin.reflect.KClass

open class MScene<R: ParentWrapper<*>>(
  root: R, val icon: MFile
): SceneWrapper<R>(root) {
  constructor(
	root: R, icon: String
  ): this(root, ICON_FOLDER["white/$icon.png"])

  constructor(
	root: R
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
	val t = tic("creating mscene", enabled = false)
	addDefaultHotkeys()
	t.toc("added default hotkeys")


	val dark = DarkModeController.darkModeProp.value
	t.toc("finished DarkModeController 0")
	reloadStyle(dark)
	t.toc("finished DarkModeController 1")
	var old = DarkModeController.darkModeProp.value
	t.toc("finished DarkModeController 2")
	DarkModeController.darkModeProp.onChangeWithWeak(this) {
	  if (it != old) {
		reloadStyle(DarkModeController.darkModeProp.value)
		old = it
	  }
	}
	t.toc("finished DarkModeController 3")







	mcontextmenu {




	  menu("style") {
		actionitem("reload style") {
		  this@MScene.reloadStyle(DarkModeController.darkModeProp.value)
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
				println(it.matt.hurricanefx.eye.wrapper.matt.hurricanefx.eye.wrapper.obs.collect.wrapped().styleInfo())
				classesPrinted += it::class
			  }
			}*/
		  (root.node as Node).recurse {
			(it as? Parent)?.childrenUnmodifiable ?: listOf()
		  }.forEach {
			if (it::class !in classesPrinted) {
			  println((it.wrapped() as StyleableWrapper).styleInfo())
			  classesPrinted += it::class
			}
		  }
		}
		/*need this*/
		menu("set border") {        /*specify this here explicitly at least once
		  * or else it will use the `matt.fx.graphics.menu.actionitem` above without import*/
		  this.actionitem("none") {
			(root as RegionWrapper<*>).border = null
			/*(root.node as? Region)?.matt.hurricanefx.eye.wrapper.matt.hurricanefx.eye.wrapper.obs.collect.wrapped()?.borderFill = null*/
		  }
		  listOf(Color.YELLOW, Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE, Color.PURPLE, Color.WHITE).forEach {
			actionitem(ColorUtils().getColorNameFromColor(it.toAwtColor())) {
			  (root as RegionWrapper<*>).border = FXBorder.solid(it)
			  /*(root.node as? Region)?.matt.hurricanefx.eye.wrapper.matt.hurricanefx.eye.wrapper.obs.collect.wrapped()?.borderFill = it*/
			}
		  }
		}
	  }


	  if (NEED_REVERSED_DISPLAYS_FEATURE) actionitem("reverse displays") {
		VarJson.reversed_displays = !VarJson.reversed_displays
	  }

	}
	t.toc("finished main mcontextmenu")
	addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED) { e ->
	  handleContextMenuReq(e)
	}
	t.toc("finished mscene init")
  }


}

