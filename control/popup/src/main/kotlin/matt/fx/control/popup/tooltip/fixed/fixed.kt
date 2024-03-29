package matt.fx.control.popup.tooltip.fixed

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.util.Duration
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.control.popup.popwinwrap.PopupWindowWrapper
import matt.fx.control.popup.tooltip.AutoDetectUnderlyingScene
import matt.fx.control.popup.tooltip.Owner
import matt.fx.control.popup.tooltip.SendMouseEvents
import matt.fx.control.popup.tooltip.SpecificScene
import matt.fx.control.popup.tooltip.correctTooltipNativeMouseEvent
import matt.fx.control.popup.tooltip.node.simple.MyFixedTooltip
import matt.fx.control.wrapper.label.LabelWrapper
import matt.fx.graphics.service.nullableParentConverter
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.lang.assertions.require.requireNull
import matt.lang.common.go
import matt.lang.delegation.lazyVarDelegate
import matt.obs.prop.writable.VarProp

fun NW.install(newToolTip: FixedTooltipWrapper) {
    MyFixedTooltip.install(
        node, newToolTip.node
    )
}

fun NW.tooltip(
    text: String = "",
    graphic: NW? = null,
    op: FixedTooltipWrapper.() -> Unit = {}
): FixedTooltipWrapper {
    val newToolTip =
        FixedTooltipWrapper().apply {


            content = LabelWrapper(text, graphic?.node)
            comfortablyShowForeverUntilEscaped()
            op()
        }
    install(newToolTip)
    return newToolTip
}

fun NW.tooltip(
    content: NW?,
    op: FixedTooltipWrapper.() -> Unit = {}
): FixedTooltipWrapper {
    val newToolTip =
        FixedTooltipWrapper().apply {
            this.content = content
            comfortablyShowForeverUntilEscaped()
            op()
        }
    install(newToolTip)
    return newToolTip
}


open class FixedTooltipWrapper(node: MyFixedTooltip = MyFixedTooltip()) : PopupWindowWrapper<MyFixedTooltip>(node) {


    val contentProperty by lazy {
        node.contentProperty().toNullableProp().proxy(nullableParentConverter)
    }
    var content by lazyVarDelegate {
        contentProperty
    }

    final override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        requireNull(index)
        contentProperty v child
    }


    fun comfortablyShowForeverUntilMouseMoved() {
        showDelay = Duration.millis(100.0)
        showDuration = Duration.INDEFINITE
    }

    fun comfortablyShowForeverUntilEscaped() {
        comfortablyShowForeverUntilMouseMoved()
        hideDelay = Duration.INDEFINITE
    }

    var showDelay: Duration
        get() = node.showDelay
        set(value) {
            node.setShowDelay(value)
        }
    var hideDelay: Duration
        get() = node.hideDelay
        set(value) {
            node.setHideDelay(value)
        }
    var showDuration: Duration
        get() = node.showDuration
        set(value) {
            node.setShowDuration(value)
        }
    var consumeAutoHidingEvents by node::consumeAutoHidingEvents
    var isAutoFix: Boolean by node.autoFixProperty().toNonNullableProp()
    var isAutoHide: Boolean by node.autoHideProperty().toNonNullableProp()


    private var transparentMouseEventHandler: EventHandler<MouseEvent>? = null

    val sendMouseEventsToProp =
        VarProp<SendMouseEvents?>(null).apply {
            onChange { opt ->
                synchronized(this) {
                    if (opt == null) {
                        transparentMouseEventHandler?.go {
                            removeEventFilter(MouseEvent.ANY, it)
                            transparentMouseEventHandler = null
                        }
                    } else {

                        addEventFilter(
                            MouseEvent.ANY,
                            EventHandler<MouseEvent> {
                                correctTooltipNativeMouseEvent(
                                    it,
                                    target =
                                        when (opt) {
                                            AutoDetectUnderlyingScene -> null
                                            is Owner                  -> node.ownerWindow.scene
                                            is SpecificScene          -> opt.scene
                                        },
                                    exclude = scene!!.node
                                )
                                it.consume()
                            }.also {
                                transparentMouseEventHandler = it
                            }
                        )
                    }
                }
            }
        }
    var sendMouseEventsTo by sendMouseEventsToProp
}
