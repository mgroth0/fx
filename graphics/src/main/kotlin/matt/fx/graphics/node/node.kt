package matt.fx.graphics.node

import javafx.scene.paint.Color
import matt.fx.graphics.style.DarkModeController
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl

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