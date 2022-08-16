package matt.fx.node.proto.scrollvbox

import javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED
import javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER
import javafx.scene.layout.Pane
import matt.hurricanefx.Scrolls
import matt.hurricanefx.eye.prop.math.minus
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.hurricanefx.wrapper.pane.PaneWrapperImpl
import matt.hurricanefx.wrapper.pane.scroll.ScrollPaneWrapper
import matt.hurricanefx.wrapper.pane.vbox.VBoxWrapper
import matt.klib.lang.applyIt

abstract class ScrollVBox(
  scrollpane: ScrollPaneWrapper<VBoxWrapper<*>> = ScrollPaneWrapper(),
  val vbox: VBoxWrapper<NodeWrapper> = VBoxWrapper()
): PaneWrapperImpl<Pane, NodeWrapper>(Pane()), Scrolls { //Refreshable
  override val scrollPane = scrollpane

  init {
	children.add(scrollPane.applyIt { sp ->
	  /*If I want to configure, make into constructor params?*/
	  vbarPolicy = AS_NEEDED
	  hbarPolicy = NEVER
	  isFitToWidth = true

	  prefWidthProperty.bind(this@ScrollVBox.widthProperty)
	  prefHeightProperty.bind(this@ScrollVBox.heightProperty)
	  val woffset = 25.0
	  layoutX = woffset
	  layoutY = 0.0

	  content = this@ScrollVBox.vbox.apply {
		/*matt.hurricanefx.tornadofx.vector.matt.hurricanefx.eye.prop.math.minus 10 here is so everything looks nicer*/
		/*also neccesary to prevent buggy javafx bug where fitToWidth doesnt work and it trys to hscroll.*/
		/*needs to be exact or content will flow out of scrollpane (doesnt obey fitToWidth)*/
		exactWidthProperty.bind(sp.widthProperty.minus(woffset*2))

		/*reason: this causes stupid buggy fx vertical scroll bar to properly hide when not needed*/
		minHeightProperty.bind(sp.heightProperty.minus(50.0))
	  }
	})
  }

  //  abstract fun VBox.refreshContent()
  //
  //  final override fun refresh() {
  //	vbox.refreshContent()
  //  }
}


