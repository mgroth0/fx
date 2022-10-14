package matt.fx.graphics.wrapper.window

import javafx.beans.property.ReadOnlyObjectProperty
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.Scene
import javafx.stage.Screen
import javafx.stage.Window
import javafx.stage.WindowEvent
import matt.fx.graphics.service.wrapped
import matt.fx.graphics.wrapper.EventTargetWrapperImpl
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.fx.graphics.wrapper.sizeman.SizeControlled
import matt.hurricanefx.eye.wrapper.obs.obsval.NonNullFXBackedReadOnlyBindableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNonNullableROProp
import matt.lang.NOT_IMPLEMENTED

open class WindowWrapper<W: Window>(override val node: W): EventTargetWrapperImpl<W>(), SizeControlled {

  override fun removeFromParent(): Unit = NOT_IMPLEMENTED

  override fun isInsideRow() = false

  var pullBackWhenOffScreen = true


  override val properties get() = node.properties
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }

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


  @Suppress("SENSELESS_COMPARISON")
  val focusedProperty by lazy {
	require(node!=null) /*debug*/
	node.focusedProperty().toNonNullableROProp()
  }
  val focused by focusedProperty

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
  val showingProperty: NonNullFXBackedReadOnlyBindableProp<Boolean> by lazy {
	node.showingProperty().toNonNullableROProp()
  }

  fun hide() = node.hide()


  open val scene: SceneWrapper<*>? get() = node.scene?.wrapped() as SceneWrapper<*>?

  fun sceneProperty(): ReadOnlyObjectProperty<Scene> = node.sceneProperty()

  val screen: Screen?
	get() = Screen.getScreensForRectangle(x, y, 1.0, 1.0).firstOrNull()


  fun <T: Event> addEventFilter(eventType: EventType<T>, handler: EventHandler<T>) =
	node.addEventFilter(eventType, handler)

  fun <T: Event> addEventHandler(eventType: EventType<T>, handler: EventHandler<T>) =
	node.addEventHandler(eventType, handler)

  fun <T: Event> removeEventFilter(eventType: EventType<T>, handler: EventHandler<T>) =
	node.removeEventFilter(eventType, handler)

  fun <T: Event> removeEventHandler(eventType: EventType<T>, handler: EventHandler<T>) =
	node.removeEventHandler(eventType, handler)

}


var WindowWrapper<*>.aboutToBeShown: Boolean
  get() = node.properties["tornadofx.aboutToBeShown"] == true
  set(value) {
	node.properties["tornadofx.aboutToBeShown"] = value
  }
