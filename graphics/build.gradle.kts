modtype = LIB

apis(
  libs.fx.graphics,
  libs.jSystemThemeDetector,

//  projects.kj.hurricanefx,
  projects.kj.async,
  projects.kj.hotkey,
  projects.kj.auto
)

implementations(
  dependencies.kotlin("reflect")
)