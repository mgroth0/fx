package matt.fx.node.proto.infosymbol

import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import javafx.scene.text.FontPosture.ITALIC
import javafx.scene.text.FontWeight.BOLD
import matt.fx.base.time.FXDuration
import matt.fx.control.popup.tooltip.fixed.tooltip
import matt.fx.control.wrapper.label.LabelWrapper
import matt.fx.graphics.font.fixed
import matt.fx.graphics.style.DarkModeController
import matt.fx.graphics.style.sty
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.node.shape.circle.circle
import matt.fx.graphics.wrapper.pane.stack.StackPaneW
import matt.fx.graphics.wrapper.style.FXColor
import matt.fx.graphics.wrapper.text.text
import matt.lang.function.DSL
import matt.obs.bind.binding
import matt.obs.prop.BindableProperty


abstract class HoverableSymbol(
  char: String,
  tooltipText: String,
  baseColor: FXColor? = null,
): StackPaneW() {
  companion object {
	private const val SIZE = 11.0
	val hoverColor by lazy {
	  DarkModeController.darkModeProp.binding {
		if (it) FXColor.YELLOW else FXColor.BLUE
	  }
	}
  }

  private val circ = circle(radius = SIZE) {
	stroke = Color.GRAY
	strokeWidth = 2.0
	this.fill = fill ?: Color.TRANSPARENT

	sty {
	  fxStroke = baseColor
	}

  }

  var fill: Paint?
	get() = circ.fill
	set(value) {
	  circ.fill = value
	}

  private val txt = text(char) {
	font = Font.font("Georgia").fixed().copy(
	  posture = ITALIC,
	  size = SIZE,
	  weight = BOLD
	).fx()
	sty {
	  fxFill = baseColor
	}
  }
  private var builtTT = false

  var char
	get() = txt.text
	set(value) {
	  txt.text = value
	}

  init {
	@Suppress("LeakingThis")
	exactHeight = SIZE*2
	@Suppress("LeakingThis")
	exactWidth = SIZE * 2
	hoverProperty.onChange { isHovering ->

	  if (!builtTT) {
		tt
		builtTT = true
	  }
	  val e = if (isHovering) Color.YELLOW else null
	  if (e != null) {
		circ.sty {
		  fxStroke = hoverColor.value
		}
		txt.sty {
		  fxFill = hoverColor.value
		}
	  } else {
		circ.sty {
		  fxStroke = baseColor
		}
		txt.sty {
		  fxFill = baseColor
		}
	  }

	}
  }


  protected open fun buildTooltipGraphic(text: String): ParentWrapper<*> {
	return LabelWrapper(text)
  }

  val content: ParentWrapper<*> by lazy {
	buildTooltipGraphic(tooltipText)
  }

  private val tt by lazy {

	tooltip(content = content) {
	  comfortablyShowForeverUntilMouseMoved()
	  showDelay = FXDuration.ZERO
	  showDuration = FXDuration.INDEFINITE
	  hideDelay = FXDuration.ZERO
	}

  }
}


fun ET.plusMinusSymbol(b: BindableProperty<Boolean>, op: DSL<PlusMinusSymbol> = {}) = PlusMinusSymbol(b).attachTo(this, op)

open class PlusMinusSymbol(b: BindableProperty<Boolean>): HoverableSymbol(
  char = if (b.value) "-" else "+",
  tooltipText = ""
) {
  init {
	@Suppress("LeakingThis")
	setOnMouseClicked {
	  b.value = !b.value
	  char = if (b.value) "-" else "+"
	}
  }
}

fun ET.infoSymbol(text: String, op: DSL<InfoSymbol> = {}) = InfoSymbol(text).attachTo(this, op)

open class InfoSymbol(info: String): HoverableSymbol(
  char = "i",
  tooltipText = info
)

fun ET.tutorialSymbol(text: String, op: DSL<TutorialSymbol> = {}) = TutorialSymbol(text).attachTo(this, op)

open class TutorialSymbol(info: String): HoverableSymbol(
  char = "?",
  tooltipText = info
)


fun ET.warningSymbol(text: String, op: DSL<WarningSymbol> = {}) = WarningSymbol(text).attachTo(this, op)

open class WarningSymbol(text: String): HoverableSymbol(char = "!", tooltipText = text, baseColor = FXColor.DARKORANGE)

fun ET.severeWarningSymbol(text: String, op: DSL<SevereWarningSymbol> = {}) = SevereWarningSymbol(text).attachTo(this, op)

open class SevereWarningSymbol(text: String): HoverableSymbol(char = "!!", tooltipText = text, baseColor = FXColor.RED)