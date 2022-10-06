package matt.fx.control.wrapper.wrapped

import com.sun.javafx.scene.control.FakeFocusTextField
import javafx.event.EventTarget
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.SubScene
import javafx.scene.chart.Axis
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.ValueAxis
import javafx.scene.control.Cell
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Control
import javafx.scene.control.Hyperlink
import javafx.scene.control.IndexedCell
import javafx.scene.control.Label
import javafx.scene.control.Labeled
import javafx.scene.control.ListView
import javafx.scene.control.RadioButton
import javafx.scene.control.RadioMenuItem
import javafx.scene.control.ScrollPane
import javafx.scene.control.Spinner
import javafx.scene.control.SplitPane
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TitledPane
import javafx.scene.control.ToggleButton
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.TreeTableRow
import javafx.scene.control.TreeTableView
import javafx.scene.control.TreeView
import javafx.scene.control.skin.TableColumnHeader
import javafx.scene.control.skin.VirtualFlow
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.FlowPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.TilePane
import javafx.scene.layout.VBox
import javafx.scene.shape.Arc
import javafx.scene.shape.Circle
import javafx.scene.shape.Ellipse
import javafx.scene.shape.Path
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.Stage
import javafx.stage.Window
import matt.fx.control.wrapper.button.radio.RadioButtonWrapper
import matt.fx.control.wrapper.button.toggle.ToggleButtonWrapper
import matt.fx.control.wrapper.chart.axis.AxisWrapper
import matt.fx.control.wrapper.chart.axis.value.ValueAxisWrapper
import matt.fx.control.wrapper.chart.axis.value.number.NumberAxisWrapper
import matt.fx.control.wrapper.chart.line.LineChartWrapper
import matt.fx.control.wrapper.control.ControlWrapper
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.choice.ChoiceBoxWrapper
import matt.fx.control.wrapper.control.column.TableColumnWrapper
import matt.fx.control.wrapper.control.list.ListViewWrapper
import matt.fx.control.wrapper.control.row.CellWrapper
import matt.fx.control.wrapper.control.row.IndexedCellWrapper
import matt.fx.control.wrapper.control.row.TableRowWrapper
import matt.fx.control.wrapper.control.row.TreeTableRowWrapper
import matt.fx.control.wrapper.control.spinner.SpinnerWrapper
import matt.fx.control.wrapper.control.tab.TabWrapper
import matt.fx.control.wrapper.control.table.TableViewWrapper
import matt.fx.control.wrapper.control.table.colhead.TableColumnHeaderWrapper
import matt.fx.control.wrapper.control.text.area.TextAreaWrapper
import matt.fx.control.wrapper.control.text.field.FakeFocusTextFieldWrapper
import matt.fx.control.wrapper.control.text.field.TextFieldWrapper
import matt.fx.control.wrapper.control.tree.TreeViewWrapper
import matt.fx.control.wrapper.control.treecol.TreeTableColumnWrapper
import matt.fx.control.wrapper.control.treetable.TreeTableViewWrapper
import matt.fx.control.wrapper.label.LabelWrapper
import matt.fx.control.wrapper.labeled.LabeledWrapper
import matt.fx.control.wrapper.link.HyperlinkWrapper
import matt.fx.control.wrapper.menu.checkitem.CheckMenuItemWrapper
import matt.fx.control.wrapper.menu.radioitem.RadioMenuItemWrapper
import matt.fx.control.wrapper.scroll.ScrollPaneWrapper
import matt.fx.control.wrapper.split.SplitPaneWrapper
import matt.fx.control.wrapper.tab.TabPaneWrapper
import matt.fx.control.wrapper.titled.TitledPaneWrapper
import matt.fx.control.wrapper.virtualflow.FlowLessVirtualFlowWrapper
import matt.fx.control.wrapper.virtualflow.VirtualFlowWrapper
import matt.fx.control.wrapper.virtualflow.clip.CLIPPED_CONTAINER_QNAME
import matt.fx.control.wrapper.virtualflow.clip.ClippedContainerWrapper
import matt.fx.graphics.service.WrapperService
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.fx.graphics.wrapper.EventTargetWrapperImpl
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.group.GroupWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.line.arc.ArcWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.node.path.PathWrapper
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper
import matt.fx.graphics.wrapper.node.shape.circle.CircleWrapper
import matt.fx.graphics.wrapper.node.shape.ellipse.EllipseWrapper
import matt.fx.graphics.wrapper.node.shape.rect.RectangleWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.anchor.AnchorPaneWrapperImpl
import matt.fx.graphics.wrapper.pane.border.BorderPaneWrapper
import matt.fx.graphics.wrapper.pane.flow.FlowPaneWrapper
import matt.fx.graphics.wrapper.pane.grid.GridPaneWrapper
import matt.fx.graphics.wrapper.pane.hbox.HBoxWrapperImpl
import matt.fx.graphics.wrapper.pane.stack.StackPaneWrapper
import matt.fx.graphics.wrapper.pane.tile.TilePaneWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.fx.graphics.wrapper.subscene.SubSceneWrapper
import matt.fx.graphics.wrapper.text.TextWrapper
import matt.fx.graphics.wrapper.textflow.TextFlowWrapper
import matt.fx.graphics.wrapper.window.WindowWrapper
import matt.lang.NEVER
import matt.lang.err

object WrapperServiceImpl: WrapperService{
  override fun <E: EventTarget> wrapped(e: E): EventTargetWrapper {
	return e.wrapped()
  }
}

inline fun <reified W: EventTargetWrapper> EventTarget.findWrapper(): W? {
  val w = SingularEventTargetWrapper.wrappers[this]
  return w as? W ?: run { require(w == null); w }
}

val EventTarget.wrapper get() = findWrapper<EventTargetWrapper>() as EventTargetWrapperImpl<*>

fun Scene.wrapped(): SceneWrapper<*> = findWrapper() ?: SceneWrapper<ParentWrapper<*>>(this@wrapped)
fun SubScene.wrapped(): SubSceneWrapper<*> = findWrapper() ?: SubSceneWrapper<ParentWrapper<*>>(this@wrapped)
fun Stage.wrapped(): StageWrapper = findWrapper() ?: StageWrapper(this@wrapped)

fun Tab.wrapped(): TabWrapper<NodeWrapper> = findWrapper() ?: TabWrapper(this@wrapped)


fun <T: IndexedCell<*>> VirtualFlow<T>.wrapped(): VirtualFlowWrapper<T> =
  findWrapper() ?: VirtualFlowWrapper(this@wrapped)

fun Pane.wrapped(): PaneWrapperImpl<Pane, *> = findWrapper() ?: PaneWrapperImpl<_, NodeWrapper>(this@wrapped)
fun StackPane.wrapped(): StackPaneWrapper<*> = findWrapper() ?: StackPaneWrapper<NodeWrapper>(this@wrapped)
fun AnchorPane.wrapped(): AnchorPaneWrapperImpl<*> = findWrapper() ?: AnchorPaneWrapperImpl<NodeWrapper>(this@wrapped)
fun GridPane.wrapped(): GridPaneWrapper<*> = findWrapper() ?: GridPaneWrapper<NodeWrapper>(this@wrapped)
fun FlowPane.wrapped(): FlowPaneWrapper<*> = findWrapper() ?: FlowPaneWrapper<NodeWrapper>(this@wrapped)
fun BorderPane.wrapped(): BorderPaneWrapper<*> = findWrapper() ?: BorderPaneWrapper<NodeWrapper>(this@wrapped)
fun SplitPane.wrapped(): SplitPaneWrapper = findWrapper() ?: SplitPaneWrapper(this@wrapped)
fun TextFlow.wrapped(): TextFlowWrapper<*> = findWrapper() ?: TextFlowWrapper<NodeWrapper>(this@wrapped)
fun VBox.wrapped(): VBoxWrapperImpl<*> = findWrapper() ?: VBoxWrapperImpl<NodeWrapper>(this@wrapped)
fun HBox.wrapped(): HBoxWrapperImpl<*> = findWrapper() ?: HBoxWrapperImpl<NodeWrapper>(this@wrapped)
fun TabPane.wrapped(): TabPaneWrapper<TabWrapper<NodeWrapper>> = findWrapper() ?: TabPaneWrapper(this@wrapped)
fun TitledPane.wrapped(): TitledPaneWrapper = findWrapper() ?: TitledPaneWrapper(this@wrapped)
fun TilePane.wrapped(): TilePaneWrapper<*> = findWrapper() ?: TilePaneWrapper<NodeWrapper>(this@wrapped)

fun CheckMenuItem.wrapped(): CheckMenuItemWrapper = findWrapper() ?: CheckMenuItemWrapper(this@wrapped)
fun RadioMenuItem.wrapped(): RadioMenuItemWrapper = findWrapper() ?: RadioMenuItemWrapper(this@wrapped)


fun ToggleButton.wrapped(): ToggleButtonWrapper = findWrapper() ?: ToggleButtonWrapper(this@wrapped)
fun RadioButton.wrapped(): RadioButtonWrapper = findWrapper() ?: RadioButtonWrapper(this@wrapped)

fun <E: Any> TableView<E>.wrapped(): TableViewWrapper<E> = findWrapper() ?: TableViewWrapper(this@wrapped)
fun <E: Any> TreeTableView<E>.wrapped(): TreeTableViewWrapper<E> = findWrapper() ?: TreeTableViewWrapper(this@wrapped)
fun <E: Any> TreeView<E>.wrapped(): TreeViewWrapper<E> = findWrapper() ?: TreeViewWrapper(this@wrapped)
fun <E: Any> ListView<E>.wrapped(): ListViewWrapper<E> = findWrapper() ?: ListViewWrapper(this@wrapped)


fun <E: Any, P> TreeTableColumn<E, P>.wrapped(): TreeTableColumnWrapper<E, P> =
  findWrapper() ?: TreeTableColumnWrapper(this@wrapped)

fun <E: Any, P> TableColumn<E, P>.wrapped(): TableColumnWrapper<E, P> = findWrapper() ?: TableColumnWrapper(this@wrapped)

fun Rectangle.wrapped(): RectangleWrapper = findWrapper() ?: RectangleWrapper(this@wrapped)
fun Circle.wrapped(): CircleWrapper = findWrapper() ?: CircleWrapper(this@wrapped)
fun Arc.wrapped(): ArcWrapper = findWrapper() ?: ArcWrapper(this@wrapped)
fun Ellipse.wrapped(): EllipseWrapper = findWrapper() ?: EllipseWrapper(this@wrapped)
fun Path.wrapped(): PathWrapper = findWrapper() ?: PathWrapper(this@wrapped)
fun Text.wrapped(): TextWrapper = findWrapper() ?: TextWrapper(this@wrapped)


fun Label.wrapped(): LabelWrapper = findWrapper() ?: LabelWrapper(this@wrapped)

fun TextArea.wrapped(): TextAreaWrapper = findWrapper() ?: TextAreaWrapper(this@wrapped)
fun <T: Any> Spinner<T>.wrapped(): SpinnerWrapper<T> = findWrapper() ?: SpinnerWrapper(this@wrapped)
fun <T: Any> ChoiceBox<T>.wrapped(): ChoiceBoxWrapper<T> = findWrapper() ?: ChoiceBoxWrapper(this@wrapped)
fun FakeFocusTextField.wrapped(): FakeFocusTextFieldWrapper = findWrapper() ?: FakeFocusTextFieldWrapper(this@wrapped)
fun TextField.wrapped(): TextFieldWrapper = findWrapper() ?: TextFieldWrapper(this@wrapped)
fun ScrollPane.wrapped(): ScrollPaneWrapper<NodeWrapper> = findWrapper() ?: ScrollPaneWrapper(this@wrapped)


fun Hyperlink.wrapped(): HyperlinkWrapper = findWrapper() ?: HyperlinkWrapper(this@wrapped)

fun TableColumnHeader.wrapped(): TableColumnHeaderWrapper = findWrapper() ?: TableColumnHeaderWrapper(this@wrapped)


fun <X, Y> LineChart<X, Y>.wrapped(): LineChartWrapper<X, Y> = findWrapper() ?: LineChartWrapper(this@wrapped)


fun Group.wrapped(): GroupWrapper<*> = findWrapper() ?: GroupWrapper<NodeWrapper>(this@wrapped)


fun <T> Cell<T>.wrapped(): CellWrapper<T, *> = findWrapper() ?: CellWrapper(this@wrapped)
fun <T> IndexedCell<T>.wrapped(): IndexedCellWrapper<T, *> = findWrapper() ?: IndexedCellWrapper(this@wrapped)
fun <T> TreeTableRow<T>.wrapped(): TreeTableRowWrapper<T> = findWrapper() ?: TreeTableRowWrapper(this@wrapped)
fun <T> TableRow<T>.wrapped(): TableRowWrapper<T> = findWrapper() ?: TableRowWrapper(this@wrapped)


//fun EventTarget.matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.obs.collect.matt.fx.control.wrapper.wrapped.wrapped() = matt.fx.control.wrapper.wrapped.findWrapper<EventTargetWrapper>()!!/* ?: SingularEventTargetWrapper(this@matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.obs.collect.matt.fx.control.wrapper.wrapped.wrapped)*/
//fun <N: Node> N.matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.obs.collect.matt.fx.control.wrapper.wrapped.wrapped(): NodeWrapper = matt.fx.control.wrapper.wrapped.findWrapper()?.let { it as NodeWrapper }!!/* ?: NodeWrapperImpl(this@matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.obs.collect.matt.fx.control.wrapper.wrapped.wrapped)*/
//fun <N: Parent> N.matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.obs.collect.matt.fx.control.wrapper.wrapped.wrapped() = matt.fx.control.wrapper.wrapped.findWrapper()?.let { it as ParentWrapper }!! /*?: ParentWrapperImpl(this@matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.obs.collect.matt.fx.control.wrapper.wrapped.wrapped)*/


/*
val constructorMap = lazyMap<KType, KFunction<EventTargetWrapper>> { typ ->
  EventTargetWrapper::class
	.subclasses()
	.asSequence()
	.filter { !it.isAbstract }
	.mapNotNull { it.primaryConstructor }
	.first {
	  it.parameters.first().type.isSupertypeOf(typ)
	  *//*(it.parameters.first().type.classifier as KClass<*>).qualifiedName == qName*//*
	}
}*/



fun NumberAxis.wrapped(): NumberAxisWrapper = findWrapper() ?: NumberAxisWrapper(this@wrapped)

@Suppress("UNCHECKED_CAST")
fun <T: Number> ValueAxis<T>.wrapped(): ValueAxisWrapper<T> = findWrapper() ?: when (this) {
  is NumberAxis -> wrapped() as ValueAxisWrapper<T>
  else          -> cannotFindWrapper()
}

fun <T> Axis<T>.wrapped(): AxisWrapper<T, Axis<T>> = findWrapper() ?: when (this) {
  is ValueAxis -> wrapped()
  else         -> cannotFindWrapper()
}


fun Labeled.wrapped(): LabeledWrapper<*> = findWrapper() ?: when (this) {
  is Label           -> wrapped()
  is TitledPane      -> wrapped()
  is Hyperlink       -> wrapped()
  is TreeTableRow<*> -> wrapped()
  is TableRow<*>     -> wrapped()
  is IndexedCell<*>  -> wrapped()
  is Cell<*>         -> wrapped()
  is RadioButton     -> wrapped()
  is ToggleButton    -> wrapped()
  else               -> cannotFindWrapper()
}

fun Control.wrapped(): ControlWrapper = findWrapper() ?: when (this) {
  is Labeled            -> wrapped()
  is TabPane            -> wrapped()
  is TextArea           -> wrapped()
  is FakeFocusTextField -> wrapped()
  is TextField          -> wrapped()
  is Spinner<*>         -> wrapped()
  else                  -> {

	when {
	  this::class.simpleName == "ScrollBarWidget" -> {
		val contrl: Control = this
		object: ControlWrapperImpl<Control>(contrl) {
		  override fun addChild(child: NodeWrapper, index: Int?) = NEVER
		}
	  }

	  else                                        -> cannotFindWrapper()
	}
  }
}


fun Region.wrapped(): RegionWrapper<*> = findWrapper() ?: when (this) {
  is GridPane          -> wrapped()
  is FlowPane          -> wrapped()
  is VBox              -> wrapped()
  is HBox              -> wrapped()
  is AnchorPane        -> wrapped()
  is StackPane         -> wrapped()
  is TilePane          -> wrapped()
  is Pane              -> wrapped()
  is TableColumnHeader -> wrapped()
  is Control           -> wrapped()
  is VirtualFlow<*>    -> wrapped()
  is Axis<*>           -> wrapped()
  else                 -> when (this::class.qualifiedName) {
	CLIPPED_CONTAINER_QNAME                                              -> ClippedContainerWrapper(this@wrapped)
	"org.fxmisc.richtext.ParagraphBox"                                   -> ParagraphBoxWrapper(this@wrapped)
	"org.fxmisc.flowless.Navigator"                                      -> NavigatorWrapper(this@wrapped)
	"org.fxmisc.flowless.VirtualFlow"                                    -> FlowLessVirtualFlowWrapper(this@wrapped)
	"eu.hansolo.fx.charts.CoxcombChart"                                  -> CoxCombWrapper(this@wrapped)
	"eu.hansolo.fx.charts.matt.fx.control.wrapper.wrapped.SunburstChart" -> SunburstChart(this@wrapped)
	else                                                                 -> cannotFindWrapper()
  }
}

/*hansolo*/
class SunburstChart(sunburst: Region): RegionWrapperImpl<Region, NodeWrapper>(sunburst) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }

}

/*hansolo*/
class CoxCombWrapper(val coxcomb: Region): RegionWrapperImpl<Region, NodeWrapper>(coxcomb) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }

}

/*RichTextFX*/
class ParagraphBoxWrapper(val paragraphBox: Region): RegionWrapperImpl<Region, NodeWrapper>(paragraphBox) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }

}

/*FlowLess*/
class NavigatorWrapper(val navigator: Region): RegionWrapperImpl<Region, NodeWrapper>(navigator) {
  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}


fun Parent.wrapped(): ParentWrapper<*> = findWrapper() ?: when (this) {
  is Region -> wrapped()
  is Group  -> wrapped()
  else      -> cannotFindWrapper()
}

fun Shape.wrapped(): ShapeWrapper<*> = findWrapper() ?: when (this) {
  is Rectangle -> wrapped()
  is Circle    -> wrapped()
  is Arc       -> wrapped()
  is Ellipse   -> wrapped()
  is Path      -> wrapped()
  is Text      -> wrapped()
  else         -> cannotFindWrapper()
}


fun Node.wrapped(): NodeWrapper = findWrapper() ?: when (this) {
  is Parent   -> wrapped()
  is SubScene -> wrapped()
  is Shape    -> wrapped()
  else        -> cannotFindWrapper()
}

fun Window.wrapped(): WindowWrapper<*> = findWrapper() ?: when (this) {
  is Stage -> wrapped()
  else     -> cannotFindWrapper()
}

fun EventTarget.wrapped(): EventTargetWrapper = findWrapper() ?: when (this) {
  is Node   -> wrapped()
  is Scene  -> wrapped()
  is Window -> wrapped()
  is Tab    -> wrapped()
  else      -> cannotFindWrapper()
}

private fun EventTarget.cannotFindWrapper(): Nothing =
  err("what is the matt.fx.control.wrapper.wrapped.getWrapper for ${this::class.qualifiedName}?")

/*?: run {
  val theMap = constructorMap
//	  W::class.starProjectedType
  val theConstructor = theMap[W::class.starProjectedType]
  theConstructor.call(this) as W
}*//*?: W::class.primaryConstructor!!.call(this)*/


