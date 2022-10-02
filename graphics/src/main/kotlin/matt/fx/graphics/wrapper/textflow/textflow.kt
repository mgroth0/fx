package matt.fx.graphics.wrapper.textflow

import javafx.scene.Node
import javafx.scene.text.TextFlow
import matt.collect.itr.mapToArray
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.text.TextWrapper
import matt.log.todo
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible

open class TextFlowWrapper<C: NodeWrapper>(
  node: TextFlow = TextFlow(),
): PaneWrapperImpl<TextFlow, C>(node) {

  constructor(vararg nodes: Node?): this(TextFlow(*nodes))
  constructor(vararg nodeWrappers: NodeWrapper?): this(*nodeWrappers.mapToArray { it?.node })

  companion object {
	val PREF_HEIGHT_UNLOCKED by lazy {
	  TextFlow::class.declaredMembers.first {
		it.name == "computePrefHeight"
	  }.apply {
		isAccessible = true
	  }
	}
  }

  fun fullText(): String {
	todo("could include any node that has text...")
	return children.filterIsInstance<TextWrapper>().joinToString("") { it.text }
  }
}

