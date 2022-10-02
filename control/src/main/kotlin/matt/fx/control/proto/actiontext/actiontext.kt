package matt.fx.control.proto.actiontext

import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.onHover
import matt.fx.graphics.wrapper.node.onLeftClick
import matt.fx.graphics.wrapper.text.text

fun ET.actionText(text: String, action: ()->Unit) = text(text) {
  onHover {
	fill = when {
	  it                                                           -> javafx.scene.paint.Color.YELLOW
	  matt.fx.graphics.style.DarkModeController.darkModeProp.value -> javafx.scene.paint.Color.WHITE
	  else                                                         -> javafx.scene.paint.Color.BLACK
	}
  }
  onLeftClick {
	action()
  }
}
