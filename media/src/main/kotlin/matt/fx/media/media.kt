package matt.fx.media

import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.scene.media.MediaView
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl

fun Media.play(): java.lang.Exception {
    MediaPlayer(this).play()
    return java.lang.Exception("weird")
}

class MediaViewWrapper(
    node: MediaView = MediaView()
) : NodeWrapperImpl<MediaView>(node) {
    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }

    val mediaPlayerProp by lazy { node.mediaPlayerProperty().toNullableProp() }
    var mediaPlayer by mediaPlayerProp

    val fitWidthProperty by lazy { node.fitWidthProperty().toNonNullableProp().cast<Double>() }
    val fitHeightProperty by lazy { node.fitHeightProperty().toNonNullableProp().cast<Double>() }
}


