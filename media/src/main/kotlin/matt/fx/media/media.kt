package matt.fx.media

import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer

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
