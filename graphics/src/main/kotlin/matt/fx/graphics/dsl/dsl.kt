package matt.fx.graphics.dsl

import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import matt.fx.graphics.style.DarkModeController
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.onHover
import matt.fx.graphics.wrapper.node.onLeftClick
import matt.fx.graphics.wrapper.pane.hbox.HBoxWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapper
import matt.fx.graphics.wrapper.text.TextWrapper
import matt.obs.bindings.str.ObsS

interface GraphicsDSL: EventTargetWrapper {
  fun text(op: TextWrapper.()->Unit = {}) = TextWrapper().attachTo(this, op)

  fun text(initialValue: String? = null, op: TextWrapper.()->Unit = {}) = TextWrapper().attachTo(this, op) {
	if (initialValue != null) it.text = initialValue
  }


  fun text(observable: ObsS, op: TextWrapper.()->Unit = {}) = text().apply {
	textProperty.bind(observable)
	op(this)
  }


  fun <C: NodeWrapper> hbox(
	spacing: Number? = null,
	alignment: Pos? = null,
	op: HBoxWrapper<C>.()->Unit = {}
  ): HBoxWrapper<C> {
	val hbox = HBoxWrapper<C>(HBox())
	if (alignment != null) hbox.alignment = alignment
	if (spacing != null) hbox.spacing = spacing.toDouble()
	return attach(hbox, op)
  }

  fun <C: NodeWrapper> vbox(
	spacing: Number? = null,
	alignment: Pos? = null,
	op: VBoxWrapper<C>.()->Unit = {}
  ): VBoxWrapper<C> {
	val vbox = VBoxWrapper<C>(VBox())
	if (alignment != null) vbox.alignment = alignment
	if (spacing != null) vbox.spacing = spacing.toDouble()
	return attach(vbox, op)
  }

  fun actionText(text: String, action: ()->Unit) = text(text) {
	onHover {
	  fill = when {
		it                                    -> Color.YELLOW
		DarkModeController.darkModeProp.value -> Color.WHITE
		else                                  -> Color.BLACK
	  }
	}
	onLeftClick {
	  action()
	}
  }

}