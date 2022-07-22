import matt.file.kt
import matt.file.mFile

apis {
  css
  libs.`fx-graphics`
  libs.jSystemThemeDetector
  hurricanefx
  async
  hotkey
  auto
  fxImage
  stream
  klib
  color
}
implementations {
  libs.svgsalamander
}
implementations(
  dependencies.kotlin("reflect"),
)

generateKt(mFile("matt") + "fx" + "graphics" + "icon" + "gen".kt) {
  """
  package matt.fx.graphics.icon.gen
  """.trimIndent()
}