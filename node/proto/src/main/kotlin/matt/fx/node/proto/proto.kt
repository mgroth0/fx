package matt.fx.node.proto

import javafx.scene.canvas.GraphicsContext
import matt.fx.control.wrapper.control.tab.TabWrapper
import matt.fx.control.wrapper.control.text.area.TextAreaWrapper
import matt.fx.control.wrapper.tab.TabPaneWrapper
import matt.fx.graphics.icon.ICON_SIZE
import matt.fx.graphics.icon.Icon
import matt.fx.graphics.icon.IconImage
import matt.fx.graphics.icon.svg.svgToFXImage
import matt.fx.graphics.style.DarkModeController
import matt.fx.graphics.wrapper.canvas.CanvasWrapper
import matt.fx.graphics.wrapper.imageview.ImageViewWrapper
import matt.fx.graphics.wrapper.imageview.imageview
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.line.line
import matt.fx.graphics.wrapper.pane.SimplePaneWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.fx.graphics.wrapper.style.FXColor
import matt.lang.l
import matt.model.data.dir.Direction
import matt.model.data.dir.Direction.BACKWARD
import matt.model.data.dir.Direction.FORWARD
import matt.model.data.rect.IntSquareSize
import matt.model.flowlogic.recursionblocker.RecursionBlocker
import matt.obs.bind.binding
import matt.obs.math.double.op.times
import matt.obs.prop.ObsVal
import matt.obs.prop.VarProp
import matt.rstruct.loader.systemResourceLoader
import kotlin.reflect.KClass

fun NW.svgIcon(
    file: String,
    size: Int
): ImageViewWrapper {
    val fullName = if (".svg" !in file) "$file.svg" else file
    return imageview(
        svgToFXImage(
            systemResourceLoader().resourceStream(fullName)!!,
            size = IntSquareSize(size * 2)
        )
    ).apply {
        isPreserveRatio = true
        fitWidth = size.toDouble()
    }
}

fun navDrawerButtonGraphic(prefHeight: ObsVal<Double>) = SimplePaneWrapper<NW>().apply {
    val pn = this
    prefHeightProperty.bind(prefHeight)
    val w = 30
    prefWidth = w.toDouble()
    val hM = 4
    repeat(3) { y ->
        line(startX = hM, startY = 0.0, endX = w - hM, endY = 0.0) {
            l(startYProperty, endYProperty).forEach {
                it.bind(
                    pn.heightProperty * (0.25 + y * 0.25)
                )
            }
            strokeProperty.bind(DarkModeController.darkModeProp.binding {
                if (it) FXColor.WHITE else FXColor.BLACK
            })
        }
    }
}

fun iconSpacer() = VBoxWrapperImpl<NodeWrapper>().apply {
    exactHeight = 20.0
    exactWidth = 5.0
}


fun Direction.graphic(): NodeWrapperImpl<*>? {
    return if (this == FORWARD) {
        Icon("white/forward")
    } else if (this == BACKWARD) {

        val canvas = CanvasWrapper(ICON_SIZE)
        val image = IconImage("white/forward")
        val xoff = 0.0 /*15.0*/
        val gc: GraphicsContext = canvas.graphicsContext
        gc.save()
        gc.translate(image.width + xoff * 2, 0.0)
        gc.scale(-1.0, 1.0)
        gc.drawImage(image, xoff, 0.0)
        gc.restore()
        gc.drawImage(image, xoff, 0.0)

        canvas
    } else null
}


class LinePrintTextArea : TextAreaWrapper() {
    operator fun plusAssign(a: Any?) {
        text += "\n$a"
    }

    infix fun tab(a: Any?) {
        text += "\n\t$a"
    }
}


class EnumTabPane<E : Enum<E>, C : NW>(
    cls: KClass<E>,
    builder: (E) -> C
) :
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

class EnumTab<E : Enum<E>, C : NW>(
    val cnst: E,
    content: C
) : TabWrapper<C>(cnst.name, content)


interface Refreshable {
    fun refresh()
}