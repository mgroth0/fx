package matt.fx.graphics.win.winfun

import javafx.application.Platform.runLater
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.hurricanefx.eye.lib.onChange

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