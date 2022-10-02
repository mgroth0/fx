package matt.fx.control.wrapper.control.tab

import javafx.beans.binding.BooleanBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.hurricanefx.eye.bind.toBinding
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.eye.prop.cleanBind
import matt.hurricanefx.eye.prop.getValue
import matt.hurricanefx.eye.prop.setValue

open class TabWrapper<C: NodeWrapper>(
  node: Tab = Tab()
): SingularEventTargetWrapper<Tab>(node) {

  override fun isInsideRow() = false

  constructor(
	text: String?,
	content: C? = null
  ): this(Tab(text, content?.node))

  var isClosable by node.closableProperty()

  override val properties get() = node.properties

  override fun removeFromParent() {
	node.tabPane?.tabs?.remove(node)
  }

  val contentProperty: ObjectProperty<Node> get() = node.contentProperty()

  open var content: C
	@Suppress("UNCHECKED_CAST")
	get() = node.content.wrapped() as C
	set(value) = contentProperty.set(value.node)

  var graphic by node.graphicProperty()
  val disabledProperty: ReadOnlyBooleanProperty get() = node.disabledProperty()
  val disableProperty: BooleanProperty get() = node.disableProperty()
  val closableProperty: BooleanProperty get() = node.closableProperty()
  val selectedProperty: ReadOnlyBooleanProperty get() = node.selectedProperty()
  val isSelected by selectedProperty
  val tabPane: TabPane? get() = node.tabPane


  fun disableWhen(predicate: ObservableValue<Boolean>) = disableProperty.cleanBind(predicate)

  fun enableWhen(predicate: ObservableValue<Boolean>) {
	val binding = if (predicate is BooleanBinding) predicate.not() else predicate.toBinding().not()
	disableProperty.cleanBind(binding)
  }

  fun closeableWhen(predicate: ObservableValue<Boolean>) {
	closableProperty.bind(predicate)
  }

  fun visibleWhen(predicate: ObservableValue<Boolean>) {
	val localTabPane = tabPane
	fun updateState() {
	  if (predicate.value.not()) localTabPane!!.tabs.remove(this.node)
	  else if (this.node !in tabPane!!.tabs) localTabPane!!.tabs.add(this.node)
	}
	updateState()
	predicate.onChange { updateState() }
  }

  fun close() = removeFromParent()

  //fun TabPane.matt.prim.str.build.tab(text: String? = null, node: Node? = null, op: Tab.() -> Unit = {}): Tab {
  //    val matt.prim.str.build.tab = Tab(text,node)
  ////    matt.prim.str.build.tab.tag = tag
  //    tabs.add(matt.prim.str.build.tab)
  //    return matt.prim.str.build.tab.also(op)
  //}

  fun whenSelected(op: ()->Unit) {
	selectedProperty.onChange { if (it) op() }
  }

  fun select() = apply { tabPane!!.selectionModel.select(this.node) }

  override fun addChild(child: NodeWrapper, index: Int?) {
	require(index == null)
	contentProperty.set(child.node)
  }
}