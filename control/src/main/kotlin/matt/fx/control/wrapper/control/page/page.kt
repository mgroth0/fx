package matt.fx.control.wrapper.control.page

import javafx.scene.control.Pagination
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.graphics.wrapper.node.NodeWrapper

class PaginationWrapper( node: Pagination = Pagination()): ControlWrapperImpl<Pagination>(node) {
  var pageCount
	get() = node.pageCount
	set(value) {
	  node.pageCount = value
	}
  var currentPageIndex
	get() = node.currentPageIndex
	set(value) {
	  node.currentPageIndex = value
	}

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}
