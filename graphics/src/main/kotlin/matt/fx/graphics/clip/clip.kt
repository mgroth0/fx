package matt.fx.graphics.clip

import javafx.scene.Cursor
import javafx.scene.SnapshotParameters
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat
import javafx.scene.input.MouseEvent
import javafx.scene.input.TransferMode
import javafx.scene.paint.Color
import matt.file.MFile
import matt.file.commons.TEMP_DIR
import matt.fx.image.save
import matt.hurricanefx.wrapper.node.NodeWrapperImpl


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


fun Clipboard.setContent(op: ClipboardContent.() -> Unit) {
  val content = ClipboardContent()
  op(content)
  setContent(content)
}

fun Clipboard.putString(value: String) = setContent { putString(value) }
fun Clipboard.putFiles(files: MutableList<MFile>) = setContent { putFiles(files.map { it.userFile }) }
fun Clipboard.put(dataFormat: DataFormat, value: Any) = setContent { put(dataFormat, value) }


fun NodeWrapperImpl<*>.drags(file: MFile) {
  setOnDragDetected {
    val db = startDragAndDrop(*TransferMode.ANY)
    db.putFiles(mutableListOf(file))
    db.dragView = this.snapshot(SnapshotParameters(), null)
    it.consume()
  }
}

fun NodeWrapperImpl<*>.dragsSnapshot(fill: Color = Color.BLACK) {
  addEventFilter(MouseEvent.DRAG_DETECTED) {
    println("drag detected")
    val params = SnapshotParameters()
    params.fill = fill
    val snapshot = snapshot(params, null)
    val imgFile = snapshot.save(TEMP_DIR["drag_image.png"])
    val db = startDragAndDrop(*TransferMode.ANY)
    db.put(DataFormat.FILES, mutableListOf(imgFile))
    it.consume()
    println("drag consumed")
  }
}



var dummyDragBoard: Any? = null
const val DUMMY_TEXT = "DUMMY TEXT"

fun NodeWrapperImpl<*>.easyDrag(data: Any, getSnapshotNode: ()->NodeWrapperImpl<*>? = { null }) = easyDrag({ true }, { data }, getSnapshotNode)

fun NodeWrapperImpl<*>.easyDrag(condition: ()->Boolean = { true }, getData: ()->Any, getSnapshotNode: ()->NodeWrapperImpl<*>? = { null }) {
  this.cursor = Cursor.DEFAULT /*just never change it please*/
  setOnDragDone {
    this.cursor = Cursor.DEFAULT /*just never change it please*/
    dummyDragBoard = null
    it.consume()
  }
  setOnDragDetected {
    this.cursor = Cursor.DEFAULT /*just never change it please*/
    if (condition()) {
      val params = SnapshotParameters()
      params.fill = Color.TRANSPARENT
      val db = startDragAndDrop(TransferMode.MOVE)
      val snapNode = getSnapshotNode() ?: this
      db.dragView = snapNode.snapshot(params, null)
      db.put(DataFormat.PLAIN_TEXT, DUMMY_TEXT)
      dummyDragBoard = getData()
      it.consume()
    }
  }
}

fun NodeWrapperImpl<*>.easyDrop(handler: ((Any)->Unit)) {
  this.cursor = Cursor.DEFAULT /*just never change it please*/
  setOnDragEntered {
    this.cursor = Cursor.DEFAULT /*just never change it please*/
    /*it.acceptTransferModes(*TransferMode.ANY)*/
    it.consume()
  }
  setOnDragOver {
    it.acceptTransferModes(TransferMode.MOVE)
    this.cursor = Cursor.DEFAULT /*just never change it please*/
    it.consume()
  }
  setOnDragDropped {
    this.cursor = Cursor.DEFAULT /*just never change it please*/
    /*if (it.dragboard.getContent(DataFormat.PLAIN_TEXT) == DUMMY_TEXT) {*/
    handler(dummyDragBoard!!)
    dummyDragBoard = null
    it.isDropCompleted = true
    it.consume()
    /*}*/
  }
}