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
  "k:color".jvm(),

  )

//repositories {
////  maven(url = "https://jitpack.io")
//  maven(url="https://packages.atlassian.com/maven-public/")
//}

implementations(
  dependencies.kotlin("reflect"),
//  libs.fxsvgimage,
libs.svgsalamander
)