package matt.fx.control.inter
import javafx.scene.control.ContentDisplay
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.text.textlike.TextLike
import matt.obs.prop.writable.Var

interface TextAndGraphic: TextLike {
    val graphicProperty: Var<NW?>
    val contentDisplayProp: Var<ContentDisplay>
}

var TextAndGraphic.graphic: NW?
    get() = graphicProperty.value
    set(value) {
        graphicProperty.value = value
    }
var TextAndGraphic.contentDisplay: ContentDisplay
    get() = contentDisplayProp.value
    set(value) {
        contentDisplayProp.value = value
    }
