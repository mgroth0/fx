package matt.fx.control.popup.tooltip.node.simple

import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.css.CssMetaData
import javafx.css.Styleable
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.NodeOrientation.RIGHT_TO_LEFT
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Tooltip
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.stage.PopupWindow
import javafx.stage.Window
import javafx.util.Duration
import matt.fx.base.rewrite.ReWrittenFxClass


/*

Let's see if subclassing from Popup instead of PopupControl fixes my issues

Also, let's make this bare-bones. It can be just a node. Don't need the text/graphic nonsense

Also removing any CSS nonsense.

* */
@ReWrittenFxClass(Tooltip::class)
class MyFixedTooltip constructor() : PopupWindow() {
    /**
     * The delay between the mouse entering the hovered node and when the associated tooltip will be shown to the user.
     * The default delay is 1000ms.
     *
     * @return show delay property
     * @since 9
     * @defaultValue 1000ms
     */
    fun showDelayProperty(): ObjectProperty<Duration> = showDelayProperty

    fun setShowDelay(showDelay: Duration) = showDelayProperty.set(showDelay)

    val showDelay: Duration get() = showDelayProperty.get()
    private val showDelayProperty: ObjectProperty<Duration> = SimpleObjectProperty(
        Duration(1000.0)
    )

    /**
     * The duration that the tooltip should remain showing for until it is no longer visible to the user.
     * If the mouse leaves the control before the showDuration finishes, then the tooltip will remain showing
     * for the duration specified in the [.hideDelayProperty], even if the remaining time of the showDuration
     * is less than the hideDelay duration. The default value is 5000ms.
     *
     * @return the show duration property
     * @since 9
     * @defaultValue 5000ms
     */
    fun showDurationProperty(): ObjectProperty<Duration> = showDurationProperty
    fun setShowDuration(showDuration: Duration) = showDurationProperty.set(showDuration)
    val showDuration: Duration get() = showDurationProperty.get()
    private val showDurationProperty: ObjectProperty<Duration> = SimpleObjectProperty(
        Duration(5000.0)
    )

    /**
     * The duration in which to continue showing the tooltip after the mouse has left the node. Once this time has
     * elapsed the tooltip will hide. The default value is 200ms.
     *
     * @return the hide delay property
     * @since 9
     * @defaultValue 200ms
     */
    fun hideDelayProperty(): ObjectProperty<Duration> = hideDelayProperty

    fun setHideDelay(hideDelay: Duration) = hideDelayProperty.set(hideDelay)

    val hideDelay: Duration get() = hideDelayProperty.get()
    private val hideDelayProperty: ObjectProperty<Duration> = SimpleObjectProperty(
        Duration(200.0)
    )

    /**
     * An MANDATORY icon for the Tooltip. This can be positioned relative to the
     * text by using the [content display][.contentDisplayProperty]
     * property.
     * The node specified for this variable cannot appear elsewhere in the
     * scene graph, otherwise the `IllegalArgumentException` is thrown.
     * See the class description of [Node][javafx.scene.Node] for more detail.
     * @return the graphic property
     */
    fun contentProperty(): ObjectProperty<Parent?> = content

    fun setContent(value: Parent?) {
        contentProperty().value = value
    }

    fun getContent(): Parent? = contentProperty().value


    private val content: ObjectProperty<Parent?> = object : SimpleObjectProperty<Parent?>() {

        override fun getBean(): Any = this@MyFixedTooltip

        override fun getName(): String = "content"
    }


    /**
     * Typically, the tooltip is "activated" when the mouse moves over a Control.
     * There is usually some delay between when the Tooltip becomes "activated"
     * and when it is actually shown. The details (such as the amount of delay, etc)
     * is left to the Skin implementation.
     */
    private val activated = ReadOnlyBooleanWrapper(this, "activated")
    fun setActivated(value: Boolean) = activated.set(value)

    fun isActivated(): Boolean = activated.get()

    fun activatedProperty(): ReadOnlyBooleanProperty = activated.readOnlyProperty


    /**
     * Creates a tooltip with the specified text.
     *
     * @param text A text string for the tooltip.
     */
    /* *************************************************************************
       *                                                                         *
       * Constructors                                                            *
       *                                                                         *
       **************************************************************************/
    /**
     * Creates a tooltip with an empty string for its text.
     */
    init {
        contentProperty().addListener { _, _, newValue ->
            scene.root = newValue
        }
        (scene.root as Pane)
    }

    private class TooltipBehavior internal constructor(private val hideOnExit: Boolean) {
        /*
             * There are two key concepts with Tooltip: activated and visible. A Tooltip
             * is activated as soon as a mouse move occurs over the target node. When it
             * becomes activated, we start off the ACTIVATION_TIMER. If the
             * ACTIVATION_TIMER expires before another mouse event occurs, then we will
             * show the popup. This timer typically lasts about 1 second.
             *
             * Once visible, we reset the ACTIVATION_TIMER and start the HIDE_TIMER.
             * This second timer will allow the tooltip to remain visible for some time
             * period (such as 5 seconds). If the mouse hasn't moved, and the HIDE_TIMER
             * expires, then the tooltip is hidden and the tooltip is no longer
             * activated.
             *
             * If another mouse move occurs, the ACTIVATION_TIMER starts again, and the
             * same rules apply as above.
             *
             * If a mouse exit event occurs while the HIDE_TIMER is ticking, we reset
             * the HIDE_TIMER. Thus, the tooltip disappears after 5 seconds from the
             * last mouse move.
             *
             * If some other mouse event occurs while the HIDE_TIMER is running, other
             * than mouse move or mouse enter/exit (such as a click), then the tooltip
             * is hidden, the HIDE_TIMER stopped, and activated set to false.
             *
             * If a mouse exit occurs while the HIDE_TIMER is running, we stop the
             * HIDE_TIMER and start the LEFT_TIMER, and immediately hide the tooltip.
             * This timer is very short, maybe about a 1/2 second. If the mouse enters a
             * new node which also has a tooltip before LEFT_TIMER expires, then the
             * second tooltip is activated and shown immediately (the ACTIVATION_TIMER
             * having been bypassed), and the HIDE_TIMER is started. If the LEFT_TIMER
             * expires and there is no mouse movement over a control with a tooltip,
             * then we are back to the initial steady state where the next mouse move
             * over a node with a tooltip installed will start the ACTIVATION_TIMER.
             */
        private val activationTimer = Timeline()
        private val hideTimer = Timeline()
        private val leftTimer = Timeline()

        /**
         * The Node with a tooltip over which the mouse is hovering. There can
         * only be one of these at a time.
         */
        var hoveredNode: Node? = null

        /**
         * The tooltip that is currently activated. There can only be one
         * of these at a time.
         */
        private var activatedTooltip: MyFixedTooltip? = null

        /**
         * The tooltip that is currently visible. There can only be one
         * of these at a time.
         */
        private var visibleTooltip: MyFixedTooltip? = null

        /**
         * The last position of the mouse, in screen coordinates.
         */
        var lastMouseX = 0.0
        var lastMouseY = 0.0
        private var cssForced = false

        /**
         * Registers for mouse move events only. When the mouse is moved, this
         * handler will detect it and decide whether to start the ACTIVATION_TIMER
         * (if the ACTIVATION_TIMER is not started), restart the ACTIVATION_TIMER
         * (if ACTIVATION_TIMER is running), or skip the ACTIVATION_TIMER and just
         * show the tooltip (if the LEFT_TIMER is running).
         */
        private val MOVE_HANDLER = EventHandler<MouseEvent> { event: MouseEvent ->
            //Screen coordinates need to be actual for dynamic tooltip.
            //See Tooltip.setText
            lastMouseX = event.screenX
            lastMouseY = event.screenY

            // If the HIDE_TIMER is running, then we don't want this event
            // handler to do anything, or change any state at all.

            if (hideTimer.status == Animation.Status.RUNNING) {
                return@EventHandler
            }

            // Note that the "install" step will both register this handler
            // with the target node and also associate the tooltip with the
            // target node, by stashing it in the client properties of the node.
            hoveredNode = event.source as Node
            val t = hoveredNode!!.properties[TOOLTIP_PROP_KEY] as MyFixedTooltip?
            if (t != null) {
                // In theory we should never get here with an invisible or
                // non-existant window hierarchy, but might in some cases where
                // people are feeding fake mouse events into the hierarchy. So
                // we'll guard against that case.
                val owner = getWindow(hoveredNode)
                val treeVisible = isWindowHierarchyVisible(hoveredNode)
                if (owner != null && treeVisible) {
                    // Now we know that the currently HOVERED node has a tooltip
                    // and that it is part of a visible window Hierarchy.
                    // If LEFT_TIMER is running, then we make this tooltip
                    // visible immediately, stop the LEFT_TIMER, and start the
                    // HIDE_TIMER.
                    if (leftTimer.status == Animation.Status.RUNNING) {
                        if (visibleTooltip != null) visibleTooltip!!.hide()
                        visibleTooltip = t
                        t.show(
                            owner, event.screenX + TOOLTIP_XOFFSET,
                            event.screenY + TOOLTIP_YOFFSET
                        )
                        leftTimer.stop()
                        @Suppress("SENSELESS_COMPARISON")
                        if (t.showDuration != null) {
                            hideTimer.keyFrames.setAll(KeyFrame(t.showDuration))
                        }
                        hideTimer.playFromStart()
                    } else {
                        // Force the CSS to be processed for the tooltip so that it uses the
                        // appropriate timings for showDelay, showDuration, and hideDelay.
                        if (!cssForced) {
                            val opacity = t.opacity
                            t.opacity = 0.0
                            t.show(owner)
                            t.hide()
                            t.opacity = opacity
                            cssForced = true
                        }

                        // Start / restart the timer and make sure the tooltip
                        // is marked as activated.
                        t.setActivated(true)
                        activatedTooltip = t
                        activationTimer.stop()
                        @Suppress("SENSELESS_COMPARISON")
                        if (t.showDelay != null) {
                            activationTimer.keyFrames.setAll(KeyFrame(t.showDelay))
                        }
                        activationTimer.playFromStart()
                    }
                }
            } else {
                // TODO should deregister, no point being here anymore!
            }
        }

        /**
         * Registers for mouse exit events. If the ACTIVATION_TIMER is running then
         * this will simply stop it. If the HIDE_TIMER is running then this will
         * stop the HIDE_TIMER, hide the tooltip, and start the LEFT_TIMER.
         */
        private val LEAVING_HANDLER = EventHandler<MouseEvent> { event: MouseEvent ->
            // detect bogus mouse exit events, if it didn't really move then ignore it
            if (activationTimer.status == Animation.Status.RUNNING) {
                activationTimer.stop()
            } else if (hideTimer.status == Animation.Status.RUNNING) {
                assert(visibleTooltip != null)
                hideTimer.stop()
                if (hideOnExit) visibleTooltip!!.hide()
                val source = event.source as Node
                val t = source.properties[TOOLTIP_PROP_KEY] as MyFixedTooltip?
                if (t != null) {
                    @Suppress("SENSELESS_COMPARISON")
                    if (t.hideDelay != null) {
                        leftTimer.keyFrames.setAll(KeyFrame(t.hideDelay))
                    }
                    leftTimer.playFromStart()
                }
            }
            hoveredNode = null
            activatedTooltip = null
            if (hideOnExit) visibleTooltip = null
        }

        /**
         * Registers for mouse click, press, release, drag events. If any of these
         * occur, then the tooltip is hidden (if it is visible), it is deactivated,
         * and any and all timers are stopped.
         */
        private val KILL_HANDLER = EventHandler { _: MouseEvent? -> /*MouseEvent must be nullable here, as I call this with a null somewhere*/
            activationTimer.stop()
            hideTimer.stop()
            leftTimer.stop()
            if (visibleTooltip != null) visibleTooltip!!.hide()
            hoveredNode = null
            activatedTooltip = null
            visibleTooltip = null
        }

        init {
            activationTimer.onFinished = EventHandler {
                assert(activatedTooltip != null)
                val owner = getWindow(hoveredNode)
                val treeVisible = isWindowHierarchyVisible(hoveredNode)

                // If the ACTIVATED tooltip is part of a visible window
                // hierarchy, we can go ahead and show the tooltip and
                // start the HIDE_TIMER.
                //
                // If the owner is null or invisible, then it either means a
                // bug in our code, the node was removed from a scene or
                // window or made invisible, or the node is not part of a
                // visible window hierarchy. In that case, we don't show the
                // tooltip, and we don't start the HIDE_TIMER. We simply let
                // ACTIVATED_TIMER expire, and wait until the next mouse
                // the movement to start it again.
                if (owner != null && owner.isShowing && treeVisible) {
                    var x = lastMouseX
                    var y = lastMouseY

                    // The tooltip always inherits the nodeOrientation of
                    // the Node that it is attached to (see RT-26147). It
                    // is possible to override this for the Tooltip content
                    // (but not the popup placement) by setting the
                    // nodeOrientation on tooltip.getScene().getRoot().
                    val nodeOrientation = hoveredNode!!.effectiveNodeOrientation
                    activatedTooltip!!.scene.nodeOrientation = nodeOrientation
                    if (nodeOrientation == RIGHT_TO_LEFT) {
                        x -= activatedTooltip!!.width
                    }
                    activatedTooltip!!.show(
                        owner,
                        x + TOOLTIP_XOFFSET,
                        y + TOOLTIP_YOFFSET
                    )

                    // RT-37107: Ensure the tooltip is displayed in a position
                    // where it will not be under the mouse, even when the tooltip
                    // is near the edge of the screen
                    if (y + TOOLTIP_YOFFSET > activatedTooltip!!.anchorY) {
                        // the tooltip has been shifted vertically upwards,
                        // most likely to be underneath the mouse cursor, so we
                        // need to shift it further by hiding and reshowing
                        // in another location
                        activatedTooltip!!.hide()
                        y -= activatedTooltip!!.height
                        activatedTooltip!!.show(owner, x + TOOLTIP_XOFFSET, y)
                    }
                    visibleTooltip = activatedTooltip
                    hoveredNode = null
                    @Suppress("SENSELESS_COMPARISON")
                    if (activatedTooltip!!.showDuration != null) {
                        hideTimer.keyFrames.setAll(KeyFrame(activatedTooltip!!.showDuration))
                    }
                    hideTimer.playFromStart()
                }

                // Once the activation timer has expired, the tooltip is no
                // longer in the activated state, it is only in the visible
                // state, so we go ahead and set activated to false
                activatedTooltip!!.setActivated(false)
                activatedTooltip = null
            }
            hideTimer.onFinished = EventHandler { _: ActionEvent? ->
                assert(visibleTooltip != null)
                visibleTooltip!!.hide()
                visibleTooltip = null
                hoveredNode = null
            }
            leftTimer.onFinished = EventHandler { _: ActionEvent? ->
                if (!hideOnExit) {
                    // Hide the currently visible tooltip.
                    assert(visibleTooltip != null)
                    visibleTooltip!!.hide()
                    visibleTooltip = null
                    hoveredNode = null
                }
            }
        }

        fun install(
            node: Node?,
            t: MyFixedTooltip
        ) {
            // Install the MOVE_HANDLER, LEAVING_HANDLER, and KILL_HANDLER on
            // the given node. Stash the tooltip in the node's client properties
            // map so that it is not gc'd. The handlers must all be installed
            // with a TODO weak reference so as not to cause a memory leak
            if (node == null) return
            node.addEventHandler(MouseEvent.MOUSE_MOVED, MOVE_HANDLER)
            node.addEventHandler(MouseEvent.MOUSE_EXITED, LEAVING_HANDLER)
            node.addEventHandler(MouseEvent.MOUSE_PRESSED, KILL_HANDLER)
            node.properties[TOOLTIP_PROP_KEY] = t
        }

        fun uninstall(node: Node?) {
            if (node == null) return
            node.removeEventHandler(MouseEvent.MOUSE_MOVED, MOVE_HANDLER)
            node.removeEventHandler(MouseEvent.MOUSE_EXITED, LEAVING_HANDLER)
            node.removeEventHandler(MouseEvent.MOUSE_PRESSED, KILL_HANDLER)
            val t = node.properties[TOOLTIP_PROP_KEY] as MyFixedTooltip?
            if (t != null) {
                node.properties.remove(TOOLTIP_PROP_KEY)
                if (t == visibleTooltip || t == activatedTooltip) {
                    KILL_HANDLER.handle(null)
                }
            }
        }

        /**
         * Gets the top level window associated with this node.
         * @param node the node
         * @return the top level window
         */
        private fun getWindow(node: Node?): Window? {
            val scene = node?.scene
            return scene?.window
        }

        /**
         * Gets whether the entire window hierarchy is visible for this node.
         * @param node the node to check
         * @return true if entire hierarchy is visible
         */
        private fun isWindowHierarchyVisible(node: Node?): Boolean {
            var treeVisible = node != null
            var parent = node?.parent
            while (parent != null && treeVisible) {
                treeVisible = parent.isVisible
                parent = parent.parent
            }
            return treeVisible
        }
    }

    companion object {
        private const val TOOLTIP_PROP_KEY = "fx.Tooltip"

        // RT-31134 : the tooltip style includes a shadow around the tooltip with a
        // width of 9 and height of 5. This causes mouse events to not reach the control
        // underneath resulting in losing hover state on the control while the tooltip is showing.
        // Displaying the tooltip at an offset indicated below resolves this issue.
        // RT-37107: The y-offset was upped to 7 to ensure no overlaps when the tooltip
        // is shown near the right edge of the screen.
        private const val TOOLTIP_XOFFSET = 10
        private const val TOOLTIP_YOFFSET = 7
        private val BEHAVIOR = TooltipBehavior(false)

        /**
         * Associates the given [Tooltip] with the given [Node]. The tooltip
         * can then behave similar to when it is set on any [Control]. A single
         * tooltip can be associated with multiple nodes.
         * @param node the node
         * @param t the tooltip
         * @see Tooltip
         */
        fun install(
            node: Node?,
            t: MyFixedTooltip
        ) = BEHAVIOR.install(node, t)

        /**
         * Removes the association of the given [Tooltip] on the specified
         * [Node]. Hence hovering on the node will no longer result in showing of the
         * tooltip.
         * @param node the node
         * @param t the tooltip
         * @see Tooltip
         */
        fun uninstall(
            node: Node?,
            @Suppress("UNUSED_PARAMETER") t: MyFixedTooltip?
        ) = BEHAVIOR.uninstall(node)

        /* *************************************************************************
         *                                                                         *
         *                         Stylesheet Handling                             *
         *                                                                         *
         **************************************************************************/

        /**
         * Gets the `CssMetaData` associated with this class, which may include the
         * `CssMetaData` of its superclasses.
         * @return the `CssMetaData`
         * @since JavaFX 8.0
         */
        val classCssMetaData: List<CssMetaData<out Styleable, *>> by lazy {

            mutableListOf()

        }


        //  override val bridge = MyPopUpCSSBridge()
    }
}



