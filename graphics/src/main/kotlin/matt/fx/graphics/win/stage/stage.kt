package matt.fx.graphics.win.stage

import javafx.stage.Stage
import javafx.stage.StageStyle
import matt.fx.graphics.core.scene.MScene
import matt.fx.graphics.hotkey.hotkeys
import matt.fx.graphics.win.stage.WMode.CLOSE
import matt.fx.graphics.win.stage.WMode.HIDE
import matt.fx.graphics.win.stage.WMode.ICONIFY
import matt.fx.graphics.win.stage.WMode.NOTHING
import matt.fx.graphics.win.winfun.pullBackWhenOffscreen
import matt.klib.commons.thisMachine
import matt.klib.sys.WINDOWS

enum class WMode {
  CLOSE,
  HIDE,
  NOTHING,
  ICONIFY
}
enum class ShowMode {
  SHOW,
  SHOW_AND_WAIT,
  DO_NOT_SHOW,
}

open class MStage(
  wMode: WMode = NOTHING,
  EscClosable: Boolean = false,
  EnterClosable: Boolean = false,
  decorated: Boolean = false,
  pullBackWhenOffScreen: Boolean = true
): Stage(if (decorated) StageStyle.DECORATED else StageStyle.UNDECORATED) {
  init {
	if (pullBackWhenOffScreen) {
	  pullBackWhenOffscreen()
	}
	hotkeys {
	  if (thisMachine ==WINDOWS) {
		Q.opt op ::close // on Mac, meta-Q quits program. this an OS feature.
	  }
	  (if (thisMachine == WINDOWS) {
		W.opt
	  } else W.meta) op when (wMode) {
		CLOSE   -> ::close
		HIDE    -> ::hide
		NOTHING -> {
		  {}
		}
		ICONIFY -> {
		  {
			(this@MStage.scene as MScene).iconify()
		  }
		}

	  }
	  if (EscClosable) ESCAPE op ::close
	  if (EnterClosable) ENTER op ::close
	}
  }
}
