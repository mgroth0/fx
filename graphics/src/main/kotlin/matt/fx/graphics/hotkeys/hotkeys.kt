package matt.fx.graphics.hotkeys

import javafx.application.Platform.runLater
import javafx.scene.layout.Border
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.stage.Stage
import matt.fx.graphics.core.scene.MScene
import matt.fx.graphics.core.scene.iconify
import matt.fx.graphics.hotkey.HotKey
import matt.fx.graphics.hotkey.hotkeys
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
import matt.fx.graphics.mag.reversed_displays
import matt.fx.graphics.mag.right
import matt.fx.graphics.mag.top
import matt.fx.graphics.mag.topleft
import matt.fx.graphics.mag.topright
import matt.fx.graphics.style.borderFill
import matt.klib.dmap.withStoringDefault
import matt.klib.lang.go
import java.lang.Thread.sleep
import java.util.WeakHashMap
import kotlin.concurrent.thread
import kotlin.contracts.ExperimentalContracts


@ExperimentalContracts
fun MScene.addDefaultHotkeys() {
  val scene = this

  /*needed filter to be true here or for some reason LEFT.ctrl.opt.shift wasn't being captured in music app even though it was captured in all other apps (globalhotkeys, brainstorm, kjg)*/
  hotkeys(quickPassForNormalTyping = true,filter=true) {


	LEFT.ctrl.opt { window?.left() }
	RIGHT.ctrl.opt { window?.right() }
	UP.ctrl.opt { window?.top() }
	DOWN.ctrl.opt { window?.bottom() }

	LEFT.ctrl.opt.meta { window?.bottomleft() }
	RIGHT.ctrl.opt.meta { window?.topright() }
	UP.ctrl.opt.meta { window?.topleft() }
	DOWN.ctrl.opt.meta {
	  window?.bottomright()
	}

	LEFT.ctrl.opt.shift {
	  window?.apply {
		if (!reversed_displays) lastdisplay()
		else nextdisplay()
	  }
	}
	RIGHT.ctrl.opt.shift {
	  window?.apply {
		if (!reversed_displays) nextdisplay()
		else lastdisplay()
	  }
	}

	F.ctrl.opt.shift { (window as? Stage?)?.isFullScreen = !((window as Stage).isFullScreen) }
	ENTER.ctrl.opt.shift { (window as? Stage?)?.max() }
	ENTER.ctrl.opt.shift.meta { window?.resetPosition() }
	I.ctrl.opt.shift { iconify(icon) }

	DIGIT1.ctrl.opt.shift { window.eighth1() }
	DIGIT2.ctrl.opt.shift { window.eighth2() }
	DIGIT3.ctrl.opt.shift { window.eighth3() }
	DIGIT4.ctrl.opt.shift { window.eighth4() }
	DIGIT5.ctrl.opt.shift { window.eighth5() }
	DIGIT6.ctrl.opt.shift { window.eighth6() }
	DIGIT7.ctrl.opt.shift { window.eighth7() }
	DIGIT8.ctrl.opt.shift { window.eighth8() }

	hotkeys.map { it as HotKey }.forEach {
	  it.wrapOp {
		val reg = (scene.root as? Region)
		val old = regs[reg]
		reg?.borderFill = Color.YELLOW
		it()
		reg?.go {
		  thread {
			sleep(750)
			runLater {
			  it.border = old
			}
		  }
		}
	  }
	}
  }

}


val regs = WeakHashMap<Region, Border>().withStoringDefault { it.border ?: Border.EMPTY }