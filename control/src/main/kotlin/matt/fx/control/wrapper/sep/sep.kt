package matt.fx.control.wrapper.sep

import javafx.geometry.Orientation
import javafx.scene.control.Separator
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach


fun ET.separator(
  orientation: Orientation = Orientation.HORIZONTAL, op: SeparatorWrapper.()->Unit = {}
) = attach(SeparatorWrapper(orientation), op)


open class SeparatorWrapper(
  node: Separator = Separator(),
): ControlWrapperImpl<Separator>(node) {

  constructor(
	o: Orientation
  ): this(Separator(o))

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }


}

