package matt.fx.web

import javafx.beans.property.SimpleDoubleProperty
import javafx.event.EventTarget
import javafx.scene.input.KeyCode
import javafx.scene.layout.Region
import javafx.scene.web.HTMLEditor
import javafx.scene.web.WebView
import javafx.stage.Stage
import matt.hurricanefx.eye.lang.DProp
import matt.hurricanefx.tornadofx.async.runLater
import matt.hurricanefx.tornadofx.fx.attachTo
import matt.klib.lang.NEVER
import org.intellij.lang.annotations.Language

fun WebView.exactWidthProperty() = SimpleDoubleProperty().also {
  minWidthProperty().bind(it)
  maxWidthProperty().bind(it)
}

fun WebView.exactHeightProperty() = SimpleDoubleProperty().also {
  minHeightProperty().bind(it)
  maxHeightProperty().bind(it)
}
var WebView.exactWidth: Number
  set(value) {
    exactWidthProperty().bind(DProp(value.toDouble()))
  }
  get() = NEVER
var WebView.exactHeight: Number
  set(value) {
    exactHeightProperty().bind(DProp(value.toDouble()))
  }
  get() = NEVER

fun EventTarget.webview(op: WebView.() -> Unit = {}) = WebView().attachTo(this, op)

fun EventTarget.htmleditor(html: String? = null, op: HTMLEditor.() -> Unit = {}) = HTMLEditor().attachTo(this, op) {
  if (html != null) it.htmlText = html
}

infix fun WebView.perfectBind(other: Region) {
  this minBind other
  this maxBind other
}

infix fun WebView.perfectBind(other: Stage) {
  this minBind other
  this maxBind other
}

infix fun WebView.maxBind(other: Region) {
  maxHeightProperty().bind(other.heightProperty()) // gotta be strict with webview, which I think tries to be big
  maxWidthProperty().bind(other.widthProperty())
}

infix fun WebView.maxBind(other: Stage) {
  maxHeightProperty().bind(other.heightProperty()) // gotta be strict with webview, which I think tries to be big
  maxWidthProperty().bind(other.widthProperty())
}

infix fun WebView.minBind(other: Region) {
  minHeightProperty().bind(other.heightProperty())
  minWidthProperty().bind(other.widthProperty())
}

infix fun WebView.minBind(other: Stage) {
  minHeightProperty().bind(other.heightProperty())
  minWidthProperty().bind(other.widthProperty())
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


fun solve(y1: Double, z1: Double, y2: Double, z2: Double): Pair<Double, Double> {
  //                y1 = z1*A + B
  //                y2 = z2*A + B

  //                y2 = z2*A + ( y1 - z1*A )
  //                y2 - y1 = z2*A - Z1*A
  val A = (y2 - y1)/(z2 - z1)
  val B = (y1 - (z1*A))
  return A to B
}

const val Y1 = 0.35
const val Z1 = 150.0

// this lower zoom may make the text small, but that will REALLY help prevent text overlap  // 1.1474 // 1.0
const val Y2 = 0.55
const val Z2 = 1080.0
val AB = solve(Y1, Z1, Y2, Z2)
val A = AB.first
val B = AB.second
fun perfectZoom(width_or_height: Double): Double {
  val r = width_or_height*A + B
  println("perfect zoom of $width_or_height is $r")
  return r
}


private const val SPECIAL_ZOOM_RATE = 1.1
private const val SCROLL_COMPENSATION_RATE = SPECIAL_ZOOM_RATE - 1.0

/*I figured this all out by myself. No help, no googling*/
fun WebView.specialZooming(par: Region? = null) {



  setOnKeyPressed {
    if (it.code == KeyCode.EQUALS) {

      zoom *= SPECIAL_ZOOM_RATE

      scrollBy(SCROLL_COMPENSATION_RATE*(width/2.0)/zoom, SCROLL_COMPENSATION_RATE*(height/2.0)/zoom)

    } else if (it.code == KeyCode.MINUS) {
      zoom /= SPECIAL_ZOOM_RATE

      scrollBy(-SCROLL_COMPENSATION_RATE*(width/2.0)/zoom, -SCROLL_COMPENSATION_RATE*(height/2.0)/zoom)

    }
  }
  setOnZoom {
    zoom *= it.zoomFactor
    val compensation = it.zoomFactor - 1.0
    scrollBy(compensation*(width/2.0)/zoom, compensation*(height/2.0)/zoom)
  }




  if (par != null) {
    runLater { zoom = perfectZoom(par.width) }
  }


}


fun WebView.scrollTo(xPos: Int, yPos: Int) {

  engine.executeScript(
    """
	  window.scrollTo($xPos,$yPos)
	""".trimIndent()
  )

}

fun WebView.scrollBy(x: Double, y: Double) {

  engine.executeScript(
    """
	  window.scrollBy($x,$y)
	""".trimIndent()
  )

}

fun WebView.scrollMult(factor: Double) {

  engine.executeScript(
    """
	  window.scrollTo(window.scrollX*${factor},window.scrollY*${factor})
	""".trimIndent()
  )

}



