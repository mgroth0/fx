package matt.fx.control.wrapper.dialog

import javafx.scene.control.ChoiceDialog
import javafx.scene.control.Dialog
import javafx.scene.control.TextInputDialog
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.lang.NOT_IMPLEMENTED

open class DialogWrapper<R>(dialog: Dialog<R>): SingularEventTargetWrapper<Dialog<R>>(dialog) {
  override val properties = NOT_IMPLEMENTED
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }

  override fun removeFromParent() {
	TODO("Not yet implemented")
  }

  override fun isInsideRow(): Boolean {
	TODO("Not yet implemented")
  }
}


class ChoiceDialogWrapper<T>(node: ChoiceDialog<T>): DialogWrapper<T>(node)
class TextInputDialogWrapper(node: TextInputDialog): DialogWrapper<String>(node)