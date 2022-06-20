modtype = LIB

apis(
  libs.fx.graphics,
  libs.jSystemThemeDetector,

  projects.kj.hurricanefx,
  projects.k.async,
  projects.k.hotkey,
  projects.k.auto,
  projects.k.fx.fxImage,
  projects.k.stream,
  projects.kj.kjlib,
  "k:color".jvm()
)

implementations(
  dependencies.kotlin("reflect")
)