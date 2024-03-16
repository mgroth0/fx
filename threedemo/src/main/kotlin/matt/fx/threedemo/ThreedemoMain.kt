package matt.fx.threedemo

import javafx.scene.PerspectiveCamera
import javafx.scene.transform.Rotate
import javafx.scene.transform.Translate
import matt.exec.app.contextForMainOnly
import matt.fx.graphics.hotkey.hotkeys
import matt.fx.graphics.wrapper.camera.PerspectiveCameraWrapper
import matt.fx.graphics.wrapper.group.GroupWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.node.shape.threed.box.BoxWrapper3D
import matt.fx.graphics.wrapper.transform.RotateWrapper
import matt.fx.graphics.wrapper.transform.TranslateWrapper
import matt.gui.app.GuiApp
import matt.gui.mscene.MScene

val demoBox by lazy {
    BoxWrapper3D().apply {
        node.height = 2.0
        node.width = 2.0
        node.depth = 2.0
        node.translateX = 2.0
        node.translateY = 2.0
        node.translateZ = 2.0
    }
}

fun main() =
    with(contextForMainOnly()) {
        GuiApp {
            scene =
                MScene(
                    GroupWrapper(childClass = NodeWrapper::class).apply {
                        addChild(demoBox)
                    },
                    rootCls = ParentWrapper::class
                )
            val pivot = TranslateWrapper()
            val yRotate = RotateWrapper(Rotate(0.0, Rotate.Y_AXIS))
            val xRotate = Rotate(-20.0, Rotate.X_AXIS)
            scene!!.apply {
                val cam = PerspectiveCameraWrapper(PerspectiveCamera(true))
                node.camera = cam.node
                cam.node.transforms.addAll(
                    pivot.node, yRotate.node, xRotate, Translate(0.0, 0.0, -50.0)
                )
                hotkeys {
                    val INCREMENT = 10.0
                    A.bare {
                        xRotate.angle -= INCREMENT
                    }
                    D.bare {
                        xRotate.angle += INCREMENT
                    }
                    W.bare {
                        yRotate.node.angle -= INCREMENT
                    }
                    S.bare {
                        yRotate.node.angle += INCREMENT
                    }
                }
            }
        }.runBlocking()
    }
