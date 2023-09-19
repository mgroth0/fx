/*@file:matt.lang.anno.See(AwtClipLink::class)*/

package matt.fx.graphics.clip

import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat
import matt.file.JioFile
import matt.file.toJioFile
/*import matt.auto.clip.AwtClipLink*/
import matt.lang.model.file.FsFile
import matt.lang.file.toJFile
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection


fun String.copyToClipboardNonFx() {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val content = StringSelection(this)
    clipboard.setContents(content, content)
}

fun String.copyToClipboard() {
    val clipboard = Clipboard.getSystemClipboard()
    val content = ClipboardContent()
    content.putString(this)
    clipboard.setContent(content)
}

fun JioFile.copyToClipboard() {
    val clipboard = Clipboard.getSystemClipboard()
    val content = ClipboardContent()
    content.putFiles(listOf(this.toJFile()))
    clipboard.setContent(content)
}


fun clipboardString(): String? =
    Clipboard
        .getSystemClipboard()
        .getContent(DataFormat.PLAIN_TEXT) as? String


fun Clipboard.setContent(op: ClipboardContent.() -> Unit) {
    val content = ClipboardContent()
    op(content)
    setContent(content)
}

fun Clipboard.putString(value: String) = setContent { putString(value) }
fun Clipboard.putFiles(files: MutableList<FsFile>) = setContent { putFiles(files.map { it.toJioFile().userFile }) }
fun Clipboard.put(
    dataFormat: DataFormat,
    value: Any
) = setContent { put(dataFormat, value) }

