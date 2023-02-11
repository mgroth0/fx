package matt.fx.control.popup.tooltip.node

import com.sun.javafx.css.StyleManager
import com.sun.javafx.scene.NodeHelper
import com.sun.javafx.stage.PopupWindowHelper
import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.css.CssMetaData
import javafx.css.FontCssMetaData
import javafx.css.SimpleStyleableBooleanProperty
import javafx.css.SimpleStyleableDoubleProperty
import javafx.css.SimpleStyleableObjectProperty
import javafx.css.StyleOrigin
import javafx.css.StyleOrigin.USER
import javafx.css.Styleable
import javafx.css.StyleableObjectProperty
import javafx.css.StyleableProperty
import javafx.css.StyleableStringProperty
import javafx.css.converter.BooleanConverter
import javafx.css.converter.DurationConverter
import javafx.css.converter.EnumConverter
import javafx.css.converter.SizeConverter
import javafx.css.converter.StringConverter
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.NodeOrientation.RIGHT_TO_LEFT
import javafx.scene.AccessibleRole.TOOLTIP
import javafx.scene.Node
import javafx.scene.control.ContentDisplay
import javafx.scene.control.OverrunStyle
import javafx.scene.control.OverrunStyle.ELLIPSIS
import javafx.scene.control.Skin
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import javafx.scene.text.TextAlignment.LEFT
import javafx.stage.Window
import javafx.util.Duration
import matt.fx.control.popup.popupcontrol.node.MyPopupControl
import matt.fx.control.popup.popupcontrol.node.bridge.MyPopUpCSSBridge
import matt.fx.control.popup.tooltip.node.skin.MyTooltipSkin
import matt.lang.addAll
import java.util.Collections


/*

Main/Original (but not only) reason for creating MyTooltip:

Because styling the built in tooltip is unsupported, requires hacks, buggy, and unreliable.

Also the design of this is to have a "text" and a "graphic". That's too specific for my needs.

* */
class MyTooltip @JvmOverloads constructor(text: String? = null): MyPopupControl() {
  /**
   * The text to display in the tooltip. If the text is set to null, an empty
   * string will be displayed, despite the value being null.
   * @return the text property
   */
  fun textProperty(): StringProperty = text

  fun setText(value: String?) {
	textProperty().value = value
  }

  fun getText(): String = if (text.value == null) "" else text.value

  private val text: StringProperty = object: SimpleStringProperty(this, "text", "") {
	override fun invalidated() {
	  super.invalidated()
	  val value = get()
	  if (isShowing && value != null && value != getText()) {
		//Dynamic tooltip content is location-dependant.
		//Chromium trick.
		anchorX = BEHAVIOR.lastMouseX
		anchorY = BEHAVIOR.lastMouseY
	  }
	}
  }

  /**
   * Specifies the behavior for lines of text *when text is multiline*.
   * Unlike [contentDisplay][.contentDisplayProperty] which affects the
   * graphic and text, this setting only affects multiple lines of text
   * relative to the text bounds.
   * @return the text alignment property
   */
  fun textAlignmentProperty(): ObjectProperty<TextAlignment> = textAlignment

  fun setTextAlignment(value: TextAlignment) {
	textAlignmentProperty().value = value
  }

  fun getTextAlignment(): TextAlignment = textAlignmentProperty().value

  private val textAlignment: ObjectProperty<TextAlignment> = SimpleStyleableObjectProperty(
	TEXT_ALIGNMENT, this, "textAlignment", LEFT
  )

  /**
   * Specifies the behavior to use if the text of the `Tooltip`
   * exceeds the available space for rendering the text.
   * @return the text overrun property
   */
  fun textOverrunProperty(): ObjectProperty<OverrunStyle> = textOverrun

  fun setTextOverrun(value: OverrunStyle) {
	textOverrunProperty().value = value
  }

  fun getTextOverrun(): OverrunStyle = textOverrunProperty().value

  private val textOverrun: ObjectProperty<OverrunStyle> = SimpleStyleableObjectProperty(
	TEXT_OVERRUN, this, "textOverrun", ELLIPSIS
  )

  /**
   * If a run of text exceeds the width of the Tooltip, then this variable
   * indicates whether the text should wrap onto another line.
   * @return the wrap text property
   */
  fun wrapTextProperty(): BooleanProperty = wrapText

  fun setWrapText(value: Boolean) {
	wrapTextProperty().value = value
  }

  fun isWrapText(): Boolean = wrapTextProperty().value

  private val wrapText: BooleanProperty = SimpleStyleableBooleanProperty(WRAP_TEXT, this, "wrapText", false)

  /**
   * The default font to use for text in the Tooltip. If the Tooltip's text is
   * rich text then this font may or may not be used depending on the font
   * information embedded in the rich text, but in any case where a default
   * font is required, this font will be used.
   * @return the font property
   */
  fun fontProperty(): ObjectProperty<Font> = font

  fun setFont(value: Font) {
	fontProperty().value = value
  }

  fun getFont(): Font = fontProperty().value

  private val font: ObjectProperty<Font> = object: StyleableObjectProperty<Font>(Font.getDefault()) {
	private var fontSetByCss = false
	override fun applyStyle(newOrigin: StyleOrigin, value: Font) {
	  // RT-20727 - if CSS is setting the font, then make sure invalidate doesn't call NodeHelper.reapplyCSS
	  try {
		// super.applyStyle calls set which might throw if value is bound.
		// Have to make sure fontSetByCss is reset.
		fontSetByCss = true
		super.applyStyle(newOrigin, value)
	  } catch (e: Exception) {
		throw e
	  } finally {
		fontSetByCss = false
	  }
	}

	override fun set(value: Font?) {
	  val oldValue = get()
	  val origin = (this as StyleableObjectProperty<Font>).styleOrigin
	  if (origin == null || (if (value != null) value != oldValue else oldValue != null)) {
		super.set(value)
	  }
	}

	override fun invalidated() {
	  // RT-20727 - if font is changed by calling setFont, then
	  // css might need to be reapplied since font size affects
	  // calculated values for styles with relative values
	  if (fontSetByCss == false) {
		NodeHelper.reapplyCSS(bridge)
	  }
	}

	override fun getCssMetaData(): CssMetaData<MyToolTipCSSBridge, Font> = FONT

	override fun getBean(): Any = this@MyTooltip

	override fun getName(): String = "font"
  }

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
  private val showDelayProperty: ObjectProperty<Duration> = SimpleStyleableObjectProperty(
	SHOW_DELAY, this, "showDelay", Duration(1000.0)
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
  private val showDurationProperty: ObjectProperty<Duration> = SimpleStyleableObjectProperty(
	SHOW_DURATION, this, "showDuration", Duration(5000.0)
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
  private val hideDelayProperty: ObjectProperty<Duration> = SimpleStyleableObjectProperty(
	HIDE_DELAY, this, "hideDelay", Duration(200.0)
  )

  /**
   * An optional icon for the Tooltip. This can be positioned relative to the
   * text by using the [content display][.contentDisplayProperty]
   * property.
   * The node specified for this variable cannot appear elsewhere in the
   * scene graph, otherwise the `IllegalArgumentException` is thrown.
   * See the class description of [Node][javafx.scene.Node] for more detail.
   * @return the graphic property
   */
  fun graphicProperty(): ObjectProperty<Node?> = graphic

  fun setGraphic(value: Node?) {
	graphicProperty().value = value
  }

  fun getGraphic(): Node? = graphicProperty().value


  private val graphic: ObjectProperty<Node?> = object: StyleableObjectProperty<Node?>() {
	// The graphic is styleable by css, but it is the
	// imageUrlProperty that handles the style value.
	/*Note from Matt: this is weird*/
	@Suppress("UNCHECKED_CAST")
	override fun getCssMetaData() = GRAPHIC as CssMetaData<Styleable, Node?>

	override fun getBean(): Any = this@MyTooltip

	override fun getName(): String = "graphic"
  }

  private fun imageUrlProperty(): StyleableStringProperty {
	if (imageUrl == null) {
	  imageUrl = object: StyleableStringProperty() {
		// If imageUrlProperty is invalidated, this is the origin of the style that
		// triggered the invalidation. This is used in the invaildated() method where the
		// value of super.getStyleOrigin() is not valid until after the call to set(v) returns,
		// by which time invalidated will have been called.
		// This value is initialized to USER in case someone calls set on the imageUrlProperty, which
		// is possible:
		//     CssMetaData metaData = ((StyleableProperty)labeled.graphicProperty()).getCssMetaData();
		//     StyleableProperty prop = metaData.getStyleableProperty(labeled);
		//     prop.set(someUrl);
		//
		// TODO: Note that prop != labeled, which violates the contract between StyleableProperty and CssMetaData.
		var origin = USER
		override fun applyStyle(origin: StyleOrigin, v: String) {
		  this.origin = origin

		  // Don't want applyStyle to throw an exception which would leave this.origin set to the wrong value
		  if (!graphic.isBound) super.applyStyle(origin, v)

		  // Origin is only valid for this invocation of applyStyle, so reset it to USER in case someone calls set.
		  this.origin = USER
		}

		override fun invalidated() {

		  // need to call super.get() here since get() is overridden to return the graphicProperty's value
		  val url = super.get()
		  if (url == null) {
			@Suppress("UNCHECKED_CAST")
			(graphicProperty() as StyleableProperty<Node?>).applyStyle(origin, null)
		  } else {
			// RT-34466 - if graphic's url is the same as this property's value, then don't overwrite.
			val graphicNode = getGraphic()
			if (graphicNode is ImageView) {
			  val image = graphicNode.image
			  if (image != null) {
				val imageViewUrl = image.url
				if (url == imageViewUrl) return
			  }
			}
			val img = StyleManager.getInstance().getCachedImage(url)
			if (img != null) {
			  // Note that it is tempting to try to re-use existing ImageView simply by setting
			  // the image on the current ImageView, if there is one. This would effectively change
			  // the image, but not the ImageView which means that no graphicProperty listeners would
			  // be notified. This is probably not what we want.

			  // Have to call applyStyle on graphicProperty so that the graphicProperty's
			  // origin matches the imageUrlProperty's origin.
			  @Suppress("UNCHECKED_CAST")
			  (graphicProperty() as StyleableProperty<Node?>).applyStyle(origin, ImageView(img))
			}
		  }
		}

		override fun get(): String? {
		  // The value of the imageUrlProperty is that of the graphicProperty.
		  // Return the value in a way that doesn't expand the graphicProperty.
		  val graphic = getGraphic()
		  if (graphic is ImageView) {
			val image = graphic.image
			if (image != null) {
			  return image.url
			}
		  }
		  return null
		}

		override fun getStyleOrigin(): StyleOrigin? {
		  // The origin of the imageUrlProperty is that of the graphicProperty.
		  // Return the origin in a way that doesn't expand the graphicProperty.
		  @Suppress("UNCHECKED_CAST")
		  return (graphic as StyleableProperty<Node?>).styleOrigin
		}

		override fun getBean(): Any = this@MyTooltip

		override fun getName(): String = "imageUrl"

		override fun getCssMetaData(): CssMetaData<MyToolTipCSSBridge, String> = GRAPHIC
	  }
	}
	return imageUrl!!
  }

  private var imageUrl: StyleableStringProperty? = null

  /**
   * Specifies the positioning of the graphic relative to the text.
   * @return the content display property
   */
  fun contentDisplayProperty(): ObjectProperty<ContentDisplay> = contentDisplay

  fun setContentDisplay(value: ContentDisplay) {
	contentDisplayProperty().value = value
  }

  fun getContentDisplay(): ContentDisplay = contentDisplayProperty().value

  private val contentDisplay: ObjectProperty<ContentDisplay> = SimpleStyleableObjectProperty(
	CONTENT_DISPLAY, this, "contentDisplay", ContentDisplay.LEFT
  )

  /**
   * The amount of space between the graphic and text
   * @return the graphic text gap property
   */
  fun graphicTextGapProperty(): DoubleProperty = graphicTextGap

  fun setGraphicTextGap(value: Double) {
	graphicTextGapProperty().value = value
  }

  fun getGraphicTextGap(): Double = graphicTextGapProperty().value

  private val graphicTextGap: DoubleProperty = SimpleStyleableDoubleProperty(
	GRAPHIC_TEXT_GAP,
	this,
	"graphicTextGap",
	4.0
  )

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
  /* *************************************************************************
     *                                                                         *
     * Methods                                                                 *
     *                                                                         *
     **************************************************************************/
  /** {@inheritDoc}  */
  override fun createDefaultSkin(): Skin<*> = MyTooltipSkin(this)



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
	text?.let { setText(it) }
	bridge = MyToolTipCSSBridge(this)
	PopupWindowHelper.getContent(this).setAll(bridge)
	styleClass.setAll("tooltip")
  }

  /**
   * {@inheritDoc}
   * @since JavaFX 8.0
   */
  override fun getCssMetaData(): List<CssMetaData<out Styleable, *>> = classCssMetaData

  override fun getStyleableParent(): Styleable {
	return if (BEHAVIOR.hoveredNode == null) {
	  super.getStyleableParent()
	} else BEHAVIOR.hoveredNode!!
  }

  /* *************************************************************************
     *                                                                         *
     * Support classes                                                         *
     *                                                                         *
     **************************************************************************/
  private class MyToolTipCSSBridge internal constructor(override val popupControl: MyTooltip): MyPopUpCSSBridge(popupControl) {
	init {
	  accessibleRole = TOOLTIP
	}
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
	private var activatedTooltip: MyTooltip? = null

	/**
	 * The tooltip that is currently visible. There can only be one
	 * of these at a time.
	 */
	private var visibleTooltip: MyTooltip? = null

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
	  val t = hoveredNode!!.properties[TOOLTIP_PROP_KEY] as MyTooltip?
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
		val t = source.properties[TOOLTIP_PROP_KEY] as MyTooltip?
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
	private val KILL_HANDLER = EventHandler { _: MouseEvent ->
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

	fun install(node: Node?, t: MyTooltip) {
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
	  val t = node.properties[TOOLTIP_PROP_KEY] as MyTooltip?
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
	fun install(node: Node?, t: MyTooltip) = BEHAVIOR.install(node, t)

	/**
	 * Removes the association of the given [Tooltip] on the specified
	 * [Node]. Hence hovering on the node will no longer result in showing of the
	 * tooltip.
	 * @param node the node
	 * @param t the tooltip
	 * @see Tooltip
	 */
	fun uninstall(node: Node?, @Suppress("UNUSED_PARAMETER") t: MyTooltip?) = BEHAVIOR.uninstall(node)

	/* *************************************************************************
     *                                                                         *
     *                         Stylesheet Handling                             *
     *                                                                         *
     **************************************************************************/

	private val FONT: CssMetaData<MyToolTipCSSBridge, Font> = object: FontCssMetaData<MyToolTipCSSBridge>(
	  "-fx-font",
	  Font.getDefault()
	) {
	  override fun isSettable(cssBridge: MyToolTipCSSBridge): Boolean = !cssBridge.popupControl.fontProperty().isBound

	  @Suppress("UNCHECKED_CAST")
	  override fun getStyleableProperty(cssBridge: MyToolTipCSSBridge) =
		cssBridge.popupControl.fontProperty() as StyleableProperty<Font>
	}
	private val TEXT_ALIGNMENT: CssMetaData<MyToolTipCSSBridge, TextAlignment> = object: CssMetaData<MyToolTipCSSBridge, TextAlignment>(
	  "-fx-text-alignment",
	  EnumConverter(
		TextAlignment::class.java
	  ),
	  LEFT
	) {
	  override fun isSettable(cssBridge: MyToolTipCSSBridge): Boolean {
		return !cssBridge.popupControl.textAlignmentProperty().isBound
	  }

	  @Suppress("UNCHECKED_CAST")
	  override fun getStyleableProperty(cssBridge: MyToolTipCSSBridge) =
		cssBridge.popupControl.textAlignmentProperty() as StyleableProperty<TextAlignment>
	}
	private val TEXT_OVERRUN: CssMetaData<MyToolTipCSSBridge, OverrunStyle> = object: CssMetaData<MyToolTipCSSBridge, OverrunStyle>(
	  "-fx-text-overrun",
	  EnumConverter(
		OverrunStyle::class.java
	  ),
	  ELLIPSIS
	) {
	  override fun isSettable(cssBridge: MyToolTipCSSBridge): Boolean = !cssBridge.popupControl.textOverrunProperty().isBound

	  @Suppress("UNCHECKED_CAST")
	  override fun getStyleableProperty(cssBridge: MyToolTipCSSBridge) =
		cssBridge.popupControl.textOverrunProperty() as StyleableProperty<OverrunStyle>
	}
	private val WRAP_TEXT: CssMetaData<MyToolTipCSSBridge, Boolean> = object: CssMetaData<MyToolTipCSSBridge, Boolean>(
	  "-fx-wrap-text",
	  BooleanConverter.getInstance(),
	  java.lang.Boolean.FALSE
	) {
	  override fun isSettable(cssBridge: MyToolTipCSSBridge): Boolean = !cssBridge.popupControl.wrapTextProperty().isBound

	  @Suppress("UNCHECKED_CAST")
	  override fun getStyleableProperty(cssBridge: MyToolTipCSSBridge) =
		cssBridge.popupControl.wrapTextProperty() as StyleableProperty<Boolean>
	}
	private val GRAPHIC: CssMetaData<MyToolTipCSSBridge, String> = object: CssMetaData<MyToolTipCSSBridge, String>(
	  "-fx-graphic",
	  StringConverter.getInstance()
	) {
	  override fun isSettable(cssBridge: MyToolTipCSSBridge) = !cssBridge.popupControl.graphicProperty().isBound

	  override fun getStyleableProperty(cssBridge: MyToolTipCSSBridge) = cssBridge.popupControl.imageUrlProperty()
	}
	private val CONTENT_DISPLAY: CssMetaData<MyToolTipCSSBridge, ContentDisplay> = object: CssMetaData<MyToolTipCSSBridge, ContentDisplay>(
	  "-fx-content-display",
	  EnumConverter(
		ContentDisplay::class.java
	  ),
	  ContentDisplay.LEFT
	) {
	  override fun isSettable(cssBridge: MyToolTipCSSBridge) = !cssBridge.popupControl.contentDisplayProperty().isBound

	  @Suppress("UNCHECKED_CAST")
	  override fun getStyleableProperty(cssBridge: MyToolTipCSSBridge) =
		cssBridge.popupControl.contentDisplayProperty() as StyleableProperty<ContentDisplay>
	}
	private val GRAPHIC_TEXT_GAP: CssMetaData<MyToolTipCSSBridge, Number> = object: CssMetaData<MyToolTipCSSBridge, Number>(
	  "-fx-graphic-text-gap",
	  SizeConverter.getInstance(),
	  4.0
	) {
	  override fun isSettable(cssBridge: MyToolTipCSSBridge) = !cssBridge.popupControl.graphicTextGapProperty().isBound

	  @Suppress("UNCHECKED_CAST")
	  override fun getStyleableProperty(cssBridge: MyToolTipCSSBridge) =
		cssBridge.popupControl.graphicTextGapProperty() as StyleableProperty<Number>
	}
	private val SHOW_DELAY: CssMetaData<MyToolTipCSSBridge, Duration> = object: CssMetaData<MyToolTipCSSBridge, Duration>(
	  "-fx-show-delay",
	  DurationConverter.getInstance(),
	  Duration(1000.0)
	) {
	  override fun isSettable(cssBridge: MyToolTipCSSBridge) = !cssBridge.popupControl.showDelayProperty().isBound

	  @Suppress("UNCHECKED_CAST")
	  override fun getStyleableProperty(cssBridge: MyToolTipCSSBridge) =
		cssBridge.popupControl.showDelayProperty() as StyleableProperty<Duration>
	}
	private val SHOW_DURATION: CssMetaData<MyToolTipCSSBridge, Duration> = object: CssMetaData<MyToolTipCSSBridge, Duration>(
	  "-fx-show-duration",
	  DurationConverter.getInstance(),
	  Duration(5000.0)
	) {
	  override fun isSettable(cssBridge: MyToolTipCSSBridge) = !cssBridge.popupControl.showDurationProperty().isBound

	  @Suppress("UNCHECKED_CAST")
	  override fun getStyleableProperty(cssBridge: MyToolTipCSSBridge) =
		cssBridge.popupControl.showDurationProperty() as StyleableProperty<Duration>
	}
	private val HIDE_DELAY: CssMetaData<MyToolTipCSSBridge, Duration> = object: CssMetaData<MyToolTipCSSBridge, Duration>(
	  "-fx-hide-delay",
	  DurationConverter.getInstance(),
	  Duration(200.0)
	) {
	  override fun isSettable(cssBridge: MyToolTipCSSBridge): Boolean = !cssBridge.popupControl.hideDelayProperty().isBound

	  @Suppress("UNCHECKED_CAST")
	  override fun getStyleableProperty(cssBridge: MyToolTipCSSBridge) =
		cssBridge.popupControl.hideDelayProperty() as StyleableProperty<Duration>
	}

	/**
	 * Gets the `CssMetaData` associated with this class, which may include the
	 * `CssMetaData` of its superclasses.
	 * @return the `CssMetaData`
	 * @since JavaFX 8.0
	 */
	val classCssMetaData: List<CssMetaData<out Styleable, *>> by lazy {

	  val styleables: MutableList<CssMetaData<out Styleable, *>> = ArrayList(
		MyPopupControl.classCssMetaData
	  )
	  styleables.addAll(
		FONT,
		TEXT_ALIGNMENT,
		TEXT_OVERRUN,
		WRAP_TEXT,
		GRAPHIC,
		CONTENT_DISPLAY,
		GRAPHIC_TEXT_GAP,
		SHOW_DELAY,
		SHOW_DURATION,
		HIDE_DELAY
	  )
	  Collections.unmodifiableList(styleables)
	}

  }


//  override val bridge = MyPopUpCSSBridge()
}



