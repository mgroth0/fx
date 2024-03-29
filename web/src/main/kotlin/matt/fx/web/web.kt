package matt.fx.web

import javafx.application.Platform
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import matt.fx.base.mtofx.createROFXPropWrapper
import matt.fx.control.lang.actionbutton
import matt.fx.graphics.clip.copyToClipboard
import matt.fx.graphics.fxthread.runLater
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.node.parent.ParentWrapperImpl
import matt.fx.graphics.wrapper.node.setOnDoubleClick
import matt.fx.graphics.wrapper.pane.PaneWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.fx.web.img.ImageRefreshingWebView
import matt.gui.interact.openInNewWindow
import matt.gui.menu.context.mcontextmenu
import matt.lang.assertions.require.requireNotEqual
import matt.lang.common.NEVER
import matt.lang.model.file.FsFile
import netscape.javascript.JSObject
import org.intellij.lang.annotations.Language

fun NW.testWebView(op: WebViewWrapper.() -> Unit = {}) =
    webview {
        /*engine.loadContent("<html><body>hello world</html></body>")*/
        fun doLoad() =
            Platform.runLater {
                engine.load("http://info.cern.ch/hypertext/WWW/TheProject.html")
            }
        doLoad()
        setOnMouseClicked {
            println("doing load")
            doLoad()
            println("send load command")
        }
        op()
    }

fun WebViewWrapper.exactWidthProperty() =
    SimpleDoubleProperty().also {
        minWidthProperty.bind(it)
        maxWidthProperty.bind(it)
    }

fun WebViewWrapper.exactHeightProperty() =
    SimpleDoubleProperty().also {
        minHeightProperty.bind(it)
        maxHeightProperty.bind(it)
    }

var WebViewWrapper.exactWidth: Number
    set(value) {
        exactWidthProperty().bind(SimpleDoubleProperty(value.toDouble()))
    }
    get() = NEVER
var WebViewWrapper.exactHeight: Number
    set(value) {
        exactHeightProperty().bind(SimpleDoubleProperty(value.toDouble()))
    }
    get() = NEVER

fun NW.webview(
    htmlContent: String? = null,
    op: WebViewWrapper.() -> Unit = {}
) = WebViewWrapper().apply {
    htmlContent?.let {
        engine.loadContent(htmlContent)
    }
}.attachTo(this, op)


infix fun WebViewWrapper.perfectBind(other: RegionWrapper<*>) {
    this minBind other
    this maxBind other
}

infix fun WebViewWrapper.perfectBind(other: StageWrapper) {
    this minBind other
    this maxBind other
}

infix fun WebViewWrapper.maxBind(other: RegionWrapper<*>) {
    maxHeightProperty.bind(
        other.heightProperty.createROFXPropWrapper()
    ) /* gotta be strict with webview, which I think tries to be big */
    maxWidthProperty.bind(other.widthProperty.createROFXPropWrapper())
}

infix fun WebViewWrapper.maxBind(other: StageWrapper) {
    maxHeightProperty.bind(
        other.heightProperty.createROFXPropWrapper()
    ) /* gotta be strict with webview, which I think tries to be big */
    maxWidthProperty.bind(other.widthProperty.createROFXPropWrapper())
}

infix fun WebViewWrapper.minBind(other: RegionWrapper<*>) {
    minHeightProperty.bind(other.heightProperty.createROFXPropWrapper())
    minWidthProperty.bind(other.widthProperty.createROFXPropWrapper())
}

infix fun WebViewWrapper.minBind(other: StageWrapper) {
    minHeightProperty.bind(other.heightProperty.createROFXPropWrapper())
    minWidthProperty.bind(other.widthProperty.createROFXPropWrapper())
}


@Language("JavaScript")
val refreshImages = """
elements = document.getElementsByTagName("img")
for (var i = 0; i < elements.length; i++) { 
    var e = elements[i]
    var original
    if (e.src.indexOf('?') > -1)
    {
      original = e.src.substring(0, e.src.indexOf("?"))
    } else {
      original = e.src  
    }
    e.src = original + "?t=" + new Date().getTime() 
    }
"""


fun solve(
    y1: Double,
    z1: Double,
    y2: Double,
    z2: Double
): Pair<Double, Double> {
    val A = (y2 - y1) / (z2 - z1)
    val B = (y1 - (z1 * A))
    return A to B
}

const val Y1 = 0.35
const val Z1 = 150.0

/* this lower zoom may make the text small, but that will REALLY help prevent text overlap  // 1.1474 // 1.0 */
const val Y2 = 0.55
const val Z2 = 1080.0
val AB = solve(Y1, Z1, Y2, Z2)
val A = AB.first
val B = AB.second
fun perfectZoom(width_or_height: Double): Double {
    requireNotEqual(width_or_height, 0.0)
    val r = width_or_height * A + B
    println("perfect zoom of $width_or_height is $r")
    return r
}


private const val SPECIAL_ZOOM_RATE = 1.1
private const val SCROLL_COMPENSATION_RATE = SPECIAL_ZOOM_RATE - 1.0

/*I figured this all out by myself. No help, no googling*/
fun WebViewWrapper.specialZooming(par: RegionWrapper<*>? = null) {


    setOnKeyPressed {
        if (it.code == KeyCode.EQUALS) {

            if (zoom == 0.0) zoom = 1.0

            zoom *= SPECIAL_ZOOM_RATE

            scrollBy(SCROLL_COMPENSATION_RATE * (width / 2.0) / zoom, SCROLL_COMPENSATION_RATE * (height / 2.0) / zoom)
        } else if (it.code == KeyCode.MINUS) {

            if (zoom == 0.0) zoom = 1.0

            zoom /= SPECIAL_ZOOM_RATE

            scrollBy(
                -SCROLL_COMPENSATION_RATE * (width / 2.0) / zoom,
                -SCROLL_COMPENSATION_RATE * (height / 2.0) / zoom
            )
        }
    }
    setOnZoom {


        if (zoom == 0.0) zoom = 1.0

        zoom *= it.zoomFactor
        val compensation = it.zoomFactor - 1.0
        scrollBy(compensation * (width / 2.0) / zoom, compensation * (height / 2.0) / zoom)
    }




    if (par != null) {
        runLater {
            val w = par.width
            if (w != 0.0) zoom = perfectZoom(par.width)
            else {
                par.widthProperty.onChange {
                    zoom = perfectZoom(it.toDouble())
                }.removeAfterInvocation = true
            }
        }
    }
}


fun WebViewWrapper.scrollTo(
    xPos: Int,
    yPos: Int
) {

    engine.executeScript(
"""
window.scrollTo($xPos,$yPos)
""".trimIndent()
    )
}


fun WebViewWrapper.scrollMult(factor: Double) {

    engine.executeScript(
"""
window.scrollTo(window.scrollX*$factor,window.scrollY*$factor)
""".trimIndent()
    )
}


fun RegionWrapper<*>.specialTransferingToWindowAndBack(par: PaneWrapper<*>) {
    val vb = this
    setOnKeyPressed { k ->
        if (k.code == KeyCode.W && k.isMetaDown) {
            if (scene?.root == this) {
                removeFromParent()
                (scene!!.window as StageWrapper).close()
                par.add(vb)
                perfectBind(par)
                if (this is WebViewPane) {
                    runLater { wv.zoom = perfectZoom(vb.width) }
                }

                k.consume()
            }
        }
        setOnDoubleClick {
            if (scene?.root != this) {
                removeFromParent()
                openInNewWindow().apply {
                    this@specialTransferingToWindowAndBack.perfectBind(this)
                    setOnCloseRequest {
                        this@specialTransferingToWindowAndBack.removeFromParent()
                        par.add(vb)
                        (this@specialTransferingToWindowAndBack as? WebViewPane)?.runLater {
                            wv.zoom = perfectZoom(vb.width)
                        }
                    }
                }
                if (this is WebViewPane) {
                    runLater { wv.zoom = perfectZoom(vb.width) }
                }
            }
        }
    }
}


open class WebViewPane private constructor(
    file: FsFile? = null,
    html: String? = null
) : VBoxWrapperImpl<NodeWrapper>(childClass = NodeWrapper::class) {

    constructor(file: FsFile) : this(file = file, html = null)

    constructor(html: String) : this(html = html, file = null)

    fun specialZooming() {
        wv.specialZooming(this)
    }


    val wv =
        if (file != null) ImageRefreshingWebView(file) else {
            WebViewWrapper().apply {
                engine.loadContent(html)
            }
        }

    init {
        if (html != null) {
            mcontextmenu {
                "copy html" does { html.copyToClipboard() }
            }
        }
        actionbutton("refresh") {
            this@WebViewPane.wv.engine.reload()
        }
        add(
            wv.apply {
                vgrow = Priority.ALWAYS
            }
        )
    }
}


@Suppress("unused")
fun WebViewWrapper.specialTransferingToWindowAndBack(par: PaneWrapperImpl<*, *>) {

    val wv = this
    setOnKeyPressed { k ->
        if (k.code == KeyCode.W && k.isMetaDown) {
            if (scene?.root == this) {
                removeFromParent()
                (scene?.window as StageWrapper).close()
                par.add(wv)
                perfectBind(par)
                runLater { zoom = perfectZoom(par.width) }
                k.consume()
            }
        }
    }

    setOnDoubleClick {
        if (scene?.root != this) {
            removeFromParent()

            openInNewWindow().apply {
                this@specialTransferingToWindowAndBack.perfectBind(this)
                setOnCloseRequest {
                    removeFromParent()
                    par.add(wv)
                    Platform.runLater {
                        this@specialTransferingToWindowAndBack.zoom = perfectZoom(par.width)
                    }
                }
            }
            runLater { zoom = perfectZoom(width) }
        }
    }
}

fun WebViewWrapper.interceptConsole() {
    engine.loadWorker.stateProperty().addListener { _, _, _ ->
        val window = engine.executeScript("window") as JSObject
        window.setMember("java", JavaBridge())
        engine.executeScript(
            """
            console.log = function(message) {
                java.log(message)
            }
            """.trimIndent()
        )
    }
}


@Suppress("unused")
class JavaBridge {
    fun log(text: String?) {
        println("WebView->JavaBridge:$text")
    }

    fun copy(s: Any) {
        s.toString().copyToClipboard()
    }
}


class WebViewWrapper(node: WebView = WebView()) : ParentWrapperImpl<WebView, NodeWrapper>(node, childClass = NodeWrapper::class) {

    companion object {
        fun WebView.wrapped() = WebViewWrapper(this)
    }

    val zoomProperty: DoubleProperty get() = node.zoomProperty()
    var zoom: Double
        get() = node.zoom
        set(value) {
            node.zoom = value
        }

    val width: Double get() = widthProperty.value
    val widthProperty: ReadOnlyDoubleProperty get() = node.widthProperty()
    val prefWidthProperty: DoubleProperty get() = node.prefWidthProperty()
    var prefWidth: Double
        get() = node.prefWidth
        set(value) {
            node.prefWidth = value
        }
    val minWidthProperty: DoubleProperty get() = node.minWidthProperty()
    var minWidth: Double
        get() = node.minWidth
        set(value) {
            node.minWidth = value
        }
    val maxWidthProperty: DoubleProperty get() = node.maxWidthProperty()
    var maxWidth: Double
        get() = node.maxWidth
        set(value) {
            node.maxWidth = value
        }

    val height: Double get() = heightProperty.value
    val heightProperty: ReadOnlyDoubleProperty get() = node.heightProperty()
    val prefHeightProperty: DoubleProperty get() = node.prefHeightProperty()
    var prefHeight: Double
        get() = node.prefHeight
        set(value) {
            node.prefHeight = value
        }
    val minHeightProperty: DoubleProperty get() = node.minHeightProperty()
    var minHeight: Double
        get() = node.minHeight
        set(value) {
            node.minHeight = value
        }
    val maxHeightProperty: DoubleProperty get() = node.maxHeightProperty()
    var maxHeight: Double
        get() = node.maxHeight
        set(value) {
            node.maxHeight = value
        }

    val engine: WebEngine get() = node.engine

    fun scrollBy(
        x: Double,
        y: Double
    ) {
        engine.executeScript(
"""
window.scrollBy($x,$y)
""".trimIndent()
        )
    }

    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }
}
