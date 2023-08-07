package matt.fx.image.test


import javafx.scene.image.WritableImage
import matt.file.commons.TEMP_DIR
import matt.fx.image.save
import matt.mbuild.mtest.fx.FXTester.runFXHeadlessApp
import matt.test.JupiterTestAssertions.assertRunsInOneMinute
import kotlin.test.Test

class ImageTests {
    @Test
    fun saveAnImage() = assertRunsInOneMinute {
        val tempImageFile = TEMP_DIR["saveAnImage.png"]
        runFXHeadlessApp {
            WritableImage(30, 30).save(tempImageFile)
        }
    }
}