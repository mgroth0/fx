package matt.fx.graphics

import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.text.Font
import matt.async.MyTimerTask
import matt.async.date.Duration
import matt.async.every
import matt.hurricanefx.eye.lib.onChange

fun <T : Node> T.refreshWhileInSceneEvery(
  refresh_rate: Duration,
  op: MyTimerTask.(T) -> Unit
) {
  val thisNode: T = this
  sceneProperty().onChange {
	if (it != null) {
	  every(refresh_rate, ownTimer = true) {
		if (thisNode.scene == null) {
		  cancel()
		}
		op(thisNode)
	  }
	}
  }
}

val fontFamilies: List<String> by lazy { Font.getFamilies() }

interface Inspectable {
  fun inspect(): Pane
}
