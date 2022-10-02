package matt.fx.control.wrapper.sep

import javafx.geometry.Orientation
import javafx.scene.control.Separator
import matt.hurricanefx.wrapper.control.ControlWrapperImpl
import matt.hurricanefx.wrapper.node.NodeWrapper

open class SeparatorWrapper(
   node: Separator = Separator(),
): ControlWrapperImpl<Separator>(node) {
  companion object {
	fun Separator.wrapped() = SeparatorWrapper(this)
  }

  constructor(
	o: Orientation
  ): this(Separator(o))

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }


}

