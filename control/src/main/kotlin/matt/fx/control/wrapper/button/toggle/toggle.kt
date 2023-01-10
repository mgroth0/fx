package matt.fx.control.wrapper.button.toggle

import javafx.css.StyleOrigin.AUTHOR
import javafx.css.StyleableObjectProperty
import javafx.scene.control.ToggleButton
import javafx.scene.layout.Background
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import matt.fx.control.inter.select.Selectable
import matt.fx.control.inter.select.SelectableValue
import matt.fx.control.toggle.mech.ToggleMechanism
import matt.fx.control.wrapper.control.button.base.ButtonBaseWrapper
import matt.fx.control.wrapper.control.value.HasWritableValue
import matt.fx.graphics.style.background.backgroundFill
import matt.fx.graphics.style.background.ensureLastFillIsIfPresent
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.lang.go
import matt.obs.bind.binding
import matt.obs.listen.OldAndNewListenerImpl
import matt.obs.prop.BindableProperty
import matt.obs.prop.VarProp

/**
 * Create a togglebutton inside the current or given toggle group. The optional value parameter will be matched against
 * the extension property `selectedValueProperty()` on Toggle Group. If the #ToggleGroup.selectedValueProperty is used,
 * it's value will be updated to reflect the value for this radio button when it's selected.
 *
 * Likewise, if the `selectedValueProperty` of the ToggleGroup is updated to a value that matches the value for this
 * togglebutton, it will be automatically selected.
 */
fun <V: Any> ET.togglebutton(
  text: String? = null,
  /*group: ToggleGroup? = getToggleGroup(),*/
  group: ToggleMechanism<V>? = null,
  selectFirst: Boolean = false,
  value: V,
  op: ValuedToggleButton<V>.()->Unit = {}
) = ValuedToggleButton(value).attachTo(this, op) {
  it.text = if (/*value != null && */text == null) value.toString() else text/* ?: ""*/
  if (group != null) it.toggleMechanism.value = group
  if (it.node.toggleGroup?.selectedToggle == null && selectFirst) it.isSelected = true
}

fun <V: Any> ET.togglebutton(
  text: VarProp<String>? = null,
  group: ToggleMechanism<V>? = null,
  selectFirst: Boolean = false,
  value: V,
  op: ValuedToggleButton<V>.()->Unit = {}
) = ValuedToggleButton(value).attachTo(this, op) {
  val thing = it
  text?.go { thing.textProperty.bind(it) }
  if (group != null) it.toggleMechanism.value = group
  if (it.node.toggleGroup?.selectedToggle == null && selectFirst) it.isSelected = true
}

fun <V: Any> ET.togglebutton(
  group: ToggleMechanism<V>? = null,
  selectFirst: Boolean = false,
  value: V,
  op: ValuedToggleButton<V>.()->Unit = {}
) = ValuedToggleButton(value).attachTo(this, op) {
  if (group != null) it.toggleMechanism.value = group
  if (it.node.toggleGroup?.selectedToggle == null && selectFirst) it.isSelected = true
}


class ValuedToggleButton<V: Any>(value: V): ToggleButtonWrapper(ToggleButton()),
											HasWritableValue<V>,
											SelectableValue<V> {

  override val valueProperty = BindableProperty(value)

  override val toggleMechanism = BindableProperty<ToggleMechanism<V>?>(null).apply {
	addListener(OldAndNewListenerImpl { old, new ->
	  old?.removeToggle(this@ValuedToggleButton)
	  new?.addToggle(this@ValuedToggleButton)
	})
  }

}

open class ToggleButtonWrapper(
  node: ToggleButton = ToggleButton(),
): ButtonBaseWrapper<ToggleButton>(node), Selectable {

  override val selectedProperty by lazy {
	node.selectedProperty().toNonNullableProp()
  }

  fun whenSelected(op: ()->Unit) {
	selectedProperty.onChange { if (it) op() }
  }


  /*attempts to copy style logic from modena.css*/
  /*ignores mnemonics*/
  fun setupSelectionColor(p: Paint) {

	/*pseudoClassStates


	pseudoClassStates.onChange {

	}
	node.pseudoClassStates

	node.pseudoClassStateChanged()*/


	var updating = false
	fun update() {
	  updating = true
	  val s = selectedProperty.value
	  val sProp = (node.backgroundProperty() as StyleableObjectProperty<Background>)
	  val v = sProp.value
	  if (v != null) {
		val lastIsCurrentlyP = v.fills.last().fill == p
		if (s) {
		  if (lastIsCurrentlyP) {
			/*do nothing*/
		  } else {
			sProp.applyStyle(AUTHOR, Background(*v.fills.toTypedArray(), backgroundFill(p)))
		  }
		} else {
		  if (lastIsCurrentlyP) {
			sProp.applyStyle(
			  AUTHOR, Background(
				*v.fills.subList(0, v.fills.size - 1).toTypedArray()
			  )
			)
		  } else {
			/*do nothing*/
		  }
		}
	  }
	  updating = false
	}
	update()
	pseudoClassStates.onChange {
	  update()
	}
	node.backgroundProperty().addListener { _, _, _ ->
	  if (!updating) {
		update()
	  }
	}
	/*selectedProperty.onChange {
	  update()
	}*/


	//	backgroundProperty.bind(
	//
	//	)
	//	selectedProperty

	//
	//	@Suppress("UNCHECKED_CAST")
	//	val BACKGROUND: CssMetaData<Region, Background> = object: CssMetaData<Region, Background>(
	//	  "-fx-region-background",
	//	  MyBackgroundConverter.INSTANCE,
	//	  null,
	//	  false,
	//	  Background.getClassCssMetaData()
	//	) {
	//	  override fun isSettable(node: Region): Boolean {
	//		return !node.backgroundProperty().isBound
	//	  }
	//
	//
	//	  override fun getStyleableProperty(node: Region): StyleableProperty<Background?> {
	//		return node.backgroundProperty() as StyleableProperty<Background?>
	//	  }
	//	}
	//
	//	BACKGROUND.getInitialValue()
	//	BACKGROUND.getStyleableProperty()
	//	BACKGROUND.converter
	//	BACKGROUND.isSettable()
	//	(node.backgroundProperty() as StyleableObjectProperty<Background>).also {
	//	  it.applyStyle()
	//	  it.cssMetaData
	//	  it.styleOrigin
	//	}
	//
	//	node.lookup()
	//
	//
	//
	//	backgroundProperty.bind(
	//	  selectedProperty.binding(
	//		armedProp,
	//		hoverProperty,
	//		focusedProperty,
	//		disableProperty
	//	  ) {
	//		backgroundFromColor(Color.TRANSPARENT)
	//	  }
	//	)
	//
	//	val shouldBe = selectedProperty.binding {
	//	  if (it == true) p else Color.TRANSPARENT
	//	}
	//
	//	fun update() = backgroundProperty.ensureLastFillIsIfPresent(shouldBe.value)
	//	update()
	//	shouldBe.matt.hurricanefx.eye.wrapper.obs.collect.list.onChange {
	//	  update()
	//	}
	//	backgroundProperty.matt.hurricanefx.eye.wrapper.obs.collect.list.onChange {
	//	  update()
	//	}
  }

  @Deprecated("tries to use style sheets, but fails and looks wrong on MaxOSX Light Mode... besides I'd rather do things programmatically")
  fun setupSelectionColorOldWay(p: Paint) {
	val shouldBe = selectedProperty.binding {
	  if (it == true) p else Color.TRANSPARENT
	}

	fun update() = backgroundProperty.ensureLastFillIsIfPresent(shouldBe.value)
	update()
	shouldBe.onChange {
	  update()
	}
	backgroundProperty.onChange {
	  update()
	}
  }

}