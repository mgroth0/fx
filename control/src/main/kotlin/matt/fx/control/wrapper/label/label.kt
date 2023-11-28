package matt.fx.control.wrapper.label

import javafx.scene.Node
import javafx.scene.control.Label
import matt.fx.control.inter.graphic
import matt.fx.control.wrapper.labeled.LabeledWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.lang.assertions.require.requireNull
import matt.obs.bind.binding
import matt.obs.prop.ObsVal
import matt.obs.prop.ValProp
import matt.prim.converters.StringConverter


inline fun <reified T> ET.label(
    observable: ObsVal<T>,
    graphicProperty: ValProp<out NW?>? = null,
    converter: StringConverter<T>? = null,
    /*tooltip: Boolean = false,*/
    noinline op: LabelWrapper.() -> Unit = {}
) = label().apply {
    if (converter == null) {
        if (T::class == String::class) {
            @Suppress("UNCHECKED_CAST")
            textProperty.bind(observable as ObsVal<String>)
        } else {
            textProperty.bind(observable.binding { it?.toString() })
        }
    } else {
        textProperty.bind(observable.binding { converter.toString(it) })
    }
    if (graphicProperty != null) {
        this.graphicProperty.bind(graphicProperty)
    }
    /*  if (tooltip) {
        tooltip {
          content = LabelWrapper().also {
            it.textProperty.bind(this@apply.textProperty)
          }
        }
      }*/
    op(this)
}

fun ET.label(text: String = "", graphic: NodeWrapper? = null, wrap: Boolean? = null, op: LabelWrapper.() -> Unit = {}) =
    LabelWrapper().apply { this.text = text }.attachTo(this, op) {
        if (graphic != null) it.graphic = graphic
        if (wrap != null) it.isWrapText = wrap
    }


open class LabelWrapper(
    node: Label = Label(),
) : LabeledWrapper<Label>(node) {

    constructor(
        text: String?,
        graphic: Node? = null,
        wrap: Boolean? = null
    ) : this(Label(text, graphic)) {
        if (wrap != null) isWrapText = wrap
    }

    override fun addChild(child: NodeWrapper, index: Int?) {
        requireNull(index)
        graphic = child
    }


}