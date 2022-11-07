package matt.fx.threedemo



fun main() {
  GuiApp {
	scene = MScene(GroupWrapper<NodeWrapper>().apply {
	  addChild(BoxWrapper3D().apply {
		node.height = 2.0
		node.width = 2.0
		node.depth = 2.0
		node.translateX = 2.0
		node.translateY = 2.0
		node.translateZ = 2.0
		//		node.layoutX = 200.0
		//		node.layoutY = 200.0
	  })
	})
	val pivot = TranslateWrapper()
	val yRotate = RotateWrapper(Rotate(0.0, Rotate.Y_AXIS))
	val xRotate = Rotate(-20.0, Rotate.X_AXIS)
	scene!!.apply {
	  val cam = PerspectiveCameraWrapper(PerspectiveCamera(true))
	  node.camera = cam.node
	  //	  cam.node.translateZ = -200.0
	  cam.node.transforms.addAll(
		pivot.node, yRotate.node, xRotate, Translate(0.0, 0.0, -50.0)
	  )
	  hotkeys {
		val INCREMENT = 10.0
		A.bare {
		  xRotate.angle -= INCREMENT
		  //		  cam.rotate -= 10.0
		}
		D.bare {
		  xRotate.angle += INCREMENT
		  //		  cam.rotate += 10.0
		}
		W.bare {
		  yRotate.node.angle -= INCREMENT
		  //		  cam.node.rotationAxis = cam.node.rotationAxis.add(0.0, 0.0, 1.0)
		}
		S.bare {
		  yRotate.node.angle += INCREMENT
		  //		  cam.node.rotationAxis = cam.node.rotationAxis.subtract(0.0, 0.0, 1.0)
		}
	  }
	}
  }.start()
}