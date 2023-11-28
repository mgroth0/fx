package matt.fx.graphics.wrapper.window

import javafx.beans.property.ReadOnlyObjectProperty
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.Scene
import javafx.stage.Screen
import javafx.stage.Window
import javafx.stage.WindowEvent
import matt.fx.base.wrapper.obs.obsval.NonNullFXBackedReadOnlyBindableProp
import matt.fx.base.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.graphics.service.wrapped
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.fx.graphics.wrapper.sizeman.SizeControlled
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.lang.NOT_IMPLEMENTED
import matt.lang.delegation.lazyDelegate
import matt.lang.assertions.require.requireNotEqual

interface HasScene {
    val scene: SceneWrapper<*>?
}

open class WindowWrapper<W : Window>(override val node: W) : SingularEventTargetWrapper<W>(node), SizeControlled, HasScene {

    companion object {
        fun windows() = Window.getWindows().map { it.wrapped() as WindowWrapper<*> }
        fun guessMainStage() = windows().filterIsInstance<StageWrapper>().firstOrNull()
    }

    override fun removeFromParent(): Unit = NOT_IMPLEMENTED

    override fun isInsideRow() = false

    var pullBackWhenOffScreen = true

    fun requestFocus() = node.requestFocus()


    override val properties get() = node.properties
    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
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
        requireNotEqual(node, null) /*debug*/
        node.focusedProperty().toNonNullableROProp()
    }
    val focused by lazyDelegate { focusedProperty }

    fun setOnCloseRequest(value: EventHandler<WindowEvent>) = node.setOnCloseRequest(value)
    fun setOnHidden(value: EventHandler<WindowEvent>) = node.setOnHidden(value)

    var x
        get() = node.x
        set(value) {
            node.x = value
        }

    val xProperty by lazy { node.xProperty().toNonNullableROProp() }

    var y
        get() = node.y
        set(value) {
            node.y = value
        }

    val yProperty by lazy { node.yProperty().toNonNullableROProp() }


    //
    //  var height
    //	get() = node.height
    //	set(value) {
    //	  node.height = value
    //	}

    override val heightProperty by lazy { node.heightProperty().toNonNullableROProp().cast<Double>() }


    //  var width
    //	get() = node.width
    //	set(value) {
    //	  node.width = value
    //	}

    override val widthProperty by lazy { node.widthProperty().toNonNullableROProp().cast<Double>() }

    fun setOnShowing(value: EventHandler<WindowEvent>) = node.setOnShowing(value)

    val isShowing get() = node.isShowing
    val showingProperty: NonNullFXBackedReadOnlyBindableProp<Boolean> by lazy {
        node.showingProperty().toNonNullableROProp()
    }

    fun hide() = node.hide()


    override val scene: SceneWrapper<*>? get() = node.scene?.wrapped() as SceneWrapper<*>?

    fun sceneProperty(): ReadOnlyObjectProperty<Scene> = node.sceneProperty()

    val screen: Screen?
        get() = Screen.getScreensForRectangle(x, y, 1.0, 1.0).firstOrNull()


    fun <T : Event> addEventFilter(
        eventType: EventType<T>,
        handler: EventHandler<T>
    ) =
        node.addEventFilter(eventType, handler)

    fun <T : Event> addEventHandler(
        eventType: EventType<T>,
        handler: EventHandler<T>
    ) =
        node.addEventHandler(eventType, handler)

    fun <T : Event> removeEventFilter(
        eventType: EventType<T>,
        handler: EventHandler<T>
    ) =
        node.removeEventFilter(eventType, handler)

    fun <T : Event> removeEventHandler(
        eventType: EventType<T>,
        handler: EventHandler<T>
    ) =
        node.removeEventHandler(eventType, handler)

}


var WindowWrapper<*>.aboutToBeShown: Boolean
    get() = node.properties["tornadofx.aboutToBeShown"] == true
    set(value) {
        node.properties["tornadofx.aboutToBeShown"] = value
    }
