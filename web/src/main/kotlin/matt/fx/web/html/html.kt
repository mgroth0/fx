package matt.fx.web.html

import javafx.event.EventTarget
import javafx.scene.web.HTMLEditor
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo

fun EventTarget.htmleditor(html: String? = null, op: HTMLEditorWrapper.()->Unit = {}) =
    HTMLEditorWrapper().attachTo(this.wrapped(), op) {
        if (html != null) it.htmlText = html
    }


class HTMLEditorWrapper(node: HTMLEditor = HTMLEditor()): ControlWrapperImpl<HTMLEditor>(node) {
    var htmlText: String?
        get() = node.htmlText
        set(value) {
            node.htmlText = value
        }

    override fun addChild(child: NodeWrapper, index: Int?) {
        TODO()
    }
}
