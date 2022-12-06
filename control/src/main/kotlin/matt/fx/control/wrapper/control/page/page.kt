package matt.fx.control.wrapper.control.page

import javafx.scene.Node
import javafx.scene.control.Pagination
import javafx.scene.text.Text
import javafx.util.Callback
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.anchor.AnchorPaneWrapperImpl
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp
import matt.lang.function.Op
import matt.obs.col.olist.ObsList

fun ET.pagination(
  pageCount: Int? = null, pageIndex: Int? = null, op: PaginationWrapper.()->Unit = {}
): PaginationWrapper {
  val pagination = PaginationWrapper()
  if (pageCount != null) pagination.pageCount = pageCount
  if (pageIndex != null) pagination.currentPageIndex = pageIndex
  return attach(pagination, op)
}


class PaginationWrapper(node: Pagination = Pagination()): ControlWrapperImpl<Pagination>(node) {
  val pageCountProperty = node.pageCountProperty().toNonNullableProp().cast<Int>()
  var pageCount by pageCountProperty
  val currentPageIndexProperty = node.currentPageIndexProperty().toNonNullableProp()
  var currentPageIndex by currentPageIndexProperty

  /*  init {
	  currentPageIndexProperty.onChange {
		println("changed current page index to $it")
		if (it == 0) {
		  dumpStack()
		}
	  }
	}*/

  val pageFactoryProperty by lazy { node.pageFactoryProperty().toNullableProp() }
  var pageFactory by pageFactoryProperty
  //
  //  private val refreshSem = java.util.concurrent.Semaphore(1)
  //  fun refresh() {
  //	thread {
  //	  refreshSem.acquire()
  //	  val oldPageFactory = pageFactory
  //	  runLater {
  //		pageFactory = null
  //		runLater {
  //		  pageFactory = oldPageFactory
  //		  runLater {
  //			currentPageIndex = pageCount - 1
  //			refreshSem.release()
  //		  }
  //		}
  //	  }
  //	}
  //
  //  }

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}

fun listPagination(list: ObsList<out NodeWrapper>) = ListPagination(list) { it }

class ListPagination<E>(
  private val list: ObsList<E>,
  factory: (E)->NodeWrapper
): AnchorPaneWrapperImpl<NW>() {

  private val paginator = PaginationWrapper()

  private val myFactory = Callback<Int, Node> { pageIndex ->
	list.getOrNull(pageIndex)?.let {
	  factory(it).node
	} ?: Text("nothing at index: $pageIndex")
  }

  init {
	allSides = paginator
	list.onChangeWithWeak(this) { lp ->
	  lp.refresh()
	}
	refresh()
  }

  @Synchronized
  fun refresh() {
	if (refreshingAfter) return
	paginator.pageCount = if (list.size == 0) 1 else list.size
	paginator.pageFactory = null
	paginator.pageFactory = myFactory
	paginator.currentPageIndex = paginator.pageCount - 1
  }

  private var refreshingAfter = false

  @Synchronized
  fun refreshAfter(op: Op) {
	refreshingAfter = true
	op()
	refreshingAfter = false
	refresh()
  }

}