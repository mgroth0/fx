package matt.fx.graphics.wrapper.node

import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.collections.ObservableMap
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.geometry.Bounds
import javafx.geometry.Insets
import javafx.geometry.Point3D
import javafx.scene.CacheHint
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.SnapshotParameters
import javafx.scene.effect.BlendMode
import javafx.scene.effect.Effect
import javafx.scene.image.WritableImage
import javafx.scene.input.ContextMenuEvent
import javafx.scene.input.DragEvent
import javafx.scene.input.Dragboard
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.input.TransferMode
import javafx.scene.input.ZoomEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import matt.collect.itr.recurse.recurse
import matt.fx.graphics.fxthread.ensureInFXThreadInPlace
import matt.fx.graphics.fxthread.ts.nonBlockingFXWatcher
import matt.fx.graphics.service.wrapped
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.node.parent.parent
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.fx.graphics.wrapper.style.StyleableWrapper
import matt.fx.graphics.wrapper.window.HasScene
import matt.lang.anno.Open
import matt.lang.assertions.require.requireNotEqual
import matt.lang.common.NOT_IMPLEMENTED
import matt.obs.bindings.bool.ObsB
import matt.obs.bindings.bool.not
import matt.obs.prop.ObsVal
import matt.obs.prop.ValProp
import matt.obs.prop.newold.MObservableValNewAndOld
import matt.obs.prop.writable.BindableProperty
import matt.obs.prop.writable.Var
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract


inline fun <reified T : NodeWrapper> NW.findRecursivelyFirstOrNull(
    shuffleChildrenOrders: Boolean = false
): T? =
    ensureInFXThreadInPlace {
        /*has to be in FX thread since I'm searching through all nodes in a huge tree. any could change at any time on the FX thread causing concurrent mod errors (and it has, I think)*/
        recurseSelfAndChildNodes<T>(shuffleChildrenOrders = shuffleChildrenOrders).firstOrNull()
    }


inline fun <reified T : NodeWrapper> NW.recurseSelfAndChildNodes(
    shuffleChildrenOrders: Boolean = false
): Sequence<T> =
    recurse {
        (it as? RegionWrapper<*>)?.children?.let {
            if (shuffleChildrenOrders) it.shuffled() else it
        } ?: listOf()
    }.filterIsInstance<T>()


typealias NW = NodeWrapper

interface NodeWrapper : EventTargetWrapper, StyleableWrapper, HasScene {

    @Open
    override fun isInsideRow(): Boolean = parent?.isInsideRow() ?: false

    override val node: Node

    val visibleAndManagedProp: BindableProperty<Boolean>

    val styleProperty: ObsVal<String>

    val hoverProperty: ObsVal<Boolean>

    @Open
    val boundsInLocal: Bounds get() = node.boundsInLocal
    @Open
    val boundsInParent: Bounds get() = node.boundsInParent

    val layoutBoundsProperty: ObsVal<Bounds>

    @Open fun setOnKeyPressed(listener: (KeyEvent) -> Unit) {
        node.setOnKeyPressed(listener)
    }

    @Open
    fun setOnKeyTyped(listener: (KeyEvent) -> Unit) = node.setOnKeyTyped(listener)

    @Open
    fun setOnMousePressed(listener: (MouseEvent) -> Unit) {
        node.setOnMousePressed(listener)
    }


    @Open
    fun setOnMouseClicked(listener: (MouseEvent) -> Unit) {
        node.setOnMouseClicked(listener)
    }


    @Open
    fun setOnMouseDragged(listener: (MouseEvent) -> Unit) {
        node.setOnMouseDragged(listener)
    }

    @Open
    fun setOnMouseReleased(listener: (MouseEvent) -> Unit) {
        node.setOnMouseReleased(listener)
    }

    @Open
    fun setOnMouseMoved(listener: (MouseEvent) -> Unit) {
        node.setOnMouseMoved(listener)
    }

    @Open
    fun setOnMouseEntered(listener: (MouseEvent) -> Unit) {
        node.setOnMouseEntered(listener)
    }

    @Open
    fun setOnMouseExited(listener: (MouseEvent) -> Unit) {
        node.setOnMouseExited(listener)
    }

    @Open
    fun setOnDragEntered(listener: (DragEvent) -> Unit) {
        node.setOnDragEntered(listener)
    }

    @Open
    fun setOnDragOver(listener: (DragEvent) -> Unit) {
        node.setOnDragOver(listener)
    }

    @Open
    fun setOnDragDetected(listener: (MouseEvent) -> Unit) {
        node.setOnDragDetected(listener)
    }

    @Open
    fun setOnDragDone(listener: (DragEvent) -> Unit) {
        node.setOnDragDone(listener)
    }

    @Open
    fun setOnDragExited(listener: (DragEvent) -> Unit) {
        node.setOnDragExited(listener)
    }

    @Open
    fun setOnDragDropped(listener: (DragEvent) -> Unit) {
        node.setOnDragDropped(listener)
    }


    @Open
    fun toFront() = node.toFront()
    @Open
    fun toBack() = node.toBack()

    @Open
    fun setOnScroll(listener: (ScrollEvent) -> Unit) {
        node.setOnScroll(listener)
    }

    val managedProperty: Var<Boolean>
    val visibleProperty: Var<Boolean>


    @Open fun fireEvent(e: Event) = node.fireEvent(e)



    @Open fun setOnContextMenuRequested(value: EventHandler<ContextMenuEvent>) = node.setOnContextMenuRequested(value)

    @Open
    var opacity
        get() = node.opacity
        set(value) {
            node.opacity = value
        }

    @Open
    fun opacityProperty(): DoubleProperty = node.opacityProperty()
    @Open
    var rotate
        get() = node.rotate
        set(value) {
            node.rotate = value
        }
    @Open
    val rotateProperty: DoubleProperty get() = node.rotateProperty()

    @Open
    var rotationAxis: Point3D
        get() = node.rotationAxis
        set(value) {
            node.rotationAxis = value
        }
    @Open
    val rotationAxisProperty: ObjectProperty<Point3D> get() = node.rotationAxisProperty()


    @Open
    override val properties: ObservableMap<Any, Any?> get() = node.properties

    @Open
    var isCache
        get() = node.isCache
        set(value) {
            node.isCache = value
        }

    @Open
    fun cacheProperty(): BooleanProperty = node.cacheProperty()

    @Open
    var cacheHint: CacheHint
        get() = node.cacheHint
        set(value) {
            node.cacheHint = value
        }

    @Open
    fun cacheHintProperty(): ObjectProperty<CacheHint> = node.cacheHintProperty()


    @Open
    fun snapshot(
        params: SnapshotParameters?,
        image: WritableImage?
    ): WritableImage = node.snapshot(params, image)

    @Open
    fun startDragAndDrop(vararg transferModes: TransferMode): Dragboard = node.startDragAndDrop(*transferModes)
    @Open
    fun startFullDrag() = node.startFullDrag()

    @Open
    fun lookupAll(selector: String): MutableSet<Node> = node.lookupAll(selector)

    @Open
    var cursor: Cursor?
        get() = cursorProperty.value
        set(value) {
            cursorProperty v value
        }

    val cursorProperty: Var<Cursor?>


    @Open
    var clip: Node?
        get() = node.clip
        set(value) {
            node.clip = value
        }

    @Open
    fun clipProperty(): ObjectProperty<Node> = node.clipProperty()

    val effectProperty: Var<Effect?>
    @Open
    var effect: Effect?
        get() = effectProperty.value
        set(value) {
            effectProperty.value = value
        }

    @Open
    var blendMode: BlendMode?
        get() = node.blendMode
        set(value) {
            node.blendMode = value
        }

    @Open
    fun blendModeProperty(): ObjectProperty<BlendMode> = node.blendModeProperty()

    @Open
    fun autosize() = node.autosize()

    @Open
    fun <T : Event> addEventFilter(
        eventType: EventType<T>,
        handler: EventHandler<T>
    ) =
        node.addEventFilter(eventType, handler)

    @Open
    fun <T : Event> addEventHandler(
        eventType: EventType<T>,
        handler: EventHandler<T>
    ) =
        node.addEventHandler(eventType, handler)


    @Open
    fun <T : Event> removeEventFilter(
        eventType: EventType<T>,
        handler: EventHandler<T>
    ) =
        node.removeEventFilter(eventType, handler)

    @Open
    fun <T : Event> removeEventHandler(
        eventType: EventType<T>,
        handler: EventHandler<T>
    ) =
        node.removeEventHandler(eventType, handler)

    @Open
    fun localToScene(bounds: Bounds): Bounds? = node.localToScene(bounds)
    @Open
    fun localToScreen(bounds: Bounds): Bounds? = node.localToScreen(bounds)


    override val scene: SceneWrapper<*>?
    val sceneProperty: ObsVal<SceneWrapper<*>?>


    @Open
    var isEnabled
        get() = !isDisable
        set(value) {
            isDisable = !value
        }
    @Open
    var isEnable
        get() = !isDisable
        set(value) {
            isDisable = !value
        }
    @Open
    var isDisable
        get() = node.isDisable
        set(value) {
            node.isDisable = value
        }

    val disabledProperty: MObservableValNewAndOld<Boolean>
    val disableProperty: Var<Boolean>

    val enableProperty: Var<Boolean>

    val isFocused: Boolean
    val focusedProperty: ObsVal<Boolean>


    @Open
    var hGrow: Priority
        get() = HBox.getHgrow(node)
        set(value) {
            HBox.setHgrow(node, value)
        }
    @Open
    var vGrow: Priority
        get() = VBox.getVgrow(node)
        set(value) {
            VBox.setVgrow(node, value)
        }

    @Open fun setOnZoom(op: (ZoomEvent) -> Unit) = node.setOnZoom(op)


    @Open
    var isVisible
        get() = node.isVisible
        set(value) {
            node.isVisible = value
        }

    @Open
    var isManaged
        get() = node.isManaged
        set(value) {
            node.isManaged = value
        }

    @Open
    var translateX
        get() = node.translateX
        set(value) {
            node.translateX = value
        }

    @Open
    val translateXProperty: DoubleProperty get() = node.translateXProperty()

    @Open
    var translateY
        get() = node.translateY
        set(value) {
            node.translateY = value
        }

    @Open
    val translateYProperty: DoubleProperty get() = node.translateYProperty()


    @Open var layoutX
        get() = node.layoutX
        set(value) {
            node.layoutX = value
        }


    val layoutXProperty: Var<Double>


    @Open var layoutY
        get() = node.layoutY
        set(value) {
            node.layoutY = value
        }

    val layoutYProperty: Var<Double>


    @Open var scaleX
        get() = node.scaleX
        set(value) {
            node.scaleX = value
        }

    val scaleXProperty: Var<Double>
    @Open var scaleY
        get() = node.scaleY
        set(value) {
            node.scaleY = value
        }

    @Open
    val scaleYProperty: Var<Double>

    @Open
    fun requestFocus() = node.requestFocus()

    @Open
    fun setAsLayoutProxyForAndProxiedFrom(
        other: NodeWrapper,
        removeAllOtherProxiesOnBoth: Boolean = true /*critical to avoid memory leaks*/
    ) {

        requireNotEqual(node, other.node)

        require(hgrow == other.hgrow || (hgrow == null || other.hgrow == null))
        if (hgrow != null) other.hgrow = hgrow
        else if (other.hgrow != null) hgrow = other.hgrow

        require(vgrow == other.vgrow || (vgrow == null || other.vgrow == null))
        if (vgrow != null) other.vgrow = vgrow
        else if (other.vgrow != null) vgrow = other.vgrow

        if (removeAllOtherProxiesOnBoth) {
            layoutProxies.clear()
            other.layoutProxies.clear()
        }

        layoutProxies.add(other)
        other.layoutProxies.add(this)
    }

    val layoutProxies: MutableSet<NodeWrapper>


    @Open
    var hgrow: Priority?
        get() = HBox.getHgrow(node)
        set(value) {
            layoutProxyNetwork().forEach {
                HBox.setHgrow(it.node, value)
            }
        }
    @Open
    var vgrow: Priority?
        get() = VBox.getVgrow(node)
        set(value) {
            layoutProxyNetwork().forEach {
                VBox.setVgrow(it.node, value)
            }
        }

    @Open
    var hMarginAll: Double?
        get() = NOT_IMPLEMENTED
        set(value) {
            hMargin = value?.let { Insets(it) }
        }

    @Open
    var hMargin: Insets?
        get() = HBox.getMargin(node)
        set(value) {
            layoutProxyNetwork().forEach {
                HBox.setMargin(it.node, value)
            }
        }
    @Open
    var vMarginAll: Double?
        get() = NOT_IMPLEMENTED
        set(value) {
            vMargin = value?.let { Insets(it) }
        }
    @Open
    var vMargin: Insets?
        get() = VBox.getMargin(node)
        set(value) {
            layoutProxyNetwork().forEach {
                VBox.setMargin(it.node, value)
            }
        }

    @Open
    fun layoutProxyNetwork(): Set<NW> {
        val net = mutableSetOf(this)
        var toSearch = layoutProxies.toSet()
        do {
            net += toSearch
            toSearch = toSearch.flatMap { it.layoutProxies }.filter { it !in net }.toSet()
        } while (toSearch.isNotEmpty())
        return net
    }

    @Open
    fun clearLayoutProxyNetwork() {
        layoutProxyNetwork().forEach {
            it.layoutProxies.clear()
        }
    }


    @Open
    val boundsInScene: Bounds
        get() = localToScene(boundsInLocal)!!

    @Open
    val boundsInScreen: Bounds
        get() = localToScreen(boundsInLocal)!!


    @Open
    var visibleAndManaged: Boolean
        get() = node.isVisible && node.isManaged
        set(value) {
            node.isVisible = value
            node.isManaged = value
        }


    @Open
    val stage get() = scene?.window as? StageWrapper

    @Open
    fun onDoubleClickConsume(action: () -> Unit) {
        node.setOnMouseClicked {
            if (it.clickCount == 2) {
                action()
                it.consume()
            }
        }
    }

    @Open
    infix fun addTo(pane: EventTargetWrapper) = pane.addChild(this)


    @Open
    fun setAsTopAnchor(offset: Double) = AnchorPane.setTopAnchor(node, offset)
    @Open
    fun setAsBottomAnchor(offset: Double) = AnchorPane.setBottomAnchor(node, offset)
    @Open
    fun setAsLeftAnchor(offset: Double) = AnchorPane.setLeftAnchor(node, offset)
    @Open
    fun setAsRightAnchor(offset: Double) = AnchorPane.setRightAnchor(node, offset)

    @Open
    override fun removeFromParent() {
        node.parent?.wrapped()?.childList?.remove(node)
    }
}


inline fun <T : NodeWrapper> EventTargetWrapper.attach(
    child: T,
    op: T.() -> Unit = {}
): T {
    contract {
        callsInPlace(op, EXACTLY_ONCE)
    }
    addChild(child)
    op(child)
    return child
}

inline fun <T : NodeWrapper> T.attachTo(
    parent: EventTargetWrapper,
    op: T.() -> Unit = {}
): T {
    contract {
        callsInPlace(op, EXACTLY_ONCE)
    }
    parent.addChild(this)
    op(this)
    return this
}


/**
 * Attaches the node to the pane and invokes the node operation.
 * Because the framework sometimes needs to setup the node, another lambda can be provided
 */
inline fun <T : NodeWrapper> T.attachTo(
    parent: EventTargetWrapper,
    after: T.() -> Unit,
    before: (T) -> Unit
): T {
    contract {
        callsInPlace(before, EXACTLY_ONCE)
        callsInPlace(after, EXACTLY_ONCE)
    }
    return also(before).attachTo(parent, after)
}


fun NodeWrapper.setOnDoubleClick(
    filter: Boolean = false,
    action: (MouseEvent) -> Unit
) {
    if (filter) {
        addEventFilter(MouseEvent.MOUSE_CLICKED) {
            if (it.clickCount == 2) action(it)
        }
    } else {
        setOnMouseClicked {
            if (it.clickCount == 2) action(it)
        }
    }
}


fun NodeWrapper.onLeftClick(
    clickCount: Int = 1,
    filter: Boolean = false,
    action: (MouseEvent) -> Unit
) {
    if (filter) {
        addEventFilter(MouseEvent.MOUSE_CLICKED) {
            if (it.clickCount == clickCount && it.button === MouseButton.PRIMARY) action(it)
        }
    } else {
        setOnMouseClicked {
            if (it.clickCount == clickCount && it.button === MouseButton.PRIMARY) action(it)
        }
    }
}

fun NodeWrapper.onRightClick(
    clickCount: Int = 1,
    filter: Boolean = false,
    action: (MouseEvent) -> Unit
) {
    if (filter) {
        addEventFilter(MouseEvent.MOUSE_CLICKED) {
            if (it.clickCount == clickCount && it.button === MouseButton.SECONDARY) action(it)
        }
    } else {
        setOnMouseClicked {
            if (it.clickCount == clickCount && it.button === MouseButton.SECONDARY) action(it)
        }
    }
}


private object TfxTransitioningProperty

/**
 * Whether this node is currently being used in a [ViewTransition]. Used to determine whether it can be used in a
 * transition. (Nodes can only exist once in the sceneGraph, so it cannot be in two transitions at once.)
 */
internal var NodeWrapper.isTransitioning: Boolean
    get() {
        val x = node.properties[TfxTransitioningProperty]
        return x != null && (x !is Boolean || x != false)
    }
    set(value) {
        node.properties[TfxTransitioningProperty] = value
    }


fun NodeWrapper.hide() {
    isVisible = false
    isManaged = false
}

fun NodeWrapper.show() {
    isVisible = true
    isManaged = true
}

fun NodeWrapper.whenVisible(
    runLater: Boolean = true,
    op: () -> Unit
) {
    visibleProperty.onChange {
        if (it) {
            if (runLater) Platform.runLater(op) else op()
        }
    }
}


fun <T : NodeWrapper> T.managedWhen(expr: () -> ObsB): T = managedWhen(expr())

fun <T : NodeWrapper> T.managedWhen(predicate: ObsB) =
    apply {
        managedProperty.bind(predicate)
    }

fun <T : NodeWrapper> T.visibleWhen(predicate: ObsB) =
    apply {

        visibleProperty.bind(predicate)
    }

fun <T : NodeWrapper> T.visibleAndManagedWhen(predicate: ObsB) =
    apply {

        visibleAndManagedProp.bind(predicate)
    }

fun <T : NodeWrapper> T.visibleWhen(expr: () -> ObsB): T = visibleWhen(expr())
fun <T : NodeWrapper> T.visibleAndManagedWhen(expr: () -> ObsB): T = visibleAndManagedWhen(expr())

fun <T : NodeWrapper> T.hiddenWhen(expr: () -> ObsB): T = hiddenWhen(expr())

fun <T : NodeWrapper> T.hiddenWhen(predicate: ObsB) =
    apply {
        val binding = predicate.not()
        visibleProperty.bind(binding)
    }

fun <T : NodeWrapper> T.disableWhen(expr: () -> ObsB): T = disableWhen(expr())

fun <T : NodeWrapper> T.disableWhen(predicate: ObsB) =
    apply {
        disableProperty.bind(predicate)
    }

fun <T : NodeWrapper> T.enableWhen(expr: () -> ObsB): T = enableWhen(expr())

fun <T : NodeWrapper> T.enableWhen(predicate: ObsB) =
    apply {
        enableProperty.bind(predicate.nonBlockingFXWatcher())
    }

fun <T : NodeWrapper> T.removeWhen(expr: () -> ValProp<Boolean>): T = removeWhen(expr())

fun <T : NodeWrapper> T.removeWhen(predicate: ValProp<Boolean>) =
    apply {
        val remove = predicate.not()
        visibleProperty.bind(remove)
        managedProperty.bind(remove)
    }


fun NodeWrapper.onHover(onHover: (Boolean) -> Unit) =
    apply {
        hoverProperty.onChange(onHover)
    }


fun NW.minYRelativeTo(ancestor: NodeWrapper): Double? {
    var p: ParentWrapper<*>? = parent
    var y = boundsInParent.minY
    while (true) {
        when (p) {
            null     -> {
                return null
            }

            ancestor -> {
                return y
            }

            else     -> {
                y += p.boundsInParent.minY
                p = p.parent
            }
        }
    }
}

fun NW.maxYRelativeTo(ancestor: NodeWrapper): Double? = minYRelativeTo(ancestor)?.plus(boundsInParent.height)

