package matt.fx.graphics.wrapper.subscene

import javafx.beans.property.ObjectProperty
import javafx.scene.Parent
import javafx.scene.SubScene
import javafx.scene.paint.Paint
import matt.hurricanefx.eye.wrapper.obs.obsval.toNonNullableROProp
import matt.hurricanefx.wrapper.node.NodeWrapperImpl
import matt.hurricanefx.wrapper.parent.ParentWrapper
import matt.fx.graphics.wrapper.scenelike.SceneLikeWrapper


class SubSceneWrapper<R: ParentWrapper<*>>(
  node: SubScene
): NodeWrapperImpl<SubScene>(node),
   SceneLikeWrapper<SubScene, R> {

  constructor(
	root: ParentWrapper<*>,
	width: Double,
	height: Double
  ): this(
	SubScene(
	  root.node,
	  width,
	  height
	)
  )

  override val rootProperty: ObjectProperty<Parent> get() = node.rootProperty()
  override val widthProperty get() = node.widthProperty().toNonNullableROProp().cast<Double>()
  override val heightProperty get() = node.heightProperty().toNonNullableROProp().cast<Double>()
  override val fillProperty: ObjectProperty<Paint> get() = node.fillProperty()

}