package matt.fx.web.md

import javafx.scene.layout.Priority.ALWAYS
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.pane.vbox.VBoxW
import matt.fx.web.md.ghdark.GH_MD_DARK_CSS
import matt.fx.web.webview
import matt.lang.anno.SeeURL
import matt.lang.function.DSL
import matt.obs.prop.BindableProperty

fun ET.markdownViewer(op: DSL<MarkDownViewer> = {}) = MarkDownViewer().attachTo(this, op)

class MarkDownViewer : VBoxW() {
    private val wv = webview {
        vgrow = ALWAYS
        engine.loadContent(
            """
		<body style="background: black">
		</body>
	  """.trimIndent()
        )
    }
    val markdown = BindableProperty("").apply {

        onChange {
            @SeeURL("https://stackoverflow.com/questions/37770620/how-to-include-markdown-md-files-inside-html-files")
            @SeeURL("https://zerodevx.github.io/zero-md/basic-usage/")
            /*<body style="background-color: black;">*/
            wv.engine.loadContent(
                """
		  
		   <body style="background: black">
		  
			<!-- Lightweight client-side loader that feature-detects and load polyfills only when necessary -->
			<script src="https://cdn.jsdelivr.net/npm/@webcomponents/webcomponentsjs@2/webcomponents-loader.min.js"></script>

			<!-- Load the element definition -->
			<script type="module" src="https://cdn.jsdelivr.net/gh/zerodevx/zero-md@1/src/zero-md.min.js"></script>

			<!-- Simply set the `src` attribute to your MD file and win -->
			<zero-md>
			
			<template data-merge="append">
					<style>
			  $GH_MD_DARK_CSS
		 	</style>
			</template>
		
			  <script type="text/markdown">
			$it
			</script>
			
			</zero-md>
			
			</body>
			
		  """.trimIndent()
            )
        }
    }
}