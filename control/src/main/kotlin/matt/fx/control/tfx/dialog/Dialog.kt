package matt.fx.control.tfx.dialog

/*slightly modified code I stole from tornadofx*/

import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.CONFIRMATION
import javafx.scene.control.Alert.AlertType.ERROR
import javafx.scene.control.Alert.AlertType.INFORMATION
import javafx.scene.control.Alert.AlertType.WARNING
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Modality
import javafx.stage.Stage
import matt.async.thread.runner.ResultRun
import matt.fx.graphics.wrapper.window.WindowWrapper
import matt.model.flowlogic.latch.asyncloaded.LoadedValueSlot
import matt.model.flowlogic.runner.Run


inline fun confirm(
  header: String,
  content: String = "",
  confirmButton: ButtonType = ButtonType.OK,
  cancelButton: ButtonType = ButtonType.CANCEL,
  owner: WindowWrapper<*>? = WindowWrapper.guessMainStage(),
  title: String? = null,
  actionFn: ()->Unit
) {
  alert(CONFIRMATION, header, content, confirmButton, cancelButton, owner = owner, title = title) {
	if (it == confirmButton) actionFn()
  }
}

inline fun alert(
  type: Alert.AlertType,
  header: String,
  content: String? = null,
  vararg buttons: ButtonType,
  owner: WindowWrapper<*>? = WindowWrapper.guessMainStage(),
  title: String? = null,
  actionFn: Alert.(ButtonType)->Unit = {}
): Alert {

  val alert = Alert(type, content ?: "", *buttons)
  title?.let { alert.title = it }
  alert.headerText = header
  owner?.also { alert.initOwner(it.node) }
  val buttonClicked = alert.showAndWait()
  if (buttonClicked.isPresent) {
	alert.actionFn(buttonClicked.get())
  }
  return alert
}


fun asyncAlert(
  type: Alert.AlertType,
  header: String,
  content: String? = null,
  vararg buttons: ButtonType,
  owner: WindowWrapper<*>? = WindowWrapper.guessMainStage(),
  title: String? = null,
  closeOnEscape: Boolean = true,
  op: Alert.()->Unit = {}
): Run<ButtonType> {
  val result = LoadedValueSlot<ButtonType>()
  val run = ResultRun(result)
  val alert = Alert(type, content ?: "", *buttons)
  title?.let { alert.title = it }
  alert.headerText = header
  owner?.also { alert.initOwner(it.node) }
  alert.initModality(Modality.NONE)
  alert.show()
  if (!closeOnEscape) {
	alert.dialogPane.scene.window.addEventFilter(KeyEvent.KEY_PRESSED) {
	  if (it.code == KeyCode.ESCAPE) it.consume()
	}
  }
  alert.setOnHidden {
	alert.result?.let {
	  result.putLoadedValue(it)
	}
  }
  alert.op()
  return run
}


inline fun warning(
  header: String,
  content: String? = null,
  vararg buttons: ButtonType,
  owner: WindowWrapper<*>? = WindowWrapper.guessMainStage(),
  title: String? = null,
  actionFn: Alert.(ButtonType)->Unit = {}
) =
  alert(WARNING, header, content, *buttons, owner = owner, title = title, actionFn = actionFn)

inline fun errorAlert(
  header: String,
  content: String? = null,
  vararg buttons: ButtonType,
  owner: WindowWrapper<*>? = WindowWrapper.guessMainStage(),
  title: String? = null,
  actionFn: Alert.(ButtonType)->Unit = {}
) =
  alert(ERROR, header, content, *buttons, owner = owner, title = title, actionFn = actionFn)

inline fun information(
  header: String,
  content: String? = null,
  vararg buttons: ButtonType,
  owner: WindowWrapper<*>? = WindowWrapper.guessMainStage(),
  title: String? = null,
  actionFn: Alert.(ButtonType)->Unit = {}
) =
  alert(INFORMATION, header, content, *buttons, owner = owner, title = title, actionFn = actionFn)

inline fun confirmation(
  header: String,
  content: String? = null,
  vararg buttons: ButtonType,
  owner: WindowWrapper<*>? = WindowWrapper.guessMainStage(),
  title: String? = null,
  actionFn: Alert.(ButtonType)->Unit = {}
) =
  alert(CONFIRMATION, header, content, *buttons, owner = owner, title = title, actionFn = actionFn)



fun Dialog<*>.toFront() = (dialogPane.scene.window as? Stage)?.toFront()