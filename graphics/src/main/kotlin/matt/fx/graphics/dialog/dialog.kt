package matt.fx.graphics.dialog

import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import matt.file.MFile
import matt.file.construct.toMFile
import matt.fx.graphics.dialog.ChooseFileFor.OPEN
import matt.fx.graphics.dialog.ChooseFileFor.SAVE
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.fx.graphics.wrapper.window.WindowWrapper
import matt.lang.function.DSL
import matt.lang.go


fun NodeWrapper.openFile(
  op: DSL<ChooseFileDSL>
) = openFile(stage, op)

fun NodeWrapper.openFiles(
  op: DSL<ChoseMultipleFilesDSL>
) = openFiles(stage, op)

fun NodeWrapper.saveFile(
  op: DSL<ChooseFileDSL>
) = saveFile(stage, op)

fun NodeWrapper.chooseFolder(
  op: DSL<ChooseFolderDSL>
) = chooseFolder(stage, op)


fun openFile(
  stage: StageWrapper? = WindowWrapper.guessMainStage(),
  op: DSL<ChooseFileDSL>
): MFile? {
  val dsl = ChooseFileDSL(OPEN, stage)
  dsl.apply(op)
  return dsl.showDialog()
}

fun openFiles(
  stage: StageWrapper? = WindowWrapper.guessMainStage(),
  op: DSL<ChoseMultipleFilesDSL>

): List<MFile>? {
  val dsl = ChoseMultipleFilesDSL(stage)
  dsl.apply(op)
  return dsl.showDialog()
}

fun saveFile(
  stage: StageWrapper? = WindowWrapper.guessMainStage(),
  op: DSL<ChooseFileDSL>

): MFile? {
  val dsl = ChooseFileDSL(SAVE, stage)
  dsl.apply(op)
  return dsl.showDialog()
}

fun chooseFolder(
  stage: StageWrapper? = WindowWrapper.guessMainStage(),
  op: DSL<ChooseFolderDSL>
): MFile? {
  val dsl = ChooseFolderDSL(stage)
  dsl.apply(op)
  return dsl.showDialog()
}


enum class ChooseFileFor {
  SAVE, OPEN
}


sealed interface ChoseFile {
  var initialDir: MFile?
  var title: String?
  var stage: StageWrapper?
}

sealed class ChoseFileBase<T>(
  override var stage: StageWrapper? = null
): ChoseFile {
  override var initialDir: MFile? = null

  /*doesn't support file open dialogs on all operating systems*/
  var initialSaveFileName: String? = null

  override var title: String? = null
  private val filters = mutableSetOf<ExtensionFilter>()

  fun extensionFilter(
	description: String,
	vararg extensions: String
  ) {
	filters += ExtensionFilter(description, *extensions)
  }

  protected fun createChooser(): FileChooser {
	val chooser = FileChooser().apply {
	  initialDir?.go { initialDirectory = it }
	  title?.go { this.title = it }
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
): ChoseFileBase<MFile>(stage) {


  override fun showDialog(): MFile? {
	val f = createChooser().run {
	  when (fileFor) {
		SAVE -> showSaveDialog(stage?.node)
		OPEN -> showOpenDialog(stage?.node)
	  }
	}
	return f?.toMFile()
  }
}

class ChoseMultipleFilesDSL(
  stage: StageWrapper?
): ChoseFileBase<List<MFile>>(stage) {
  override fun showDialog(): List<MFile>? {
	return createChooser().showOpenMultipleDialog(stage?.node)?.let { it.map { it.toMFile() } }
  }
}

class ChooseFolderDSL(
  override var stage: StageWrapper?
): ChoseFile {
  override var initialDir: MFile? = null
  override var title: String? = null
  fun showDialog(): MFile? {
	return DirectoryChooser().apply {
	  initialDir?.go { initialDirectory = it }
	  title?.go { this.title = it }
	}.showDialog(stage?.node)?.toMFile()
  }
}