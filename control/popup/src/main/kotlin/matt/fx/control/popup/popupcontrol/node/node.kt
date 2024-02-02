package matt.fx.control.popup.popupcontrol.node

import com.sun.javafx.application.PlatformImpl
import com.sun.javafx.logging.PlatformLogger.Level.FINEST
import com.sun.javafx.scene.NodeHelper
import com.sun.javafx.scene.control.Logging
import com.sun.javafx.stage.PopupWindowHelper
import javafx.application.Application
import javafx.beans.property.DoubleProperty
import javafx.beans.property.DoublePropertyBase
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ObjectPropertyBase
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableSet
import javafx.css.CssMetaData
import javafx.css.PseudoClass
import javafx.css.Styleable
import javafx.css.StyleableProperty
import javafx.css.StyleableStringProperty
import javafx.css.converter.StringConverter
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.PopupControl
import javafx.scene.control.Skin
import javafx.scene.control.Skinnable
import javafx.stage.PopupWindow
import javafx.stage.PopupWindow.AnchorLocation.CONTENT_TOP_LEFT
import matt.fx.base.rewrite.ReWrittenFxClass
import matt.fx.control.popup.popupcontrol.node.bridge.MyPopUpCSSBridge
import matt.lang.anno.Open
import matt.lang.go
import java.util.Collections

/**
 * An extension of PopupWindow that allows for CSS styling.
 * @since JavaFX 2.0
 */

/*

Why Matt had to override this class:

because the bridge thing is the source of all my tooltip style issues, and it is package private

this is the original reason, but not the only

* */

@ReWrittenFxClass(PopupControl::class)
open class MyPopupControl: PopupWindow(), Skinnable, Styleable {

    /**
     * We need a special root node, except we can't replace the special
     * root node already in the PopupControl. So we'll set our own
     * special almost-root node that is a child of the root.
     *
     * This special root node is responsible for mapping the id, styleClass,
     * and style defined on the PopupControl such that CSS will read the
     * values from the PopupControl, and then apply CSS state to that
     * special node. The node will then be able to pass cssSet calls
     * along, such that any subclass of PopupControl will be able to
     * use the Styleable properties  and we'll be able to style it from
     * CSS, in such a way that it participates and applies to the skin,
     * exactly the way that normal Skin's work for normal Controls.
     * @since JavaFX 2.1
     */
    protected var bridge: MyPopUpCSSBridge = MyPopUpCSSBridge(this)
    // TODO the fact that PopupWindow uses a group for auto-moving things
    // around means that the scene resize semantics don't work if the
    // child is a resizable. I will need to replicate those semantics
    // here sometime, such that if the Skin provides a resizable, it is
    // given to match the popup window's width & height.
    /**
     * The id of this `PopupControl`. This simple string identifier is useful for
     * finding a specific Node within the scene graph. While the id of a Node
     * should be unique within the scene graph, this uniqueness is not enforced.
     * This is analogous to the "id" attribute on an HTML element
     * ([CSS ID Specification](http://www.w3.org/TR/CSS21/syndata.html#value-def-identifier)).
     *
     * @return the id property
     * @defaultValue `null`
     */
    fun idProperty(): StringProperty = bridge.idProperty()

    /**
     * Sets the id of this `PopupControl`. This simple string identifier is useful for
     * finding a specific Node within the scene graph. While the id of a Node
     * should be unique within the scene graph, this uniqueness is not enforced.
     * This is analogous to the "id" attribute on an HTML element
     * ([CSS ID Specification](http://www.w3.org/TR/CSS21/syndata.html#value-def-identifier)).
     *
     * @param value  the id assigned to this `PopupControl` using the `setId`
     * method or `null`, if no id has been assigned.
     * @defaultValue `null`
     */
    fun setId(value: String) = idProperty().set(value)

    final /**
     * The id of this `PopupControl`. This simple string identifier is useful for
     * finding a specific Node within the scene graph. While the id of a Node
     * should be unique within the scene graph, this uniqueness is not enforced.
     * This is analogous to the "id" attribute on an HTML element
     * ([CSS ID Specification](http://www.w3.org/TR/CSS21/syndata.html#value-def-identifier)).
     *
     * @return the id assigned to this `PopupControl` using the `setId`
     * method or `null`, if no id has been assigned.
     * @defaultValue `null`
     */
    override fun getId(): String? = idProperty().get()

    final /**
     * Returns the list of String identifiers that make up the styleClass
     * for this PopupControl.
     */
    override fun getStyleClass(): ObservableList<String> = bridge.styleClass

    fun setStyle(value: String) {
        styleProperty().set(value)
    }

    final override fun getStyle(): String = styleProperty().get()

    /**
     * A string representation of the CSS style associated with this
     * specific `PopupControl`. This is analogous to the "style" attribute of an
     * HTML element. Note that, like the HTML style attribute, this
     * variable contains style properties and values and not the
     * selector portion of a style rule.
     *
     * A value of `null` is implicitly converted to an empty `String`.
     *
     * @return the `style` property
     * @defaultValue `null`
     */
    fun styleProperty(): StringProperty = bridge.styleProperty()

    final /**
     * Skin is responsible for rendering this `PopupControl`. From the
     * perspective of the `PopupControl`, the `Skin` is a black box.
     * It listens and responds to changes in state in a `PopupControl`.
     *
     *
     * There is a one-to-one relationship between a `PopupControl` and its
     * `Skin`. Every `Skin` maintains a back reference to the
     * `PopupControl`.
     *
     *
     * A skin may be `null`.
     */
    override fun skinProperty(): ObjectProperty<Skin<*>> = skin

    final override fun setSkin(value: Skin<*>) {
        skinProperty().value = value
    }

    final override fun getSkin(): Skin<*>? = skinProperty().value

    private val skin: ObjectProperty<Skin<*>> = object: ObjectPropertyBase<Skin<*>>() {
        // We store a reference to the oldValue so that we can handle
        // changes in the skin properly in the case of binding. This is
        // only needed because invalidated() does not currently take
        // a reference to the old value.
        private var oldValue: Skin<*>? = null
        override fun set(v: Skin<*>?) {
            if (if (v == null) oldValue == null else oldValue != null && v.javaClass == oldValue!!.javaClass) return
            super.set(v)
        }

        override fun invalidated() {
            val skin = get()

            // Collect the name of the currently installed skin class. We do this
            // so that subsequent updates from CSS to the same skin class will not
            // result in reinstalling the skin
            currentSkinClassName = skin?.javaClass?.name

            // if someone calls setSkin, we need to make it look like they
            // called set on skinClassName in order to keep CSS from overwriting
            // the skin.
            skinClassNameProperty().set(currentSkinClassName)

            // Let CSS know that this property has been manually changed
            // Dispose of the old skin
            if (oldValue != null) {
                oldValue!!.dispose()
            }

            // Get the new value, and save it off as the new oldValue
            oldValue = value
            prefWidthCache = -1.0
            prefHeightCache = -1.0
            minWidthCache = -1.0
            minHeightCache = -1.0
            maxWidthCache = -1.0
            maxHeightCache = -1.0
            skinSizeComputed = false
            val n: Node? = skinNode
            n?.go {
                bridge.children.setAll(it)
            } ?: bridge.children.clear()

            // let the new skin modify this control
            skin?.install()

            // calling NodeHelper.reapplyCSS() as the styleable properties may now
            // be different, as we will now be able to return styleable properties
            // belonging to the skin. If NodeHelper.reapplyCSS() is not called, the
            // getCssMetaData() method is never called, so the
            // skin properties are never exposed.
            NodeHelper.reapplyCSS(bridge)

            // DEBUG: Log that we've changed the skin
            val logger = Logging.getControlsLogger()
            if (logger.isLoggable(FINEST)) {
                logger.finest("Stored skin[$value] on $this")
            }
        }

        override fun getBean(): Any = this@MyPopupControl

        override fun getName(): String = "skin"
    }

    /**
     * Keeps a reference to the name of the class currently acting as the skin.
     */
    private var currentSkinClassName: String? = null

    /**
     * A property that acts as a proxy between the skin property and css.
     */
    private var skinClassName: StringProperty? = null
    private fun skinClassNameProperty(): StringProperty {
        if (skinClassName == null) {
            skinClassName = object: StyleableStringProperty() {
                override fun set(v: String?) {
                    // do not allow the skin to be set to null through CSS
                    if (v.isNullOrEmpty() || v == get()) return
                    super.set(v)
                }

                public override fun invalidated() {

                    //
                    // if the current skin is not null, then
                    // see if then check to see if the current skin's class name
                    // is the same as skinClassName. If it is, then there is
                    // no need to load the skin class. Note that the only time
                    // this would be the case is if someone called setSkin since
                    // the skin would be set ahead of the skinClassName
                    // (skinClassName is set from the skinProperty's invalidated
                    // method, so the skin would be set, then the skinClassName).
                    // If the skinClassName is set first (via CSS), then this
                    // invalidated method won't get called unless the value
                    // has changed (thus, we won't reload the same skin).
                    //
                    if (get() != null) {
                        if (get() != currentSkinClassName) {
                            controlLoadSkinClass.invoke(this@MyPopupControl, get())
                            //			  Control.loadSkinClass(this@MyPopupControl, get())
                        }
                        // CSS should not set skin to null
                        //                    } else {
                        //                        setSkin(null);
                    }
                }

                override fun getBean(): Any = this@MyPopupControl

                override fun getName(): String = "skinClassName"

                override fun getCssMetaData(): CssMetaData<MyPopUpCSSBridge, String> = SKIN
            }
        }
        return skinClassName!!
    }

    private val skinNode: Node?
        /**
         * Gets the Skin's node, or returns null if there is no Skin.
         * Convenience method for getting the node of the skin. This is null-safe,
         * meaning if skin is null then it will return null instead of throwing
         * a NullPointerException.
         *
         * @return The Skin's node, or null.
         */
        get() = if (getSkin() == null) null else getSkin()!!.node

    /**
     * Property for overriding the control's computed minimum width.
     * This should only be set if the control's internally computed minimum width
     * doesn't meet the application's layout needs.
     *
     *
     * Defaults to the `USE_COMPUTED_SIZE` flag, which means that
     * `getMinWidth(forHeight)` will return the control's internally
     * computed minimum width.
     *
     *
     * Setting this value to the `USE_PREF_SIZE` flag will cause
     * `getMinWidth(forHeight)` to return the control's preferred width,
     * enabling applications to easily restrict the resizability of the control.
     */
    private var minWidth: DoubleProperty? = null

    /**
     * Property for overriding the control's computed minimum width.
     * This should only be set if the control's internally computed minimum width
     * doesn't meet the application's layout needs.
     *
     *
     * Defaults to the `USE_COMPUTED_SIZE` flag, which means that
     * `getMinWidth(forHeight)` will return the control's internally
     * computed minimum width.
     *
     *
     * Setting this value to the `USE_PREF_SIZE` flag will cause
     * `getMinWidth(forHeight)` to return the control's preferred width,
     * enabling applications to easily restrict the resizability of the control.
     * @param value the minimum width
     */
    fun setMinWidth(value: Double) = minWidthProperty().set(value)

    /**
     * Property for overriding the control's computed minimum width.
     * This should only be set if the control's internally computed minimum width
     * doesn't meet the application's layout needs.
     *
     *
     * Defaults to the `USE_COMPUTED_SIZE` flag, which means that
     * `getMinWidth(forHeight)` will return the control's internally
     * computed minimum width.
     *
     *
     * Setting this value to the `USE_PREF_SIZE` flag will cause
     * `getMinWidth(forHeight)` to return the control's preferred width,
     * enabling applications to easily restrict the resizability of the control.
     * @return the minimum width
     */
    fun getMinWidth(): Double = if (minWidth == null) USE_COMPUTED_SIZE else minWidth!!.get()

    fun minWidthProperty(): DoubleProperty {
        if (minWidth == null) {
            minWidth = object: DoublePropertyBase(USE_COMPUTED_SIZE) {
                public override fun invalidated() {
                    if (isShowing) bridge.requestLayout()
                }

                override fun getBean(): Any = this@MyPopupControl

                override fun getName(): String = "minWidth"
            }
        }
        return minWidth!!
    }

    /**
     * Property for overriding the control's computed minimum height.
     * This should only be set if the control's internally computed minimum height
     * doesn't meet the application's layout needs.
     *
     *
     * Defaults to the `USE_COMPUTED_SIZE` flag, which means that
     * `getMinHeight(forWidth)` will return the control's internally
     * computed minimum height.
     *
     *
     * Setting this value to the `USE_PREF_SIZE` flag will cause
     * `getMinHeight(forWidth)` to return the control's preferred height,
     * enabling applications to easily restrict the resizability of the control.
     *
     */
    private var minHeight: DoubleProperty? = null

    /**
     * Property for overriding the control's computed minimum height.
     * This should only be set if the control's internally computed minimum height
     * doesn't meet the application's layout needs.
     *
     *
     * Defaults to the `USE_COMPUTED_SIZE` flag, which means that
     * `getMinHeight(forWidth)` will return the control's internally
     * computed minimum height.
     *
     *
     * Setting this value to the `USE_PREF_SIZE` flag will cause
     * `getMinHeight(forWidth)` to return the control's preferred height,
     * enabling applications to easily restrict the resizability of the control.
     *
     * @param value the minimum height
     */
    fun setMinHeight(value: Double) {
        minHeightProperty().set(value)
    }

    /**
     * Property for overriding the control's computed minimum height.
     * This should only be set if the control's internally computed minimum height
     * doesn't meet the application's layout needs.
     *
     *
     * Defaults to the `USE_COMPUTED_SIZE` flag, which means that
     * `getMinHeight(forWidth)` will return the control's internally
     * computed minimum height.
     *
     *
     * Setting this value to the `USE_PREF_SIZE` flag will cause
     * `getMinHeight(forWidth)` to return the control's preferred height,
     * enabling applications to easily restrict the resizability of the control.
     *
     * @return the minimum height
     */
    fun getMinHeight(): Double = if (minHeight == null) USE_COMPUTED_SIZE else minHeight!!.get()

    fun minHeightProperty(): DoubleProperty {
        if (minHeight == null) {
            minHeight = object: DoublePropertyBase(USE_COMPUTED_SIZE) {
                public override fun invalidated() {
                    if (isShowing) bridge.requestLayout()
                }

                override fun getBean(): Any = this@MyPopupControl

                override fun getName(): String = "minHeight"
            }
        }
        return minHeight!!
    }

    /**
     * Convenience method for overriding the control's computed minimum width and height.
     * This should only be called if the control's internally computed minimum size
     * doesn't meet the application's layout needs.
     *
     * @see .setMinWidth
     *
     * @see .setMinHeight
     *
     * @param minWidth  the override value for minimum width
     * @param minHeight the override value for minimum height
     */
    fun setMinSize(minWidth: Double, minHeight: Double) {
        setMinWidth(minWidth)
        setMinHeight(minHeight)
    }

    /**
     * Property for overriding the control's computed preferred width.
     * This should only be set if the control's internally computed preferred width
     * doesn't meet the application's layout needs.
     *
     *
     * Defaults to the `USE_COMPUTED_SIZE` flag, which means that
     * `getPrefWidth(forHeight)` will return the control's internally
     * computed preferred width.
     */
    private var prefWidth: DoubleProperty? = null

    /**
     * Property for overriding the control's computed preferred width.
     * This should only be set if the control's internally computed preferred width
     * doesn't meet the application's layout needs.
     *
     *
     * Defaults to the `USE_COMPUTED_SIZE` flag, which means that
     * `getPrefWidth(forHeight)` will return the control's internally
     * computed preferred width.
     * @param value the preferred width
     */
    fun setPrefWidth(value: Double) {
        prefWidthProperty().set(value)
    }

    /**
     * Property for overriding the control's computed preferred width.
     * This should only be set if the control's internally computed preferred width
     * doesn't meet the application's layout needs.
     *
     *
     * Defaults to the `USE_COMPUTED_SIZE` flag, which means that
     * `getPrefWidth(forHeight)` will return the control's internally
     * computed preferred width.
     * @return the preferred width
     */
    fun getPrefWidth(): Double = if (prefWidth == null) USE_COMPUTED_SIZE else prefWidth!!.get()

    fun prefWidthProperty(): DoubleProperty {
        if (prefWidth == null) {
            prefWidth = object: DoublePropertyBase(USE_COMPUTED_SIZE) {
                public override fun invalidated() {
                    if (isShowing) bridge.requestLayout()
                }

                override fun getBean(): Any = this@MyPopupControl

                override fun getName(): String = "prefWidth"
            }
        }
        return prefWidth!!
    }

    /**
     * Property for overriding the control's computed preferred height.
     * This should only be set if the control's internally computed preferred height
     * doesn't meet the application's layout needs.
     *
     *
     * Defaults to the `USE_COMPUTED_SIZE` flag, which means that
     * `getPrefHeight(forWidth)` will return the control's internally
     * computed preferred width.
     *
     */
    private var prefHeight: DoubleProperty? = null

    /**
     * Property for overriding the control's computed preferred height.
     * This should only be set if the control's internally computed preferred height
     * doesn't meet the application's layout needs.
     *
     *
     * Defaults to the `USE_COMPUTED_SIZE` flag, which means that
     * `getPrefHeight(forWidth)` will return the control's internally
     * computed preferred width.
     *
     * @param value the preferred height
     */
    fun setPrefHeight(value: Double) = prefHeightProperty().set(value)

    /**
     * Property for overriding the control's computed preferred height.
     * This should only be set if the control's internally computed preferred height
     * doesn't meet the application's layout needs.
     *
     *
     * Defaults to the `USE_COMPUTED_SIZE` flag, which means that
     * `getPrefHeight(forWidth)` will return the control's internally
     * computed preferred width.
     *
     * @return the preferred height
     */
    fun getPrefHeight(): Double = if (prefHeight == null) USE_COMPUTED_SIZE else prefHeight!!.get()

    fun prefHeightProperty(): DoubleProperty {
        if (prefHeight == null) {
            prefHeight = object: DoublePropertyBase(USE_COMPUTED_SIZE) {
                public override fun invalidated() {
                    if (isShowing) bridge.requestLayout()
                }

                override fun getBean(): Any = this@MyPopupControl

                override fun getName(): String = "prefHeight"
            }
        }
        return prefHeight!!
    }

    /**
     * Convenience method for overriding the control's computed preferred width and height.
     * This should only be called if the control's internally computed preferred size
     * doesn't meet the application's layout needs.
     *
     * @see .setPrefWidth
     *
     * @see .setPrefHeight
     *
     * @param prefWidth the override value for preferred width
     * @param prefHeight the override value for preferred height
     */
    fun setPrefSize(prefWidth: Double, prefHeight: Double) {
        setPrefWidth(prefWidth)
        setPrefHeight(prefHeight)
    }

    /**
     * Property for overriding the control's computed maximum width.
     * This should only be set if the control's internally computed maximum width
     * doesn't meet the application's layout needs.
     *
     *
     * Defaults to the `USE_COMPUTED_SIZE` flag, which means that
     * `getMaxWidth(forHeight)` will return the control's internally
     * computed maximum width.
     *
     *
     * Setting this value to the `USE_PREF_SIZE` flag will cause
     * `getMaxWidth(forHeight)` to return the control's preferred width,
     * enabling applications to easily restrict the resizability of the control.
     */
    private var maxWidth: DoubleProperty? = null

    /**
     * Property for overriding the control's computed maximum width.
     * This should only be set if the control's internally computed maximum width
     * doesn't meet the application's layout needs.
     *
     *
     * Defaults to the `USE_COMPUTED_SIZE` flag, which means that
     * `getMaxWidth(forHeight)` will return the control's internally
     * computed maximum width.
     *
     *
     * Setting this value to the `USE_PREF_SIZE` flag will cause
     * `getMaxWidth(forHeight)` to return the control's preferred width,
     * enabling applications to easily restrict the resizability of the control.
     * @param value the maximum width
     */
    fun setMaxWidth(value: Double) {
        maxWidthProperty().set(value)
    }

    /**
     * Property for overriding the control's computed maximum width.
     * This should only be set if the control's internally computed maximum width
     * doesn't meet the application's layout needs.
     *
     *
     * Defaults to the `USE_COMPUTED_SIZE` flag, which means that
     * `getMaxWidth(forHeight)` will return the control's internally
     * computed maximum width.
     *
     *
     * Setting this value to the `USE_PREF_SIZE` flag will cause
     * `getMaxWidth(forHeight)` to return the control's preferred width,
     * enabling applications to easily restrict the resizability of the control.
     * @return the maximum width
     */
    fun getMaxWidth(): Double = maxWidth?.get() ?: USE_COMPUTED_SIZE

    fun maxWidthProperty(): DoubleProperty {
        if (maxWidth == null) {
            maxWidth = object: DoublePropertyBase(USE_COMPUTED_SIZE) {
                public override fun invalidated() {
                    if (isShowing) bridge.requestLayout()
                }

                override fun getBean(): Any = this@MyPopupControl

                override fun getName(): String = "maxWidth"
            }
        }
        return maxWidth!!
    }

    /**
     * Property for overriding the control's computed maximum height.
     * This should only be set if the control's internally computed maximum height
     * doesn't meet the application's layout needs.
     *
     *
     * Defaults to the `USE_COMPUTED_SIZE` flag, which means that
     * `getMaxHeight(forWidth)` will return the control's internally
     * computed maximum height.
     *
     *
     * Setting this value to the `USE_PREF_SIZE` flag will cause
     * `getMaxHeight(forWidth)` to return the control's preferred height,
     * enabling applications to easily restrict the resizability of the control.
     *
     */
    private var maxHeight: DoubleProperty? = null

    /**
     * Property for overriding the control's computed maximum height.
     * This should only be set if the control's internally computed maximum height
     * doesn't meet the application's layout needs.
     *
     *
     * Defaults to the `USE_COMPUTED_SIZE` flag, which means that
     * `getMaxHeight(forWidth)` will return the control's internally
     * computed maximum height.
     *
     *
     * Setting this value to the `USE_PREF_SIZE` flag will cause
     * `getMaxHeight(forWidth)` to return the control's preferred height,
     * enabling applications to easily restrict the resizability of the control.
     *
     * @param value the maximum height
     */
    fun setMaxHeight(value: Double) = maxHeightProperty().set(value)

    /**
     * Property for overriding the control's computed maximum height.
     * This should only be set if the control's internally computed maximum height
     * doesn't meet the application's layout needs.
     *
     *
     * Defaults to the `USE_COMPUTED_SIZE` flag, which means that
     * `getMaxHeight(forWidth)` will return the control's internally
     * computed maximum height.
     *
     *
     * Setting this value to the `USE_PREF_SIZE` flag will cause
     * `getMaxHeight(forWidth)` to return the control's preferred height,
     * enabling applications to easily restrict the resizability of the control.
     *
     * @return the maximum height
     */
    fun getMaxHeight(): Double = maxHeight?.get() ?: USE_COMPUTED_SIZE

    fun maxHeightProperty(): DoubleProperty {
        if (maxHeight == null) {
            maxHeight = object: DoublePropertyBase(USE_COMPUTED_SIZE) {
                public override fun invalidated() {
                    if (isShowing) bridge.requestLayout()
                }

                override fun getBean(): Any = this@MyPopupControl

                override fun getName(): String = "maxHeight"
            }
        }
        return maxHeight!!
    }

    /**
     * Convenience method for overriding the control's computed maximum width and height.
     * This should only be called if the control's internally computed maximum size
     * doesn't meet the application's layout needs.
     *
     * @see .setMaxWidth
     *
     * @see .setMaxHeight
     *
     * @param maxWidth  the override value for maximum width
     * @param maxHeight the override value for maximum height
     */
    fun setMaxSize(maxWidth: Double, maxHeight: Double) {
        setMaxWidth(maxWidth)
        setMaxHeight(maxHeight)
    }

    /**
     * Cached prefWidth, prefHeight, minWidth, minHeight. These
     * results are repeatedly sought during the layout pass,
     * and caching the results leads to a significant decrease
     * in overhead.
     */
    private var prefWidthCache = -1.0
    private var prefHeightCache = -1.0
    private var minWidthCache = -1.0
    private var minHeightCache = -1.0
    private var maxWidthCache = -1.0
    private var maxHeightCache = -1.0
    private var skinSizeComputed = false

    /**
     * Called during layout to determine the minimum width for this node.
     * Returns the value from `minWidth(forHeight)` unless
     * the application overrode the minimum width by setting the minWidth property.
     *
     * @param height the height
     * @see .setMinWidth
     *
     * @return the minimum width that this node should be resized to during layout
     */
    fun minWidth(height: Double): Double {
        val theOverride = getMinWidth()
        return if (theOverride == USE_COMPUTED_SIZE) {
            if (minWidthCache == -1.0) minWidthCache = recalculateMinWidth(height)
            minWidthCache
        } else if (theOverride == USE_PREF_SIZE) {
            prefWidth(height)
        } else theOverride
    }

    /**
     * Called during layout to determine the minimum height for this node.
     * Returns the value from `minHeight(forWidth)` unless
     * the application overrode the minimum height by setting the minHeight property.
     *
     * @param width The width
     * @see .setMinHeight
     *
     * @return the minimum height that this node should be resized to during layout
     */
    fun minHeight(width: Double): Double {
        val theOverride = getMinHeight()
        if (theOverride == USE_COMPUTED_SIZE) {
            if (minHeightCache == -1.0) minHeightCache = recalculateMinHeight(width)
            return minHeightCache
        } else if (theOverride == USE_PREF_SIZE) {
            return prefHeight(width)
        }
        return theOverride
    }

    /**
     * Called during layout to determine the preferred width for this node.
     * Returns the value from `prefWidth(forHeight)` unless
     * the application overrode the preferred width by setting the prefWidth property.
     *
     * @param height the height
     * @see .setPrefWidth
     *
     * @return the preferred width that this node should be resized to during layout
     */
    fun prefWidth(height: Double): Double {
        val theOverride = getPrefWidth()
        if (theOverride == USE_COMPUTED_SIZE) {
            if (prefWidthCache == -1.0) prefWidthCache = recalculatePrefWidth(height)
            return prefWidthCache
        } else if (theOverride == USE_PREF_SIZE) {
            return prefWidth(height)
        }
        return theOverride
    }

    /**
     * Called during layout to determine the preferred height for this node.
     * Returns the value from `prefHeight(forWidth)` unless
     * the application overrode the preferred height by setting the prefHeight property.
     *
     * @param width the width
     * @see .setPrefHeight
     *
     * @return the preferred height that this node should be resized to during layout
     */
    fun prefHeight(width: Double): Double {
        val theOverride = getPrefHeight()
        if (theOverride == USE_COMPUTED_SIZE) {
            if (prefHeightCache == -1.0) prefHeightCache = recalculatePrefHeight(width)
            return prefHeightCache
        } else if (theOverride == USE_PREF_SIZE) {
            return prefHeight(width)
        }
        return theOverride
    }

    /**
     * Called during layout to determine the maximum width for this node.
     * Returns the value from `maxWidth(forHeight)` unless
     * the application overrode the maximum width by setting the maxWidth property.
     *
     * @param height the height
     * @see .setMaxWidth
     *
     * @return the maximum width that this node should be resized to during layout
     */
    fun maxWidth(height: Double): Double {
        val theOverride = getMaxWidth()
        if (theOverride == USE_COMPUTED_SIZE) {
            if (maxWidthCache == -1.0) maxWidthCache = recalculateMaxWidth(height)
            return maxWidthCache
        } else if (theOverride == USE_PREF_SIZE) {
            return prefWidth(height)
        }
        return theOverride
    }

    /**
     * Called during layout to determine the maximum height for this node.
     * Returns the value from `maxHeight(forWidth)` unless
     * the application overrode the maximum height by setting the maxHeight property.
     *
     * @param width the width
     * @see .setMaxHeight
     *
     * @return the maximum height that this node should be resized to during layout
     */
    fun maxHeight(width: Double): Double {
        val theOverride = getMaxHeight()
        if (theOverride == USE_COMPUTED_SIZE) {
            if (maxHeightCache == -1.0) maxHeightCache = recalculateMaxHeight(width)
            return maxHeightCache
        } else if (theOverride == USE_PREF_SIZE) {
            return prefHeight(width)
        }
        return theOverride
    }

    // Implementation of the Resizable interface.
    // Because only the skin can know the min, pref, and max sizes, these
    // functions are implemented to delegate to skin. If there is no skin then
    // we simply return 0 for all the values since a Control without a Skin
    // doesn't render
    private fun recalculateMinWidth(height: Double): Double {
        recomputeSkinSize()
        return skinNode?.minWidth(height) ?: 0.0
    }

    private fun recalculateMinHeight(width: Double): Double {
        recomputeSkinSize()
        return if (skinNode == null) 0.0 else skinNode!!.minHeight(width)
    }

    private fun recalculateMaxWidth(height: Double): Double {
        recomputeSkinSize()
        return if (skinNode == null) 0.0 else skinNode!!.maxWidth(height)
    }

    private fun recalculateMaxHeight(width: Double): Double {
        recomputeSkinSize()
        return if (skinNode == null) 0.0 else skinNode!!.maxHeight(width)
    }

    private fun recalculatePrefWidth(height: Double): Double {
        recomputeSkinSize()
        return if (skinNode == null) 0.0 else skinNode!!.prefWidth(height)
    }

    private fun recalculatePrefHeight(width: Double): Double {
        recomputeSkinSize()
        return if (skinNode == null) 0.0 else skinNode!!.prefHeight(width)
    }

    private fun recomputeSkinSize() {
        if (!skinSizeComputed) {
            // RT-14094, RT-16754: We need the skins of the popup
            // and it children before the stage is visible so we
            // can calculate the popup position based on content
            // size.
            bridge.applyCss()
            skinSizeComputed = true
        }
    }
    //    public double getBaselineOffset() { return getSkinNode() == null? 0 : getSkinNode().getBaselineOffset(); }
    /**
     * Create a new instance of the default skin for this control. This is called to create a skin for the control if
     * no skin is provided via CSS `-fx-skin` or set explicitly in a sub-class with `setSkin(...)`.
     *
     * @return  new instance of default skin for this control. If null then the control will have no skin unless one
     * is provided by css.
     * @since JavaFX 8.0
     */
    protected open fun createDefaultSkin(): Skin<*>? = null

    /**
     * Creates a new empty `PopupControl`.
     */
    init {
        anchorLocation = CONTENT_TOP_LEFT
        PopupWindowHelper.getContent(this).add(bridge)
    }

    @Open
    override fun getCssMetaData(): List<CssMetaData<out Styleable, *>> = classCssMetaData

    /**
     * Used to specify that a pseudo-class of this node has changed.
     *
     * @param pseudoClass the pseudo class
     * @param active the active state
     * @see Node.pseudoClassStateChanged
     * @since JavaFX 8.0
     */
    fun pseudoClassStateChanged(pseudoClass: PseudoClass?, active: Boolean) {
        bridge.pseudoClassStateChanged(pseudoClass, active)
    }

    final /**
     * {@inheritDoc}
     * @return "PopupControl"
     * @since JavaFX 8.0
     */
    override fun getTypeSelector(): String = "PopupControl"

    @Open  /**
     * {@inheritDoc}
     *
     * A PopupControl&#39;s styles are based on the popup &quot;owner&quot; which is the
     * [ownerNode][javafx.stage.PopupWindow.getOwnerNode] or,
     * if the ownerNode is not set, the root of the
     * [ownerWindow&#39;s][javafx.stage.PopupWindow.getOwnerWindow]
     * scene. If the popup has not been shown, both ownerNode and ownerWindow will be null and `null` will be returned.
     *
     * Note that the PopupWindow&#39;s scene root is not returned because
     * there is no way to guarantee that the PopupWindow&#39;s scene root would
     * properly return the ownerNode or ownerWindow.
     *
     * @return [javafx.stage.PopupWindow.getOwnerNode], [javafx.stage.PopupWindow.getOwnerWindow],
     * or null.
     * @since JavaFX 8.0
     */
    override fun getStyleableParent(): Styleable {
        val ownerNode = ownerNode
        if (ownerNode != null) return ownerNode else {
            val ownerWindow = ownerWindow
            if (ownerWindow != null) {
                val ownerScene = ownerWindow.scene
                if (ownerScene != null) return ownerScene.root
            }
        }
        return bridge.parent
    }

    final /**
     * {@inheritDoc}
     * @since JavaFX 8.0
     */
    override fun getPseudoClassStates(): ObservableSet<PseudoClass> = FXCollections.emptyObservableSet()

    final override fun getStyleableNode(): Node = bridge


    companion object {

        val controlLoadSkinClass by lazy {
            Control::class.java.getMethod("loadSkinClass").apply {
                isAccessible = true
            }
        }

        /**
         * Sentinel value which can be passed to a control's setMinWidth(), setMinHeight(),
         * setMaxWidth() or setMaxHeight() methods to indicate that the preferred dimension
         * should be used for that max and/or min constraint.
         */
        const val USE_PREF_SIZE = Double.NEGATIVE_INFINITY

        /**
         * Sentinel value which can be passed to a control's setMinWidth(), setMinHeight(),
         * setPrefWidth(), setPrefHeight(), setMaxWidth(), setMaxHeight() methods
         * to reset the control's size constraint back to it's intrinsic size returned
         * by computeMinWidth(), computeMinHeight(), computePrefWidth(), computePrefHeight(),
         * computeMaxWidth(), or computeMaxHeight().
         */
        const val USE_COMPUTED_SIZE = -1.0

        init {
            // Ensures that the default application user agent stylesheet is loaded
            if (Application.getUserAgentStylesheet() == null) {
                PlatformImpl.setDefaultPlatformUserAgentStylesheet()
            }
        }

	/* *************************************************************************
	 *                                                                         *
	 *                         StyleSheet Handling                             *
	 *                                                                         *
	 **************************************************************************/
        private val SKIN: CssMetaData<MyPopUpCSSBridge, String> = object: CssMetaData<MyPopUpCSSBridge, String>(
            "-fx-skin",
            StringConverter.getInstance()
        ) {
            override fun isSettable(cssBridge: MyPopUpCSSBridge): Boolean = !cssBridge.popupControl.skinProperty().isBound

            @Suppress("UNCHECKED_CAST")
            override fun getStyleableProperty(cssBridge: MyPopUpCSSBridge): StyleableProperty<String> =
                cssBridge.popupControl.skinClassNameProperty() as StyleableProperty<String>
        }

        /**
         * Gets the `CssMetaData` associated with this class, which may include the
         * `CssMetaData` of its superclasses.
         * @return the `CssMetaData`
         * @since JavaFX 8.0
         */
        val classCssMetaData: List<CssMetaData<out Styleable, *>> by lazy {
            val styleables: ArrayList<CssMetaData<out Styleable, *>> = ArrayList()
            Collections.addAll(
                styleables,
                SKIN
            )
            Collections.unmodifiableList(styleables)
        }

    }


    internal fun createDefaultSkinInternal() = createDefaultSkin()
}
