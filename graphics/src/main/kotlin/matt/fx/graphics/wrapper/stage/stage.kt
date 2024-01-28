package matt.fx.graphics.wrapper.stage

import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.Window
import matt.fx.base.wrapper.obs.obsval.prop.NullableFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.base.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.graphics.service.wrapped
import matt.fx.graphics.wrapper.inter.titled.Titled
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.fx.graphics.wrapper.window.WindowWrapper
import matt.lang.delegation.lazyDelegate
import matt.log.warn.warn

open class StageWrapper(node: Stage = Stage()) : WindowWrapper<Stage>(node), Titled {

    constructor(stageStyle: StageStyle) : this(Stage(stageStyle))

    final override val titleProperty: NullableFXBackedBindableProp<String> by lazy { node.titleProperty().toNullableProp() }

    val icons get() = node.icons

    fun showAndWait() = node.showAndWait()

    val owner: WindowWrapper<*>? get() = node.owner?.wrapped() as? WindowWrapper<*>
    fun initOwner(owner: Window) = node.initOwner(owner)
    fun initOwner(owner: WindowWrapper<*>) = node.initOwner(owner.node)
    fun initNoOwner() = node.initOwner(null)
    fun initModality(m: Modality) = node.initModality(m)
    fun initStyle(style: StageStyle) = node.initStyle(style)

    val iconifiedProperty by lazy { node.iconifiedProperty().toNonNullableROProp() }
    val isIconified by lazyDelegate { iconifiedProperty }

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

    final override var scene: SceneWrapper<*>?
        get() = super.scene
        set(value) {
            node.scene = value?.node
        }

    final override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }

    final override fun removeFromParent() {
        warn("removeFromParent used on a stage... closing stage")
        close()
    }

    final override fun isInsideRow() = false


    var isAlwaysOnTop
        get() = node.isAlwaysOnTop
        set(value) {
            node.isAlwaysOnTop = value
        }

    fun alwaysOnTopProperty(): ReadOnlyBooleanProperty = node.alwaysOnTopProperty()


}