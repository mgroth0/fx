modtype = LIB

dependencies {
  implementation(projects.kj.kjlib.lang)
  api(libs.fx.web)
//  implementation(projects.kj.fx.graphics)
  implementation(projects.kj.hurricanefx.eye)
  implementation(projects.kj.hurricanefx)
  implementation(libs.jsoup)
}