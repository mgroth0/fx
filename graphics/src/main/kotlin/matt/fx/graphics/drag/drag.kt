package matt.fx.graphics.drag

import javafx.scene.SnapshotParameters
import javafx.scene.image.Image
import javafx.scene.input.DataFormat
import javafx.scene.input.Dragboard
import javafx.scene.input.TransferMode
import javafx.scene.paint.Color
import matt.file.MFile
import matt.file.commons.TEMP_DIR
import matt.file.construct.toMFile
import matt.fx.graphics.clip.put
import matt.fx.graphics.clip.putFiles
import matt.fx.graphics.drag.BetterTransferMode.COPY
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.image.save
import matt.lang.function.On
import matt.lang.function.Op
import matt.lang.function.Produce
import matt.lang.go
import matt.lang.inList
import matt.obs.prop.ObsVal

enum class BetterTransferMode(@PublishedApi internal vararg val modes: TransferMode) {
    MOVE(TransferMode.MOVE),
    COPY(TransferMode.COPY),
    LINK(TransferMode.LINK),
    COPY_OR_MOVE(*TransferMode.COPY_OR_MOVE),
    COPY_OR_LINK(
        TransferMode.COPY,
        TransferMode.LINK
    ),
    MOVE_OR_LINK(
        TransferMode.MOVE,
        TransferMode.LINK
    ),
    ANY(*TransferMode.ANY)
}

fun NodeWrapper.dragsFile(
    file: ObsVal<MFile>,
    mode: BetterTransferMode
) = drags(
    type = DragType.SYSTEM_SINGLE_FILE,
    predicate = { true },
    mode = mode,
    getData = { file.value },
)

fun NodeWrapper.dragsFile(
    file: MFile,
    mode: BetterTransferMode
) = drags(
    type = DragType.SYSTEM_SINGLE_FILE,
    predicate = { true },
    mode = mode,
    data = file,
)

fun NodeWrapper.dragsSnapshot(
    fill: Color = Color.BLACK
) = drags(
    type = DragType.SYSTEM_SINGLE_FILE,
    predicate = { true },
    mode = COPY,
    getData = {
        val params = SnapshotParameters()
        params.fill = fill
        val snapshot = snapshot(
            params,
            null
        )
        val imgFile = snapshot.save(TEMP_DIR["drag_image.png"])
        imgFile.toMFile()
    },
    getDragView = { null })


inline fun <reified T : Any> NodeWrapper.drags(
    type: DragType<T> = DragType.generic(),
    crossinline predicate: Produce<Boolean> = { true },
    mode: BetterTransferMode,
    data: T,
    noinline getDragView: Produce<Image?> = defaultDragViewGet()
) = this@drags.drags(
    type = type,
    predicate = predicate,
    mode = mode,
    getData = { data },
    getDragView = getDragView
)

private val defaultSnapshotParams = SnapshotParameters().apply {
    fill = Color.TRANSPARENT
}

fun NW.defaultSnapshot() = snapshot(
    defaultSnapshotParams,
    null
)

@PublishedApi
internal fun NodeWrapper.defaultDragViewGet(): Produce<Image?> = {
    defaultSnapshot()
}

inline fun <reified T : Any> NodeWrapper.drags(
    type: DragType<T> = DragType.generic(),
    crossinline predicate: Produce<Boolean> = { true },
    mode: BetterTransferMode,
    crossinline getData: Produce<T>,
    noinline getDragView: Produce<Image?> = defaultDragViewGet()
) {
    setOnDragDetected {
        if (predicate()) {

            val db = startDragAndDrop(*mode.modes)

            getDragView.invoke()?.go {
                db.dragView = it
            }

            type.setData(
                db,
                getData()
            )
            it.consume()
        }
    }
    setOnDragDone {
        it.consume()
    }
}

inline fun <reified T : Any> NodeWrapper.acceptDrops(
    type: DragType<T> = DragType.generic(),
    crossinline predicate: (Dragboard) -> Boolean,
    mode: BetterTransferMode,
    crossinline onEnter: Op = {},
    crossinline onDrop: On<T>,
    crossinline finalize: Op = {}
) {

    setOnDragEntered {
        if (predicate(it.dragboard)) {
            it.acceptTransferModes(*mode.modes)
            if (it.isAccepted) {
                onEnter()
                it.consume()
            }
        }
    }

    /*NEED THIS OR ACCEPT DOES NOT WORK*/
    setOnDragOver {
        if (predicate(it.dragboard)) {
            it.acceptTransferModes(*mode.modes)
        }
    }

    setOnDragDropped {
        onDrop(type.getData(it.dragboard))
        it.isDropCompleted = true
        finalize()
        it.consume()
    }
    setOnDragExited {
//        println("drag exited ${this}: accepting object = ${it.acceptingObject}, isAccepted = ${it.isAccepted}, gSource = ${it.gestureSource}, gTarget = ${it.gestureTarget}, isDropCompleted=${it.isDropCompleted},pickResult= ${it.pickResult}")

        finalize()
    }
}

@PublishedApi
internal val GENERIC_DATA_FORMAT = DataFormat("matt.generic-data-format")

data class DragType<T : Any>(
    val predicate: (Dragboard) -> Boolean,
    val getData: (Dragboard) -> T,
    val setData: (Dragboard, T) -> Unit
) {
    companion object {

        val SYSTEM_SINGLE_FILE =
            DragType(predicate = { it.hasFiles() && it.files.size == 1 },
                     getData = { it.files.first().toMFile() },
                     setData = { db, f -> db.putFiles(f.inList().toMutableList()) })
        val SYSTEM_IMAGE = DragType(predicate = { it.hasImage() },
                                    getData = { it.image!! },
                                    setData = { db, im ->
                                        db.put(
                                            DataFormat.IMAGE,
                                            im
                                        )
                                    })

        inline fun <reified TT : Any> generic() = DragType(predicate = { it.getContent(GENERIC_DATA_FORMAT) is TT },
                                                           getData = { it.getContent(GENERIC_DATA_FORMAT) as TT },
                                                           setData = { db, t -> db.setContent(mutableMapOf<DataFormat, Any>(GENERIC_DATA_FORMAT to t)) })
    }
}



