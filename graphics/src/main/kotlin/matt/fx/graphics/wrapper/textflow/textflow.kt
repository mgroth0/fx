package matt.fx.graphics.wrapper.textflow

import javafx.scene.Node
import javafx.scene.text.TextFlow
import matt.collect.itr.mapToArray
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.text.TextWrapper
import matt.log.warn.common.warn
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible


inline fun <reified C: NodeWrapper> ET.textflow(
    op: TextFlowWrapper<C>.() -> Unit = {
    }
) = TextFlowWrapper<C>(childClass = C::class).attachTo(this, op)

open class TextFlowWrapper<C: NodeWrapper>(
    node: TextFlow = TextFlow(),
    childClass: KClass<C>
): PaneWrapperImpl<TextFlow, C>(node, childClass) {




    companion object {
        operator fun invoke(vararg nodes: Node?) = TextFlowWrapper(TextFlow(*nodes), NodeWrapper::class)
        operator fun invoke(vararg nodeWrappers: NodeWrapper?) = TextFlowWrapper(*nodeWrappers.mapToArray { it?.node })
        val PREF_HEIGHT_UNLOCKED by lazy {
            TextFlow::class.declaredMembers.first {
                it.name == "computePrefHeight"
            }.apply {
                isAccessible = true
            }
        }
    }

    fun fullText(): String {
        warn("could include any node that has text...")
        return children.filterIsInstance<TextWrapper>().joinToString("") { it.text }
    }
}

