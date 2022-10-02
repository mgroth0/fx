package matt.fx.graphics.wrapper.window

import javafx.beans.property.ReadOnlyObjectProperty
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.stage.Screen
import javafx.stage.Window
import javafx.stage.WindowEvent
import matt.fx.graphics.wrapper.EventTargetWrapperImpl
import matt.hurricanefx.eye.wrapper.obs.obsval.NonNullFXBackedReadOnlyBindableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.fx.graphics.wrapper.sizeman.SizeControlled

abstract class WindowWrapper<W: Window>(override val node: W): EventTargetWrapperImpl<W>(), SizeControlled {

  var pullBackWhenOffScreen = true



  override val properties get() = node.properties

  override var height
	get() = node.height
	set(value) {
	  node.height = value
	}

  override var width
	get() = node.width
	set(value) {
	  node.width = value
	}


  fun setOnCloseRequest(value: EventHandler<WindowEvent>) = node.setOnCloseRequest(value)
  fun setOnHidden(value: EventHandler<WindowEvent>) = node.setOnHidden(value)

  var x
	get() = node.x
	set(value) {
	  node.x = value
	}

  val xProperty get() = node.xProperty().toNonNullableROProp()

  var y
	get() = node.y
	set(value) {
	  node.y = value
	}

  val yProperty get() = node.yProperty().toNonNullableROProp()


  //
  //  var height
  //	get() = node.height
  //	set(value) {
  //	  node.height = value
  //	}

  override val heightProperty get() = node.heightProperty().toNonNullableROProp().cast<Double>()


  //  var width
  //	get() = node.width
  //	set(value) {
  //	  node.width = value
  //	}

  override val widthProperty get() = node.widthProperty().toNonNullableROProp().cast<Double>()

  fun setOnShowing(value: EventHandler<WindowEvent>) = node.setOnShowing(value)

  val isShowing get() = node.isShowing
  val showingProperty: NonNullFXBackedReadOnlyBindableProp<Boolean> by lazy { node.showingProperty().toNonNullableROProp() }

  fun hide() = node.hide()


  open val scene: SceneWrapper<*>? get() = node.scene?.wrapped()

  fun sceneProperty(): ReadOnlyObjectProperty<Scene> = node.sceneProperty()

  val screen: Screen?
	get() = Screen.getScreensForRectangle(x, y, 1.0, 1.0).firstOrNull()

}


var WindowWrapper<*>.aboutToBeShown: Boolean
  get() = node.properties["tornadofx.aboutToBeShown"] == true
  set(value) {
	node.properties["tornadofx.aboutToBeShown"] = value
  }
