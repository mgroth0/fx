package matt.fx.media

import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.image.ImageView
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import java.util.WeakHashMap

fun Media.play(): java.lang.Exception {
  MediaPlayer(this).play()
  return java.lang.Exception("weird")
}

/*
class MediaViewWrapper(
  override val node: MediaView = MediaView(),
): NodeWrapperImpl(node) {
  companion object {
    fun Hyperlink.wrapped() = HyperlinkWrapper(this)
  }

}*/




val fitBothProps: DefaultStoringMap<ImageView, DoubleProperty> =
  WeakHashMap<ImageView, DoubleProperty>().withStoringDefault {
    SimpleDoubleProperty().apply {
      it.fitWidthProperty().bind(this)
      it.fitWidthProperty().bind(this)
    }
  }