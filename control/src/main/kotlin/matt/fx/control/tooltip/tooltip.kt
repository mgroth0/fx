package matt.fx.control.tooltip

import javafx.scene.Node
import javafx.scene.control.Tooltip
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.graphics.wrapper.node.NW

fun NW.add(newToolTip: Tooltip) {
  if (this is ControlWrapperImpl<*>) node.tooltip = newToolTip else javafx.scene.control.Tooltip.install(this.node, newToolTip)
}

fun NW.tooltip(text: String? = null, graphic: Node? = null, op: Tooltip.()->Unit = {}): Tooltip {
  val newToolTip = Tooltip(text)
  graphic?.apply { newToolTip.graphic = this }
  newToolTip.op()
  add(newToolTip)
  return newToolTip
}

