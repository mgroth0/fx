package matt.fx.graphics.im

import javafx.scene.image.Image
import javafx.scene.image.WritableImage


fun Image.transferPixelsTo(wi: WritableImage) {

    wi.pixelWriter.setPixels(
        0,
        0,
        width.toInt(),
        height.toInt(),
        pixelReader,
        0,
        0
    )

}

fun WritableImage.loadPixelsFrom(im: Image) {

    pixelWriter.setPixels(
        0,
        0,
        im.width.toInt(),
        im.height.toInt(),
        im.pixelReader,
        0,
        0
    )

}
