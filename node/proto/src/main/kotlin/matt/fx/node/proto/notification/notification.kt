package matt.fx.node.proto.notification

import javafx.application.Platform
import javafx.geometry.Pos.TOP_CENTER
import javafx.scene.paint.Color
import javafx.stage.Screen
import matt.async.thread.namedThread
import matt.fx.base.mtofx.createWritableFXPropWrapper
import matt.fx.base.time.toFXDuration
import matt.fx.graphics.anim.animation.keyframe
import matt.fx.graphics.anim.animation.timeline
import matt.fx.graphics.anim.interp.MyInterpolator
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.fx.graphics.wrapper.text.text
import matt.gui.mscene.MScene
import matt.gui.mstage.MStage
import matt.obs.prop.writable.VarProp
import matt.time.dur.common.sec
import java.util.WeakHashMap

const val NOTIFICATION_WIDTH = 200.0
const val NOTIFICATION_HEIGHT = 100.0
const val INTER_NOTIFICATION_SPACE = 20.0
const val Y_MOVE_AMOUNT = NOTIFICATION_HEIGHT + INTER_NOTIFICATION_SPACE
val openNotifications = mutableListOf<MStage>()
val notificationYs = WeakHashMap<MStage, Double>()
val fakeYProps = WeakHashMap<MStage, VarProp<Double>>()

fun notification(
    text: String
) {

    Platform.runLater {
        val stage =
            MStage().apply {
                pullBackWhenOffScreen = false
                isAlwaysOnTop = true
            }
        stage.scene =
            MScene(
                VBoxWrapperImpl<NodeWrapper>().apply {
                    alignment = TOP_CENTER
                    Platform.runLater {
                        backgroundFill = Color.SKYBLUE
                    }
                    exactHeight = 100.00
                    exactWidth = NOTIFICATION_WIDTH
                    this.text(text) {
                        Platform.runLater {
                            fill = Color.YELLOW
                        }
                    }
                    setOnMousePressed {
                        stage.close()
                    }
                },
                rootCls = VBoxWrapperImpl::class
            ).apply {
                fill = Color.SKYBLUE
            }
        val screen = Screen.getScreens().minByOrNull { it.bounds.minX }!!

        stage.x = screen.bounds.minX - NOTIFICATION_WIDTH - 10.0
        stage.y = screen.bounds.minY + 50.0 + Y_MOVE_AMOUNT * openNotifications.size
        notificationYs[stage] = stage.y

        val fakeXProp =
            VarProp(stage.x).apply {
                onChange {
                    stage.x = it
                }
            }
        val fakeYProp =
            VarProp(stage.y).apply {
                onChange {
                    stage.y = it
                }
            }
        fakeYProps[stage] = fakeYProp

        stage.showingProperty.onChange {
            if (!it) {
                openNotifications -= stage
                timeline {
                    openNotifications.forEach {
                        keyframe(0.2.sec.toFXDuration()) {
                            keyvalue(
                                fakeYProps[it]!!.createWritableFXPropWrapper(), notificationYs[it]!! - Y_MOVE_AMOUNT,
                                MyInterpolator.EASE_BOTH
                            )
                        }
                        notificationYs[it] = notificationYs[it]!! - Y_MOVE_AMOUNT
                    }
                }
            }
        }
        stage.show()
        openNotifications += stage


        namedThread(name = "notification Thread") {
            Thread.sleep(1000)
            Platform.runLater {
                timeline {
                    keyframe(0.75.sec.toFXDuration()) {
                        keyvalue(
                            fakeXProp.createWritableFXPropWrapper(),
                            screen.bounds.minX + 30.0,
                            MyInterpolator.EASE_BOTH
                        )
                    }
                }
            }
        }
    }
}
