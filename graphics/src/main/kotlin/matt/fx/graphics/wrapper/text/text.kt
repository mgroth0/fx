package matt.fx.graphics.wrapper.text

import javafx.beans.property.ObjectProperty
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.graphics.fxthread.ts.nonBlockingFXWatcher
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper
import matt.fx.graphics.wrapper.text.textlike.ColoredText
import matt.lang.delegation.lazyVarDelegate
import matt.obs.bindings.str.ObsS
import matt.prim.str.truncateWithElipses
import kotlin.reflect.KProperty

fun ET.text(op: TextWrapper.() -> Unit = {}) = TextWrapper().attachTo(this, op)

fun ET.text(
    initialValue: String? = null,
    op: TextWrapper.() -> Unit = {}
) = TextWrapper().attachTo(this, op) {
    if (initialValue != null) it.text = initialValue
}


fun ET.text(
    observable: ObsS,
    op: TextWrapper.() -> Unit = {}
) = text().apply {
    textProperty.bind(observable.nonBlockingFXWatcher())
    op(this)
}

open class TextWrapper(
    node: Text = Text(),
) : ShapeWrapper<Text>(node), ColoredText {

    constructor(text: String) : this(Text(text))

    private val textLength get() = text.length
    private val textTruncated get() = text.truncateWithElipses(100)

    final override fun reflectingToStringProps(): Set<KProperty<*>> {
        return setOf(::textLength, ::textTruncated)
    }

    final override val textProperty by lazy { node.textProperty().toNonNullableProp() }
    final override val fontProperty by lazy { node.fontProperty().toNonNullableProp() }
    final override val textFillProperty by lazy { node.fillProperty().toNullableProp() }

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


}