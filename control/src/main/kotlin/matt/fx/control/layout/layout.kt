package matt.fx.control.layout

import javafx.scene.control.ToolBar

@Deprecated(
  "No need to wrap ToolBar matt.fx.control.layout.children in matt.fx.control.layout.children{} anymore. Remove the wrapper and all builder items will still be added as before.",
  ReplaceWith("no matt.fx.control.layout.children{} wrapper"),
  DeprecationLevel.WARNING
)
fun ToolBar.children(op: ToolBar.()->Unit) = apply { op() }