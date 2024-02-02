package matt.fx.control.popup.tooltip.node.skin

import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.Skin
import javafx.scene.control.Tooltip
import javafx.scene.control.skin.TooltipSkin
import matt.fx.base.rewrite.ReWrittenFxClass
import matt.fx.control.popup.tooltip.node.MyTooltip

/**
 * CSS based skin for Tooltip. It deals mostly with show hide logic for
 * Popup based controls, and specifically in this case for tooltip. It also
 * implements some of the Skin interface methods.
 *
 * TooltipContent class is the actual skin implementation of the tooltip
 */
@ReWrittenFxClass(TooltipSkin::class)
class MyTooltipSkin(t: MyTooltip): Skin<MyTooltip?> {
  /* *************************************************************************
   *                                                                         *
   * Private fields                                                          *
   *                                                                         *
   **************************************************************************/
    private var tipLabel: Label?
    private var tooltip: MyTooltip?
  /* *************************************************************************
   *                                                                         *
   * Constructors                                                            *
   *                                                                         *
   **************************************************************************/
    /**
     * Creates a new TooltipSkin instance for the given [Tooltip].
     * @param t the tooltip
     */
    init {
        tooltip = t
        tipLabel = Label().apply {

            contentDisplayProperty().bind(t.contentDisplayProperty())
            fontProperty().bind(t.fontProperty())
            graphicProperty().bind(t.graphicProperty())
            graphicTextGapProperty().bind(t.graphicTextGapProperty())
            textAlignmentProperty().bind(t.textAlignmentProperty())
            textOverrunProperty().bind(t.textOverrunProperty())
            textProperty().bind(t.textProperty())
            wrapTextProperty().bind(t.wrapTextProperty())
            minWidthProperty().bind(t.minWidthProperty())
            prefWidthProperty().bind(t.prefWidthProperty())
            maxWidthProperty().bind(t.maxWidthProperty())
            minHeightProperty().bind(t.minHeightProperty())
            prefHeightProperty().bind(t.prefHeightProperty())
            maxHeightProperty().bind(t.maxHeightProperty())

            // RT-7512 - skin needs to have styleClass of the control
            // TODO - This needs to be bound together, not just set! Probably should
            // do the same for id and style as well.
            styleClass.setAll(t.styleClass)
            style = t.style
            id = t.id
        }

    }

    override fun getSkinnable(): MyTooltip? = tooltip
    override fun getNode(): Node = tipLabel!!
    override fun dispose() {
        tooltip = null
        tipLabel = null
    }
}

