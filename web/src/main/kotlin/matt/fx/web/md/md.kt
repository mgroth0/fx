package matt.fx.web.md

import javafx.scene.layout.Priority.ALWAYS
import matt.codegen.markdown.Markdown
import matt.codegen.markdown.embedInHtml
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.pane.vbox.VBoxW
import matt.fx.web.webview
import matt.lang.function.Dsl
import matt.obs.prop.writable.BindableProperty

fun ET.markdownViewer(op: Dsl<MarkDownViewer> = {}) = MarkDownViewer().attachTo(this, op)

class MarkDownViewer : VBoxW(childClass = NodeWrapper::class) {
    private val wv =
        webview {
            vgrow = ALWAYS
            engine.loadContent(
                """
                <body style="background: black">
                </body>
                """.trimIndent()
            )
        }
    val markdown =
        BindableProperty("").apply {
            onChange {
                wv.engine.loadContent(Markdown(it).embedInHtml().code)
            }
        }
}
