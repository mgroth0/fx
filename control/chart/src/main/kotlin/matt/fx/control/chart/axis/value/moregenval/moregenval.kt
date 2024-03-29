package matt.fx.control.chart.axis.value.moregenval

import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ObjectPropertyBase
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.ReadOnlyDoubleWrapper
import javafx.beans.property.SimpleDoubleProperty
import javafx.css.CssMetaData
import javafx.css.Styleable
import javafx.css.StyleableBooleanProperty
import javafx.css.StyleableDoubleProperty
import javafx.css.StyleableIntegerProperty
import javafx.css.StyleableProperty
import javafx.css.converter.SizeConverter
import javafx.geometry.Side
import javafx.geometry.Side.LEFT
import javafx.geometry.Side.RIGHT
import javafx.geometry.Side.TOP
import javafx.scene.chart.ValueAxis
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.util.StringConverter
import matt.fig.modell.plot.convert.InternalData
import matt.fig.modell.plot.convert.ValueAxisConverter
import matt.fx.base.rewrite.ReWrittenFxClass
import matt.fx.control.chart.axis.value.axis.AxisForPackagePrivateProps
import matt.fx.control.chart.axis.value.axis.NullRangeProp
import matt.fx.control.chart.axis.value.axis.RangeProps
import matt.fx.control.css.BooleanCssMetaData
import matt.lang.anno.Open
import matt.obs.prop.writable.BindableProperty
import java.util.Collections


private typealias UpperBound = Any

/**
 * An axis whose data is defined as Numbers. It can also draw minor
 * tick-marks between the major ones.
 * @since JavaFX 2.0
 */
@ReWrittenFxClass(ValueAxis::class)
abstract class MoreGenericValueAxis<T : UpperBound>(
    lowerBound: T? = null,
    upperBound: T? = null,
    protected val converter: ValueAxisConverter<T>
) : AxisForPackagePrivateProps<T>() {

    protected fun T.convert() = converter.convertToB(this)
    protected fun InternalData.convert() = converter.convertToA(this)


    private val minorTickPath = Path()
    private var offset = 0.0

    /** This is the minimum current data value and it is used while auto ranging.
     * Package private solely for test purposes  */
    private var dataMinValue = 0.0

    /** This is the maximum current data value and it is used while auto ranging.
     * Package private solely for test purposes  */
    private var dataMaxValue = 0.0

    /** List of the values at which there are minor ticks  */
    private var minorTickMarkValues: List<T>? = null
    private var minorTickMarksDirty = true
    /**
     * The current value for the lowerBound of this axis (minimum value).
     * This may be the same as lowerBound or different. It is used by NumberAxis to animate the
     * lowerBound from the old value to the new value.
     */
    protected val currentLowerBound: DoubleProperty = SimpleDoubleProperty(this, "currentLowerBound")
    /** true if minor tick marks should be displayed  */
    private val minorTickVisible: StyleableBooleanProperty =
        object : StyleableBooleanProperty(true) {
            override fun invalidated() {
                minorTickPath.isVisible = get()
                requestAxisLayout()
            }

            override fun getBean(): Any = this@MoreGenericValueAxis

            override fun getName(): String = "minorTickVisible"

            override fun getCssMetaData(): CssMetaData<MoreGenericValueAxis<out UpperBound>, Boolean> = StyleableProperties.MINOR_TICK_VISIBLE
        }

    fun isMinorTickVisible(): Boolean = minorTickVisible.get()

    fun setMinorTickVisible(value: Boolean) {
        minorTickVisible.set(value)
    }

    fun minorTickVisibleProperty(): StyleableBooleanProperty = minorTickVisible

    /** The scale factor from data units to visual units  */
    internal val scale: ReadOnlyDoubleWrapper =
        object : ReadOnlyDoubleWrapper(this, "scale", 0.0) {
            override fun invalidated() {
                requestAxisLayout()
                measureInvalid = true
            }
        }

    fun getScale(): Double = scale.get()

    protected fun setScale(scale: Double) {
        this.scale.set(scale)
    }

    fun scaleProperty(): ReadOnlyDoubleProperty = scale.readOnlyProperty

    fun scalePropertyImpl(): ReadOnlyDoubleWrapper = scale


    /** The value for the upper bound of this axis (maximum value). This is automatically set if auto ranging is on.  */
    val upperBound =
        BindableProperty(converter.convertToA(100.0)).apply {
            onChange {
                if (!isAutoRanging()) {
                    invalidateRange()
                    requestAxisLayout()
                }
            }
        }

    /** The value for the lower bound of this axis (minimum value). This is automatically set if auto ranging is on.  */
    val lowerBound =
        BindableProperty(converter.convertToA(0.0)).apply {
            onChange {
                if (!isAutoRanging()) {
                    invalidateRange()
                    requestAxisLayout()
                }
            }
        }

    /** StringConverter used to format tick mark labels. If null a default will be used  */
    protected val tickLabelFormatter: ObjectProperty<StringConverter<in T>> =
        object : ObjectPropertyBase<StringConverter<in T>>(null) {
            override fun invalidated() {
                invalidateRange()
                requestAxisLayout()
            }

            override fun getBean(): Any = this@MoreGenericValueAxis

            override fun getName(): String = "tickLabelFormatter"
        }

    fun getTickLabelFormatter(): StringConverter<in T> = tickLabelFormatter.value

    fun setTickLabelFormatter(value: StringConverter<in T>) {
        tickLabelFormatter.value = value
    }

    fun tickLabelFormatterProperty(): ObjectProperty<StringConverter<in T>> = tickLabelFormatter

    /** The length of minor tick mark lines. Set to 0 to not display minor tick marks.  */
    private val minorTickLength: StyleableDoubleProperty =
        object : StyleableDoubleProperty(5.0) {
            override fun invalidated() {
                requestAxisLayout()
            }

            override fun getBean(): Any = this@MoreGenericValueAxis

            override fun getName(): String = "minorTickLength"

            override fun getCssMetaData(): CssMetaData<MoreGenericValueAxis<out UpperBound>, Number> = StyleableProperties.MINOR_TICK_LENGTH
        }

    fun getMinorTickLength(): Double = minorTickLength.get()

    fun setMinorTickLength(value: Double) {
        minorTickLength.set(value)
    }

    fun minorTickLengthProperty(): StyleableDoubleProperty = minorTickLength

    /**
     * The number of minor tick divisions to be displayed between each major tick mark.
     * The number of actual minor tick marks will be one less than this.
     */
    protected val minorTickCount: StyleableIntegerProperty =
        object : StyleableIntegerProperty(5) {
            override fun invalidated() {
                invalidateRange()
                requestAxisLayout()
            }

            override fun getBean(): Any = this@MoreGenericValueAxis

            override fun getName(): String = "minorTickCount"

            override fun getCssMetaData(): CssMetaData<MoreGenericValueAxis<out UpperBound>, Number> = StyleableProperties.MINOR_TICK_COUNT
        }

    fun getMinorTickCount(): Int = minorTickCount.get()

    fun setMinorTickCount(value: Int) {
        minorTickCount.set(value)
    }

    fun minorTickCountProperty(): StyleableIntegerProperty = minorTickCount
    /**
     * Creates a auto-ranging matt.fx.control.wrapper.chart.axis.value.fxextend.ValueAxis.
     */
    init {
        minorTickPath.styleClass.add("axis-minor-tick-mark")
        children.add(minorTickPath)
        if (lowerBound != null || upperBound != null) {
            require(upperBound != null && lowerBound != null)
            this.lowerBound.value = lowerBound
            this.upperBound.value = upperBound
            setAutoRanging(false)
        }
    }
    /**
     * This calculates the upper and lower bound based on the data provided to invalidateRange() method. This must not
     * affect the state of the axis. Any results of the auto-ranging should be
     * returned in the range object. This will we passed to setRange() if it has been decided to adopt this range for
     * this axis.
     *
     * @param length The length of the axis in screen coordinates
     * @return Range information, this is implementation dependent
     */
    final override fun autoRange(length: Double): RangeProps {
        /* guess a sensible starting size for label size, that is approx 2 lines vertically or 2 charts horizontally */
        return if (isAutoRanging()) {
            /* guess a sensible starting size for label size, that is approx 2 lines vertically or 2 charts horizontally */
            val labelSize = tickLabelFont.value.size * 2
            autoRange(dataMinValue, dataMaxValue, length, labelSize)
        } else {
            range
        }
    }

    /**
     * Calculates new scale for this axis. This should not affect any properties of this axis.
     *
     * @param length The display length of the axis
     * @param lowerBound The lower bound value
     * @param upperBound The upper bound value
     * @return new scale to fit the range from lower bound to upper bound in the given display length
     */
    protected fun calculateNewScale(
        length: Double,
        lowerBound: InternalData,
        upperBound: InternalData
    ): Double {
        val newScale: Double
        val side = effectiveSide
        /*val side = getEffectiveSide()*/
        if (side.isVertical) {
            offset = length
            newScale = if (upperBound - lowerBound == 0.0) -length else -(length / (upperBound - lowerBound))
        } else {
            /* HORIZONTAL */
            offset = 0.0
            newScale = if (upperBound - lowerBound == 0.0) length else length / (upperBound - lowerBound)
        }
        return newScale
    }

    /**
     * Called to set the upper and lower bound and anything else that needs to be auto-ranged. This must not affect
     * the state of the axis. Any results of the auto-ranging should be returned
     * in the range object. This will we passed to setRange() if it has been decided to adopt this range for this axis.
     *
     * @param minValue The min data value that needs to be plotted on this axis
     * @param maxValue The max data value that needs to be plotted on this axis
     * @param length The length of the axis in display coordinates
     * @param labelSize The approximate average size a label takes along the axis
     * @return The calculated range
     */
    protected open fun autoRange(
        minValue: Double,
        maxValue: Double,
        length: Double,
        labelSize: Double
    ): RangeProps {
        return NullRangeProp /*
 this method should have been abstract as there is no way for it to
 return anything correct. so just return null.
         */
    }

    /**
     * Calculates a list of the data values for every minor tick mark
     *
     * @return List of data values where to draw minor tick marks
     */
    protected abstract fun calculateMinorTickMarks(): List<T>?

    /**
     * Called during layout if the tickmarks have been updated, allowing subclasses to do anything they need to
     * in reaction.
     */
    final override fun tickMarksUpdated() {
        super.tickMarksUpdated()
        /* recalculate minor tick marks */
        minorTickMarkValues = calculateMinorTickMarks()
        minorTickMarksDirty = true
    }

    /**
     * Invoked during the layout pass to layout this axis and all its content.
     */
    final override fun layoutChildren() {
        val side = effectiveSide
        /*val side = getEffectiveSide()*/
        val length = if (side.isVertical) height else width
        /* if we are not auto ranging we need to calculate the new scale */
        if (!isAutoRanging()) {
            /* calculate new scale */
            setScale(calculateNewScale(length, lowerBound.value.convert(), upperBound.value.convert()))
            /* update current lower bound */
            currentLowerBound.set(converter.convertToB(lowerBound.value))
        }
        /* we have done all auto calcs, let Axis position major tickmarks */
        super.layoutChildren()
        if (minorTickMarksDirty) {
            minorTickMarksDirty = false
            updateMinorTickPath(side, length)
        }
    }

    private fun updateMinorTickPath(
        side: Side,
        length: Double
    ) {
        val numMinorTicks = (tickMarks.size - 1) * (Math.max(1, getMinorTickCount()) - 1)
        val neededLength = ((tickMarks.size + numMinorTicks) * 2).toDouble()

        /* Update minor tickmarks */
        minorTickPath.elements.clear()
        /* Don't draw minor tick marks if there isn't enough space for them! */
        val minorTickLength = Math.max(0.0, getMinorTickLength())
        if (minorTickLength > 0 && length > neededLength) {
            if (LEFT == side) {
                /* snap minorTickPath to pixels */
                minorTickPath.layoutX = -0.5
                minorTickPath.layoutY = 0.5
                for (value in minorTickMarkValues!!) {
                    val y = getDisplayPosition(value)
                    if (y >= 0 && y <= length) {
                        minorTickPath.elements.addAll(
                            MoveTo(width - minorTickLength, y),
                            LineTo(width - 1, y)
                        )
                    }
                }
            } else if (RIGHT == side) {
                /* snap minorTickPath to pixels */
                minorTickPath.layoutX = 0.5
                minorTickPath.layoutY = 0.5
                for (value in minorTickMarkValues!!) {
                    val y = getDisplayPosition(value)
                    if (y >= 0 && y <= length) {
                        minorTickPath.elements.addAll(
                            MoveTo(1.0, y),
                            LineTo(minorTickLength, y)
                        )
                    }
                }
            } else if (TOP == side) {
                /* snap minorTickPath to pixels */
                minorTickPath.layoutX = 0.5
                minorTickPath.layoutY = -0.5
                for (value in minorTickMarkValues!!) {
                    val x = getDisplayPosition(value)
                    if (x >= 0 && x <= length) {
                        minorTickPath.elements.addAll(
                            MoveTo(x, height - 1),
                            LineTo(x, height - minorTickLength)
                        )
                    }
                }
            } else {
                /*
 BOTTOM
 snap minorTickPath to pixels
                 */
                minorTickPath.layoutX = 0.5
                minorTickPath.layoutY = 0.5
                for (value in minorTickMarkValues!!) {
                    val x = getDisplayPosition(value)
                    if (x >= 0 && x <= length) {
                        minorTickPath.elements.addAll(
                            MoveTo(x, 1.0),
                            LineTo(x, minorTickLength)
                        )
                    }
                }
            }
        }
    }
    /**
     * Called when the data has changed and the range may not be valid anymore. This is only called by the chart if
     * isAutoRanging() returns true. If we are auto ranging it will cause layout to be requested and auto ranging to
     * happen on next layout pass.
     *
     * @param data The current set of all data that needs to be plotted on this axis
     */
    final override fun invalidateRange(data: List<T>) {
        if (data.isEmpty()) {
            dataMaxValue = upperBound.value.convert()
            dataMinValue = lowerBound.value.convert()
        } else {
            dataMinValue = Double.MAX_VALUE
            /*
            We need to init to the lowest negative double (which is NOT Double.MIN_VALUE)
            in order to find the maximum (positive or negative)
             */
            dataMaxValue = -Double.MAX_VALUE
        }
        for (dataValue in data) {
            dataMinValue = Math.min(dataMinValue, converter.convertToB(dataValue))
            dataMaxValue = Math.max(dataMaxValue, converter.convertToB(dataValue))
        }
        super.invalidateRange(data)
    }

    /**
     * Gets the display position along this axis for a given value.
     * If the value is not in the current range, the returned value will be an extrapolation of the display
     * position.
     *
     * @param value The data value to work out display position for
     * @return display position
     */
    final override fun getDisplayPosition(value: T): Double = offset + (converter.convertToB(value) - currentLowerBound.get()) * getScale()

    /**
     * Gets the data value for the given display position on this axis. If the axis
     * is a CategoryAxis this will be the nearest value.
     *
     * @param  displayPosition A pixel position on this axis
     * @return the nearest data value to the given pixel position or
     * null if not on axis;
     */
    final override fun getValueForDisplay(
        displayPosition: Double
    ): T = toRealValue((displayPosition - offset) / getScale() + currentLowerBound.get())

    /**
     * Gets the display position of the zero line along this axis.
     *
     * @return display position or Double.NaN if zero is not in current range;
     */


    final override val zeroPosition: Double
        get() =
            if (0 < lowerBound.value.convert() || 0 > upperBound.value.convert()) Double.NaN else getDisplayPosition(
                0.0.convert()
            )

    /**
     * Checks if the given value is plottable on this axis
     *
     * @param value The value to check if its on axis
     * @return true if the given value is plottable on this axis
     */
    final override fun isValueOnAxis(value: T): Boolean {
        val num = converter.convertToB(value)
        return num >= lowerBound.value.convert() && num <= upperBound.value.convert()
    }

    /**
     * All axis values must be representable by some numeric value. This gets the numeric value for a given data value.
     *
     * @param value The data value to convert
     * @return Numeric value for the given data value
     */
    final override fun toNumericValue(value: T): Double = converter.convertToB(value)

    /**
     * All axis values must be representable by some numeric value. This gets the data value for a given numeric value.
     *
     * @param value The numeric value to convert
     * @return Data value for given numeric value
     */
    final override fun toRealValue(value: Double): T = converter.convertToA(value)

    private object StyleableProperties {

        val MINOR_TICK_LENGTH: CssMetaData<MoreGenericValueAxis<out UpperBound>, Number> =
            object : CssMetaData<MoreGenericValueAxis<out UpperBound>, Number>(
                "-fx-minor-tick-length",
                SizeConverter.getInstance(), 5.0
            ) {
                override fun isSettable(
                    n: MoreGenericValueAxis<out UpperBound>
                ): Boolean = n.minorTickLength.value == null || !n.minorTickLength.isBound

                override fun getStyleableProperty(n: MoreGenericValueAxis<out UpperBound>): StyleableProperty<Number> = n.minorTickLengthProperty()
            }
        val MINOR_TICK_COUNT: CssMetaData<MoreGenericValueAxis<out UpperBound>, Number> =
            object : CssMetaData<MoreGenericValueAxis<out UpperBound>, Number>(
                "-fx-minor-tick-count",
                SizeConverter.getInstance(), 5
            ) {
                override fun isSettable(
                    n: MoreGenericValueAxis<out UpperBound>
                ): Boolean = n.minorTickCount.value == null || !n.minorTickCount.isBound

                override fun getStyleableProperty(n: MoreGenericValueAxis<out UpperBound>): StyleableProperty<Number> = n.minorTickCountProperty()
            }

        val MINOR_TICK_VISIBLE: CssMetaData<MoreGenericValueAxis<out UpperBound>, Boolean> =

            object : BooleanCssMetaData<MoreGenericValueAxis<out UpperBound>>(
                "-fx-minor-tick-visible",
                true
            ) {
                override fun isSettable(
                    n: MoreGenericValueAxis<out UpperBound>
                ): Boolean = n.minorTickVisible.value == null || !n.minorTickVisible.isBound

                override fun getStyleableProperty(n: MoreGenericValueAxis<out UpperBound>): StyleableProperty<Boolean> = n.minorTickVisibleProperty()
            }
        var classCssMetaData: List<CssMetaData<out Styleable?, *>>? = null
            private set

        /**
         * Gets the `CssMetaData` associated with this class, which may include the
         * `CssMetaData` of its superclasses.
         * @return the `CssMetaData`
         * @since JavaFX 8.0
         */

        init {
            val styleables: MutableList<CssMetaData<out Styleable?, *>> =
                ArrayList(
                    getClassCssMetaData()
                )
            styleables.add(MINOR_TICK_COUNT)
            styleables.add(MINOR_TICK_LENGTH)
            styleables.add(MINOR_TICK_COUNT)
            styleables.add(MINOR_TICK_VISIBLE)
            classCssMetaData = Collections.unmodifiableList(styleables)
        }
    }

    @Open
    /**
     * {@inheritDoc}
     * @since JavaFX 8.0
     */
    override fun getCssMetaData(): List<CssMetaData<out Styleable?, *>>? = StyleableProperties.classCssMetaData
}
