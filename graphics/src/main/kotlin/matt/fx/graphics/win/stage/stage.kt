package matt.fx.graphics.win.stage

import javafx.stage.StageStyle
import matt.fx.graphics.core.scene.MScene
import matt.fx.graphics.hotkey.hotkeys
import matt.fx.graphics.win.stage.WMode.CLOSE
import matt.fx.graphics.win.stage.WMode.HIDE
import matt.fx.graphics.win.stage.WMode.ICONIFY
import matt.fx.graphics.win.stage.WMode.NOTHING
import matt.fx.graphics.win.winfun.pullBackWhenOffscreen
import matt.hurricanefx.wrapper.stage.StageWrapper
import matt.klib.commons.thisMachine
import matt.klib.log.warn
import matt.klib.sys.GAMING_WINDOWS

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
): StageWrapper(if (decorated) StageStyle.DECORATED else StageStyle.UNDECORATED) {
  init {
	if (pullBackWhenOffScreen) {
	  pullBackWhenOffscreen()
	}
	hotkeys {
	  if (thisMachine == GAMING_WINDOWS) {
		Q.opt op ::close // on Mac, meta-Q quits program. this an OS feature.
	  }
	  (if (thisMachine == GAMING_WINDOWS) {
		W.opt
	  } else W.meta) op when (wMode) {
		CLOSE   -> ::close
		HIDE    -> ::hide
		NOTHING -> {
		  {}
		}

		ICONIFY -> {
		  {
			warn("dealing with this later... i guess now")
			@Suppress("CAST_NEVER_SUCCEEDS")
			(this@MStage.scene as MScene<*>).iconify()
		  }
		}

	  }
	  if (EscClosable) ESCAPE op ::close
	  if (EnterClosable) ENTER op ::close
	}
  }
}

