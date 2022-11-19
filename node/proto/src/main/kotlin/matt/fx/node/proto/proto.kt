package matt.fx.node.proto

import javafx.scene.canvas.GraphicsContext
import matt.fx.control.wrapper.control.tab.TabWrapper
import matt.fx.control.wrapper.control.text.area.TextAreaWrapper
import matt.fx.control.wrapper.tab.TabPaneWrapper
import matt.fx.graphics.icon.ICON_HEIGHT
import matt.fx.graphics.icon.ICON_WIDTH
import matt.fx.graphics.icon.Icon
import matt.fx.graphics.icon.IconImage
import matt.fx.graphics.wrapper.canvas.CanvasWrapper
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.NodeWrapperImpl
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.model.data.dir.Direction
import matt.model.data.dir.Direction.BACKWARD
import matt.model.data.dir.Direction.FORWARD
import matt.model.flowlogic.recursionblocker.RecursionBlocker
import matt.obs.prop.VarProp
import kotlin.reflect.KClass

fun iconSpacer() = VBoxWrapperImpl<NodeWrapper>().apply {
  exactHeight = 20.0
  exactWidth = 5.0
}


fun Direction.graphic(): NodeWrapperImpl<*>? {
  return if (this == FORWARD) {
	Icon("white/forward")
  } else if (this == BACKWARD) {

	val canvas = CanvasWrapper(ICON_WIDTH, ICON_HEIGHT)
	val image = IconImage("white/forward")
	val xoff = 0.0 /*15.0*/
	val gc: GraphicsContext = canvas.graphicsContext
	gc.save()
	gc.translate(image.width + xoff*2, 0.0)
	gc.scale(-1.0, 1.0)
	gc.drawImage(image, xoff, 0.0)
	gc.restore()
	gc.drawImage(image, xoff, 0.0)

	canvas
  } else null
}


class LinePrintTextArea: TextAreaWrapper() {
  operator fun plusAssign(a: Any?) {
	text += "\n$a"
  }

  infix fun tab(a: Any?) {
	text += "\n\t$a"
  }
}


class EnumTabPane<E: Enum<E>, C: NW>(cls: KClass<E>, builder: (E)->C):
  TabPaneWrapper<EnumTab<E, C>>() {
  init {
	cls.java.enumConstants.forEach {
	  tabs += EnumTab(it, builder(it)).apply { isClosable = false }
	}
  }

  private val tabsByEnum = tabs.associateBy { it.cnst }

  val selectedConstant by lazy {
	VarProp(selectedItem?.cnst).apply {
	  val rBlocker = RecursionBlocker()
	  selectedItemProperty.onChange {
		rBlocker.with {
		  value = it?.cnst
		}
	  }
	  onChange { e ->
		rBlocker.with {
		  select(tabsByEnum[e])
		}
	  }
	}
  }

}

class EnumTab<E: Enum<E>, C: NW>(val cnst: E, content: C): TabWrapper<C>(cnst.name, content)


interface Refreshable {
  fun refresh()
}