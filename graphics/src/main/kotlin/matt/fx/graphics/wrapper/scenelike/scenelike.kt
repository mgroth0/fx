package matt.fx.graphics.wrapper.scenelike

import javafx.beans.property.ObjectProperty
import javafx.event.EventTarget
import javafx.scene.Parent
import javafx.scene.paint.Paint
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.hurricanefx.wrapper.parent.ParentWrapper
import matt.hurricanefx.wrapper.sizeman.Sized
import matt.hurricanefx.wrapper.target.EventTargetWrapper
import matt.hurricanefx.wrapper.wrapped

interface SceneLikeWrapper<N: EventTarget, R: ParentWrapper<*>>:
  EventTargetWrapper,
  Sized {
  override val node: N
  val rootProperty: ObjectProperty<Parent>


  var root: R
	@Suppress("UNCHECKED_CAST")
	get() = rootProperty.get().wrapped() as R
	set(value) {
	  rootProperty.set(value.node)
	}


  val fillProperty: ObjectProperty<Paint>

  var fill: Paint
	get() = fillProperty.get()
	set(value) {
	  fillProperty.set(value)
	}

  override fun addChild(child: NodeWrapper, index: Int?) {
	require(index == null)
	/*matt was here*/
	rootProperty.set((child as ParentWrapper<*>).node)
  }


}