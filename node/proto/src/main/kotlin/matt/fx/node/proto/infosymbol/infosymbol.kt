package matt.fx.node.proto.infosymbol

import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontPosture.ITALIC
import javafx.scene.text.FontWeight.BOLD
import matt.fx.control.wrapper.tooltip.tooltip
import matt.fx.graphics.font.fixed
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.shape.circle.circle
import matt.fx.graphics.wrapper.pane.stack.StackPaneW
import matt.fx.graphics.wrapper.text.text
import matt.lang.function.DSL

fun ET.infoSymbol(text: String, op: DSL<InfoSymbol> = {}) = InfoSymbol(text).attachTo(this, op)

class InfoSymbol(info: String): StackPaneW() {
  init {
	circle(radius = 15.0) {
	  stroke = Color.GRAY
	  fill = Color.TRANSPARENT
	}
	text("i") {
	  font = Font.font("Georgia").fixed().copy(
		posture = ITALIC,
		size = 15.0,
		weight = BOLD
	  ).fx()
	}
	/*text("\uD83D\uDEC8")*/
	tooltip(info) {
	  comfortablyShowForeverUntilMouseMoved()
	}
  }
}