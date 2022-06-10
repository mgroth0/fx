package matt.fx.graphics.layout

import javafx.geometry.Bounds
import javafx.geometry.Rectangle2D
import javafx.scene.Node
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.stage.Stage


infix fun Region.minBind(other: Region) {
  minHeightProperty().bind(other.heightProperty())
  minWidthProperty().bind(other.widthProperty())
}

infix fun Region.minBind(other: Stage) {
  minHeightProperty().bind(other.heightProperty())
  minWidthProperty().bind(other.widthProperty())
}


infix fun Region.maxBind(other: Region) {
  maxHeightProperty().bind(other.heightProperty())
  maxWidthProperty().bind(other.widthProperty())
}

infix fun Region.maxBind(other: Stage) {
  maxHeightProperty().bind(other.heightProperty())
  maxWidthProperty().bind(other.widthProperty())
}


infix fun Region.perfectBind(other: Region) {
  this minBind other
  this maxBind other
}

infix fun Region.perfectBind(other: Stage) {
  this minBind other
  this maxBind other
}

fun Pane.spacer() {
  this.children.add(Pane().apply {
	minWidth = 20.0
	minHeight = 20.0
  })
}

fun Bounds.toRect() = Rectangle2D(minX, minY, width, height)
fun Rectangle2D.shrink(n: Int) = Rectangle2D(minX + n, minY + n, width - (n*2), height - (n*2))

var Node.hgrow: Priority?
  get() = HBox.getHgrow(this)
  set(value) {
    HBox.setHgrow(this, value)
  }
var Node.vgrow: Priority?
  get() = VBox.getVgrow(this)
  set(value) {
    VBox.setVgrow(this, value)
    // Input Container vgrow must propagate to Field and Fieldset
  }