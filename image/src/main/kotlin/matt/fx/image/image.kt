package matt.fx.image

import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.ImageView
import matt.image.common.Image
import matt.image.desktop.save
import matt.lang.model.file.FsFile
import java.awt.image.BufferedImage


@JvmInline
value class FxImage(val image: Image) : Image

@JvmInline
value class FxImageView(val imageView: ImageView) : Image

fun javafx.scene.image.Image.toBufferedImage(): BufferedImage {
    require(Platform.isFxApplicationThread()) {
        "Let's try to be extra safe and only do this one on the FX Application thread too"
    }
    return SwingFXUtils.fromFXImage(this, null)
}

fun BufferedImage.toFXImage(): javafx.scene.image.Image {
    require(Platform.isFxApplicationThread()) {
        "I think there is a somewhat hard to reproduce but horrible deadlock that could happen if this is not done on the JavaFX application thread. But that should be fine, since this method only needs to happen in FX applications anyway. By the way, the place I saw this deadlock was when trying to do a large find operation in brainstorm, which caused lots of favicons and icons to be asynchronously loaded"
    }
    return SwingFXUtils.toFXImage(this, null)
}


fun javafx.scene.image.Image.save(file: FsFile) = toBufferedImage().save(file)
