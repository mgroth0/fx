package matt.fx.graphics.win.stage

import javafx.stage.StageStyle
import matt.file.thismachine.thisMachine
import matt.fx.graphics.core.scene.MScene
import matt.fx.graphics.hotkey.hotkeys
import matt.fx.graphics.win.stage.WMode.CLOSE
import matt.fx.graphics.win.stage.WMode.HIDE
import matt.fx.graphics.win.stage.WMode.ICONIFY
import matt.fx.graphics.win.stage.WMode.NOTHING
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.model.sys.GAMING_WINDOWS

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
): StageWrapper(if (decorated) StageStyle.DECORATED else StageStyle.UNDECORATED) {
  init {
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
			(this@MStage.scene as? MScene<*>)?.iconify()
			Unit
		  }
		}

	  }
	  if (EnterClosable) ENTER op ::close
	}
	hotkeys(filter = true) {
	  if (EscClosable) ESCAPE op ::close
	}
  }
}

