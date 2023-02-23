package matt.fx.control.wrapper.labeled

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.scene.control.Labeled
import javafx.scene.text.TextAlignment
import matt.fx.control.inter.TextAndGraphic
import matt.fx.control.inter.graphic
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.graphics.service.nullableNodeConverter
import matt.fx.graphics.stylelock.toNonNullableStyleProp
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.text.textlike.ColoredText
import matt.fx.base.wrapper.obs.obsval.prop.NullableFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp

open class LabeledWrapper<N: Labeled>(node: N): ControlWrapperImpl<N>(node), ColoredText, TextAndGraphic {

  var textAlignment: TextAlignment
	get() = node.textAlignment
	set(value) {
	  node.textAlignment = value
	}

  fun textAlignmentProperty(): ObjectProperty<TextAlignment> = node.textAlignmentProperty()


  var isMnemonicParsing
	get() = node.isMnemonicParsing
	set(value) {
	  node.isMnemonicParsing = value
	}

  fun mnemonicParsingProperty(): BooleanProperty = node.mnemonicParsingProperty()

  override val textProperty: NullableFXBackedBindableProp<String> by lazy { node.textProperty().toNullableProp() }

  final override val graphicProperty by lazy {
	node.graphicProperty().toNullableProp().proxy(nullableNodeConverter)
  }

  final override val contentDisplayProp by lazy {
	node.contentDisplayProperty().toNonNullableStyleProp()
  }

  override val fontProperty by lazy { node.fontProperty().toNonNullableProp() }

  var isWrapText
	get() = node.isWrapText
	set(value) {
	  node.isWrapText = value
	}

  val wrapTextProperty by lazy { node.wrapTextProperty().toNonNullableProp() }

  override val textFillProperty by lazy { node.textFillProperty().toNullableProp() }
  override fun addChild(child: NodeWrapper, index: Int?) {
	require(index == null)
	graphic = child
  }


}