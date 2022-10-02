package matt.fx.control.wrapper.label

import javafx.scene.Node
import javafx.scene.control.Label
import matt.fx.control.wrapper.labeled.LabeledWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.mtofx.createROFXPropWrapper
import matt.model.convert.StringConverter
import matt.obs.bind.binding
import matt.obs.prop.ObsVal
import matt.obs.prop.ValProp


inline fun <reified T> ET.label(
  observable: ObsVal<T>,
  graphicProperty: ValProp<Node>? = null,
  converter: StringConverter<T>? = null,
  noinline op: LabelWrapper.()->Unit = {}
) = label().apply {
  if (converter == null) {
	if (T::class == String::class) {
	  @Suppress("UNCHECKED_CAST")
	  textProperty.bind(observable as ValProp<String>)
	} else {
	  textProperty.bind(observable.binding { it?.toString() })
	}
  } else {
	textProperty.bind(observable.binding { converter.toString(it) })
  }
  if (graphic != null) graphicProperty().bind(graphicProperty?.createROFXPropWrapper())
  op(this)
}

fun ET.label(text: String = "", graphic: NodeWrapper? = null, wrap: Boolean? = null, op: LabelWrapper.()->Unit = {}) =
  LabelWrapper().apply { this.text = text }.attachTo(this, op) {
	if (graphic != null) it.graphic = graphic
	if (wrap != null) it.isWrapText = wrap
  }


open class LabelWrapper(
  node: Label = Label(),
): LabeledWrapper<Label>(node) {

  constructor(
	text: String?,
	graphic: Node? = null,
	wrap: Boolean? = null
  ): this(Label(text, graphic).apply {
	if (wrap != null) isWrapText = wrap
  })

  override fun addChild(child: NodeWrapper, index: Int?) {
	require(index == null)
	graphic = child
  }


}