/*
 * Copyright (c) 2010, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package matt.fx.control.chart.axis.value.axis

import com.sun.javafx.scene.NodeHelper
import javafx.animation.FadeTransition
import javafx.beans.binding.DoubleExpression
import javafx.beans.binding.ObjectExpression
import javafx.beans.binding.StringExpression
import javafx.beans.property.BooleanProperty
import javafx.beans.property.BooleanPropertyBase
import javafx.beans.property.DoubleProperty
import javafx.beans.property.DoublePropertyBase
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ObjectPropertyBase
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.StringProperty
import javafx.beans.property.StringPropertyBase
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.css.CssMetaData
import javafx.css.FontCssMetaData
import javafx.css.PseudoClass
import javafx.css.Styleable
import javafx.css.StyleableBooleanProperty
import javafx.css.StyleableDoubleProperty
import javafx.css.StyleableObjectProperty
import javafx.css.StyleableProperty
import javafx.css.converter.EnumConverter
import javafx.css.converter.PaintConverter
import javafx.css.converter.SizeConverter
import javafx.event.EventHandler
import javafx.geometry.Dimension2D
import javafx.geometry.Orientation
import javafx.geometry.Orientation.HORIZONTAL
import javafx.geometry.Orientation.VERTICAL
import javafx.geometry.Pos.CENTER
import javafx.geometry.Side
import javafx.geometry.Side.BOTTOM
import javafx.geometry.Side.LEFT
import javafx.geometry.Side.RIGHT
import javafx.geometry.Side.TOP
import javafx.scene.chart.Axis
import javafx.scene.chart.CategoryAxis
import javafx.scene.control.Label
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.transform.Rotate
import javafx.scene.transform.Translate
import javafx.util.Duration
import matt.fx.base.rewrite.ReWrittenFxClass
import matt.fx.control.chart.axis.value.axis.AxisForPackagePrivateProps.StyleableProperties.classCssMetaData
import matt.fx.control.css.BooleanCssMetaData
import matt.lang.anno.Open
import java.util.BitSet
import java.util.Collections

/**
 * Base class for all axes in JavaFX that represents an axis drawn on a chart area.
 * It holds properties for axis auto ranging, ticks and labels along the axis.
 *
 *
 * Some examples of concrete subclasses include [NumberAxis] whose axis plots data
 * in numbers and [CategoryAxis] whose values / ticks represent string
 * categories along its axis.
 * @since JavaFX 2.0
 */
@ReWrittenFxClass(Axis::class)
abstract class AxisForPackagePrivateProps<T> : Region() {
    var measure = Text()
    private var effectiveOrientation: Orientation? = null

    /**
     *
     * @param rotation NaN for using the tickLabelRotationProperty()
     */
    var effectiveTickLabelRotation = Double.NaN
        get() = if (!isAutoRanging() || java.lang.Double.isNaN(field)) getTickLabelRotation() else field
    private val axisLabel = Label()
    private val tickMarkPath = Path()
    private var oldLength = 0.0
    /**
     * See if the current range is valid, if it is not then any range dependent calulcations need to redone on the next layout pass
     *
     * @return true if current range calculations are valid
     *
     * True when the current range invalid and all dependent calculations need to be updated
     */
    protected var isRangeValid = false
    var measureInvalid = false
    var tickLabelsVisibleInvalid = false
    private val labelsToSkip = BitSet()

    internal val tickMarks: ObservableList<TickMark<T>> = FXCollections.observableArrayList()
    private val unmodifiableTickMarks = FXCollections.unmodifiableObservableList(tickMarks)

    /**
     * Unmodifiable observable list of tickmarks, each TickMark directly representing a tickmark on this axis. This is updated
     * whenever the displayed tickmarks changes.
     *
     * @return Unmodifiable observable list of TickMarks on this axis
     */
    fun getTickMarks(): ObservableList<TickMark<T>> = unmodifiableTickMarks

    /** The side of the plot which this axis is being drawn on  */
    internal val side: StyleableObjectProperty<Side> =
        object : StyleableObjectProperty<Side>() {
            override fun invalidated() {
                /* cause refreshTickMarks */
                val edge = get()
                pseudoClassStateChanged(TOP_PSEUDOCLASS_STATE, edge == TOP)
                pseudoClassStateChanged(RIGHT_PSEUDOCLASS_STATE, edge == RIGHT)
                pseudoClassStateChanged(BOTTOM_PSEUDOCLASS_STATE, edge == BOTTOM)
                pseudoClassStateChanged(LEFT_PSEUDOCLASS_STATE, edge == LEFT)
                requestAxisLayout()
            }

            override fun getCssMetaData(): CssMetaData<out Styleable, Side> = StyleableProperties.SIDE

            override fun getBean(): Any = this@AxisForPackagePrivateProps

            override fun getName(): String = "side"
        }

    fun getSide(): Side = side.get()

    fun setSide(value: Side) {
        side.set(value)
    }

    fun sideProperty(): StyleableObjectProperty<Side> = side

    fun setEffectiveOrientation(orientation: Orientation?) {
        effectiveOrientation = orientation
    }

    val effectiveSide: Side
        get() {
            val side = getSide()
            @Suppress("SENSELESS_COMPARISON") return if (
                side == null
                || side.isVertical
                && effectiveOrientation == HORIZONTAL
                || side.isHorizontal
                && effectiveOrientation == VERTICAL
            ) {
                /* Means side == null && effectiveOrientation == null produces Side.BOTTOM */
                if (effectiveOrientation == VERTICAL) LEFT else BOTTOM
            } else side
        }

    /** The axis label  */
    private val label: ObjectProperty<String> =
        object : ObjectPropertyBase<String>() {
            override fun invalidated() {
                axisLabel.text = get()
                requestAxisLayout()
            }

            override fun getBean(): Any = this@AxisForPackagePrivateProps

            override fun getName(): String = "label"
        }

    fun getLabel(): String? = label.get()

    fun setLabel(value: String) {
        label.set(value)
    }

    fun labelProperty(): ObjectProperty<String> = label

    /** true if tick marks should be displayed  */
    private val tickMarkVisible: StyleableBooleanProperty =
        object : StyleableBooleanProperty(true) {
            override fun invalidated() {
                tickMarkPath.isVisible = get()
                requestAxisLayout()
            }

            override fun getCssMetaData(): CssMetaData<AxisForPackagePrivateProps<*>, Boolean> = StyleableProperties.TICK_MARK_VISIBLE

            override fun getBean(): Any = this@AxisForPackagePrivateProps

            override fun getName(): String = "tickMarkVisible"
        }

    fun isTickMarkVisible(): Boolean = tickMarkVisible.get()

    fun setTickMarkVisible(value: Boolean) {
        tickMarkVisible.set(value)
    }

    fun tickMarkVisibleProperty(): StyleableBooleanProperty = tickMarkVisible

    /** true if tick mark labels should be displayed  */
    private val tickLabelsVisible: StyleableBooleanProperty =
        object : StyleableBooleanProperty(true) {
            override fun invalidated() {
                /* update textNode visibility for each tick */
                for (tick in tickMarks) {
                    tick.setTextVisible(get())
                }
                tickLabelsVisibleInvalid = true
                requestAxisLayout()
            }

            override fun getCssMetaData(): CssMetaData<AxisForPackagePrivateProps<*>, Boolean> = StyleableProperties.TICK_LABELS_VISIBLE

            override fun getBean(): Any = this@AxisForPackagePrivateProps

            override fun getName(): String = "tickLabelsVisible"
        }

    fun isTickLabelsVisible(): Boolean = tickLabelsVisible.get()

    fun setTickLabelsVisible(value: Boolean) {
        tickLabelsVisible.set(value)
    }

    fun tickLabelsVisibleProperty(): StyleableBooleanProperty = tickLabelsVisible

    /** The length of tick mark lines  */
    private val tickLength: StyleableDoubleProperty =
        object : StyleableDoubleProperty(8.0) {
            override fun invalidated() {
                if (get() < 0 && !isBound) {
                    set(0.0)
                }
                /* this effects preferred size so request layout */
                requestAxisLayout()
            }

            override fun getCssMetaData(): CssMetaData<AxisForPackagePrivateProps<*>, Number> = StyleableProperties.TICK_LENGTH

            override fun getBean(): Any = this@AxisForPackagePrivateProps

            override fun getName(): String = "tickLength"
        }

    fun getTickLength(): Double = tickLength.get()

    fun setTickLength(value: Double) {
        tickLength.set(value)
    }

    fun tickLengthProperty(): StyleableDoubleProperty = tickLength

    /** This is true when the axis determines its range from the data automatically  */
    private val autoRanging: BooleanProperty =
        object : BooleanPropertyBase(true) {
            override fun invalidated() {
                if (get()) {
                    /*
                    auto range turned on, so need to auto range now
                    autoRangeValid = false;
                     */
                    requestAxisLayout()
                }
            }

            override fun getBean(): Any = this@AxisForPackagePrivateProps

            override fun getName(): String = "autoRanging"
        }

    fun isAutoRanging(): Boolean = autoRanging.get()

    fun setAutoRanging(value: Boolean) {
        autoRanging.set(value)
    }

    fun autoRangingProperty(): BooleanProperty = autoRanging

    /** The font for all tick labels  */
    internal val tickLabelFont: StyleableObjectProperty<Font> =
        object : StyleableObjectProperty<Font>(Font.font("System", 8.0)) {
            override fun invalidated() {
                val f = get()
                measure.font = f
                for (tm in getTickMarks()) {
                    tm.textNode.font = f
                }
                measureInvalid = true
                requestAxisLayout()
            }

            override fun getCssMetaData(): CssMetaData<out Styleable, Font> = StyleableProperties.TICK_LABEL_FONT

            override fun getBean(): Any = this@AxisForPackagePrivateProps

            override fun getName(): String = "tickLabelFont"
        }

    fun getTickLabelFont(): Font = tickLabelFont.get()

    fun setTickLabelFont(value: Font) {
        tickLabelFont.set(value)
    }

    fun tickLabelFontProperty(): StyleableObjectProperty<Font> = tickLabelFont

    /** The fill for all tick labels  */
    private val tickLabelFill: StyleableObjectProperty<Paint> =
        object : StyleableObjectProperty<Paint>(Color.BLACK) {
            override fun invalidated() {
                for (tick in tickMarks) {
                    tick.textNode.fill = getTickLabelFill()
                }
            }

            override fun getCssMetaData(): CssMetaData<out Styleable, Paint> = StyleableProperties.TICK_LABEL_FILL

            override fun getBean(): Any = this@AxisForPackagePrivateProps

            override fun getName(): String = "tickLabelFill"
        }

    fun getTickLabelFill(): Paint = tickLabelFill.get()

    fun setTickLabelFill(value: Paint) {
        tickLabelFill.set(value)
    }

    fun tickLabelFillProperty(): StyleableObjectProperty<Paint> = tickLabelFill

    /** The gap between tick labels and the tick mark lines  */
    private val tickLabelGap: StyleableDoubleProperty =
        object : StyleableDoubleProperty(3.0) {
            override fun invalidated() {
                requestAxisLayout()
            }

            override fun getCssMetaData(): CssMetaData<AxisForPackagePrivateProps<*>, Number> = StyleableProperties.TICK_LABEL_TICK_GAP

            override fun getBean(): Any = this@AxisForPackagePrivateProps

            override fun getName(): String = "tickLabelGap"
        }

    fun getTickLabelGap(): Double = tickLabelGap.get()

    fun setTickLabelGap(value: Double) {
        tickLabelGap.set(value)
    }

    fun tickLabelGapProperty(): StyleableDoubleProperty = tickLabelGap

    /**
     * When true any changes to the axis and its range will be animated.
     */
    private val animated: BooleanProperty = SimpleBooleanProperty(this, "animated", true)

    /**
     * Indicates whether the changes to axis range will be animated or not.
     *
     * @return true if axis range changes will be animated and false otherwise
     */
    fun getAnimated(): Boolean = animated.get()

    fun setAnimated(value: Boolean) {
        animated.set(value)
    }

    fun animatedProperty(): BooleanProperty = animated

    /**
     * Rotation in degrees of tick mark labels from their normal horizontal.
     */
    internal val tickLabelRotation: DoubleProperty =
        object : DoublePropertyBase(0.0) {
            override fun invalidated() {
                if (isAutoRanging()) {
                    invalidateRange() /* NumberAxis and CategoryAxis use this property in autorange */
                }
                requestAxisLayout()
            }

            override fun getBean(): Any = this@AxisForPackagePrivateProps

            override fun getName(): String = "tickLabelRotation"
        }

    fun getTickLabelRotation(): Double = tickLabelRotation.value

    fun setTickLabelRotation(value: Double) {
        tickLabelRotation.value = value
    }

    fun tickLabelRotationProperty(): DoubleProperty = tickLabelRotation
    /**
     * Mark the current range invalid, this will cause anything that depends on the range to be recalculated on the
     * next layout.
     */
    protected fun invalidateRange() {
        isRangeValid = false
    }

    /**
     * This is used to check if any given animation should run. It returns true if animation is enabled and the node
     * is visible and in a scene.
     *
     * @return true if animations should happen
     */
    protected fun shouldAnimate(): Boolean = getAnimated() && NodeHelper.isTreeShowing(this)

    /**
     * We suppress requestLayout() calls here by doing nothing as we don't want changes to our children to cause
     * layout. If you really need to request layout then call requestAxisLayout().
     */
    final override fun requestLayout() {}

    /**
     * Request that the axis is laid out in the next layout pass. This replaces requestLayout() as it has been
     * overridden to do nothing so that changes to children's bounds etc do not cause a layout. This was done as a
     * optimization as the Axis knows the exact minimal set of changes that really need layout to be updated. So we
     * only want to request layout then, not on any child change.
     */
    fun requestAxisLayout() {
        super.requestLayout()
    }

    /**
     * Called when data has changed and the range may not be valid any more. This is only called by the chart if
     * isAutoRanging() returns true. If we are auto ranging it will cause layout to be requested and auto ranging to
     * happen on next layout pass.
     *
     * @param data The current set of all data that needs to be plotted on this axis
     */
    open fun invalidateRange(data: List<T>) {
        invalidateRange()
        requestAxisLayout()
    }

    /**
     * This calculates the upper and lower bound based on the data provided to invalidateRange() method. This must not
     * effect the state of the axis, changing any properties of the axis. Any results of the auto-ranging should be
     * returned in the range object. This will we passed to setRange() if it has been decided to adopt this range for
     * this axis.
     *
     * @param length The length of the axis in screen coordinates
     * @return Range information, this is implementation dependent
     */
    protected abstract fun autoRange(length: Double): RangeProps

    /**
     * Called to set the current axis range to the given range. If isAnimating() is true then this method should
     * animate the range to the new range.
     *
     * @param range A range object returned from autoRange()
     * @param animate If true animate the change in range
     */
    protected abstract fun setRange(
        range: RangeProps,
        animate: Boolean
    )

    protected abstract val range: RangeProps

    /**
     * Get the display position of the zero line along this axis.
     *
     * @return display position or Double.NaN if zero is not in current range;
     */
    abstract val zeroPosition: Double

    /**
     * Get the display position along this axis for a given value.
     * If the value is not in the current range, the returned value will be an extrapolation of the display
     * position.
     *
     * If the value is not valid for this Axis and the axis cannot display such value in any range,
     * Double.NaN is returned
     *
     * @param value The data value to work out display position for
     * @return display position or Double.NaN if value not valid
     */
    abstract fun getDisplayPosition(value: T): Double

    /**
     * Get the data value for the given display position on this axis. If the axis
     * is a CategoryAxis this will be the nearest value.
     *
     * @param  displayPosition A pixel position on this axis
     * @return the nearest data value to the given pixel position or
     * null if not on axis;
     */
    abstract fun getValueForDisplay(displayPosition: Double): T?

    /**
     * Checks if the given value is plottable on this axis
     *
     * @param value The value to check if its on axis
     * @return true if the given value is plottable on this axis
     */
    abstract fun isValueOnAxis(value: T): Boolean

    /**
     * All axis values must be representable by some numeric value. This gets the numeric value for a given data value.
     *
     * @param value The data value to convert
     * @return Numeric value for the given data value
     */
    abstract fun toNumericValue(value: T): Double

    /**
     * All axis values must be representable by some numeric value. This gets the data value for a given numeric value.
     *
     * @param value The numeric value to convert
     * @return Data value for given numeric value
     */
    abstract fun toRealValue(value: Double): T?

    /**
     * Calculate a list of all the data values for each tick mark in range
     *
     * @param length The length of the axis in display units
     * @param range A range object returned from autoRange()
     * @return A list of tick marks that fit along the axis if it was the given length
     */
    protected abstract fun calculateTickValues(
        length: Double,
        range: RangeProps
    ): List<T>

    final
    /**
     * Computes the preferred height of this axis for the given width. If axis orientation
     * is horizontal, it takes into account the tick mark length, tick label gap and
     * label height.
     *
     * @return the computed preferred width for this axis
     */
    override fun computePrefHeight(width: Double): Double {
        val side = effectiveSide
        return if (side.isVertical) {
            /*
            TODO for now we have no hard and fast answer here, I guess it should work
            TODO out the minimum size needed to display min, max and zero tick mark labels.
             */
            100.0
        } else {
            /*
 HORIZONTAL
 we need to first auto range as this may/will effect tick marks
             */
            val range = autoRange(width)
            /* calculate max tick label height */
            var maxLabelHeight = 0.0
            /* calculate the new tick marks */
            if (isTickLabelsVisible()) {
                val newTickValues = calculateTickValues(width, range)
                for (value in newTickValues) {
                    maxLabelHeight = Math.max(maxLabelHeight, measureTickMarkSize(value, range).height)
                }
            }
            /* calculate tick mark length */
            val tickMarkLength: Double =
                if (isTickMarkVisible()) if (getTickLength() > 0) getTickLength() else 0.0 else 0.0
            /* calculate label height */
            val labelHeight: Double =
                if (axisLabel.text == null || axisLabel.text.length == 0) 0.0 else axisLabel.prefHeight(-1.0)
            maxLabelHeight + getTickLabelGap() + tickMarkLength + labelHeight
        }
    }

    /**
     * Computes the preferred width of this axis for the given height. If axis orientation
     * is vertical, it takes into account the tick mark length, tick label gap and
     * label height.
     *
     * @return the computed preferred width for this axis
     */
    final override fun computePrefWidth(height: Double): Double {
        val side = effectiveSide
        return if (side.isVertical) {
            /* we need to first auto range as this may/will effect tick marks */
            val range = autoRange(height)
            /* calculate max tick label width */
            var maxLabelWidth = 0.0
            /* calculate the new tick marks */
            if (isTickLabelsVisible()) {
                val newTickValues = calculateTickValues(height, range)
                for (value in newTickValues) {
                    maxLabelWidth = Math.max(maxLabelWidth, measureTickMarkSize(value, range).width)
                }
            }
            /* calculate tick mark length */
            val tickMarkLength: Double =
                if (isTickMarkVisible()) if (getTickLength() > 0) getTickLength() else 0.0 else 0.0
            /* calculate label height */
            val labelHeight: Double =
                if (axisLabel.text == null || axisLabel.text.length == 0) 0.0 else axisLabel.prefHeight(-1.0)
            maxLabelWidth + getTickLabelGap() + tickMarkLength + labelHeight
        } else {
            /*
 HORIZONTAL
 TODO for now we have no hard and fast answer here, I guess it should work
 TODO out the minimum size needed to display min, max and zero tick mark labels.
             */
            100.0
        }
    }

    /**
     * Called during layout if the tickmarks have been updated, allowing subclasses to do anything they need to
     * in reaction.
     */
    protected open fun tickMarksUpdated() {}

    @Open
    /**
     * Invoked during the layout pass to layout this axis and all its content.
     */
    override fun layoutChildren() {
        val isFirstPass = oldLength == 0.0
        /* auto range if it is not valid */
        val side = effectiveSide
        val length = if (side.isVertical) height else width
        val rangeInvalid = !isRangeValid
        val lengthDiffers = oldLength != length
        if (lengthDiffers || rangeInvalid) {
            /* get range */
            val range: Any
            if (isAutoRanging()) {
                /* auto range */
                range = autoRange(length)
                /* set current range to new range */
                setRange(range, getAnimated() && !isFirstPass && NodeHelper.isTreeShowing(this) && rangeInvalid)
            } else {
                range = this.range
            }
            /* calculate new tick marks */
            val newTickValues = calculateTickValues(length, range)

            /* remove everything */
            val tickMarkIterator = tickMarks.iterator()
            while (tickMarkIterator.hasNext()) {
                val tick = tickMarkIterator.next()
                if (shouldAnimate()) {
                    val ft = FadeTransition(Duration.millis(250.0), tick.textNode)
                    ft.toValue = 0.0
                    ft.onFinished =
                        EventHandler {
                            children.remove(
                                tick.textNode
                            )
                        }
                    ft.play()
                } else {
                    children.remove(tick.textNode)
                }
                /* we have to remove the tick mark immediately so we don't draw tick line for it or grid lines and fills */
                tickMarkIterator.remove()
            }

            /* add new tick marks for new values */
            for (newValue in newTickValues) {
                val tick = TickMark<T>()
                tick.setValue(newValue)
                tick.textNode.text = getTickMarkLabel(newValue)
                tick.textNode.font = getTickLabelFont()
                tick.textNode.fill = getTickLabelFill()
                tick.setTextVisible(isTickLabelsVisible())
                if (shouldAnimate()) tick.textNode.opacity = 0.0
                children.add(tick.textNode)
                tickMarks.add(tick)
                if (shouldAnimate()) {
                    val ft = FadeTransition(Duration.millis(750.0), tick.textNode)
                    ft.fromValue = 0.0
                    ft.toValue = 1.0
                    ft.play()
                }
            }

            /* call tick marks updated to inform subclasses that we have updated tick marks */
            tickMarksUpdated()
            /* mark all done */
            oldLength = length
            isRangeValid = true
        }
        if (lengthDiffers || rangeInvalid || measureInvalid || tickLabelsVisibleInvalid) {
            measureInvalid = false
            tickLabelsVisibleInvalid = false
            /*
            RT-12272 : tick labels overlapping
            first check if all visible labels fit, if not, retain every nth label
             */
            labelsToSkip.clear()
            var numLabelsToSkip = 0
            var totalLabelsSize = 0.0
            var maxLabelSize = 0.0
            for (m in tickMarks) {
                m.setPosition(getDisplayPosition(m.getValue()))
                if (m.isTextVisible()) {
                    val tickSize = measureTickMarkSize(m.getValue(), side)
                    totalLabelsSize += tickSize
                    maxLabelSize = Math.round(Math.max(maxLabelSize, tickSize)).toDouble()
                }
            }
            if (maxLabelSize > 0 && length < totalLabelsSize) {
                numLabelsToSkip = (tickMarks.size * maxLabelSize / length).toInt() + 1
            }
            if (numLabelsToSkip > 0) {
                var tickIndex = 0
                for (m in tickMarks) {
                    if (m.isTextVisible()) {
                        m.setTextVisible(tickIndex++ % numLabelsToSkip == 0)
                    }
                }
            }

            /*
            now check if labels for bounds overlap nearby labels, this can happen due to JDK-8097501
            use tickLabelGap to prevent sticking
             */
            if (tickMarks.size > 2) {
                var m1 = tickMarks[0]
                var m2 = tickMarks[1]
                if (isTickLabelsOverlap(side, m1, m2, getTickLabelGap())) {
                    m2.setTextVisible(false)
                }
                m1 = tickMarks[tickMarks.size - 2]
                m2 = tickMarks[tickMarks.size - 1]
                if (isTickLabelsOverlap(side, m1, m2, getTickLabelGap())) {
                    m1.setTextVisible(false)
                }
            }
            updateTickMarks(side, length)
        }
    }

    private fun updateTickMarks(
        side: Side,
        length: Double
    ) {
        /* clear tick mark path elements as we will recreate */
        tickMarkPath.elements.clear()
        /* do layout of axis label, tick mark lines and text */
        val width = width
        val height = height
        val tickMarkLength: Double = if (isTickMarkVisible() && getTickLength() > 0) getTickLength() else 0.0
        val effectiveLabelRotation = effectiveTickLabelRotation
        if (LEFT == side) {
            /* offset path to make strokes snap to pixel */
            tickMarkPath.layoutX = -0.5
            tickMarkPath.layoutY = 0.5
            if (getLabel() != null) {
                axisLabel.transforms.setAll(Translate(0.0, height), Rotate(-90.0, 0.0, 0.0))
                axisLabel.layoutX = 0.0
                axisLabel.layoutY = 0.0
                axisLabel.resize(height, Math.ceil(axisLabel.prefHeight(width)))
            }
            for (tick in tickMarks) {
                positionTextNode(
                    tick.textNode,
                    width - getTickLabelGap() - tickMarkLength,
                    tick.getPosition(),
                    effectiveLabelRotation,
                    side
                )
                updateTickMark(
                    tick, length, width - tickMarkLength, tick.getPosition(), width, tick.getPosition()
                )
            }
        } else if (RIGHT == side) {
            /* offset path to make strokes snap to pixel */
            tickMarkPath.layoutX = 0.5
            tickMarkPath.layoutY = 0.5
            if (getLabel() != null) {
                val axisLabelWidth = Math.ceil(axisLabel.prefHeight(width))
                axisLabel.transforms.setAll(Translate(0.0, height), Rotate(-90.0, 0.0, 0.0))
                axisLabel.layoutX = width - axisLabelWidth
                axisLabel.layoutY = 0.0
                axisLabel.resize(height, axisLabelWidth)
            }
            for (tick in tickMarks) {
                positionTextNode(
                    tick.textNode, getTickLabelGap() + tickMarkLength, tick.getPosition(), effectiveLabelRotation, side
                )
                updateTickMark(
                    tick, length, 0.0, tick.getPosition(), tickMarkLength, tick.getPosition()
                )
            }
        } else if (TOP == side) {
            /* offset path to make strokes snap to pixel */
            tickMarkPath.layoutX = 0.5
            tickMarkPath.layoutY = -0.5
            if (getLabel() != null) {
                axisLabel.transforms.clear()
                axisLabel.layoutX = 0.0
                axisLabel.layoutY = 0.0
                axisLabel.resize(width, Math.ceil(axisLabel.prefHeight(width)))
            }
            for (tick in tickMarks) {
                positionTextNode(
                    tick.textNode,
                    tick.getPosition(),
                    height - tickMarkLength - getTickLabelGap(),
                    effectiveLabelRotation,
                    side
                )
                updateTickMark(
                    tick, length, tick.getPosition(), height, tick.getPosition(), height - tickMarkLength
                )
            }
        } else {
            /*
            BOTTOM
            offset path to make strokes snap to pixel
             */
            tickMarkPath.layoutX = 0.5
            tickMarkPath.layoutY = 0.5
            if (getLabel() != null) {
                axisLabel.transforms.clear()
                val labelHeight = Math.ceil(axisLabel.prefHeight(width))
                axisLabel.layoutX = 0.0
                axisLabel.layoutY = height - labelHeight
                axisLabel.resize(width, labelHeight)
            }
            for (tick in tickMarks) {
                positionTextNode(
                    tick.textNode, tick.getPosition(), tickMarkLength + getTickLabelGap(), effectiveLabelRotation, side
                )
                updateTickMark(
                    tick, length, tick.getPosition(), 0.0, tick.getPosition(), tickMarkLength
                )
            }
        }
    }

    /**
     * Checks if two consecutive tick mark labels overlaps.
     * @param side side of the Axis
     * @param m1 first tick mark
     * @param m2 second tick mark
     * @param gap minimum space between labels
     * @return true if labels overlap
     */
    private fun isTickLabelsOverlap(
        side: Side,
        m1: TickMark<T>,
        m2: TickMark<T>,
        gap: Double
    ): Boolean {
        if (!m1.isTextVisible() || !m2.isTextVisible()) return false
        val m1Size = measureTickMarkSize(m1.getValue(), side)
        val m2Size = measureTickMarkSize(m2.getValue(), side)
        val m1Start = m1.getPosition() - m1Size / 2
        val m1End = m1.getPosition() + m1Size / 2
        val m2Start = m2.getPosition() - m2Size / 2
        val m2End = m2.getPosition() + m2Size / 2
        return if (side.isVertical) m1Start - m2End <= gap else m2Start - m1End <= gap
    }

    /**
     * Positions a text node to one side of the given point, it X height is vertically centered on point if LEFT or
     * RIGHT and its centered horizontally if TOP ot BOTTOM.
     *
     * @param node The text node to position
     * @param posX The x position, to place text next to
     * @param posY The y position, to place text next to
     * @param angle The text rotation
     * @param side The side to place text next to position x,y at
     */
    private fun positionTextNode(
        node: Text,
        posX: Double,
        posY: Double,
        angle: Double,
        side: Side
    ) {
        node.layoutX = 0.0
        node.layoutY = 0.0
        node.rotate = angle
        val bounds = node.boundsInParent
        if (LEFT == side) {
            node.layoutX = posX - bounds.width - bounds.minX
            node.layoutY = posY - bounds.height / 2.0 - bounds.minY
        } else if (RIGHT == side) {
            node.layoutX = posX - bounds.minX
            node.layoutY = posY - bounds.height / 2.0 - bounds.minY
        } else if (TOP == side) {
            node.layoutX = posX - bounds.width / 2.0 - bounds.minX
            node.layoutY = posY - bounds.height - bounds.minY
        } else {
            node.layoutX = posX - bounds.width / 2.0 - bounds.minX
            node.layoutY = posY - bounds.minY
        }
    }

    /**
     * Updates visibility of the text node and adds the tick mark to the path
     */
    private fun updateTickMark(
        tick: TickMark<T>,
        length: Double,
        startX: Double,
        startY: Double,
        endX: Double,
        endY: Double
    ) {
        /* check if position is inside bounds */
        if (tick.getPosition() >= 0 && tick.getPosition() <= Math.ceil(length)) {
            tick.textNode.isVisible = tick.isTextVisible()
            /* add tick mark line */
            tickMarkPath.elements.addAll(
                MoveTo(startX, startY), LineTo(endX, endY)
            )
        } else {
            tick.textNode.isVisible = false
        }
    }

    /**
     * Get the string label name for a tick mark with the given value
     *
     * @param value The value to format into a tick label string
     * @return A formatted string for the given value
     */
    protected abstract fun getTickMarkLabel(value: T): String?

    /**
     * Measure the size of the label for given tick mark value. This uses the font that is set for the tick marks
     *
     *
     * @param labelText     tick mark label text
     * @param rotation  The text rotation
     * @return size of tick mark label for given value
     */
    protected fun measureTickMarkLabelSize(
        labelText: String?,
        rotation: Double
    ): Dimension2D {
        measure.rotate = rotation
        measure.text = labelText
        val bounds = measure.boundsInParent
        return Dimension2D(bounds.width, bounds.height)
    }

    /**
     * Measure the size of the label for given tick mark value. This uses the font that is set for the tick marks
     *
     * @param value     tick mark value
     * @param rotation  The text rotation
     * @return size of tick mark label for given value
     */
    protected fun measureTickMarkSize(
        value: T,
        rotation: Double
    ): Dimension2D = measureTickMarkLabelSize(getTickMarkLabel(value), rotation)

    /**
     * Measure the size of the label for given tick mark value. This uses the font that is set for the tick marks
     *
     * @param value tick mark value
     * @param range range to use during calculations
     * @return size of tick mark label for given value
     */
    protected open fun measureTickMarkSize(
        value: T,
        range: RangeProps
    ): Dimension2D = measureTickMarkSize(value, effectiveTickLabelRotation)

    /**
     * Measure the size of the label for given tick mark value. This uses the font that is set for the tick marks
     *
     * @param value tick mark value
     * @param side side of this Axis
     * @return size of tick mark label for given value
     * @see .measureTickMarkSize
     */
    private fun measureTickMarkSize(
        value: T,
        side: Side
    ): Double {
        val size = measureTickMarkSize(value, effectiveTickLabelRotation)
        return if (side.isVertical) size.height else size.width
    }
    /**
     * TickMark represents the label text, its associated properties for each tick
     * along the Axis.
     * @since JavaFX 2.0
     */
    class TickMark<T> {
        /**
         * The display text for tick mark
         */
        private val label: StringProperty =
            object : StringPropertyBase() {
                override fun invalidated() {
                    textNode.text = value
                }

                override fun getBean(): Any = this@TickMark

                override fun getName(): String = "label"
            }

        fun getLabel(): String = label.get()

        fun setLabel(value: String) {
            label.set(value)
        }

        fun labelProperty(): StringExpression = label

        /**
         * The value for this tick mark in data units
         */
        private val value: ObjectProperty<T> = SimpleObjectProperty(this, "value")
        fun getValue(): T = value.get()

        fun setValue(v: T) {
            value.set(v)
        }

        fun valueProperty(): ObjectExpression<T> = value

        /**
         * The display position along the axis from axis origin in display units
         */
        private val position: DoubleProperty = SimpleDoubleProperty(this, "position")
        fun getPosition(): Double = position.get()

        fun setPosition(value: Double) {
            position.set(value)
        }

        fun positionProperty(): DoubleExpression = position

        var textNode = Text()

        /** true if tick mark labels should be displayed  */
        private val textVisible: BooleanProperty =
            object : BooleanPropertyBase(true) {
                override fun invalidated() {
                    if (!get()) {
                        textNode.isVisible = false
                    }
                }

                override fun getBean(): Any = this@TickMark

                override fun getName(): String = "textVisible"
            }

        /**
         * Indicates whether this tick mark label text is displayed or not.
         * @return true if tick mark label text is visible and false otherwise
         */
        fun isTextVisible(): Boolean = textVisible.get()

        /**
         * Specifies whether this tick mark label text is displayed or not.
         * @param value true if tick mark label text is visible and false otherwise
         */
        fun setTextVisible(value: Boolean) {
            textVisible.set(value)
        }

        /**
         * Returns a string representation of this `TickMark` object.
         * @return a string representation of this `TickMark` object.
         */
        override fun toString(): String = value.get().toString()
    }

    private object StyleableProperties {
        val SIDE: CssMetaData<AxisForPackagePrivateProps<*>, Side> =
            object : CssMetaData<AxisForPackagePrivateProps<*>, Side>(
                "-fx-side", EnumConverter(Side::class.java)
            ) {
                override fun isSettable(n: AxisForPackagePrivateProps<*>): Boolean = n.side.value == null || !n.side.isBound

                override fun getStyleableProperty(n: AxisForPackagePrivateProps<*>): StyleableProperty<Side> = n.sideProperty()
            }
        val TICK_LENGTH: CssMetaData<AxisForPackagePrivateProps<*>, Number> =
            object : CssMetaData<AxisForPackagePrivateProps<*>, Number>(
                "-fx-tick-length", SizeConverter.getInstance(), 8.0
            ) {
                override fun isSettable(n: AxisForPackagePrivateProps<*>): Boolean = n.tickLength.value == null || !n.tickLength.isBound

                override fun getStyleableProperty(n: AxisForPackagePrivateProps<*>): StyleableProperty<Number?> = n.tickLengthProperty()
            }
        val TICK_LABEL_FONT: CssMetaData<AxisForPackagePrivateProps<*>, Font> =
            object : FontCssMetaData<AxisForPackagePrivateProps<*>>(
                "-fx-tick-label-font", Font.font("system", 8.0)
            ) {
                override fun isSettable(
                    n: AxisForPackagePrivateProps<*>
                ): Boolean = n.tickLabelFont.value == null || !n.tickLabelFont.isBound

                override fun getStyleableProperty(n: AxisForPackagePrivateProps<*>): StyleableProperty<Font> = n.tickLabelFontProperty()
            }
        val TICK_LABEL_FILL: CssMetaData<AxisForPackagePrivateProps<*>, Paint> =
            object : CssMetaData<AxisForPackagePrivateProps<*>, Paint>(
                "-fx-tick-label-fill", PaintConverter.getInstance(), Color.BLACK
            ) {
                override fun isSettable(
                    n: AxisForPackagePrivateProps<*>
                ): Boolean = (n.tickLabelFill.value == null) or !n.tickLabelFill.isBound

                override fun getStyleableProperty(n: AxisForPackagePrivateProps<*>): StyleableProperty<Paint> = n.tickLabelFillProperty()
            }
        val TICK_LABEL_TICK_GAP: CssMetaData<AxisForPackagePrivateProps<*>, Number> =
            object : CssMetaData<AxisForPackagePrivateProps<*>, Number>(
                "-fx-tick-label-gap", SizeConverter.getInstance(), 3.0
            ) {
                override fun isSettable(n: AxisForPackagePrivateProps<*>): Boolean = n.tickLabelGap.value == null || !n.tickLabelGap.isBound

                override fun getStyleableProperty(n: AxisForPackagePrivateProps<*>): StyleableProperty<Number?> = n.tickLabelGapProperty()
            }
        val TICK_MARK_VISIBLE: CssMetaData<AxisForPackagePrivateProps<*>, Boolean> =
            object : BooleanCssMetaData<AxisForPackagePrivateProps<*>>(
                "-fx-tick-mark-visible", true
            ) {
                override fun isSettable(
                    n: AxisForPackagePrivateProps<*>
                ): Boolean = n.tickMarkVisible.value == null || !n.tickMarkVisible.isBound

                override fun getStyleableProperty(n: AxisForPackagePrivateProps<*>): StyleableProperty<Boolean?> = n.tickMarkVisibleProperty()
            }
        val TICK_LABELS_VISIBLE: CssMetaData<AxisForPackagePrivateProps<*>, Boolean> =
            object : BooleanCssMetaData<AxisForPackagePrivateProps<*>>("-fx-tick-labels-visible", true) {
                override fun isSettable(
                    n: AxisForPackagePrivateProps<*>
                ): Boolean = n.tickLabelsVisible.value == null || !n.tickLabelsVisible.isBound

                override fun getStyleableProperty(n: AxisForPackagePrivateProps<*>): StyleableProperty<Boolean?> = n.tickLabelsVisibleProperty()
            }
        val classCssMetaData: List<CssMetaData<out Styleable?, *>>? by lazy {
            val styleables: MutableList<CssMetaData<out Styleable?, *>> = ArrayList(getClassCssMetaData())
            styleables.add(SIDE)
            styleables.add(TICK_LENGTH)
            styleables.add(TICK_LABEL_FONT)
            styleables.add(TICK_LABEL_FILL)
            styleables.add(TICK_LABEL_TICK_GAP)
            styleables.add(TICK_MARK_VISIBLE)
            styleables.add(TICK_LABELS_VISIBLE)
            Collections.unmodifiableList(styleables)
        }
    }

    /**
     * {@inheritDoc}
     * @since JavaFX 8.0
     */
    @Open override fun getCssMetaData(): List<CssMetaData<out Styleable?, *>>? = classCssMetaData
    /**
     * Creates and initializes a new instance of the Axis class.
     */
    init {
        styleClass.setAll("axis")
        axisLabel.styleClass.add("axis-label")
        axisLabel.alignment = CENTER
        tickMarkPath.styleClass.add("axis-tick-mark")
        children.addAll(axisLabel, tickMarkPath)
    }

    companion object {
        /** pseudo-class indicating this is a vertical Top side Axis.  */
        private val TOP_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("top")

        /** pseudo-class indicating this is a vertical Bottom side Axis.  */
        private val BOTTOM_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("bottom")

        /** pseudo-class indicating this is a vertical Left side Axis.  */
        private val LEFT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("left")

        /** pseudo-class indicating this is a vertical Right side Axis.  */
        private val RIGHT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("right")
    }
}


sealed interface RangeProps
class CategoryRangeProps(
    val allDataCategories: List<String>,
    val newCategorySpacing: Double,
    val newFirstPos: Double,
    val tickLabelRotation: Double
) : RangeProps

class NumberRangeProps(
    val lowerBound: Double,
    val upperBound: Double,
    val tickUnit: Double,
    val scale: Double,
    val formatter: String? = null
) : RangeProps

object NullRangeProp : RangeProps
