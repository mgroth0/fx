modtype = LIB

apis(
  ":k:file".jvm()
)
implementations(
//  ":k:klib".jvm()
)


dependencies {
  //  implementation(projects.k.kjlib.lang)
  implementation(libs.fx.swing)
  //  implementation(projects.k.kjlib)
  //  implementation(projects.k.kl.)
  //  projectOrLocalMavenJVM("implementation", ":k:klib")
}