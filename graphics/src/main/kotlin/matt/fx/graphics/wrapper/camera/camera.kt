package matt.fx.graphics.wrapper.camera

import javafx.scene.Camera
import javafx.scene.ParallelCamera
import javafx.scene.PerspectiveCamera
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.NodeWrapperImpl

abstract class CameraWrapper(node: Camera): NodeWrapperImpl<Camera>(node) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}

class PerspectiveCameraWrapper(node: PerspectiveCamera = PerspectiveCamera()): CameraWrapper(node)
class ParallelCameraWrapper(node: ParallelCamera = ParallelCamera()): CameraWrapper(node)