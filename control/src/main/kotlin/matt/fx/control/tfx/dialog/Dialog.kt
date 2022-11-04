package matt.fx.control.tfx.dialog

/*slightly modified code I stole from tornadofx*/

import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.CONFIRMATION
import javafx.scene.control.Alert.AlertType.ERROR
import javafx.scene.control.Alert.AlertType.INFORMATION
import javafx.scene.control.Alert.AlertType.WARNING
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import matt.file.MFile
import matt.file.construct.toMFile
import matt.fx.control.tfx.dialog.FileChooserMode.Multi
import matt.fx.control.tfx.dialog.FileChooserMode.Save
import matt.fx.control.tfx.dialog.FileChooserMode.Single
import matt.fx.graphics.wrapper.window.WindowWrapper
import matt.model.latch.asyncloaded.LoadedValueSlot
import matt.model.runner.ResultRun
import matt.model.runner.Run


inline fun confirm(
  header: String,
  content: String = "",
  confirmButton: ButtonType = ButtonType.OK,
  cancelButton: ButtonType = ButtonType.CANCEL,
  owner: WindowWrapper<*>? = null,
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
  owner: WindowWrapper<*>? = null,
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
  owner: WindowWrapper<*>? = null,
  title: String? = null,
  op: Alert.() -> Unit = {}
): Run<ButtonType> {
  val result = LoadedValueSlot<ButtonType>()
  val run = ResultRun(result)
  val alert = Alert(type, content ?: "", *buttons)
  title?.let { alert.title = it }
  alert.headerText = header
  owner?.also { alert.initOwner(it.node) }
  alert.initModality(Modality.NONE)
  alert.show()
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
  owner: WindowWrapper<*>? = null,
  title: String? = null,
  actionFn: Alert.(ButtonType)->Unit = {}
) =
  alert(WARNING, header, content, *buttons, owner = owner, title = title, actionFn = actionFn)

inline fun error(
  header: String,
  content: String? = null,
  vararg buttons: ButtonType,
  owner: WindowWrapper<*>? = null,
  title: String? = null,
  actionFn: Alert.(ButtonType)->Unit = {}
) =
  alert(ERROR, header, content, *buttons, owner = owner, title = title, actionFn = actionFn)

inline fun information(
  header: String,
  content: String? = null,
  vararg buttons: ButtonType,
  owner: WindowWrapper<*>? = null,
  title: String? = null,
  actionFn: Alert.(ButtonType)->Unit = {}
) =
  alert(INFORMATION, header, content, *buttons, owner = owner, title = title, actionFn = actionFn)

inline fun confirmation(
  header: String,
  content: String? = null,
  vararg buttons: ButtonType,
  owner: WindowWrapper<*>? = null,
  title: String? = null,
  actionFn: Alert.(ButtonType)->Unit = {}
) =
  alert(CONFIRMATION, header, content, *buttons, owner = owner, title = title, actionFn = actionFn)

enum class FileChooserMode { None, Single, Multi, Save }

fun chooseFile(
  title: String? = null,
  filters: Array<out FileChooser.ExtensionFilter>,
  initialDirectory: MFile? = null,
  mode: FileChooserMode = Single,
  owner: WindowWrapper<*>? = null,
  op: FileChooser.()->Unit = {}
): List<MFile> {
  val chooser = FileChooser()
  if (title != null) chooser.title = title
  chooser.extensionFilters.addAll(filters)
  chooser.initialDirectory = initialDirectory
  op(chooser)
  return when (mode) {
	Single -> {
	  val result = chooser.showOpenDialog(owner?.node)?.toMFile()
	  if (result == null) emptyList() else listOf(result)
	}

	Multi  -> chooser.showOpenMultipleDialog(owner?.node).map { it.toMFile() } /*?: emptyList()*/
	Save   -> {
	  val result = chooser.showSaveDialog(owner?.node)?.toMFile()
	  if (result == null) emptyList() else listOf(result)
	}

	else   -> emptyList()
  }
}

fun chooseDirectory(
  title: String? = null,
  initialDirectory: MFile? = null,
  owner: WindowWrapper<*>? = null,
  op: DirectoryChooser.()->Unit = {}
): MFile? {
  val chooser = DirectoryChooser()
  if (title != null) chooser.title = title
  if (initialDirectory != null) chooser.initialDirectory = initialDirectory
  op(chooser)
  return chooser.showDialog(owner?.node)?.toMFile()
}

fun Dialog<*>.toFront() = (dialogPane.scene.window as? Stage)?.toFront()