package matt.fx.graphics.drag

import javafx.scene.SnapshotParameters
import javafx.scene.input.DataFormat
import javafx.scene.input.MouseEvent
import javafx.scene.input.TransferMode
import javafx.scene.paint.Color
import matt.file.MFile
import matt.file.commons.TEMP_DIR
import matt.fx.graphics.clip.put
import matt.fx.graphics.clip.putFiles
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.NodeWrapperImpl
import matt.fx.image.save

fun NodeWrapper.drags(file: MFile) {
  setOnDragDetected {
	val db = startDragAndDrop(*TransferMode.ANY)
	db.putFiles(mutableListOf(file))
	db.dragView = this.snapshot(SnapshotParameters(), null)
	it.consume()
  }
}

fun NodeWrapper.dragsSnapshot(fill: Color = Color.BLACK) {
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
fun NodeWrapper.easyDrag(data: Any, getSnapshotNode: ()->NodeWrapperImpl<*>? = { null }) =
  easyDrag({ true }, { data }, getSnapshotNode)

fun NodeWrapper.easyDrag(
  condition: ()->Boolean = { true },
  getData: ()->Any,
  getSnapshotNode: ()->NodeWrapperImpl<*>? = { null }
) {
  setOnDragDone {
	dummyDragBoard = null
	it.consume()
  }
  setOnDragDetected {
	if (condition()) {
	  val params = SnapshotParameters()
	  params.fill = Color.TRANSPARENT
	  val db = startDragAndDrop(TransferMode.MOVE)
	  val snapNode = getSnapshotNode() ?: this
	  db.dragView = snapNode.snapshot(params, null)
	  db.put(DataFormat.PLAIN_TEXT, "DUMMY TEXT")
	  dummyDragBoard = getData()
	  it.consume()
	}
  }
}

fun NodeWrapper.easyDrop(handler: ((Any)->Unit)) {
  setOnDragEntered {
	it.consume()
  }
  setOnDragOver {
	it.acceptTransferModes(TransferMode.MOVE)
	it.consume()
  }
  setOnDragDropped {
	handler(dummyDragBoard!!)
	dummyDragBoard = null
	it.isDropCompleted = true
	it.consume()
  }
}