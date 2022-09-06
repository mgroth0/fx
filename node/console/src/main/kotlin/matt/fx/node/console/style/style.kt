package matt.fx.node.console.style

import javafx.scene.paint.Color
import javafx.scene.text.Font
import matt.hurricanefx.wrapper.text.TextWrapper


fun ConsoleText(size: Double, color: Color? = null) = TextWrapper("").apply {
  font = Font.font("Consolas", size)

  if (color != null) {
	fill = color
  }
}