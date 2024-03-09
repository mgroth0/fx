package matt.fx.control.wrapper.control.choice

import javafx.beans.property.ObjectProperty
import javafx.event.ActionEvent
import javafx.scene.control.ChoiceBox
import javafx.scene.input.KeyCode.ENTER
import javafx.scene.input.KeyCode.ESCAPE
import javafx.scene.input.KeyCode.SPACE
import javafx.scene.text.Font
import javafx.util.StringConverter
import matt.fx.base.wrapper.obs.collect.list.createFXWrapper
import matt.fx.base.wrapper.obs.collect.list.mfxMutableListConverter
import matt.fx.base.wrapper.obs.obsval.prop.NullableFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.value.HasWritableValue
import matt.fx.control.wrapper.selects.SelectingControl
import matt.fx.control.wrapper.selects.wrap
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.lang.anno.SeeURL
import matt.lang.assertions.require.requireNull
import matt.lang.common.go
import matt.lang.delegation.lazyVarDelegate
import matt.obs.bind.smartBind
import matt.obs.col.olist.MutableObsList
import matt.obs.col.olist.toBasicObservableList
import matt.obs.prop.ValProp
import matt.obs.prop.writable.BindableProperty
import matt.prim.str.upper
import matt.time.dur.common.sec
import java.lang.System.currentTimeMillis
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <T : Any> ET.choicebox(
    property: BindableProperty<T>? = null,
    nullableProp: BindableProperty<T?>? = null,
    values: List<T>? = null,
    op: ChoiceBoxWrapper<T>.() -> Unit = {}
): ChoiceBoxWrapper<T> {
    contract {
        callsInPlace(op, InvocationKind.EXACTLY_ONCE)
    }
    return ChoiceBoxWrapper<T>().attachTo(this, op) {
        if (values != null) it.items = (values as? MutableObsList<T>) ?: values.toBasicObservableList()
        if (property != null) it.bind(property)
        if (nullableProp != null) {
            requireNull(property)
            it.bind(nullableProp)
        }
    }
}


inline fun <T : Any> ET.choicebox(
    property: BindableProperty<T>? = null,
    nullableProp: BindableProperty<T?>? = null,
    values: Array<T>? = null,
    op: ChoiceBoxWrapper<T>.() -> Unit = {}
) = choicebox(property, nullableProp = nullableProp, values?.toList(), op)


class ChoiceBoxWrapper<T : Any>(
    node: ChoiceBox<T> = ChoiceBox()
) : ControlWrapperImpl<ChoiceBox<T>>(node), SelectingControl<T>, HasWritableValue<T?> {

    constructor(items: MutableObsList<T>) : this(ChoiceBox(items.createFXWrapper()))


    @SeeURL("https://stackoverflow.com/a/15023364/6596010")
    @SeeURL("https://docs.oracle.com/cd/E17802_01/javafx/javafx/1.3/docs/api/javafx.scene/doc-files/cssref.html#typefont")
    fun stupidlySetFont(font: Font) {
        require(style.isNullOrBlank())
        style = "-fx-font: ${font.size}px \"${font.name}\";"
    }


    var converter: StringConverter<T?>?
        get() = node.converter
        set(value) {
            node.converter = value
        }

    val converterProperty: ObjectProperty<StringConverter<T>> by lazy {
        node.converterProperty()
    }

    fun <R> convertBy(op: (T) -> R) {
        converter =
            object : StringConverter<T?>() {
                override fun toString(`object`: T?): String = `object`?.let { op(it) }.toString()

                override fun fromString(string: String?): T? {
                    TODO()
                }
            }
    }

    val itemsProperty by lazy { node.itemsProperty().toNullableProp().proxy(mfxMutableListConverter<T>().nullable()) }
    var items by lazyVarDelegate { itemsProperty }


    override val valueProperty: NullableFXBackedBindableProp<T> by lazy { node.valueProperty().toNullableProp() }

    fun setOnAction(op: (ActionEvent) -> Unit) {
        node.setOnAction(op)
    }

    override val selectionModel by lazy { node.selectionModel.wrap() }

    fun selectOnType() {
        val timer = 1.sec
        var lastKey: Long? = null
        var recent = ""
        setOnKeyTyped { e ->
            if (e.code !in listOf(ESCAPE, SPACE, ENTER)) {
                e.character.go { letter ->
                    val now = currentTimeMillis()
                    if (lastKey != null && (now - lastKey!! > timer.inWholeMilliseconds)) recent = ""
                    lastKey = now

                    recent += letter.upper()

                    items?.asSequence()?.map { it to (converter?.toString(it) ?: it.toString()).uppercase() }?.onEach {
                        if (it.second.startsWith(recent)) {
                            select(it.first)
                        }
                    }?.firstOrNull {
                        it.second.contains(recent)
                    }?.let {
                        select(it.first)
                        e.consume()
                        return@setOnKeyTyped
                    } ?: run {
                        recent = ""
                    }
                    e.consume()
                }
            }
        }
    }

    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }

    fun action(op: () -> Unit) = setOnAction { op() }
}

fun <T : Any> ChoiceBoxWrapper<T>.bind(
    property: ValProp<T?>,
    readonly: Boolean = false
) =
    valueProperty.smartBind(property, readonly)

@Suppress("ForbiddenAnnotation")
@JvmName("bindNonNull")
fun <T : Any> ChoiceBoxWrapper<T>.bind(
    property: ValProp<T>,
    readonly: Boolean = false
) =
    valueProperty.smartBind(property.cast(), readonly)
