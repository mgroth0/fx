package matt.fx.media.test


import matt.fx.media.MediaViewWrapper
import matt.mbuild.mtest.fx.FxTests
import kotlin.test.Test

class MediaTests : FxTests() {
    @Test
    fun createMediaView() {
        MediaViewWrapper()
    }
}
