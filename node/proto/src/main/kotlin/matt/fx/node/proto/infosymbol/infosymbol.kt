package matt.fx.node.proto.infosymbol

import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontPosture.ITALIC
import javafx.scene.text.FontWeight.BOLD
import matt.fx.control.wrapper.label.LabelWrapper
import matt.fx.control.wrapper.tooltip.fixed.tooltip
import matt.fx.graphics.font.fixed
import matt.fx.graphics.style.DarkModeController
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.node.shape.circle.circle
import matt.fx.graphics.wrapper.pane.stack.StackPaneW
import matt.fx.graphics.wrapper.text.text
import matt.hurricanefx.eye.time.FXDuration
import matt.lang.function.DSL
import matt.obs.bind.binding

/*fun ET.infoSymbol(text: ObsS, op: DSL<InfoSymbol> = {}) = InfoSymbol(text.value).attachTo(this) {
  textProperty.bind(text)
  op()
}*/

fun ET.infoSymbol(text: String, op: DSL<InfoSymbol> = {}) = InfoSymbol(text).attachTo(this, op)

open class InfoSymbol(info: String): StackPaneW() {

  companion object {
	val hoverColor by lazy {
	  DarkModeController.darkModeProp.binding {
		if (it) "yellow" else "blue"
	  }
	}
  }

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


  private var builtTT = false

  init {
	hoverProperty.onChange {

	  if (!builtTT) {
		tt
		builtTT = true
	  }
	  val e = if (it) Color.YELLOW else null
	  if (e != null) {
		circ.style = "-fx-stroke: ${hoverColor.value}"
		/*circ.stroke = e*/
		/*txt.fill = e*/
		txt.style = "-fx-fill: ${hoverColor.value}"
	  } else {
		circ.style = ""
		txt.style = ""
		/*(circ.node.strokeProperty() as StyleableObjectProperty).*/
	  }

	}
  }


  protected open fun buildTooltipGraphic(info: String): ParentWrapper<*> {
	return LabelWrapper(info)
  }

  val content: ParentWrapper<*> by lazy {
	buildTooltipGraphic(info)
  }

  /*text("\uD83D\uDEC8")*/
  private val tt by lazy {

	tooltip(content = content) {
	  comfortablyShowForeverUntilMouseMoved()
	  showDelay = FXDuration.ZERO
	  showDuration = FXDuration.INDEFINITE
	  hideDelay = FXDuration.ZERO
	}
	/*val textProperty by lazy { tooltipLabel.textProperty }
  val tooltipFontProperty by lazy { tooltipLabel.fontProperty }
  val wrapTextProp by lazy { tooltipLabel.wrapTextProperty }*/

  }
}