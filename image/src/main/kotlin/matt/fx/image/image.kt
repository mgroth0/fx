package matt.fx.image

import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import matt.file.MFile

import java.awt.image.BufferedImage
import javax.imageio.ImageIO


fun jswingIconToImage(jswingIcon: javax.swing.Icon): Image? {
    require(Platform.isFxApplicationThread()) {
        "Let's try to be extra safe and only do this one on the FX Application thread too"
    }
    val bufferedImage = BufferedImage(
        jswingIcon.iconWidth, jswingIcon.iconHeight,
        BufferedImage.TYPE_INT_ARGB
    )
    jswingIcon.paintIcon(null, bufferedImage.graphics, 0, 0)
    return SwingFXUtils.toFXImage(bufferedImage, null)
}

fun Image.toBufferedImage(): BufferedImage {
    require(Platform.isFxApplicationThread()) {
        "Let's try to be extra safe and only do this one on the FX Application thread too"
    }
    return SwingFXUtils.fromFXImage(this, null)
}

fun BufferedImage.toFXImage(): Image {
    require(Platform.isFxApplicationThread()) {
        "I think there is a somewhat hard to reproduce but horrible deadlock that could happen if this is not done on the JavaFX application thread. But that should be fine, since this method only needs to happen in FX applications anyway. By the way, the place I saw this deadlock was when trying to do a large find operation in brainstorm, which caused lots of favicons and icons to be asynchronously loaded"
    }
    return SwingFXUtils.toFXImage(this, null)
}

fun Image.save(file: MFile): MFile {
    ImageIO.write(toBufferedImage(), file.extension, file)
    return file
}