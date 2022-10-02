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
import matt.auto.openInIntelliJ
import matt.color.name.ColorUtils
import matt.file.MFile
import matt.file.commons.ICON_FOLDER
import matt.file.construct.mFile
import matt.fx.graphics.hotkeys.addDefaultHotkeys
import matt.fx.graphics.mag.VarJson
import matt.fx.graphics.service.wrapped
import matt.fx.graphics.style.CUSTOM_CSS
import matt.fx.graphics.style.DARK_MODENA_CSS
import matt.fx.graphics.style.DarkModeController.darkModeProp
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.fx.graphics.wrapper.style.StyleableWrapper
import matt.log.tab
import matt.stream.recurse.recurse
import java.net.URL
import kotlin.reflect.KClass

/*this is for when I have two monitors that are the scame brand and therefore mac is mixing up which side each is on I think*/
const val NEED_REVERSED_DISPLAYS_FEATURE = false

fun SceneWrapper<*>.reloadStyle(darkMode: Boolean) {
  stylesheets.clear()
  fill = WHITE /*FX DEFAULT*/
  if (darkMode) {
	stylesheets.add(DARK_MODENA_CSS)
	stylesheets.add(CUSTOM_CSS)

	//     ensure that even while the screen is loading it is black. So not white flashes or flickering while refreshing
	fill =
	  Color.BLACK    //        maybe also possible by styleing .root in css, but if I remember correctly that also affects other nodes
  }
}
