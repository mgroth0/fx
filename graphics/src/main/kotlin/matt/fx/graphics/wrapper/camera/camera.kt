package matt.fx.graphics.wrapper.camera

import javafx.scene.Camera
import javafx.scene.ParallelCamera
import javafx.scene.PerspectiveCamera
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl

abstract class CameraWrapper(node: Camera): NodeWrapperImpl<Camera>(node) {
  final override fun addChild(child: NodeWrapper, index: Int?) {
	TODO()
  }
}

class PerspectiveCameraWrapper(node: PerspectiveCamera = PerspectiveCamera()): CameraWrapper(node)
class ParallelCameraWrapper(node: ParallelCamera = ParallelCamera()): CameraWrapper(node)