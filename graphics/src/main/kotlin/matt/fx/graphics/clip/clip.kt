@file:See(AwtClipLink::class)

package matt.fx.graphics.clip

import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat
import matt.auto.clip.AwtClipLink
import matt.file.MFile
import matt.lang.See


fun String.copyToClipboard() {
  val clipboard = Clipboard.getSystemClipboard()
  val content = ClipboardContent()
  content.putString(this)
  clipboard.setContent(content)
}

fun MFile.copyToClipboard() {
  val clipboard = Clipboard.getSystemClipboard()
  val content = ClipboardContent()
  content.putFiles(listOf(this))
  clipboard.setContent(content)
}


fun clipboardString(): String? =
  Clipboard
	.getSystemClipboard()
	.getContent(DataFormat.PLAIN_TEXT) as? String


fun Clipboard.setContent(op: ClipboardContent.()->Unit) {
  val content = ClipboardContent()
  op(content)
  setContent(content)
}

fun Clipboard.putString(value: String) = setContent { putString(value) }
fun Clipboard.putFiles(files: MutableList<MFile>) = setContent { putFiles(files.map { it.userFile }) }
fun Clipboard.put(dataFormat: DataFormat, value: Any) = setContent { put(dataFormat, value) }

