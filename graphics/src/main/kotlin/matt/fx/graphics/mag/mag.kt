package matt.fx.graphics.mag

import javafx.stage.Screen
import matt.fx.graphics.wrapper.window.WindowWrapper

const val NEW_MAC_NOTCH_ESTIMATE = 32.0 /*35*/
const val NEW_MAC_MENU_BAR_ESTIMATE = NEW_MAC_NOTCH_ESTIMATE + 2.0
val NEW_MAX_MENU_Y_ESTIMATE_SECONDARY = 25.0

data class RectSize(
  val width: Number,
  val height: Number
)

/*val MENU_BAR_Y = if (isNewMac) NEW_MAC_NOTCH_ESTIMATE else 0.0*/


/*no idea if this will work*/
fun Screen.isPrimary() = bounds.minX == 0.0 && bounds.minY == 0.0

fun WindowWrapper<*>.extraMinY() = when {
  screen?.isPrimary() != false -> NEW_MAC_NOTCH_ESTIMATE
  else                         -> 0.0
}

fun WindowWrapper<*>.maxSize() {
  screen?.let {
	width = it.bounds.width
	height = it.bounds.height - extraMinY()
  }
}

fun WindowWrapper<*>.hHalf() {
  println("hhalf")
  println("screen=${screen}")
  screen?.let {
	width = it.bounds.width/2
	height = it.bounds.height - extraMinY()
	println("width = $width")
	println("height=${height}")
  }
}

fun WindowWrapper<*>.vHalf() {
  screen?.let {
	width = it.bounds.width
	height = it.bounds.height/2 - (extraMinY()/2.0)
  }
}

fun WindowWrapper<*>.corner() {
  screen?.let {
	width = it.bounds.width/2
	height = it.bounds.height/2 - (extraMinY()/2.0)
  }
}

fun WindowWrapper<*>.eigth() {
  screen?.let {
	width = it.bounds.width/4
	height = it.bounds.height/2 - (extraMinY()/2.0)
  }
}

fun WindowWrapper<*>.resetPosition() {
  Screen.getPrimary().let {
	x = it.bounds.minX
	y = it.bounds.minY + extraMinY()
	width = it.bounds.width
	height = it.bounds.height - extraMinY()
  }
}

fun WindowWrapper<*>.myMax() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY + extraMinY()
	maxSize()
  }
}

fun WindowWrapper<*>.left() {
  println("left")
  println("screen = $screen")
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY + extraMinY()
	println("x=${x}")
	println("y=${y}")
	hHalf()
  }
}


fun WindowWrapper<*>.right() {
  println("right")
  println("screen = $screen")
  screen?.let {
	x = it.bounds.maxX - (it.bounds.width/2)
	y = it.bounds.minY + extraMinY()
	println("x=${x}")
	println("y=${y}")
	hHalf()
  }
}

fun WindowWrapper<*>.top() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY + extraMinY()
	vHalf()
  }
}

fun WindowWrapper<*>.bottom() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.maxY - (it.bounds.height/2) - (extraMinY()/2.0)
	vHalf()
  }
}


fun WindowWrapper<*>.topLeft() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY + extraMinY()
	corner()
  }
}

fun WindowWrapper<*>.topRight() {
  screen?.let {
	x = it.bounds.maxX - (it.bounds.width/2)
	y = it.bounds.minY + extraMinY()
	corner()
  }
}

fun WindowWrapper<*>.bottomLeft() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.maxY - (it.bounds.height/2) - (extraMinY()/2.0)
	corner()
  }
}

fun WindowWrapper<*>.bottomRight() {
  screen?.let {
	x = it.bounds.maxX - (it.bounds.width/2)
	y = it.bounds.maxY - (it.bounds.height/2) - (extraMinY()/2.0)
	corner()
  }
}

fun WindowWrapper<*>.max() {
  // isMaximized isnt working for undecorated
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY + extraMinY()
	width = it.bounds.width
	height = it.bounds.height - extraMinY()
  }
}

fun WindowWrapper<*>.nextDisplay(reversed: Boolean = false) {
  val myScreen = screen
  val screens = Screen.getScreens().let { if (reversed) it.reversed() else it }
  if (screens.size > 1) {
	var next = screens[0]
	var found = false
	for (s in screens) {
	  if (found) {
		next = s
		break
	  }
	  if (s == myScreen) {
		found = true
	  }
	}
	x = next.bounds.minX
	y = next.bounds.minY + extraMinY()
	width = next.bounds.width/2
	height = next.bounds.height/2 - (extraMinY()/2.0)
  }
}

fun WindowWrapper<*>.lastDisplay() {
  return nextDisplay(reversed = true)
}


fun WindowWrapper<*>.eighth1() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY + extraMinY()
	eigth()
  }
}

fun WindowWrapper<*>.eighth2() {
  screen?.let {
	x = it.bounds.minX + (it.bounds.width/4)
	y = it.bounds.minY + extraMinY()
	eigth()
  }
}

fun WindowWrapper<*>.eighth3() {
  screen?.let {
	x = it.bounds.minX + (it.bounds.width/2)
	y = it.bounds.minY + extraMinY()
	eigth()
  }
}

fun WindowWrapper<*>.eighth4() {
  screen?.let {
	x = it.bounds.maxX - (it.bounds.width/4)
	y = it.bounds.minY + extraMinY()
	eigth()
  }
}

fun WindowWrapper<*>.eighth5() {
  screen?.let {
	x = it.bounds.minX
	y = it.bounds.minY + (it.bounds.height/2) - (extraMinY()/2.0)
	eigth()
  }
}

fun WindowWrapper<*>.eighth6() {
  screen?.let {
	x = it.bounds.minX + (it.bounds.width/4)
	y = it.bounds.minY + (it.bounds.height/2) - (extraMinY()/2.0)
	eigth()
  }
}

fun WindowWrapper<*>.eighth7() {
  screen?.let {
	x = it.bounds.minX + (it.bounds.width/2)
	y = it.bounds.minY + (it.bounds.height/2) - (extraMinY()/2.0)
	eigth()
  }
}

fun WindowWrapper<*>.eighth8() {
  screen?.let {
	x = it.bounds.maxX - (it.bounds.width/4)
	y = it.bounds.minY + (it.bounds.height/2) - (extraMinY()/2.0)
	eigth()
  }
}