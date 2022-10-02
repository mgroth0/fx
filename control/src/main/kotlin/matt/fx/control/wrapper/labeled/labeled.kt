package matt.fx.control.wrapper.labeled

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.scene.Node
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Labeled
import javafx.scene.text.TextAlignment
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.text.textlike.TextLike
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.NullableFXBackedBindableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp

open class LabeledWrapper<N: Labeled>(node: N): ControlWrapperImpl<N>(node), TextLike {

  fun heightProperty(): ReadOnlyDoubleProperty = node.heightProperty()

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

  var graphic
	get() = node.graphic?.wrapped()
	set(value) {
	  node.graphic = value?.node
	}

  fun graphicProperty(): ObjectProperty<Node> = node.graphicProperty()


  var contentDisplay: ContentDisplay
	get() = node.contentDisplay
	set(value) {
	  node.contentDisplay = value
	}

  override val fontProperty by lazy { node.fontProperty().toNonNullableProp() }

  var isWrapText
	get() = node.isWrapText
	set(value) {
	  node.isWrapText = value
	}

  fun wrapTextProperty(): BooleanProperty = node.wrapTextProperty()

  override val textFillProperty by lazy { node.textFillProperty().toNonNullableProp() }
  override fun addChild(child: NodeWrapper, index: Int?) {
	require(index == null)
	graphic = child
  }


}