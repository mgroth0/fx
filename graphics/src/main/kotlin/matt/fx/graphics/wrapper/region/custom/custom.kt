package matt.fx.graphics.wrapper.region.custom

/*
class CustomRegionFX(): Region() {
  fun addCustomChild(child: Node) {
	children.add(child)
  }
}

*/
/*
* After thinking about it, the best route probably to not even use this. Just subclass StackPaneWrapper or whatever directly. Yes, it exposes all functions and properties of StackPane. So what? One might argue it gets complicated because it exposed more properties or functions that aren't meant to be used. But that's what subclassing is all about! Put abstract useful layers over functions and properties that should be used in InfoSymbol directly.
*
* The main counterargument that I'm telling myself is that say I start doing something like CustomRegion everywhere. Then say that one day, I want edit a certain property or use a function that CustomRegion doesn't expose. Now what? Invent something that can take the protected members of CustomRegion and somehow un-protect them? Then I just came full circle! What a waste!
*
* *//*

abstract class CustomRegion: RegionWrapperImpl<Region, NW>(CustomRegionFX()) {

  private val customNode by lazy {
	node as CustomRegionFX
  }

  */
/*the major flaw of this is that now extension functions like node.text{} will work on this node... but its a start. Most importantly, I've declared my intention here of creating a CustomRegion. Maybe if I end up creating an entire kotlin remake of JavaFX.graphics this won't even matter... besides arbitrary region wrappers could always seemingly call region.label{} anyway...*//*

  override fun addChild(child: NodeWrapper, index: Int?) {
	require(index == null)
	customNode.addCustomChild(child.node)
  }

}*/
