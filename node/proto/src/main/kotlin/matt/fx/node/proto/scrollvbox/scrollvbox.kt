package matt.fx.node.proto.scrollvbox

import javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED
import javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER
import javafx.scene.layout.Pane
import matt.fx.control.Scrolls
import matt.fx.control.wrapper.scroll.ScrollPaneWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.lang.common.applyIt
import matt.obs.math.double.op.minus

abstract class ScrollVBox(
    scrollpane: ScrollPaneWrapper<VBoxWrapperImpl<*>> = ScrollPaneWrapper(contentCls = VBoxWrapperImpl::class),
    val vbox: VBoxWrapperImpl<NodeWrapper> = VBoxWrapperImpl()
) : PaneWrapperImpl<Pane, NodeWrapper>(Pane(), NodeWrapper::class), Scrolls { /* Refreshable */
    final override val scrollPane = scrollpane

    init {
        children.add(
            scrollPane.applyIt { sp ->
                /*If I want to configure, make into constructor params?*/
                vbarPolicy = AS_NEEDED
                hbarPolicy = NEVER
                isFitToWidth = true

                prefWidthProperty.bind(this@ScrollVBox.widthProperty)
                prefHeightProperty.bind(this@ScrollVBox.heightProperty)
                val woffset = 25.0
                layoutX = woffset
                layoutY = 0.0

                content =
                    this@ScrollVBox.vbox.apply {
                        /*minus 10 here is so everything looks nicer


                        also neccesary to prevent buggy javafx bug where fitToWidth doesnt work and it trys to hscroll.


                        needs to be exact or content will flow out of scrollpane (doesnt obey fitToWidth)*/
                        exactWidthProperty.bind(sp.widthProperty.minus(woffset * 2))

                        /*reason: this causes stupid buggy fx vertical scroll bar to properly hide when not needed*/
                        minHeightProperty.bind(sp.heightProperty.minus(50.0))
                    }
            }
        )
    }
}


