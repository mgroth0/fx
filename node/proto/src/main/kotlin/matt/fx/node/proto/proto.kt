package matt.fx.node.proto

import javafx.scene.canvas.GraphicsContext
import matt.fx.control.wrapper.control.text.area.TextAreaWrapper
import matt.fx.graphics.icon.ICON_HEIGHT
import matt.fx.graphics.icon.ICON_WIDTH
import matt.fx.graphics.icon.Icon
import matt.fx.graphics.icon.IconImage
import matt.fx.graphics.wrapper.canvas.CanvasWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.NodeWrapperImpl
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.model.dir.Direction
import matt.model.dir.Direction.BACKWARD
import matt.model.dir.Direction.FORWARD

fun iconSpacer() = VBoxWrapperImpl<NodeWrapper>().apply {
  exactHeight = 20.0
  exactWidth = 5.0
}


fun Direction.graphic(): NodeWrapperImpl<*>? {
  return if (this == FORWARD) {
    Icon("white/forward")
  } else if (this == BACKWARD) {

    val canvas = CanvasWrapper(ICON_WIDTH, ICON_HEIGHT)
    val image = IconImage("white/forward")
    val xoff = 0.0 /*15.0*/
    val gc: GraphicsContext = canvas.graphicsContext
    gc.save()
    gc.translate(image.width + xoff*2, 0.0)
    gc.scale(-1.0, 1.0)
    gc.drawImage(image, xoff, 0.0)
    gc.restore()
    gc.drawImage(image, xoff, 0.0)

    canvas
  } else null
}




class LinePrintTextArea: TextAreaWrapper() {
  operator fun plusAssign(a: Any?) {
    text += "\n$a"
  }

  infix fun tab(a: Any?) {
    text += "\n\t$a"
  }
}