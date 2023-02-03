package matt.fx.control.wrapper.popupcontrol

import matt.fx.control.wrapper.popupcontrol.node.MyPopupControl
import matt.fx.control.wrapper.popwinwrap.PopupWindowWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper

open class PopupControlWrapper<W: MyPopupControl>(node: W): PopupWindowWrapper<W>(node) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}