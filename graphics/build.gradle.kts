modtype = LIB

apis(
  libs.fx.graphics,
  libs.jSystemThemeDetector,

//  projects.kj.hurricanefx,
  projects.kj.async,
  projects.kj.hotkey,
  projects.kj.auto,
  projects.kj.fx.image
)

implementations(
  dependencies.kotlin("reflect")
)