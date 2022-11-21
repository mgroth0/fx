package matt.fx.control.wrapper.control.page

import javafx.scene.control.Pagination
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.graphics.fxthread.runLater
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp
import kotlin.concurrent.thread

fun ET.pagination(
  pageCount: Int? = null, pageIndex: Int? = null, op: PaginationWrapper.()->Unit = {}
): PaginationWrapper {
  val pagination = PaginationWrapper()
  if (pageCount != null) pagination.pageCount = pageCount
  if (pageIndex != null) pagination.currentPageIndex = pageIndex
  return attach(pagination, op)
}

class PaginationWrapper(node: Pagination = Pagination()): ControlWrapperImpl<Pagination>(node) {
  val pageCountProperty = node.pageCountProperty().toNonNullableProp()
  var pageCount by pageCountProperty
  val currentPageIndexProperty = node.currentPageIndexProperty().toNonNullableProp()
  var currentPageIndex by currentPageIndexProperty

  val pageFactoryProperty by lazy { node.pageFactoryProperty().toNullableProp() }
  var pageFactory by pageFactoryProperty

  private val refreshSem = java.util.concurrent.Semaphore(1)
  fun refresh() {
	thread {
	  refreshSem.acquire()
	  val oldPageFactory = pageFactory
	  runLater {
		pageFactory = null
		runLater {
		  pageFactory = oldPageFactory
		  refreshSem.release()
		}
	  }
	}

  }

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}
