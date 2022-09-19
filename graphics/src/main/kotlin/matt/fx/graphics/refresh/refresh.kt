package matt.fx.graphics.refresh

import matt.async.schedule.MyTimerTask
import matt.async.schedule.every
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.time.dur.Duration

fun <T: NodeWrapper> T.refreshWhileInSceneEvery(
  refreshRate: Duration,
  op: MyTimerTask.(T)->Unit
) {
  val thisNode: T = this
  sceneProperty().onChange {
	if (it != null) {
	  every(refreshRate, ownTimer = true) {
		if (thisNode.node.scene == null) {
		  cancel()
		}
		op(thisNode)
	  }
	}
  }
}


