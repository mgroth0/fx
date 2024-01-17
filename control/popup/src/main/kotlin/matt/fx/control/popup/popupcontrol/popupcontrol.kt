package matt.fx.control.popup.popupcontrol

import matt.fx.control.popup.popupcontrol.node.MyPopupControl
import matt.fx.control.popup.popwinwrap.PopupWindowWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper

open class PopupControlWrapper<W: MyPopupControl>(node: W): PopupWindowWrapper<W>(node) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO()
  }
}