package matt.fx.graphics.node

import javafx.scene.paint.Color
import matt.fx.graphics.style.DarkModeController
import matt.hurricanefx.wrapper.node.onHover
import matt.hurricanefx.wrapper.node.onLeftClick
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.hurricanefx.wrapper.target.EventTargetWrapper

interface Inspectable<N: PaneWrapperImpl<*, *>> {
  fun inspect(): N
}


fun EventTargetWrapper.actionText(text: String, action: ()->Unit) = text(text) {
  onHover {
	fill = when {
	  it                                    -> Color.YELLOW
	  DarkModeController.darkModeProp.value -> Color.WHITE
	  else                                  -> Color.BLACK
	}
  }
  onLeftClick {
	action()
  }
}