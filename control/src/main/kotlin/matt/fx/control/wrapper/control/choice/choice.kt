package matt.fx.control.wrapper.control.choice

import javafx.beans.property.ObjectProperty
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.scene.control.ChoiceBox
import javafx.scene.input.KeyCode.ENTER
import javafx.scene.input.KeyCode.ESCAPE
import javafx.scene.input.KeyCode.SPACE
import javafx.util.StringConverter
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.value.HasWritableValue
import matt.fx.control.wrapper.selects.SelectingControl
import matt.fx.control.wrapper.selects.wrap
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.collect.asObservable
import matt.hurricanefx.eye.wrapper.obs.collect.createFXWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.NullableFXBackedBindableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp
import matt.lang.go
import matt.obs.bind.smartBind
import matt.obs.col.olist.ObsList
import matt.obs.prop.BindableProperty
import matt.obs.prop.ValProp
import matt.prim.str.upper
import matt.time.dur.sec
import java.lang.System.currentTimeMillis
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

inline fun <T: Any> ET.choicebox(
  property: BindableProperty<T?>? = null,
  values: List<T>? = null,
  op: ChoiceBoxWrapper<T>.()->Unit = {}
): ChoiceBoxWrapper<T> {
  contract {
	callsInPlace(op, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
  }
  return ChoiceBoxWrapper<T>().attachTo(this, op) {
	if (values != null) it.items = (values as? ObservableList<T>) ?: values.asObservable()
	if (property != null) it.bind(property)
  }
}

inline fun <T: Any> ET.choicebox(
  property: BindableProperty<T?>? = null,
  values: Array<T>? = null,
  op: ChoiceBoxWrapper<T>.()->Unit = {}
) = choicebox(property, values?.toList(), op)


inline fun <T: Any> choicebox(
  property: BindableProperty<T>? = null,
  values: List<T>? = null,
  op: ChoiceBoxWrapper<T>.()->Unit = {}
): ChoiceBoxWrapper<T> {
  contract {
	callsInPlace(op, EXACTLY_ONCE)
  }
  return ChoiceBoxWrapper<T>().also {
	it.op()
	if (values != null) it.items = (values as? ObservableList<T>) ?: values.asObservable()
	if (property != null) it.bind(property)
  }
}


class ChoiceBoxWrapper<T: Any>(
  node: ChoiceBox<T> = ChoiceBox(),
): ControlWrapperImpl<ChoiceBox<T>>(node), SelectingControl<T>, HasWritableValue<T?> {

  constructor(items: ObsList<T>): this(ChoiceBox(items.createFXWrapper()))


  var converter: StringConverter<T?>?
	get() = node.converter
	set(value) {
	  node.converter = value
	}

  fun converterProperty(): ObjectProperty<StringConverter<T>> = node.converterProperty()


  var items: ObservableList<T>
	get() = node.items
	set(value) {
	  node.items = value
	}

  override val valueProperty: NullableFXBackedBindableProp<T> by lazy { node.valueProperty().toNullableProp() }

  fun setOnAction(op: (ActionEvent)->Unit) {
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

		  items
			.asSequence()
			.map { it to (converter?.toString(it) ?: it.toString()).uppercase() }
			.onEach {
			  if (it.second.startsWith(recent)) {
				select(it.first)
			  }
			}.firstOrNull {
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

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }

  fun action(op: ()->Unit) = setOnAction { op() }
}

fun <T: Any> ChoiceBoxWrapper<T>.bind(property: ValProp<T?>, readonly: Boolean = false) =
  valueProperty.smartBind(property, readonly)

@JvmName("bindNonNull") fun <T: Any> ChoiceBoxWrapper<T>.bind(property: ValProp<T>, readonly: Boolean = false) =
  valueProperty.smartBind(property.cast(), readonly)