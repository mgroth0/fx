package matt.fx.control.wrapper.control.text.area

import javafx.scene.control.TextArea
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.control.wrapper.control.text.input.TextInputControlWrapper
import matt.fx.graphics.fxthread.runLater
import matt.fx.graphics.fxthread.ts.periodicFXUpdates
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.obs.bindings.str.ObsS
import matt.obs.prop.writable.VarProp
import matt.prim.converters.StringConverter

fun ET.textarea(
    value: String? = null,
    op: TextAreaWrapper.() -> Unit = {}
) = TextAreaWrapper().attachTo(this, op) {
    if (value != null) it.text = value
}


fun ET.textarea(
    property: VarProp<String>,
    op: TextAreaWrapper.() -> Unit = {}
) = textarea().apply {
    textProperty.bindBidirectional(property)
    op(this)
}


fun <T> ET.textarea(
    property: VarProp<T>,
    converter: StringConverter<T>,
    op: TextAreaWrapper.() -> Unit = {}
) = textarea().apply {
    textProperty.bindBidirectional(property, converter)
    op(this)
}


open class TextAreaWrapper(
    node: TextArea = TextArea()
) : TextInputControlWrapper<TextArea>(node) {

    constructor(text: String) : this(TextArea(text))

    final override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }


    val wrapTextProperty: NonNullFXBackedBindableProp<Boolean> by lazy { node.wrapTextProperty().toNonNullableProp() }
    var isWrapText by wrapTextProperty


    fun receiveTextFrom(prop: ObsS) {
        textProperty.bind(
            prop.periodicFXUpdates().also {
                it.onChange {
                    runLater {
                        end()
                    }
                }
            }
        )
    }
}
