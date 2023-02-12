package matt.fx.graphics.wrapper.text.textlike

import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import matt.fx.graphics.font.fixed
import matt.fx.graphics.fxthread.runLater
import matt.fx.graphics.style.sty
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.style.FXColor
import matt.obs.prop.Var

val MONO_FONT by lazy { Font.font("monospaced") }

interface TextLike: EventTargetWrapper {
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

  fun monospace() {
	font = MONO_FONT
  }

}

interface ColoredText: TextLike, NodeWrapper {

  val textFillProperty: Var<Paint?>
  var textFill: Paint?
	get() = textFillProperty.value
	set(value) {
	  textFillProperty v value
	}



}

fun <T: ColoredText> T.applyConsoleStyle(size: Double? = null, color: Color? = null): T {
  font = font.fixed().copy(family = "Consolas").fx()
  if (size != null) {
	font = font.fixed().copy(size = size).fx()
  }
  if (color != null) {
	textFill = color
  }
  return this
}