modtype = LIB

dependencies {
  //  implementation(projects.kj.kjlib.lang)
  implementation(libs.fx.swing)
  //  implementation(projects.kj.kjlib)
  //  implementation(projects.k.kl.)
  projectOrLocalMavenJVM("implementation", "k:klib")
}