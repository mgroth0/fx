package matt.fx.control.popup.tooltip.wrapped
import javafx.scene.control.ChoiceDialog
import javafx.scene.control.Dialog
import javafx.scene.control.PopupControl
import javafx.scene.control.TextInputDialog
import javafx.scene.control.Tooltip
import javafx.stage.Popup
import matt.fx.control.popup.dialog.ChoiceDialogWrapper
import matt.fx.control.popup.dialog.DialogWrapper
import matt.fx.control.popup.dialog.TextInputDialogWrapper
import matt.fx.control.popup.popup.PopupWrapper
import matt.fx.control.popup.popupcontrol.PopupControlWrapper
import matt.fx.control.popup.popupcontrol.node.MyPopupControl
import matt.fx.control.popup.tooltip.TooltipWrapper
import matt.fx.control.popup.tooltip.node.MyTooltip
import matt.fx.control.wrapper.wrapped.findWrapper
import matt.fx.graphics.wrapper.window.WindowWrapper


/*fun Tooltip.wrapped(): tooltipWrapper = findWrapper() ?: tooltipWrapper(this@wrapped)*/
fun Tooltip.wrapped(): WindowWrapper<*> = findWrapper() ?: WindowWrapper(this@wrapped)
fun MyTooltip.wrapped(): TooltipWrapper = findWrapper() ?: TooltipWrapper(this@wrapped)
fun PopupControl.wrapped(): WindowWrapper<*> = findWrapper() ?: WindowWrapper(this@wrapped)
fun MyPopupControl.wrapped(): PopupControlWrapper<*> = findWrapper() ?: PopupControlWrapper(this@wrapped)
fun Popup.wrapped(): PopupWrapper = findWrapper() ?: PopupWrapper(this@wrapped)


fun ChoiceDialog<*>.wrapped(): ChoiceDialogWrapper<*> = findWrapper() ?: ChoiceDialogWrapper(this@wrapped)
fun TextInputDialog.wrapped(): TextInputDialogWrapper = findWrapper() ?: TextInputDialogWrapper(this@wrapped)


fun Dialog<*>.wrapped(): DialogWrapper<*> = findWrapper() ?: when (this) {
    is ChoiceDialog<*> -> wrapped()
    is TextInputDialog -> wrapped()
    else               -> findWrapper() ?: DialogWrapper(this)
}
