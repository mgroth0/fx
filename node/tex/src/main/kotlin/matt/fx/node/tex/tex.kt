package matt.fx.node.tex

import javafx.scene.paint.Color
import kotlinx.serialization.Serializable
import matt.caching.cache.LRUCache
import matt.collect.map.dmap.withStoringDefault
import matt.fig.model.EquationIr
import matt.fig.render.EquationRenderer
import matt.fx.graphics.style.DarkModeController
import matt.fx.graphics.wrapper.pane.vbox.VBoxW
import matt.fx.node.proto.scaledcanvas.ScaledCanvas
import matt.fx.node.proto.scaledcanvas.toCanvas
import matt.fx.node.tex.internal.TexToPixelRenderer
import matt.fx.node.tex.internal.isValidTeX
import matt.lang.go
import matt.model.code.tex.TexCode
import matt.obs.math.double.op.times
import matt.obs.prop.BindableProperty

@Serializable
class EquationNotReallyIrTex(val code: TexCode) : EquationIr



object TexEquationRenderer : EquationRenderer<TexCode> {
    override fun render(equationData: EquationIr): TexCode {
        return (equationData as EquationNotReallyIrTex).code
    }
}

private const val RESOLUTION_CONTROL = 2f
private const val TEX_FONT_SIZE = 50f * RESOLUTION_CONTROL

class TexNodeFactory(val scale: Double) {


    private val scaleCoef = scale * 0.05f / RESOLUTION_CONTROL


    fun isValidTex(code: TexCode) = code.isValidTeX()


    fun toCanvas(code: TexCode) = texPixels[code to DarkModeController.darkModeProp.value]?.toCanvas()?.let {
        TexCanvas(it, scaleCoef, this, code)
    }


    internal val texPixels by lazy {
        LRUCache<Pair<TexCode, Boolean>, Array<Array<Color?>>?>(100)
            .withStoringDefault {
                TexToPixelRenderer(
                    texFontSize = TEX_FONT_SIZE,
                    darkMode = it.second
                ).render(EquationNotReallyIrTex(it.first))
            }
    }
}

class TexCanvas(
    innerCanv: ScaledCanvas,
    someScaleThing: Double,
    private val fact: TexNodeFactory,
    private val code: TexCode
) : VBoxW() {
    val fontSizeSortOf = BindableProperty(12.0)

    init {
        DarkModeController.darkModeProp.onChangeWithWeak(this) { texCanvas, dark ->
            texCanvas.children.clear()
            val newCanv = texCanvas.fact.texPixels[code to dark]?.toCanvas()
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