package matt.fx.node.file

import javafx.application.Platform.runLater
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority.ALWAYS
import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.img
import kotlinx.html.stream.createHTML
import matt.async.thread.daemon
import matt.async.thread.schedule.AccurateTimer
import matt.async.thread.schedule.every
import matt.auto.openInFinder
import matt.css.props.Color.black
import matt.css.sty
import matt.file.ext.FileExtension.Companion.APP
import matt.file.ext.FileExtension.Companion.COFFEESCRIPT
import matt.file.ext.FileExtension.Companion.CSS
import matt.file.ext.FileExtension.Companion.GRADLE
import matt.file.ext.FileExtension.Companion.HTML
import matt.file.ext.FileExtension.Companion.JAVA
import matt.file.ext.FileExtension.Companion.JS
import matt.file.ext.FileExtension.Companion.JSON
import matt.file.ext.FileExtension.Companion.KT
import matt.file.ext.FileExtension.Companion.KTS
import matt.file.ext.FileExtension.Companion.LESS
import matt.file.ext.FileExtension.Companion.PY
import matt.file.ext.FileExtension.Companion.SH
import matt.file.ext.FileExtension.Companion.SVG
import matt.file.ext.FileExtension.Companion.TAGS
import matt.file.ext.FileExtension.Companion.TXT
import matt.file.ext.FileExtension.Companion.YML
import matt.file.ext.finalExtensionOrNull
import matt.file.ext.hasAnyExtension
import matt.file.ext.hasExtension
import matt.file.ext.isImage
import matt.file.ext.singleExtension
import matt.file.toJioFile
import matt.file.types.typed
import matt.fx.control.lang.actionbutton
import matt.fx.control.wrapper.checkbox.checkbox
import matt.fx.control.wrapper.control.button.button
import matt.fx.control.wrapper.control.spinner.spinner
import matt.fx.control.wrapper.control.text.area.textarea
import matt.fx.graphics.drag.BetterTransferMode
import matt.fx.graphics.drag.dragsFile
import matt.fx.graphics.fxthread.runLaterReturn
import matt.fx.graphics.icon.Icon
import matt.fx.graphics.icon.ObsStringIcon
import matt.fx.graphics.wrapper.imageview.imageview
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.disableWhen
import matt.fx.graphics.wrapper.pane.SimplePaneWrapper
import matt.fx.graphics.wrapper.pane.hbox.hbox
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.fx.graphics.wrapper.pane.vbox.vbox
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.text.TextWrapper
import matt.fx.node.file.tree.fileTreeAndViewerPane
import matt.fx.web.WebViewPane
import matt.fx.web.WebViewWrapper
import matt.fx.web.specialZooming
import matt.gui.interact.doubleClickToOpenInWindow
import matt.gui.interact.openImageInWindow
import matt.gui.interact.openInNewWindow
import matt.gui.interact.safe
import matt.gui.menu.context.mcontextmenu
import matt.gui.mstage.WMode.CLOSE
import matt.lang.err
import matt.lang.file.toJFile
import matt.lang.model.file.FsFile
import matt.lang.model.file.types.FolderType
import matt.lang.model.file.types.Html
import matt.lang.model.file.types.Log
import matt.lang.model.file.types.Svg
import matt.obs.bind.binding
import matt.obs.prop.BindableProperty
import matt.obs.prop.ObsVal
import matt.shell.context.ReapingShellExecutionContext
import matt.time.dur.sec
import java.lang.Thread.sleep
import java.lang.ref.WeakReference

fun matt.file.JioFile.draggableIcon() = staticIcon().apply {
    dragsFile(this@draggableIcon, mode = BetterTransferMode.COPY)
}

fun matt.file.JioFile.staticIcon() = Icon(iconString())

private fun matt.file.JioFile.iconString() = when {
    (isDirectory)                    -> "white/folder"
    (finalExtensionOrNull() == null) -> "file/bin"
    else                             -> "file/${singleExtension.afterDot}"/*, invert = extension in listOf("md", "txt")*/
}

fun ObsVal<out FsFile>.draggableIcon() = ObsStringIcon(binding {
    it.toJioFile().iconString()
}).apply {
    dragsFile(this@draggableIcon, mode = BetterTransferMode.COPY)
}


context(ReapingShellExecutionContext)
fun matt.file.JioFile.createNode(renderHTMLAndSVG: Boolean = false): RegionWrapper<NodeWrapper> {
    val node = createNodeInner(renderHTMLAndSVG = renderHTMLAndSVG)
    node.mcontextmenu {
        item(s = "", g = draggableIcon())
        actionitem("show in finder") {
            openInFinder()
        }
    }
    return node
}

context(ReapingShellExecutionContext)
private fun matt.file.JioFile.createNodeInner(renderHTMLAndSVG: Boolean = false): RegionWrapper<NodeWrapper> {
    if (exists()) {
        println("opening file")
        if (isImage()) {
            return SimplePaneWrapper<NodeWrapper>().apply {
                imageview {
                    image = Image(toURI().toString())
                    isPreserveRatio = true
                    bindFitTo(this@apply)
                    isSmooth = true
                    mcontextmenu {
                        item("os open") {
                            setOnAction {
                                java.awt.Desktop.getDesktop().open(this@createNodeInner.toJFile())
                            }
                        }
                        item("test open in new window") { onAction = EventHandler { openImageInWindow() } }
                    }
                    doubleClickToOpenInWindow()
                }
            }
        }




        if (hasAnyExtension(
                TXT, YML, JSON, SH, KT, JAVA, PY, KTS, GRADLE, CSS, LESS, JS, TAGS, COFFEESCRIPT
            ) || (!renderHTMLAndSVG && hasAnyExtension(HTML, SVG))
        ) {
            val viewbox = SimplePaneWrapper<NodeWrapper>()
            var fsText = readText()
            val ta = viewbox.textarea {
                text = fsText
                prefHeightProperty.bind(viewbox.prefHeightProperty)
                prefWidthProperty.bind(viewbox.prefWidthProperty)
            }
            viewbox.button("matt.gui.ser.save changes") {
                isDisable = true
                setOnAction {
                    writeText(ta.text)
                }
                ta.textProperty.onChange {
                    val fsTextCurrent = readText()
                    if (fsTextCurrent != fsText) {
                        safe("file content on system changed. Reload?") {
                            text = fsTextCurrent
                            fsText = fsTextCurrent
                        }
                    } else {
                        isDisable = fsTextCurrent == text
                    }
                }
            }
            if (hasAnyExtension(HTML, SVG) && !renderHTMLAndSVG) {
                viewbox.button("render") {
                    setOnAction {
                        WebViewPane(this@createNodeInner).openInNewWindow(wMode = CLOSE)
                    }
                }
            }
            return viewbox
        }






        return when (this.typed().fileType) {
            Log        -> VBoxWrapperImpl<NodeWrapper>().apply {
                val lineLimit = BindableProperty(1000)
                val infiniteLines = BindableProperty(false)
                hbox<NodeWrapper> {
                    checkbox("infinite", property = infiniteLines)
                    spinner(property = lineLimit, amountToStepBy = 1000) {
                        disableWhen { infiniteLines }
                    }
                    actionbutton("perma-clear") {
                        this@createNodeInner.write("")
                    }
                }

                textarea {
                    vgrow = ALWAYS
                    addEventFilter(KeyEvent.KEY_TYPED) { it.consume() }
                    addEventFilter(KeyEvent.KEY_PRESSED) {
                        if (it.code in listOf(
                                KeyCode.DELETE, KeyCode.BACK_SPACE
                            )
                        ) it.consume()
                    }
                    val weakRef = WeakReference(this)

                    @Synchronized
                    fun refresh() {
                        val ta = weakRef.get() ?: return
                        val linLim = if (infiniteLines.value) Integer.MAX_VALUE else lineLimit.value
                        var lines = readText().lines()
                        if (lines.size > linLim) {
                            lines = lines.subList(lines.size - linLim, lines.size)
                        }
                        val newText = lines.joinToString("\n")
                        runLaterReturn {
                            ta.text = newText
                            runLater { ta.end() }
                        }
                    }

                    lineLimit.onChange { daemon("lineLimit Thread") { refresh() } }
                    infiniteLines.onChange { daemon("infiniteLines Thread") { refresh() } }

                    var lastMod = 0L
                    every(1.sec, timer = AccurateTimer(name = "log refresher"), zeroDelayFirst = true) {
                        if (weakRef.get() == null) cancel()
                        val mod = lastModified()
                        if (mod != lastMod) {
                            refresh()
                            lastMod = mod
                        }
                    }
                }
            }

            Html       -> WebViewPane(this@createNodeInner).apply {
                specialZooming()
            }

            Svg        -> WebViewWrapper().apply {
                runLater {
                    vgrow = ALWAYS
                    hgrow = ALWAYS

                    //		  this.scroll

                    specialZooming()

                    blendMode


                    /*  engine.loadWorker.stateProperty().addListener { _, _, newValue ->
                        if (newValue == State.RUNNING || newValue == State.SUCCEEDED) {
                          engine.executeScript("document.body.style.overflow = 'hidden';")
                        }
                      }*/

                    // hide webview scrollbars whenever they appear.
                    // hide webview scrollbars whenever they appear.
                    childrenUnmodifiable.observe {
                        val deadSeaScrolls: Set<Node> = lookupAll(".scroll-bar")
                        for (scroll in deadSeaScrolls) {
                            scroll.isVisible = false
                        }
                    }
                    //		  childrenUnmodifiable.addListener(ListChangeListener<Any?> {
                    //
                    //		  })


                    val cacheBreaker = "?${System.currentTimeMillis()}" /*omfg it works...*/


                    /*because i need that black background, and this is the only way i think*/
                    val svgHTML = createHTML().apply {
                        html {
                            body {
                                sty.background = black
                                img {
                                    src = "${toURI().toURL()}$cacheBreaker"
                                    alt = "bad svg"
                                }
                            }
                        }
                    }.finalize()

                    engine.loadContent(svgHTML)

                    //		  thread {
                    //			while (true) {
                    //			  matt.time.dur.sleep(1000)
                    //			  runLater { engine.reload() }
                    //			}
                    //		  }

                    println("opening window")
                }

            }.let { wv ->

                val weakRef = WeakReference(wv)

                /*areas around for right clicking!*/
                val root = VBoxWrapperImpl<NodeWrapper>().apply {
                    mcontextmenu {
                        "refresh" does {


                            /*wv.engine.document.normalizeDocument()*/ /*shot in the dark, but not supported yet*/

                            //			  wv.engine.


                            wv.engine.reload()
                        }
                    }
                    //		  yellow()
                    //		  vgrow = ALWAYS
                    //		  hgrow = ALWAYS
                    hbox<NodeWrapper> { exactHeight = 10.0 }
                    hbox<NodeWrapper> {
                        vgrow = ALWAYS
                        hgrow = ALWAYS
                        vbox<NodeWrapper> { exactWidth = 10.0 }
                        add(wv)
                        vbox<NodeWrapper> { exactWidth = 10.0 }
                    }
                    hbox<NodeWrapper> { exactHeight = 10.0 }
                }

                val svgText = this@createNodeInner.readText()
                val svgWidthPx = svgText.substringAfter("width=\"").substringBefore("px").toInt()
                val svgHeightPx = svgText.substringAfter("height=\"").substringBefore("px").toInt()

                runLater {
                    val widthRatio = wv.width / svgWidthPx
                    val heightRatio = wv.height / svgHeightPx
                    wv.zoom *= minOf(widthRatio, heightRatio)
                    wv.zoom *= 2 /*idk*/
                }


                runLater {
                    daemon("createNode Inner Thread") {
                        var mtime = lastModified()
                        while (true) {
                            sleep(1000)
                            val wvv = weakRef.get() ?: break
                            val newmtime = lastModified()
                            if (newmtime != mtime) {
                                mtime = newmtime
                                runLater {
                                    wvv.engine.reload()
                                }
                            }
                        }
                    }
                }
                root
            }

            FolderType -> when {
                !hasExtension(APP) -> fileTreeAndViewerPane(this).apply {
                    prefWidth = 600.0
                }

                else               -> err("how to make node for files with extension:${singleExtension}")
            }

            else       -> err("how to make node for files with extension:${singleExtension}")
        }
    } else return VBoxWrapperImpl(TextWrapper("file $this does not exist"))
}





