@file:Suppress("UNUSED_PARAMETER")

package matt.fx.control.wrapper.titled

import javafx.scene.Node
import javafx.scene.control.TitledPane
import javafx.scene.layout.Pane
import javafx.util.StringConverter
import matt.fx.control.inter.graphic
import matt.fx.control.wrapper.labeled.LabeledWrapper
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.inter.titled.Titled
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.vbox.VBoxW
import matt.lang.err
import matt.lang.go
import matt.obs.prop.MObservableValNewAndOld
import matt.obs.prop.ValProp
import java.text.Format


fun ET.titledpane(
    title: String? = null, node: NodeWrapper? = null, collapsible: Boolean = true, op: (TitledPaneWrapper).()->Unit = {}
): TitledPaneWrapper {
    val titledPane = TitledPaneWrapper().apply {
        title?.let { text = it };
        graphic = node
    }
    titledPane.isCollapsible = collapsible
    attach(titledPane, op)
    return titledPane
}

fun ET.titledpane(
    title: ValProp<String>,
    node: NodeWrapper? = null,
    collapsible: Boolean = true,
    op: (TitledPaneWrapper).()->Unit = {}
): TitledPaneWrapper {
    val titledPane = TitledPaneWrapper().apply {
        text = "";
        graphic = node
    }
    titledPane.textProperty.bind(title)
    titledPane.isCollapsible = collapsible
    attach(titledPane, op)
    return titledPane
}

open class TitledPaneWrapper(
    node: TitledPane = TitledPane(),
): LabeledWrapper<TitledPane>(node), Titled {

    final override val titleProperty by lazy { textProperty }

    var content: NodeWrapper?
        get() = node.content?.wrapped()
        set(value) {
            node.content = value?.node
        }

    var isCollapsible: Boolean
        get() = node.isCollapsible
        set(value) {
            node.isCollapsible = value
        }
    var isExpanded: Boolean
        get() = node.isExpanded
        set(value) {
            node.isExpanded = value
        }

    final override fun addChild(child: NodeWrapper, index: Int?) {
        when (content) {
            is Pane -> content!!.addChild(child, index)

            is Node -> {
                val container = VBoxW()
                content?.go {
                    container.children.add(it)
                }
                container.children.add(child)
                content = container
            }

            else    -> {
                content = child
            }
        }
    }
}


inline fun <reified S: T, reified T: Any> TitledPaneWrapper.bind(
    property: MObservableValNewAndOld<S>,
    readonly: Boolean = false,
    converter: StringConverter<T>? = null,
    format: Format? = null
): Unit = err("bindStringProperty(textProperty, converter, format, property, readonly)")
