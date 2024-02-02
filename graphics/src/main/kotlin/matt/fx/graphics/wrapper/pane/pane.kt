package matt.fx.graphics.wrapper.pane

import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import matt.fx.base.wrapper.obs.collect.list.createMutableWrapper
import matt.fx.graphics.service.uncheckedWrapperConverter
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.node.shape.rect.RectangleWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.lang.anno.Open
import matt.lang.assertions.require.requireNull
import matt.model.data.corner.Corner
import matt.model.data.corner.Corner.NE
import matt.model.data.corner.Corner.NW
import matt.model.data.corner.Corner.SE
import matt.model.data.corner.Corner.SW
import matt.obs.col.olist.MutableObsList
import matt.obs.col.olist.sync.toSyncedList

fun <C : NodeWrapper> ET.pane(op: PaneWrapperImpl<*, C>.() -> Unit = {}) = attach(SimplePaneWrapper(), op)

private var didThisWarning = false
interface PaneWrapper<C : NodeWrapper> : RegionWrapper<C> {


    override val node: Pane

    @Open
    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        if (!didThisWarning) {
            println("Bad that C is not being type checked")
            didThisWarning = true
        }

        requireNull(index)
        @Suppress("UNCHECKED_CAST")
        children.add(child as C)
    }

    @Open   operator fun Collection<C>.unaryPlus() {
        addAll(this)
    }


    override val children: MutableObsList<C>


    @Open    fun resizer(corner: Corner) {
        /*var y = 0.0
          var x = 0.0*/
        var initEventX = 0.0
        var initEventY = 0.0
        var initStageX = 0.0
        var initStageY = 0.0
        var initStageWidth = 0.0
        var initStageHeight = 0.0
        var initStageMaxX = 0.0
        var initStageMaxY = 0.0/*val MIN = 100.0*/
        var dragging = false
        fun isInDraggableZone(
            @Suppress(
                "UNUSED_PARAMETER"
            ) event: MouseEvent
        ): Boolean {    /*return event.y > region.height - RESIZE_MARGIN*/
            return true
        }
        add(RectangleWrapper(50.0, 50.0, Color.BLUE).apply {
            setOnMouseReleased {
                dragging = false
                cursor = Cursor.DEFAULT
            }

            setOnMouseMoved {
                cursor = if (isInDraggableZone(it) || dragging) {
                    when (corner) {
                        NW -> Cursor.NW_RESIZE
                        NE -> Cursor.NE_RESIZE
                        SW -> Cursor.SW_RESIZE
                        SE -> Cursor.SE_RESIZE
                    }
                } else {
                    Cursor.DEFAULT
                }
            }
            setOnMouseDragged {
                if (dragging) {

                    when (corner) {
                        NW -> {
                            stage!!.y = initStageY + (it.screenY - initEventY)
                            stage!!.height = initStageMaxY - stage!!.y
                            stage!!.x = initStageX + (it.screenX - initEventX)
                            stage!!.width = initStageMaxX - stage!!.x
                        }

                        NE -> {
                            stage!!.y = initStageY + (it.screenY - initEventY)
                            stage!!.height = initStageMaxY - stage!!.y
                            stage!!.width = initStageWidth + (it.screenX - initEventX)
                        }

                        SW -> {
                            stage!!.height = initStageHeight + (it.screenY - initEventY)
                            stage!!.x = initStageX + (it.screenX - initEventX)
                            stage!!.width = initStageMaxX - stage!!.x
                        }

                        SE -> {
                            stage!!.height = initStageHeight + (it.screenY - initEventY)
                            stage!!.width = initStageWidth + (it.screenX - initEventX)
                        }
                    }
                }
            }
            setOnMousePressed {
                if (isInDraggableZone(it)) {
                    dragging = true
                    initEventX = it.screenX
                    initEventY = it.screenY
                    initStageHeight = stage!!.height
                    initStageWidth = stage!!.width
                    initStageX = stage!!.x
                    initStageY = stage!!.y
                    initStageMaxY = initStageHeight + initStageY
                    initStageMaxX = initStageWidth + initStageX
                }
            }
        })

    }

    @Open fun addAll(vararg nodes: C) = children.addAll(nodes)
    @Open fun addAll(nodes: Iterable<C>) = children.addAll(nodes)

    @Open fun clear() = children.clear()
}

fun <C : NodeWrapper> PaneWrapper<C>.spacer(size: Double = 20.0) {

    addChild(SimplePaneWrapper<VBoxWrapperImpl<*>>().apply {
        minWidth = size
        minHeight = size
    })
}

fun <C : NodeWrapper> PaneWrapper<C>.hSpacer(size: Double = 20.0) {

    addChild(SimplePaneWrapper<VBoxWrapperImpl<*>>().apply {
        minWidth = size
        minHeight = 1.0
    })
}

fun <C : NodeWrapper> PaneWrapper<C>.vSpacer(size: Double = 20.0) {

    addChild(SimplePaneWrapper<VBoxWrapperImpl<*>>().apply {
        minWidth = 1.0
        minHeight = size
    })
}


fun <C : NodeWrapper> SimplePaneWrapper() = PaneWrapperImpl<_, C>(Pane())


open class PaneWrapperImpl<N : Pane, C : NodeWrapper>(
    node: N
) : RegionWrapperImpl<N, C>(node), PaneWrapper<C> {


    final override val childList get() = node.children
    final override val children by lazy {
        node.children.createMutableWrapper().toSyncedList(uncheckedWrapperConverter<Node, C>())
    }
}
