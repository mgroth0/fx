package matt.fx.control.popup.dialog

import javafx.scene.Node
import javafx.scene.control.ChoiceDialog
import javafx.scene.control.Dialog
import javafx.scene.control.TextInputDialog
import javafx.stage.Modality
import javafx.stage.StageStyle
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.base.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.service.uncheckedNullableWrapperConverter
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.window.WindowWrapper
import matt.lang.common.NOT_IMPLEMENTED
import java.util.Optional

open class DialogWrapper<R>(dialog: Dialog<R> = Dialog()): SingularEventTargetWrapper<Dialog<R>>(dialog) {
    final override val properties get() = NOT_IMPLEMENTED
    final override fun addChild(child: NodeWrapper, index: Int?) {
        TODO()
    }

    final override fun removeFromParent() {
        TODO()
    }

    final override fun isInsideRow(): Boolean {
        TODO()
    }

    fun showAndWait(): Optional<R> = node.showAndWait()
    fun show() = node.show()

    fun initModality(modality: Modality) = node.initModality(modality)
    fun initOwner(owner: WindowWrapper<*>) = node.initOwner(owner.node)
    fun initStageStyle(stageStyle: StageStyle) = node.initStyle(stageStyle)

    val dialogPane by lazy {
        node.dialogPane.wrapped()
    }

    val graphicProp by lazy {
        node.graphicProperty().toNullableProp().proxy(uncheckedNullableWrapperConverter<Node, NodeWrapper>())
    }
    var graphic by graphicProp

    val headerTextProp by lazy {
        node.headerTextProperty().toNullableProp()
    }
    var headerText by headerTextProp

    fun close() = node.close()

    val resultProp by lazy {
        node.resultProperty().toNullableProp()
    }
    var result by resultProp


    val widthProp by lazy {

        node.widthProperty().toNonNullableROProp().cast<Double>()
    }
    var width
        get() = widthProp.value
        set(value) {
            node.width = value
        }
    val heightProp by lazy {
        node.heightProperty().toNonNullableROProp().cast<Double>()
    }
    var height
        get() = heightProp.value
        set(value) {
            node.height = value
        }
}


class ChoiceDialogWrapper<T>(node: ChoiceDialog<T>): DialogWrapper<T>(node)
class TextInputDialogWrapper(node: TextInputDialog): DialogWrapper<String>(node)
