@file:Suppress("UNUSED_PARAMETER")

package matt.fx.control.wrapper.titled

import javafx.scene.Node
import javafx.scene.control.TitledPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.util.StringConverter
import matt.fx.control.wrapper.labeled.LabeledWrapper
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.wrapper.inter.titled.Titled
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.lang.err
import matt.obs.prop.MObservableValNewAndOld
import java.text.Format

open class TitledPaneWrapper(
  node: TitledPane = TitledPane(),
): LabeledWrapper<TitledPane>(node), Titled {

  override val titleProperty by lazy { textProperty }

  var content: Node?
	get() = node.content
	set(value) {
	  node.content = value
	}

  var isCollapsible: Boolean
	get() = node.isCollapsible
	set(value) {
	  node.isCollapsible = value
	}
  var isExpanded: Boolean
	get() = node.isExpanded
	set(value) {
	  node.isExpanded = value
	}

  override fun addChild(child: NodeWrapper, index: Int?) {
	when (content) {
	  is Pane -> content!!.wrapped().addChild(child, index)

	  is Node -> {
		val container = VBox()
		container.children.addAll(content, child.node)
		content = container
	  }

	  else    -> {
		content = child.node
	  }
	}
  }
}


inline fun <reified S: T, reified T: Any> TitledPaneWrapper.bind(
  property: MObservableValNewAndOld<S>,
  readonly: Boolean = false,
  converter: StringConverter<T>? = null,
  format: Format? = null
): Unit = err("bindStringProperty(textProperty, converter, format, property, readonly)")
