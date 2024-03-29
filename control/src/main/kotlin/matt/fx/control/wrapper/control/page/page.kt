package matt.fx.control.wrapper.control.page

import javafx.scene.Node
import javafx.scene.control.Pagination
import javafx.scene.text.Text
import javafx.util.Callback
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.anchor.AnchorPaneWrapperImpl
import matt.lang.function.Op
import matt.obs.col.olist.MutableObsList

fun ET.pagination(
    pageCount: Int? = null,
    pageIndex: Int? = null,
    op: PaginationWrapper.() -> Unit = {}
): PaginationWrapper {
    val pagination = PaginationWrapper()
    if (pageCount != null) pagination.pageCount = pageCount
    if (pageIndex != null) pagination.currentPageIndex = pageIndex
    return attach(pagination, op)
}


class PaginationWrapper(node: Pagination = Pagination()): ControlWrapperImpl<Pagination>(node) {
    val pageCountProperty = node.pageCountProperty().toNonNullableProp().cast<Int>(Int::class)
    var pageCount: Int by pageCountProperty
    val currentPageIndexProperty: NonNullFXBackedBindableProp<Number> = node.currentPageIndexProperty().toNonNullableProp()
    var currentPageIndex by currentPageIndexProperty

  /*  init {
	  currentPageIndexProperty.matt.hurricanefx.eye.wrapper.obs.collect.list.onChange {
		println("changed current page index to $it")
		if (it == 0) {
		  dumpStack()
		}
	  }
	}*/

    val pageFactoryProperty by lazy { node.pageFactoryProperty().toNullableProp() }
    var pageFactory by pageFactoryProperty


    override fun addChild(child: NodeWrapper, index: Int?) {
        TODO()
    }
}

fun listPagination(list: MutableObsList<out NodeWrapper>) = ListPagination(list) { it }

class ListPagination<E>(
    private val list: MutableObsList<E>,
    factory: (E) -> NodeWrapper
): AnchorPaneWrapperImpl<NW>(childClass = NW::class) {

    private val paginator = PaginationWrapper()

    private val myFactory =
        Callback<Int, Node> { pageIndex ->
            list.getOrNull(pageIndex)?.let {
                factory(it).node
            } ?: Text("nothing at index: $pageIndex")
        }

    init {
        allSides = paginator
        list.onChangeWithWeak(this) { lp, _ ->
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
