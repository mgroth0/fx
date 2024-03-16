package matt.fx.control.wrapper.control.spinner

import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory
import matt.fx.base.converter.ConverterConverter
import matt.fx.base.wrapper.obs.collect.list.createFXWrapper
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.base.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.spinner.fact.int.MyIntegerSpinnerValueFactory
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.fxthread.runLater
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.lang.common.err
import matt.lang.common.go
import matt.lang.convert.BiConverter
import matt.lang.delegation.lazyVarDelegate
import matt.model.flowlogic.recursionblocker.RecursionBlocker
import matt.obs.bind.smartBind
import matt.obs.col.olist.MutableObsList
import matt.obs.prop.ObsVal
import matt.obs.prop.proxy.ProxyProp
import matt.obs.prop.writable.BindableProperty
import matt.obs.prop.writable.Var
import matt.prim.converters.StringConverter
import kotlin.reflect.KClass


/**
 * Create a spinner for an arbitrary type. This spinner requires you to configure a value factory, or it will throw an exception.
 */
inline fun <reified T: Any> ET.spinner(
    editable: Boolean = false,
    property: Var<T>? = null,
    enableScroll: Boolean = false,
    converter: StringConverter<T>? = null,
    op: SpinnerWrapper<T>.() -> Unit = {}
) = SpinnerWrapper<T>(valueCls = T::class).also {
    it.attachTo(this, op)

    if (property != null) requireNotNull(it.valueFactory) {
        "You must configure the value factory or use the Number based spinner builder " +
            "which configures a default value factory along with min, max and initialValue!"
    }.valueProperty.apply {
        bindBidirectional(property)
    }
    it.initialConfig(
        editable = editable,
        enableScroll = enableScroll,
        converter = converter
    )
}


fun ET.intSpinner(
    min: Int? = null,
    max: Int? = null,
    initialValue: Int? = null,
    amountToStepBy: Int? = null,
    editable: Boolean = false,
    property: BindableProperty<Int>? = null,
    enableScroll: Boolean = false,
    converter: StringConverter<Int>? = null,
    op: SpinnerWrapper<Int>.() -> Unit = {}
): SpinnerWrapper<Int> {
    val spinner =
        SpinnerWrapper(
            min?.toInt() ?: 0,
            max?.toInt() ?: 100,
            initialValue?.toInt() ?: 0,
            amountToStepBy?.toInt() ?: 1
        )
    if (property != null) {
        spinner.valueFactory!!.valueProperty.bindBidirectional(property)
    }
    spinner.initialConfig(
        editable = editable,
        enableScroll = enableScroll,
        converter = converter
    )

    return spinner.attachTo(this, op)
}


fun ET.doubleSpinner(
    min: Double? = null,
    max: Double? = null,
    initialValue: Double? = null,
    amountToStepBy: Double? = null,
    editable: Boolean = false,
    property: BindableProperty<Double>? = null,
    enableScroll: Boolean = false,
    converter: StringConverter<Double>? = null,
    op: SpinnerWrapper<Double>.() -> Unit = {}
): SpinnerWrapper<Double> {
    val spinner =
        SpinnerWrapper(
            min?.toDouble() ?: 0.0, max?.toDouble() ?: 100.0,
            initialValue?.toDouble()
                ?: 0.0,
            amountToStepBy?.toDouble() ?: 1.0
        )
    if (property != null) {
        spinner.valueFactory!!.valueProperty.bindBidirectional(property)
    }
    spinner.initialConfig(
        editable = editable,
        enableScroll = enableScroll,
        converter = converter
    )

    return spinner.attachTo(this, op)
}



inline fun <reified T: Any> ET.spinner(
    items: MutableObsList<T>,
    editable: Boolean = false,
    property: Var<T>? = null,
    enableScroll: Boolean = false,
    converter: StringConverter<T>? = null,
    op: SpinnerWrapper<T>.() -> Unit = {}
) = SpinnerWrapper(items).attachTo(this, op) {
    if (property != null) it.valueFactory!!.valueProperty.apply {
        bindBidirectional(property)
    }
    it.initialConfig(
        editable = editable,
        enableScroll = enableScroll,
        converter = converter
    )
}


inline fun <reified T: Any> ET.spinner(
    valueFactory: SpinnerValueFactory<T>,
    editable: Boolean = false,
    property: Var<T>? = null,
    enableScroll: Boolean = false,
    converter: StringConverter<T>? = null,
    op: SpinnerWrapper<T>.() -> Unit = {}
) = SpinnerWrapper(valueFactory).attachTo(this, op) {
    if (property != null) it.valueFactory!!.valueProperty.apply {
        bindBidirectional(property)
    }
    it.initialConfig(
        editable = editable,
        enableScroll = enableScroll,
        converter = converter
    )
}

@PublishedApi
internal inline fun <reified T: Any> SpinnerWrapper<T>.initialConfig(
    editable: Boolean = false,
    enableScroll: Boolean = false,
    converter: StringConverter<T>?
) {
    isEditable = editable

    if (enableScroll) listenToScrolls()

    if (editable) tfxWeirdEditableThing()


    converter?.go {
        valueFactory!!.converter = it

        /*causes converter to be used GUIs so first one converts to correct string...*/
        node.editor.text = it.convertToA(value)
    }
}

class IntSpinnerWrapper(
    min: Int,
    max: Int,
    initial: Int,
    step: Int
): SpinnerWrapper<Int>(Spinner(min, max, initial, step), Int::class) {
    init {
        val svf = valueFactory?.svf as IntegerSpinnerValueFactory

        val newSVF =
            MyIntegerSpinnerValueFactory(
                min = svf.min,
                max = svf.max,
                initialValue = svf.value,
                amountToStepBy = svf.amountToStepBy
            )

        valueFactory = SpinnerValueFactoryWrapper(newSVF as SpinnerValueFactory<Int>)
    }
}

open class SpinnerWrapper<T: Any>(
    node: Spinner<T> = Spinner<T>(),
    private val valueCls: KClass<T>
): ControlWrapperImpl<Spinner<T>>(node) {

    companion object {
        operator fun invoke(
            min: Int,
            max: Int,
            initial: Int,
            step: Int
        ) = IntSpinnerWrapper(min = min, max = max, initial = initial, step = step)

        operator fun invoke(
            min: Double,
            max: Double,
            initialVal: Double,
            step: Double
        ) = SpinnerWrapper(Spinner(min, max, initialVal, step), Double::class)

        operator fun invoke(
            min: Double,
            max: Double,
            initialVal: Double
        ) = SpinnerWrapper(Spinner(min, max, initialVal), Double::class)

        inline operator fun <reified T: Any> invoke(items: MutableObsList<T>) = SpinnerWrapper(Spinner(items.createFXWrapper()), T::class)
        inline operator fun <reified T: Any> invoke(valueFactory: SpinnerValueFactory<T>) = SpinnerWrapper(Spinner(valueFactory), T::class)
    }

    init {
        if (valueCls == Int::class) {
            check(this is IntSpinnerWrapper)
        }
    }


    val editor by lazy { node.editor.wrapped() }


    val textProperty by lazy {
        editor.textProperty
    }

    fun autoCommitOnType() {
        textProperty.onChangeWithWeak(this) { spinner, _ ->
            @Suppress("UNUSED_VARIABLE") val oldSelection = spinner.node.editor.selection
            runLater {
                spinner.node.commitValue()
                spinner.node.editor.selectRange(oldSelection.start + 1, oldSelection.end + 1)
                runLater {
                    spinner.node.editor.selectRange(oldSelection.start + 1, oldSelection.end + 1)
                }
            }
        }
    }

    val value: T get() = node.value
    val valueProperty by lazy { node.valueProperty().toNonNullableROProp() }

    val valueFactoryProperty by lazy {
        node.valueFactoryProperty().toNullableProp().proxy(
            object: BiConverter<SpinnerValueFactory<T>?, SpinnerValueFactoryWrapper<T>?> {
                override fun convertToB(a: SpinnerValueFactory<T>?): SpinnerValueFactoryWrapper<T>? = a?.wrap(valueCls)

                override fun convertToA(b: SpinnerValueFactoryWrapper<T>?): SpinnerValueFactory<T>? = b?.svf
            }
        )
    }

    var valueFactory by valueFactoryProperty


    fun bindBidirectional(
        prop: Var<T?>,
        default: T,
        acceptIf: (T?) -> Boolean
    ) {
        val fact = valueFactory!!
        val rBlocker = RecursionBlocker()
        prop.onChangeWithWeak(fact) { deRefedFact, newNeuron ->
            if (acceptIf(newNeuron)) {
                rBlocker {
                    deRefedFact.valueProperty.value = newNeuron ?: default
                }
            }
        }
        valueProperty.onChangeWithWeak(prop) { deRefedProp, newValue ->
            rBlocker {
                deRefedProp.value = newValue
            }
        }
    }



    var isEditable
        get() = node.isEditable
        set(value) {
            node.isEditable = value
        }

    fun listenToScrolls() {
        setOnScroll { event ->
            if (event.deltaY > 0) increment()
            if (event.deltaY < 0) decrement()
        }
    }

    fun increment() = node.increment()
    fun decrement() = node.decrement()
    fun increment(steps: Int) = node.increment(steps)
    final override fun addChild(child: NodeWrapper, index: Int?) {
        TODO()
    }

    fun tfxWeirdEditableThing() {
        focusedProperty.onChange { newValue: Boolean? ->
            if (newValue == null) err("here it is")
            if (!newValue) increment(0)
        }
    }
}

fun <T: Any> SpinnerWrapper<T>.bind(property: ObsVal<T>, readonly: Boolean = false) =
    valueFactory!!.valueProperty.smartBind(property, readonly)


inline fun <reified T: Any> SpinnerValueFactory<T>.wrap() = SpinnerValueFactoryWrapper<T>(this)
fun <T: Any> SpinnerValueFactory<T>.wrap(cls: KClass<T>) = SpinnerValueFactoryWrapper<T>(this)


class SpinnerValueFactoryWrapper<T: Any>(
    internal val svf: SpinnerValueFactory<T>
) {
    fun increment(steps: Int) = svf.increment(steps)
    fun decrement(steps: Int) = svf.decrement(steps)
    val valueProperty by lazy {
        svf.valueProperty().toNonNullableProp()
    }


    /*internal because if you set these later, gui does NOT automatically update. No easy way to fix this other than using the extension methods at the top of the file*/
    internal val converterProperty: ProxyProp<javafx.util.StringConverter<T>, StringConverter<T>> by lazy {
        svf.converterProperty().toNonNullableProp().proxy(ConverterConverter())
    }
    @PublishedApi
    internal var converter by converterProperty

    val wrapAroundProperty: NonNullFXBackedBindableProp<Boolean> by lazy {
        svf.wrapAroundProperty().toNonNullableProp()
    }

    var wrapAround by lazyVarDelegate {
        wrapAroundProperty
    }
}
