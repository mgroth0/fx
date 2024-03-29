package matt.fx.graphics.wrapper.scenelike

import javafx.beans.property.ObjectProperty
import javafx.event.EventTarget
import javafx.scene.Parent
import javafx.scene.paint.Paint
import matt.fx.graphics.service.wrapped
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.sizeman.Sized
import matt.lang.anno.Open
import matt.lang.assertions.require.requireNull

interface SceneLikeWrapper<N : EventTarget, R : ParentWrapper<*>> : EventTargetWrapper, Sized {
    override val node: N
    val rootProperty: ObjectProperty<Parent>

    fun castRoot(nw: NodeWrapper): R


    @Open
    var root: R
        get() = castRoot(rootProperty.get().wrapped())
        set(value) {
            rootProperty.set(value.node)
        }


    val fillProperty: ObjectProperty<Paint>

    @Open
    var fill: Paint
        get() = fillProperty.get()
        set(value) {
            fillProperty.set(value)
        }

    @Open
    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        requireNull(index)/*matt was here*/
        rootProperty.set((child as ParentWrapper<*>).node)
    }
}
