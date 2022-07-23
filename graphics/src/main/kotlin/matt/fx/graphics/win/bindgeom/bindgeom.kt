package matt.fx.graphics.win.bindgeom

import matt.file.MFile
import matt.file.commons.WINDOW_GEOMETRY_FOLDER
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.wrapper.StageWrapper
import matt.json.prim.readJson
import matt.json.prim.writeJson
import matt.klib.math.Geometry


fun StageWrapper.bindGeometry(key: String) = bindGeometry(WINDOW_GEOMETRY_FOLDER["$key.json"])
fun StageWrapper.bindGeometry(file: MFile, defaultWidth: Double = 400.0, defaultHeight: Double = 600.0) {
  if (file.exists()) {
	loadGeometry(file)
  } else {
	file.parentFile!!.mkdirs()
	width = defaultWidth
	height = defaultHeight
  }
  listOf(
	xProperty(), yProperty(), widthProperty(), heightProperty()
  ).forEach { it.onChange { saveGeometryIfValid(file) } }
}

private fun StageWrapper.saveGeometryIfValid(file: MFile) {
  if (!isShowing || isIconified || isFullScreen || x.isNaN() || y.isNaN() || width.isNaN() || height.isNaN()) {
	return
  }
  file.writeJson(
	Geometry(
	  x,
	  y,
	  width,
	  height
	)
  )
}

private fun StageWrapper.loadGeometry(file: MFile) {
  this.apply {
	val stageGeometry = file.readJson<Geometry>()
	x = stageGeometry.x
	y = stageGeometry.y
	width = stageGeometry.width
	height = stageGeometry.height
  }
}

