package matt.fx.graphics.wrapper.stage

import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.Window
import matt.fx.graphics.wrapper.inter.titled.Titled
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.fx.graphics.wrapper.window.WindowWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.NullableFXBackedBindableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNonNullableROProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNullableROProp
import matt.log.warn.warn

open class StageWrapper(override val node: Stage): WindowWrapper<Stage>(node), Titled {

  constructor(stageStyle: StageStyle): this(Stage(stageStyle))

  override val titleProperty: NullableFXBackedBindableProp<String> by lazy { node.titleProperty().toNullableProp() }

  fun showAndWait() = node.showAndWait()

  val owner: Window? get() = node.owner
  fun initOwner(owner: Window?) = node.initOwner(owner)
  fun initOwner(owner: WindowWrapper<*>?) = node.initOwner(owner?.node)
  fun initModality(m: Modality) = node.initModality(m)



  val iconifiedProperty by lazy { node.iconifiedProperty().toNonNullableROProp() }
  val isIconified by iconifiedProperty

  fun makeIconinfied() {
	node.isIconified = true
  }


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

  override fun removeFromParent() {
	warn("removeFromParent used on a stage... closing stage")
	close()
  }

  override fun isInsideRow() = false



  var isAlwaysOnTop
	get() = node.isAlwaysOnTop
	set(value) {
	  node.isAlwaysOnTop = value
	}

  fun alwaysOnTopProperty(): ReadOnlyBooleanProperty = node.alwaysOnTopProperty()


}