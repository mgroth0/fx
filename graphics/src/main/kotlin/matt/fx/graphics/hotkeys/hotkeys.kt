package matt.fx.graphics.hotkeys

import javafx.application.Platform.runLater
import javafx.scene.paint.Color
import javafx.stage.Stage
import matt.fx.graphics.core.scene.MScene
import matt.fx.graphics.hotkey.HotKey
import matt.fx.graphics.hotkey.hotkeys
import matt.fx.graphics.mag.VarJson
import matt.fx.graphics.mag.bottom
import matt.fx.graphics.mag.bottomleft
import matt.fx.graphics.mag.bottomright
import matt.fx.graphics.mag.eighth1
import matt.fx.graphics.mag.eighth2
import matt.fx.graphics.mag.eighth3
import matt.fx.graphics.mag.eighth4
import matt.fx.graphics.mag.eighth5
import matt.fx.graphics.mag.eighth6
import matt.fx.graphics.mag.eighth7
import matt.fx.graphics.mag.eighth8
import matt.fx.graphics.mag.lastdisplay
import matt.fx.graphics.mag.left
import matt.fx.graphics.mag.max
import matt.fx.graphics.mag.nextdisplay
import matt.fx.graphics.mag.resetPosition
import matt.fx.graphics.mag.right
import matt.fx.graphics.mag.top
import matt.fx.graphics.mag.topleft
import matt.fx.graphics.mag.topright
import matt.lang.go
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.contracts.ExperimentalContracts
import matt.fx.graphics.service.wrapped


@ExperimentalContracts fun MScene<*>.addDefaultHotkeys() {
  val scene = this

  /*needed filter to be true here or for some reason LEFT.ctrl.opt.shift wasn't being captured in music app even though it was captured in all other apps (globalhotkeys, brainstorm, kjg)*/
  hotkeys(quickPassForNormalTyping = true, filter = true) {

	LEFT.ctrl.opt { window!!.x -= window!!.width }
	RIGHT.ctrl.opt { window!!.x += window!!.width }
	UP.ctrl.opt { window!!.y -= window!!.height }
	DOWN.ctrl.opt { window!!.y += window!!.height }

	LEFT.ctrl.meta {
	  window!!.width /= 2	//	  window.x -= window.width
	}
	RIGHT.ctrl.meta {
	  window!!.width /= 2
	  window!!.x += window!!.width
	}
	UP.ctrl.meta {
	  window!!.height /= 2	//	  window.y -= window.height
	}
	DOWN.ctrl.meta {
	  window!!.height /= 2
	  window!!.y += window!!.height
	}

	LEFT.ctrl.meta.shift {
	  window!!.x -= window!!.width
	  window!!.width *= 2
	}
	RIGHT.ctrl.meta.shift {
	  window!!.width *= 2	//	  window.x += window.width
	}
	UP.ctrl.meta.shift {
	  window!!.y -= window!!.height
	  window!!.height *= 2
	}
	DOWN.ctrl.meta.shift {
	  window!!.height *= 2	//	  window.y += window.height
	}

	A.ctrl.opt { window?.wrapped()?.left() }
	D.ctrl.opt { window?.wrapped()?.right() }
	W.ctrl.opt { window?.wrapped()?.top() }
	S.ctrl.opt { window?.wrapped()?.bottom() }

	Z.ctrl.opt { window?.wrapped()?.bottomleft() }
	E.ctrl.opt { window?.wrapped()?.topright() }
	Q.ctrl.opt { window?.wrapped()?.topleft() }
	C.ctrl.opt { window?.wrapped()?.bottomright() }

	LEFT_BRACKET.ctrl.opt {
	  window?.wrapped()?.apply {
		if (!VarJson.reversed_displays) lastdisplay()
		else nextdisplay()
	  }
	}
	RIGHT_BRACKET.ctrl.opt {
	  window?.wrapped()?.apply {
		if (!VarJson.reversed_displays) nextdisplay()
		else lastdisplay()
	  }
	}

	F.ctrl.opt { (window as? Stage?)?.isFullScreen = !((window as Stage).isFullScreen) }
	TAB.ctrl.opt { (window as? Stage?)?.wrapped()?.max() }
	ENTER.ctrl.opt { window?.wrapped()?.resetPosition() }
	X.ctrl.opt { iconify(icon) }

	DIGIT1.ctrl.opt { window?.wrapped()?.eighth1() }
	DIGIT2.ctrl.opt { window?.wrapped()?.eighth2() }
	DIGIT3.ctrl.opt { window?.wrapped()?.eighth3() }
	DIGIT4.ctrl.opt { window?.wrapped()?.eighth4() }
	DIGIT5.ctrl.opt { window?.wrapped()?.eighth5() }
	DIGIT6.ctrl.opt { window?.wrapped()?.eighth6() }
	DIGIT7.ctrl.opt { window?.wrapped()?.eighth7() }
	DIGIT8.ctrl.opt { window?.wrapped()?.eighth8() }

	hotkeys.map { it as HotKey }.forEach {
	  it.wrapOp {
		val reg = (scene.root as? RegionWrapper)
		reg?.border = FXBorder.solid(Color.YELLOW)
		it()
		(reg as? RegionWrapperImpl<*,*>)?.go {
		  thread {
			sleep(750)
			runLater {
			  it.border = it.defaultBorder
			}
		  }
		}
	  }
	}
  }
}

