modtype = LIB

apis(
  libs.fx.graphics,
  libs.jSystemThemeDetector,

  projects.k.hurricanefx,
  projects.k.async,
  projects.k.hotkey,
  projects.k.auto,
  projects.k.fx.image,
  projects.k.stream,
  projects.k.kjlib,
  projects.k.klib,
  ":k:color".auto(),

  )

implementations(
  dependencies.kotlin("reflect"),
  libs.svgsalamander
)