package matt.fx.node.tex

import javafx.scene.paint.Color
import kotlinx.serialization.Serializable
import matt.caching.cache.LRUCache
import matt.collect.map.dmap.inter.withStoringDefault
import matt.fig.modell.EquationIr
import matt.fig.render.EquationRenderer
import matt.fx.graphics.style.DarkModeController
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxW
import matt.fx.node.proto.scaledcanvas.ScaledCanvas
import matt.fx.node.proto.scaledcanvas.toCanvas
import matt.fx.node.tex.internal.TexToPixelRenderer
import matt.fx.node.tex.internal.isValidTeX
import matt.lang.common.go
import matt.lang.model.value.Value
import matt.model.code.tex.TexCode
import matt.obs.math.double.op.times
import matt.obs.prop.writable.BindableProperty

@Serializable
class EquationNotReallyIrTex(val code: TexCode) : EquationIr


object TexEquationRenderer : EquationRenderer<TexCode> {
    override fun render(equationData: EquationIr): TexCode = (equationData as EquationNotReallyIrTex).code
}

private const val RESOLUTION_CONTROL = 2f
private const val TEX_FONT_SIZE = 50f * RESOLUTION_CONTROL

class TexNodeFactory(val scale: Double) {


    private val scaleCoef = scale * 0.05f / RESOLUTION_CONTROL


    fun isValidTex(code: TexCode) = code.isValidTeX()


    fun toCanvas(code: TexCode) =
        texPixels[code to DarkModeController.darkModeProp.value].value?.toCanvas()?.let {
            TexCanvas(it, scaleCoef, this, code)
        }


    internal val texPixels by lazy {
        LRUCache<Pair<TexCode, Boolean>, Value<Array<Array<Color?>>?>>(100)
            .withStoringDefault {
                Value(
                    TexToPixelRenderer(
                        texFontSize = TEX_FONT_SIZE,
                        darkMode = it.second
                    ).render(EquationNotReallyIrTex(it.first))
                )
            }
    }
}

class TexCanvas(
    innerCanv: ScaledCanvas,
    someScaleThing: Double,
    private val fact: TexNodeFactory,
    private val code: TexCode
) : VBoxW(childClass = NodeWrapper::class) {
    val fontSizeSortOf = BindableProperty(12.0)

    init {
        DarkModeController.darkModeProp.onChangeWithWeak(this) { texCanvas, dark ->
            texCanvas.children.clear()
            val newCanv = texCanvas.fact.texPixels[code to dark].value?.toCanvas()
            newCanv?.scale?.bind(
                texCanvas.fontSizeSortOf * someScaleThing
            )
            newCanv?.go {
                texCanvas.add(it)
            }
        }
        innerCanv.scale.bind(
            fontSizeSortOf * someScaleThing
        )
        +innerCanv
    }
}
