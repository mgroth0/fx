package matt.fx.control.wrapper.control.spinner.fact.int

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.value.ObservableValue
import javafx.scene.control.SpinnerValueFactory

/*avoids stupid errors*/
class MyIntegerSpinnerValueFactory @JvmOverloads constructor(
  min: Int,
  max: Int,
  initialValue: Int = min,
  amountToStepBy: Int = 1
): SpinnerValueFactory<Int>() {
  /* *********************************************************************
         *                                                                     *
         * Properties                                                          *
         *                                                                     *
         **********************************************************************/ // --- min
  private val min: IntegerProperty = object: SimpleIntegerProperty(this, "min") {
	override fun invalidated() {
	  val currentValue = this@MyIntegerSpinnerValueFactory.value ?: return
	  val newMin = get()
	  if (newMin > getMax()) {
		setMin(getMax())
		return
	  }
	  if (currentValue < newMin) {
		this@MyIntegerSpinnerValueFactory.value = newMin
	  }
	}
  }

  fun setMin(value: Int) {
	min.set(value)
  }

  fun getMin(): Int {
	return min.get()
  }

  /**
   * Sets the minimum allowable value for this value factory
   * @return the minimum allowable value for this value factory
   */
  fun minProperty(): IntegerProperty {
	return min
  }

  // --- max
  private val max: IntegerProperty = object: SimpleIntegerProperty(this, "max") {
	override fun invalidated() {
	  val currentValue = this@MyIntegerSpinnerValueFactory.value ?: return
	  val newMax = get()
	  if (newMax < getMin()) {
		setMax(getMin())
		return
	  }
	  if (currentValue > newMax) {
		this@MyIntegerSpinnerValueFactory.value = newMax
	  }
	}
  }

  fun setMax(value: Int) {
	max.set(value)
  }

  fun getMax(): Int {
	return max.get()
  }

  /**
   * Sets the maximum allowable value for this value factory
   * @return the maximum allowable value for this value factory
   */
  fun maxProperty(): IntegerProperty {
	return max
  }

  // --- amountToStepBy
  private val amountToStepBy: IntegerProperty = SimpleIntegerProperty(this, "amountToStepBy")
  /**
   * Constructs a new IntegerSpinnerValueFactory.
   *
   * @param min The minimum allowed integer value for the Spinner.
   * @param max The maximum allowed integer value for the Spinner.
   * @param initialValue The value of the Spinner when first instantiated, must
   * be within the bounds of the min and max arguments, or
   * else the min value will be used.
   * @param amountToStepBy The amount to increment or decrement by, per step.
   */
  /**
   * Constructs a new IntegerSpinnerValueFactory with a default
   * `amountToStepBy` of one.
   *
   * @param min The minimum allowed integer value for the Spinner.
   * @param max The maximum allowed integer value for the Spinner.
   * @param initialValue The value of the Spinner when first instantiated, must
   * be within the bounds of the min and max arguments, or
   * else the min value will be used.
   *//* *********************************************************************
         *                                                                     *
         * Constructors                                                        *
         *                                                                     *
         **********************************************************************/
  /**
   * Constructs a new IntegerSpinnerValueFactory that sets the initial value
   * to be equal to the min value, and a default `amountToStepBy` of one.
   *
   * @param min The minimum allowed integer value for the Spinner.
   * @param max The maximum allowed integer value for the Spinner.
   */
  init {
	setMin(min)
	setMax(max)
	setAmountToStepBy(amountToStepBy)


	/*this is the key change I (Matt) introduced: if the user doesn't enter a valid int, just default to the minimum.*/
	converter = object: javafx.util.StringConverter<Int?>() {
	  override fun toString(`object`: Int?): String {
		return `object`.toString()
	  }

	  override fun fromString(string: String): Int? {
		return string.toIntOrNull() ?: value
	  }
	}



	valueProperty().addListener { _: ObservableValue<out Int?>?, _: Int?, newValue: Int? ->
	  if (newValue == null) return@addListener

	  // when the value is set, we need to react to ensure it is a
	  // valid value (and if not, blow up appropriately)
	  if (newValue < getMin()) {
		value = getMin()
	  } else if (newValue > getMax()) {
		value = getMax()
	  }
	}
	value = if (initialValue >= min && initialValue <= max) initialValue else min
  }

  fun setAmountToStepBy(value: Int) {
	amountToStepBy.set(value)
  }

  fun getAmountToStepBy(): Int {
	return amountToStepBy.get()
  }

  /**
   * Sets the amount to increment or decrement by, per step.
   * @return the amount to increment or decrement by, per step
   */
  fun amountToStepByProperty(): IntegerProperty {
	return amountToStepBy
  }/* *********************************************************************
         *                                                                     *
         * Overridden methods                                                  *
         *                                                                     *
         **********************************************************************/
  /** {@inheritDoc}  */
  override fun decrement(steps: Int) {
	val min = getMin()
	val max = getMax()
	val newIndex = value!! - steps*getAmountToStepBy()
	value = if (newIndex >= min) newIndex else if (isWrapAround) wrapValuePublic(newIndex, min, max) + 1 else min
  }

  /** {@inheritDoc}  */
  override fun increment(steps: Int) {
	val min = getMin()
	val max = getMax()
	val currentValue = value!!
	val newIndex = currentValue + steps*getAmountToStepBy()
	value = if (newIndex <= max) newIndex else if (isWrapAround) wrapValuePublic(newIndex, min, max) - 1 else max
  }
}

/*
     * Convenience method to support wrapping values around their min / max
     * constraints. Used by the SpinnerValueFactory implementations when
     * the Spinner wrapAround property is true.
     */
private fun wrapValuePublic(value: Int, min: Int, max: Int): Int {
  if (max == 0) {
	throw RuntimeException()
  }
  var r = value%max
  if (r > min && max < min) {
	r = r + max - min
  } else if (r < min && max > min) {
	r = r + max - min
  }
  return r
}
