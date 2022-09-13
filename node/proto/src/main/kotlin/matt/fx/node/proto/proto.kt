package matt.fx.node.proto

import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.hurricanefx.wrapper.pane.vbox.VBoxWrapper

fun iconSpacer() = VBoxWrapper<NodeWrapper>().apply {
  exactHeight = 20.0
  exactWidth = 5.0
}