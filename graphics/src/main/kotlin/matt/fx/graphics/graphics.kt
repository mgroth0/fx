package matt.fx.graphics

import javafx.application.Application
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.Node
import javafx.scene.text.Text
import javafx.stage.Stage

fun <T: Event> Node.filterAndConsume(eventType: EventType<T>, handler: EventHandler<T>) {
  addEventFilter(eventType) {
	handler.handle(it)
	it.consume()
  }
}

fun <T: Event> Node.handleAndConsume(eventType: EventType<T>, handler: EventHandler<T>) {
  addEventHandler(eventType) {
	handler.handle(it)
	it.consume()
  }
}



class DummyAppForFxThreadForScreen: Application() {
  override fun start(primaryStage: Stage?) = Unit
}


val String.fxWidth: Double
  get() = Text(this).layoutBounds.width