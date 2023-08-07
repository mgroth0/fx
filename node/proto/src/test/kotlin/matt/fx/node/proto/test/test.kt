package matt.fx.node.proto.test


import matt.fx.node.proto.notification.INTER_NOTIFICATION_SPACE
import matt.fx.node.proto.notification.NOTIFICATION_HEIGHT
import matt.fx.node.proto.notification.NOTIFICATION_WIDTH
import matt.fx.node.proto.notification.Y_MOVE_AMOUNT
import matt.fx.node.proto.notification.fakeYProps
import matt.fx.node.proto.notification.notificationYs
import matt.fx.node.proto.notification.openNotifications
import matt.fx.node.proto.scaledcanvas.ScaledCanvas
import matt.mbuild.mtest.fx.FXTester
import matt.mbuild.mtest.fx.FxTests
import kotlin.test.Test

class ProtoTests : FxTests() {
    @Test
    fun initVals() {
        NOTIFICATION_HEIGHT
        NOTIFICATION_WIDTH
        INTER_NOTIFICATION_SPACE
        Y_MOVE_AMOUNT
        openNotifications
        notificationYs
        fakeYProps
    }

    @Test
    fun instantiateClasses() {
        FXTester.runFXHeadlessApp {
            ScaledCanvas()
        }
    }
}