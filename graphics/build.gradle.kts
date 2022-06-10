modtype = LIB

apis(
//  projects.kj.kjlib.lang,
  libs.fx.graphics,
  projects.kj.hurricanefx,
  libs.jSystemThemeDetector,
  projects.kj.hotkey,
  projects.kj.auto
)

implementations(
  dependencies.kotlin("reflect")
)