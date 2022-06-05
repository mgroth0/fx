package matt.fx.media

import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer

fun Media.play() = MediaPlayer(this).play()