modtype = LIB

apis(
  libs.fx.graphics,
  libs.jSystemThemeDetector,

  projects.kj.hurricanefx,
  projects.k.async,
  projects.kj.hotkey,
  projects.kj.auto,
  projects.kj.fx.fxImage,
  projects.k.stream,
  projects.kj.kjlib,
  "k:color".jvm()
)

implementations(
  dependencies.kotlin("reflect")
)