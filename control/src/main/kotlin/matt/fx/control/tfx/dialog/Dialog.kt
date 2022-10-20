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
import javafx.stage.Stage
import javafx.stage.Window
import matt.file.MFile
import matt.file.construct.toMFile
import matt.fx.control.tfx.dialog.FileChooserMode.Multi
import matt.fx.control.tfx.dialog.FileChooserMode.Save
import matt.fx.control.tfx.dialog.FileChooserMode.Single


inline fun confirm(
  header: String,
  content: String = "",
  confirmButton: ButtonType = ButtonType.OK,
  cancelButton: ButtonType = ButtonType.CANCEL,
  owner: Window? = null,
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
  owner: Window? = null,
  title: String? = null,
  actionFn: Alert.(ButtonType)->Unit = {}
): Alert {

  val alert = Alert(type, content ?: "", *buttons)
  title?.let { alert.title = it }
  alert.headerText = header
  owner?.also { alert.initOwner(it) }
  val buttonClicked = alert.showAndWait()
  if (buttonClicked.isPresent) {
	alert.actionFn(buttonClicked.get())
  }
  return alert
}

inline fun warning(
  header: String,
  content: String? = null,
  vararg buttons: ButtonType,
  owner: Window? = null,
  title: String? = null,
  actionFn: Alert.(ButtonType)->Unit = {}
) =
  alert(WARNING, header, content, *buttons, owner = owner, title = title, actionFn = actionFn)

inline fun error(
  header: String,
  content: String? = null,
  vararg buttons: ButtonType,
  owner: Window? = null,
  title: String? = null,
  actionFn: Alert.(ButtonType)->Unit = {}
) =
  alert(ERROR, header, content, *buttons, owner = owner, title = title, actionFn = actionFn)

inline fun information(
  header: String,
  content: String? = null,
  vararg buttons: ButtonType,
  owner: Window? = null,
  title: String? = null,
  actionFn: Alert.(ButtonType)->Unit = {}
) =
  alert(INFORMATION, header, content, *buttons, owner = owner, title = title, actionFn = actionFn)

inline fun confirmation(
  header: String,
  content: String? = null,
  vararg buttons: ButtonType,
  owner: Window? = null,
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
  owner: Window? = null,
  op: FileChooser.()->Unit = {}
): List<MFile> {
  val chooser = FileChooser()
  if (title != null) chooser.title = title
  chooser.extensionFilters.addAll(filters)
  chooser.initialDirectory = initialDirectory
  op(chooser)
  return when (mode) {
	Single -> {
	  val result = chooser.showOpenDialog(owner)?.toMFile()
	  if (result == null) emptyList() else listOf(result)
	}

	Multi  -> chooser.showOpenMultipleDialog(owner).map { it.toMFile() } /*?: emptyList()*/
	Save   -> {
	  val result = chooser.showSaveDialog(owner)?.toMFile()
	  if (result == null) emptyList() else listOf(result)
	}

	else   -> emptyList()
  }
}

fun chooseDirectory(
  title: String? = null,
  initialDirectory: MFile? = null,
  owner: Window? = null,
  op: DirectoryChooser.()->Unit = {}
): MFile? {
  val chooser = DirectoryChooser()
  if (title != null) chooser.title = title
  if (initialDirectory != null) chooser.initialDirectory = initialDirectory
  op(chooser)
  return chooser.showDialog(owner)?.toMFile()
}

fun Dialog<*>.toFront() = (dialogPane.scene.window as? Stage)?.toFront()