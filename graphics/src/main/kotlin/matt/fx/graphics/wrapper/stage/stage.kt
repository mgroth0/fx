package matt.fx.graphics.wrapper.stage

import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.Window
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.NullableFXBackedBindableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp
import matt.hurricanefx.wrapper.inter.titled.Titled
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.hurricanefx.wrapper.window.WindowWrapper
import matt.hurricanefx.wrapper.wrapped

open class StageWrapper(override val node: Stage): WindowWrapper<Stage>(node), Titled {

  constructor(stageStyle: StageStyle): this(Stage(stageStyle))

  override val titleProperty: NullableFXBackedBindableProp<String> by lazy { node.titleProperty().toNullableProp() }

  fun showAndWait() = node.showAndWait()

  val owner: Window? get() = node.owner
  fun initOwner(owner: Window?) = node.initOwner(owner)
  fun initOwner(owner: WindowWrapper<*>?) = node.initOwner(owner?.node)
  fun initModality(m: Modality) = node.initModality(m)

  var isIconified
	get() = node.isIconified
	set(value) {
	  node.isIconified = value
	}

  fun iconifiedProperty(): ReadOnlyBooleanProperty = node.iconifiedProperty()


  fun show() = node.show()
  fun close() = node.close()
  fun toFront() = node.toFront()
  fun centerOnScreen() = node.centerOnScreen()


  var isFullScreen
	get() = node.isFullScreen
	set(value) {
	  node.isFullScreen = value
	}

  fun fullScreenProperty(): ReadOnlyBooleanProperty = node.fullScreenProperty()


  var isMaximized
	get() = node.isMaximized
	set(value) {
	  node.isMaximized = value
	}

  fun maximizedProperty(): ReadOnlyBooleanProperty = node.maximizedProperty()

  override var scene: SceneWrapper<*>?
	get() = super.scene
	set(value) {
	  node.scene = value?.node
	}

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }


  fun <T: Event> addEventFilter(eventType: EventType<T>, handler: EventHandler<T>) =
	node.addEventFilter(eventType, handler)

  fun <T: Event> addEventHandler(eventType: EventType<T>, handler: EventHandler<T>) =
	node.addEventHandler(eventType, handler)

  var isAlwaysOnTop
	get() = node.isAlwaysOnTop
	set(value) {
	  node.isAlwaysOnTop = value
	}

  fun alwaysOnTopProperty(): ReadOnlyBooleanProperty = node.alwaysOnTopProperty()


}