package matt.fx.media

import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer

fun Media.play(): java.lang.Exception {
  MediaPlayer(this).play()
  return java.lang.Exception("weird")
}