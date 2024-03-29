package matt.fx.graphics.dialog

import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import matt.file.construct.toMFile
import matt.file.ext.FileExtension
import matt.fx.graphics.dialog.ChooseFileFor.OPEN
import matt.fx.graphics.dialog.ChooseFileFor.SAVE
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.fx.graphics.wrapper.window.WindowWrapper
import matt.lang.common.go
import matt.lang.file.toJFile
import matt.lang.function.Dsl
import matt.lang.model.file.FsFile
import matt.lang.model.file.MacFileSystem


fun NodeWrapper.openFile(
    op: Dsl<ChooseFileDSL> = {}
) = openFile(stage, op)

fun NodeWrapper.openFiles(
    op: Dsl<ChoseMultipleFilesDSL> = {}
) = openFiles(stage, op)

fun NodeWrapper.saveFile(
    op: Dsl<ChooseFileDSL> = {}
) = saveFile(stage, op)

fun NodeWrapper.chooseFolder(
    op: Dsl<ChooseFolderDSL> = {}
) = chooseFolder(stage, op)


fun openFile(
    stage: StageWrapper? = WindowWrapper.guessMainStage(),
    op: Dsl<ChooseFileDSL> = {}
): FsFile? {
    val dsl = ChooseFileDSL(OPEN, stage)
    dsl.apply(op)
    return dsl.showDialog()
}

fun openFiles(
    stage: StageWrapper? = WindowWrapper.guessMainStage(),
    op: Dsl<ChoseMultipleFilesDSL> = {}

): List<FsFile>? {
    val dsl = ChoseMultipleFilesDSL(stage)
    dsl.apply(op)
    return dsl.showDialog()
}

fun saveFile(
    stage: StageWrapper? = WindowWrapper.guessMainStage(),
    op: Dsl<ChooseFileDSL> = {}

): FsFile? {
    val dsl = ChooseFileDSL(SAVE, stage)
    dsl.apply(op)
    return dsl.showDialog()
}

fun chooseFolder(
    stage: StageWrapper? = WindowWrapper.guessMainStage(),
    op: Dsl<ChooseFolderDSL> = {}
): FsFile? {
    val dsl = ChooseFolderDSL(stage)
    dsl.apply(op)
    return dsl.showDialog()
}


enum class ChooseFileFor {
    SAVE, OPEN
}


sealed interface ChoseFile {
    var initialDir: FsFile?
    var title: String?
    var stage: StageWrapper?
}

sealed class ChoseFileBase<T>(
    final override var stage: StageWrapper? = null
) : ChoseFile {
    final override var initialDir: FsFile? = null

    /*doesn't support file open dialogs on all operating systems*/
    var initialSaveFileName: String? = null

    final override var title: String? = null
    private val filters = mutableSetOf<ExtensionFilter>()

    fun extensionFilter(
        description: String,
        vararg extensions: FileExtension
    ) {
        filters += ExtensionFilter(description, *extensions.map { "*." + it.afterDot }.toTypedArray())
    }

    protected fun createChooser(): FileChooser {
        val chooser =
            FileChooser().apply {
                initialDir?.go { initialDirectory = it.toJFile() }
                this@ChoseFileBase.title?.go { title = it }
                filters.takeIf { it.isNotEmpty() }?.go {
                    extensionFilters.setAll(it)
                }
                initialSaveFileName?.go {
                    initialFileName = it
                }
            }
        return chooser
    }

    abstract fun showDialog(): T?
}


class ChooseFileDSL(
    private val fileFor: ChooseFileFor,
    stage: StageWrapper?
) : ChoseFileBase<FsFile>(stage) {


    override fun showDialog(): FsFile? {
        val f =
            createChooser().run {
                when (fileFor) {
                    SAVE -> showSaveDialog(stage?.node)
                    OPEN -> showOpenDialog(stage?.node)
                }
            }
        return f?.toMFile(MacFileSystem)
    }
}

class ChoseMultipleFilesDSL(
    stage: StageWrapper?
) : ChoseFileBase<List<FsFile>>(stage) {
    override fun showDialog(): List<FsFile>? =
        createChooser().showOpenMultipleDialog(stage?.node)?.let {
            it.map { it.toMFile(MacFileSystem) }
        }
}

class ChooseFolderDSL(
    override var stage: StageWrapper?
) : ChoseFile {
    override var initialDir: FsFile? = null
    override var title: String? = null
    fun showDialog(): FsFile? =
        DirectoryChooser().apply {
            initialDir?.go { initialDirectory = it.toJFile() }
            this@ChooseFolderDSL.title?.go { title = it }
        }.showDialog(stage?.node)?.toMFile(MacFileSystem)
}
