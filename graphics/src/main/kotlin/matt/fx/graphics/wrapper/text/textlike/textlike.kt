package matt.fx.graphics.wrapper.text.textlike

import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import matt.hurricanefx.font.fixed
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.obs.prop.Var

interface TextLike: NodeWrapper {
  val textProperty: Var<String?>
  var text: String
	get() = textProperty.value ?: ""
	set(value) {
	  textProperty.value = value
	}

  val fontProperty: Var<Font>
  var font: Font
	get() = fontProperty.value
	set(value) {
	  fontProperty v value
	}

  val textFillProperty: Var<Paint?>
  var textFill: Paint?
	get() = textFillProperty.value
	set(value) {
	  textFillProperty v value
	}


}

fun <T: TextLike> T.applyConsoleStyle(size: Double? = null, color: Color? = null): T {
  font = font.fixed().copy(family = "Consolas").fx()
  if (size != null) {
	font = font.fixed().copy(size = size).fx()
  }
  if (color != null) {
	textFill = color
  }
  return this
}