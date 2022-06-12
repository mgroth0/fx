modtype = LIB

implementations(
  ":k:klib".jvm()
)

dependencies {
  //  implementation(projects.kj.kjlib.lang)
  implementation(libs.fx.swing)
  //  implementation(projects.kj.kjlib)
  //  implementation(projects.k.kl.)
  //  projectOrLocalMavenJVM("implementation", ":k:klib")
}