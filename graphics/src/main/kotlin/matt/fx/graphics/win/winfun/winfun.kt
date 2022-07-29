package matt.fx.graphics.win.winfun

import javafx.application.Platform.runLater
import matt.async.daemon
import matt.fx.graphics.async.runLaterReturn
import matt.fx.graphics.mag.screen
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.wrapper.stage.StageWrapper
import matt.klib.log.warn

fun StageWrapper.pullBackWhenOffscreen() {
  setOnShowing {
	daemon {
	  while (isShowing) {
		runLaterReturn { /*this runLaterReturn is essential. It fixed a bug where new windows were getting reset.*/
		  if (screen == null) {
			warn("resetting offscreen window")
			x = 0.0
			y = 0.0
			width = 500.0
			height = 500.0
		  }
		}
		Thread.sleep(5000)
	  }
	}
  }
}

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