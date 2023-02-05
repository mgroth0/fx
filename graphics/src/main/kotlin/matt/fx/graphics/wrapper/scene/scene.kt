package matt.fx.graphics.wrapper.scene

import javafx.beans.property.ObjectProperty
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
import matt.fx.base.wrapper.obs.collect.list.createMutableWrapper
import matt.fx.base.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.base.wrapper.obs.obsval.toNullableROProp
import matt.obs.bind.binding


open class SceneWrapper<R: ParentWrapper<*>>(
  node: Scene
): SingularEventTargetWrapper<Scene>(node), SceneLikeWrapper<Scene, R> {


  constructor(root: ParentWrapper<*>): this(Scene(root.node))
  constructor(root: ParentWrapper<*>, userWidth: Double, userHeight: Double): this(
	Scene(root.node, userWidth, userHeight)
  )


  val focusOwner get() = node.focusOwner?.wrapped()

  override val widthProperty get() = node.widthProperty().toNonNullableROProp().cast<Double>()
  override val heightProperty get() = node.heightProperty().toNonNullableROProp().cast<Double>()

  val stylesheets by lazy {
	node.stylesheets.createMutableWrapper()
  }

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
  val window get() = windowProperty.value
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


  /*val parsedStyleSheets by lazy {
	val parser = CssParser()
	stylesheets.toLazyMappedList {
	  parser.parse(URI(it).toURL())
	}
  }*/

  /*inner class ParsedStyles internal constructor() {
	val fxBase by lazy {
	  parsedStyleSheets.mapNotNull {
		it.rules.filter {
		  it.selectors
		}
	  }
	  parsedStyleSheets.forEach {
		it
		it.rules.forEach {
		  it.selectors.forEach {
			it.applies()
		  }
		  it.declarations.forEach {
			it.property
			it.parsedValue
		  }

		}
	  }
	}
  }

  val parsedStyles by lazy { ParsedStyles() }


  class Modena {
	val fxBase by lazy {

	  node.style

	  scene.stylesheets

	  DarkModeController.darkModeProp.binding {
		if (it) {
		  fxColorFromHex()
		} else {

		}
	  }
	}
	val fxBackground by lazy {
	  DarkModeController.darkModeProp.binding {

	  }
	}
  }*/


}