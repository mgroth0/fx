package matt.fx.graphics.wrapper.scene

import javafx.beans.property.ObjectProperty
import javafx.collections.ObservableMap
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.paint.Paint
import javafx.stage.Window
import matt.fx.base.wrapper.obs.collect.list.createMutableWrapper
import matt.fx.base.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.base.wrapper.obs.obsval.toNullableROProp
import matt.fx.graphics.service.nullableWrapperConverter
import matt.fx.graphics.service.wrapped
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.scenelike.SceneLikeWrapper
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.fx.graphics.wrapper.window.WindowWrapper
import matt.obs.bind.binding
import kotlin.reflect.KClass
import kotlin.reflect.cast


open class SceneWrapper<R : ParentWrapper<*>>(
    node: Scene,
    val parentCls: KClass<R>
) : SingularEventTargetWrapper<Scene>(node), SceneLikeWrapper<Scene, R> {

    companion object {
        inline operator fun <reified P: ParentWrapper<*>> invoke(root: P) = SceneWrapper(Scene(root.node), P::class)
        inline operator fun <reified P: ParentWrapper<*>> invoke(
            root: P,
            userWidth: Double,
            userHeight: Double
        ) = SceneWrapper(
            Scene(root.node, userWidth, userHeight), P::class
        )
    }


    final override fun castRoot(nw: NodeWrapper): R = parentCls.cast(nw)

    val focusOwner get() = node.focusOwner?.wrapped()

    final override val widthProperty get() = node.widthProperty().toNonNullableROProp().cast<Double>(Double::class)
    final override val heightProperty get() = node.heightProperty().toNonNullableROProp().cast<Double>(Double::class)

    val stylesheets by lazy {
        node.stylesheets.createMutableWrapper()
    }

    final override val rootProperty: ObjectProperty<Parent> get() = node.rootProperty()
    final override val fillProperty: ObjectProperty<Paint> get() = node.fillProperty()


    fun <T : Event> addEventFilter(
        eventType: EventType<T>,
        handler: EventHandler<T>
    ) = node.addEventFilter(eventType, handler)

    fun <T : Event> addEventHandler(
        eventType: EventType<T>,
        handler: EventHandler<T>
    ) = node.addEventHandler(eventType, handler)


    val windowProperty by lazy {
        node.windowProperty().toNullableROProp().binding(
            converter =
                nullableWrapperConverter<Window, WindowWrapper<*>>(
                    Window::class,
                    WindowWrapper::class
                )
        )
    }
    val window get() = windowProperty.value
    val stage get() = window as StageWrapper

    final override val properties: ObservableMap<Any, Any?> get() = node.properties
    final override fun removeFromParent() {
        (window as? StageWrapper)?.let { it.scene = null }
    }

    final override fun isInsideRow() = false

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
    }




    inner class ParsedStyles internal constructor() {
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
