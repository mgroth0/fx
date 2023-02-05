package matt.fx.graphics.wrapper.text

import javafx.beans.property.ObjectProperty
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import matt.fx.graphics.fxthread.ts.nonBlockingFXWatcher
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.onHover
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper
import matt.fx.graphics.wrapper.text.textlike.ColoredText
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.lang.delegation.lazyVarDelegate
import matt.obs.bindings.str.ObsS

fun ET.text(op: TextWrapper.()->Unit = {}) = TextWrapper().attachTo(this, op)

fun ET.text(initialValue: String? = null, op: TextWrapper.()->Unit = {}) = TextWrapper().attachTo(this, op) {
  if (initialValue != null) it.text = initialValue
}


fun ET.text(observable: ObsS, op: TextWrapper.()->Unit = {}) = text().apply {
  textProperty.bind(observable.nonBlockingFXWatcher())
  op(this)
}

open class TextWrapper(
  node: Text = Text(),
): ShapeWrapper<Text>(node), ColoredText {

  constructor(text: String): this(Text(text))


  override val textProperty by lazy { node.textProperty().toNonNullableProp() }
  override val fontProperty by lazy { node.fontProperty().toNonNullableProp() }
  override val textFillProperty by lazy { node.fillProperty().toNonNullableProp() }

  val wrappingWidthProperty get() = node.wrappingWidthProperty()

  var textAlignment: TextAlignment
	get() = node.textAlignment
	set(value) {
	  node.textAlignment = value
	}

  fun textAlignmentProperty(): ObjectProperty<TextAlignment> = node.textAlignmentProperty()

  val xProperty by lazy { node.xProperty().toNonNullableProp().cast<Double>() }
  var x by lazyVarDelegate { xProperty }
  val yProperty by lazy { node.yProperty().toNonNullableProp().cast<Double>() }
  var y by lazyVarDelegate { yProperty }

  fun highlightOnHover() {
	onHover {
	  val dark = matt.fx.graphics.style.DarkModeController.darkModeProp.value
	  fill = when {
		it   -> if (dark) javafx.scene.paint.Color.YELLOW else javafx.scene.paint.Color.BLUE
		dark -> javafx.scene.paint.Color.WHITE
		else -> javafx.scene.paint.Color.BLACK
	  }
	}
  }

}