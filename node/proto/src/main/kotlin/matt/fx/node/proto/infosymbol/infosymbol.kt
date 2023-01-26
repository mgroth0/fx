package matt.fx.node.proto.infosymbol

import matt.fx.control.wrapper.tooltip.tooltip
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.pane.stack.StackPaneW
import matt.fx.graphics.wrapper.text.text
import matt.lang.function.DSL

fun ET.infoSymbol(text: String, op: DSL<InfoSymbol> = {}) = InfoSymbol(text).attachTo(this, op)

class InfoSymbol(info: String): StackPaneW() {
  init {
	/*	circle(radius = 15.0) {
		  fill = FXColor.GRAY
		}*/
	text("\uD83D\uDEC8")
	tooltip(info) {
	  comfortablyShowForeverUntilMouseMoved()
	}
  }
}