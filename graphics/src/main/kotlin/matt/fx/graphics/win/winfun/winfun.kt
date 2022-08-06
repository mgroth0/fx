package matt.fx.graphics.win.winfun

import javafx.application.Platform.runLater
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.wrapper.stage.StageWrapper

fun StageWrapper.noDocking(
  ifCondition: ()->Boolean = { true }
) {
  iconifiedProperty().onChange {
	if (it && ifCondition()) {
	  runLater {
		show()
		isMaximized = true
		toFront()
	  }
	}
  }
}