package matt.fx.control.iconify

import javafx.stage.Stage
import matt.file.MFile
import matt.fx.graphics.core.scene.MScene
import matt.fx.graphics.icon.ICON_HEIGHT
import matt.fx.graphics.icon.ICON_WIDTH
import matt.fx.graphics.icon.Icon
import matt.fx.graphics.style.DarkModeController
import matt.fx.graphics.win.interact.WinGeom
import matt.fx.graphics.win.interact.WinOwn
import matt.fx.graphics.win.winfun.noDocking
import matt.fx.graphics.wrapper.node.setOnDoubleClick
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapper

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
	x = this@iconify.window!!.x + (this@iconify.window!!.width/2) - (ICON_WIDTH/2),
	y = this@iconify.window!!.y + (this@iconify.window!!.height/2) - (ICON_HEIGHT/2),
  ), mScene = false, border = false, beforeShowing = {
	scene!!.reloadStyle(DarkModeController.darkModeProp.value)
	DarkModeController.darkModeProp.onChangeWithWeak(this) { scene!!.reloadStyle(DarkModeController.darkModeProp.value) }
  }).apply {
	iconWindow = this
	isAlwaysOnTop = true
	noDocking()
  }
  window!!.hide()
}

fun MScene<*>.iconify() = iconify(icon)