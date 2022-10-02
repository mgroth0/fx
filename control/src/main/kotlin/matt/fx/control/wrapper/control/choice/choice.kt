package matt.fx.control.wrapper.control.choice

import javafx.beans.property.ObjectProperty
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.scene.control.ChoiceBox
import javafx.scene.control.SingleSelectionModel
import javafx.scene.input.KeyCode.ENTER
import javafx.scene.input.KeyCode.ESCAPE
import javafx.scene.input.KeyCode.SPACE
import javafx.util.StringConverter
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.value.HasWritableValue
import matt.fx.control.wrapper.selects.SelectingControl
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.hurricanefx.eye.bind.smartBind
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.NullableFXBackedBindableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp
import matt.lang.go
import matt.obs.prop.ValProp
import matt.prim.str.upper
import matt.time.dur.sec
import java.lang.System.currentTimeMillis

class ChoiceBoxWrapper<T>(
  node: ChoiceBox<T> = ChoiceBox(),
): ControlWrapperImpl<ChoiceBox<T>>(node), SelectingControl<T>, HasWritableValue<T?> {

  constructor(items: ObservableList<T>): this(ChoiceBox(items))


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

  override val selectionModel: SingleSelectionModel<T> = node.selectionModel

  fun selectOnType() {
	val timer = 1.sec
	var lastKey: Long? = null
	var recent = ""
	setOnKeyTyped { e ->
	  if (e.code !in listOf(ESCAPE, SPACE, ENTER)) {
		e.character.go { letter ->
		  val now = currentTimeMillis()
		  if (lastKey != null && (now - lastKey!! > timer.inMilliseconds)) recent = ""
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

fun <T> ChoiceBoxWrapper<T>.bind(property: ValProp<T?>, readonly: Boolean = false) =
  valueProperty.smartBind(property, readonly)

@JvmName("bindNonNull") fun <T> ChoiceBoxWrapper<T>.bind(property: ValProp<T>, readonly: Boolean = false) =
  valueProperty.smartBind(property.cast(), readonly)