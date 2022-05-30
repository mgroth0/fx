package matt.fx.image

import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image

import java.awt.image.BufferedImage


 fun jswingIconToImage(jswingIcon: javax.swing.Icon): Image? {
  val bufferedImage = BufferedImage(
	jswingIcon.iconWidth, jswingIcon.iconHeight,
	BufferedImage.TYPE_INT_ARGB
  )
  jswingIcon.paintIcon(null, bufferedImage.graphics, 0, 0)
  return SwingFXUtils.toFXImage(bufferedImage, null)
}

fun Image.toBufferedImage(): BufferedImage = SwingFXUtils.fromFXImage(this, null)
fun BufferedImage.toFXImage(): Image = SwingFXUtils.toFXImage(this, null)

