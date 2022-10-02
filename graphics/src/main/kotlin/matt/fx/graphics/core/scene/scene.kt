package matt.fx.graphics.core.scene

import javafx.scene.paint.Color
import javafx.scene.paint.Color.WHITE
import matt.fx.graphics.style.CUSTOM_CSS
import matt.fx.graphics.style.DARK_MODENA_CSS
import matt.fx.graphics.wrapper.scene.SceneWrapper

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
