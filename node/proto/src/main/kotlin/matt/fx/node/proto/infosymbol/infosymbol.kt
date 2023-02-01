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
import matt.hurricanefx.eye.time.FXDuration
import matt.lang.function.DSL
import matt.obs.bindings.str.ObsS

fun ET.infoSymbol(text: ObsS, op: DSL<InfoSymbol> = {}) = InfoSymbol(text.value).attachTo(this) {
  textProperty.bind(text)
  op()
}

fun ET.infoSymbol(text: String, op: DSL<InfoSymbol> = {}) = InfoSymbol(text).attachTo(this, op)

class InfoSymbol(info: String): StackPaneW() {



  private val circ = circle(radius = 15.0) {
	stroke = Color.GRAY
	fill = Color.TRANSPARENT
  }
  private val txt = text("i") {
	font = Font.font("Georgia").fixed().copy(
	  posture = ITALIC,
	  size = 15.0,
	  weight = BOLD
	).fx()
  }


  init {
	hoverProperty.onChange {

	  val e = if (it) Color.YELLOW else null
	  if (e!= null) {
		circ.style = "-fx-stroke: yellow"
		/*circ.stroke = e*/
		/*txt.fill = e*/
		txt.style = "-fx-fill: yellow"
	  } else {
		circ.style = ""
		txt.style = ""
		/*(circ.node.strokeProperty() as StyleableObjectProperty).*/
	  }

	}
  }

  /*text("\uD83D\uDEC8")*/
  private val tt = tooltip(info) {
	comfortablyShowForeverUntilMouseMoved()
	showDelay = FXDuration.ZERO
	showDuration = FXDuration.INDEFINITE
	hideDelay = FXDuration.ZERO
  }
  val textProperty by lazy{ tt.textProperty }
  val tooltipFontProperty by lazy{  tt.fontProperty }
  val wrapTextProp by lazy{  tt.wrapTextProp }


}