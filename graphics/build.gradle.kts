modtype = LIB

apis(
  libs.fx.graphics,
  libs.jSystemThemeDetector,

  projects.kj.hurricanefx,
  projects.kj.async,
  projects.kj.hotkey,
  projects.kj.auto,
  projects.kj.fx.fxImage,
  projects.k.stream,
  projects.kj.kjlib,
  projects.k.color
)

implementations(
  dependencies.kotlin("reflect")
)