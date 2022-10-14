package matt.fx.graphics.wrapper.scene

import javafx.beans.property.ObjectProperty
import javafx.collections.ObservableList
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.paint.Paint
import javafx.stage.Window
import matt.fx.graphics.service.uncheckedNullableWrapperConverter
import matt.fx.graphics.service.wrapped
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.scenelike.SceneLikeWrapper
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.fx.graphics.wrapper.window.WindowWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.toNonNullableROProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNullableROProp
import matt.obs.bind.binding


open class SceneWrapper<R: ParentWrapper<*>>(
  node: Scene
): SingularEventTargetWrapper<Scene>(node), SceneLikeWrapper<Scene, R> {


  constructor(root: ParentWrapper<*>): this(Scene(root.node))


  override val widthProperty get() = node.widthProperty().toNonNullableROProp().cast<Double>()
  override val heightProperty get() = node.heightProperty().toNonNullableROProp().cast<Double>()

  val stylesheets: ObservableList<String> get() = node.stylesheets

  override val rootProperty: ObjectProperty<Parent> get() = node.rootProperty()
  override val fillProperty: ObjectProperty<Paint> get() = node.fillProperty()


  fun <T: Event> addEventFilter(eventType: EventType<T>, handler: EventHandler<T>) =
	node.addEventFilter(eventType, handler)

  fun <T: Event> addEventHandler(eventType: EventType<T>, handler: EventHandler<T>) =
	node.addEventHandler(eventType, handler)


  val windowProperty by lazy {
	node.windowProperty().toNullableROProp().binding(
	  converter = uncheckedNullableWrapperConverter<Window, WindowWrapper<*>>()
	)
  }
  val window by windowProperty
  val stage get() = window as StageWrapper

  override val properties get() = node.properties
  override fun removeFromParent() {
	(window as? StageWrapper)?.let { it.scene = null }
  }

  override fun isInsideRow() = false

  fun reloadStylesheets() {
	val styles = stylesheets.toMutableList()
	stylesheets.clear()
	styles.forEachIndexed { i, s ->
	  if (s.startsWith("css://")) {
		val b = StringBuilder()
		val queryPairs = mutableListOf<String>()

		if (s.contains("?")) {
		  val urlAndQuery = s.split(Regex("\\?"), 2)
		  b.append(urlAndQuery[0])
		  val query = urlAndQuery[1]

		  val pairs = query.split("&")
		  pairs.filterNot { s.startsWith("squash=") }.forEach { queryPairs.add(it) }
		} else {
		  b.append(s)
		}

		queryPairs.add("squash=${System.currentTimeMillis()}")
		b.append("?").append(queryPairs.joinToString("&"))
		styles[i] = b.toString()
	  }
	}
	stylesheets.addAll(styles)
  }
}