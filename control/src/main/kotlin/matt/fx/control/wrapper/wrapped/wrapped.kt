
package matt.fx.control.wrapper.wrapped


import com.sun.javafx.scene.control.DoubleField
import com.sun.javafx.scene.control.FakeFocusTextField
import com.sun.javafx.scene.control.IntegerField
import com.sun.javafx.scene.control.WebColorField
import com.sun.javafx.scene.control.skin.FXVK
import javafx.event.EventTarget
import javafx.scene.AmbientLight
import javafx.scene.DirectionalLight
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.ParallelCamera
import javafx.scene.Parent
import javafx.scene.PerspectiveCamera
import javafx.scene.PointLight
import javafx.scene.Scene
import javafx.scene.SpotLight
import javafx.scene.SubScene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Accordion
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonBase
import javafx.scene.control.Cell
import javafx.scene.control.CheckBox
import javafx.scene.control.CheckBoxTreeItem
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ColorPicker
import javafx.scene.control.ComboBox
import javafx.scene.control.ContextMenu
import javafx.scene.control.Control
import javafx.scene.control.CustomMenuItem
import javafx.scene.control.DatePicker
import javafx.scene.control.Hyperlink
import javafx.scene.control.IndexedCell
import javafx.scene.control.Label
import javafx.scene.control.Labeled
import javafx.scene.control.ListView
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuButton
import javafx.scene.control.MenuItem
import javafx.scene.control.Pagination
import javafx.scene.control.PasswordField
import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.RadioButton
import javafx.scene.control.RadioMenuItem
import javafx.scene.control.ScrollBar
import javafx.scene.control.ScrollPane
import javafx.scene.control.Separator
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.Slider
import javafx.scene.control.Spinner
import javafx.scene.control.SplitMenuButton
import javafx.scene.control.SplitPane
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TableColumn
import javafx.scene.control.TableColumnBase
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TitledPane
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToolBar
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.TreeTableRow
import javafx.scene.control.TreeTableView
import javafx.scene.control.TreeView
import javafx.scene.control.skin.TableColumnHeader
import javafx.scene.control.skin.VirtualFlow
import javafx.scene.image.ImageView
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
import javafx.scene.shape.Box
import javafx.scene.shape.Circle
import javafx.scene.shape.CubicCurve
import javafx.scene.shape.Cylinder
import javafx.scene.shape.Ellipse
import javafx.scene.shape.Line
import javafx.scene.shape.MeshView
import javafx.scene.shape.Path
import javafx.scene.shape.Polygon
import javafx.scene.shape.Polyline
import javafx.scene.shape.QuadCurve
import javafx.scene.shape.Rectangle
import javafx.scene.shape.SVGPath
import javafx.scene.shape.Shape
import javafx.scene.shape.Shape3D
import javafx.scene.shape.Sphere
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.scene.transform.Affine
import javafx.scene.transform.Rotate
import javafx.scene.transform.Scale
import javafx.scene.transform.Shear
import javafx.scene.transform.Transform
import javafx.scene.transform.Translate
import javafx.stage.Stage
import javafx.stage.Window
import matt.fx.control.wrapper.button.radio.RadioButtonWrapper
import matt.fx.control.wrapper.button.toggle.ToggleButtonWrapper
import matt.fx.control.wrapper.buttonbar.ButtonBarWrapper
import matt.fx.control.wrapper.checkbox.CheckBoxWrapper
import matt.fx.control.wrapper.contextmenu.ContextMenuWrapper
import matt.fx.control.wrapper.control.ControlWrapper
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.control.accordion.AccordionWrapper
import matt.fx.control.wrapper.control.button.ButtonWrapper
import matt.fx.control.wrapper.control.button.base.ButtonBaseWrapper
import matt.fx.control.wrapper.control.choice.ChoiceBoxWrapper
import matt.fx.control.wrapper.control.colbase.TableColumnBaseWrapper
import matt.fx.control.wrapper.control.colorpick.ColorPickerWrapper
import matt.fx.control.wrapper.control.column.TableColumnWrapper
import matt.fx.control.wrapper.control.combo.ComboBoxWrapper
import matt.fx.control.wrapper.control.datepick.DatePickerWrapper
import matt.fx.control.wrapper.control.fxvk.FXVKWrapper
import matt.fx.control.wrapper.control.list.ListViewWrapper
import matt.fx.control.wrapper.control.page.PaginationWrapper
import matt.fx.control.wrapper.control.row.CellWrapper
import matt.fx.control.wrapper.control.row.IndexedCellWrapper
import matt.fx.control.wrapper.control.row.TableRowWrapper
import matt.fx.control.wrapper.control.row.TreeTableRowWrapper
import matt.fx.control.wrapper.control.slider.SliderWrapper
import matt.fx.control.wrapper.control.spinner.SpinnerWrapper
import matt.fx.control.wrapper.control.tab.TabWrapper
import matt.fx.control.wrapper.control.table.TableViewWrapper
import matt.fx.control.wrapper.control.table.colhead.TableColumnHeaderWrapper
import matt.fx.control.wrapper.control.text.area.TextAreaWrapper
import matt.fx.control.wrapper.control.text.field.FakeFocusTextFieldWrapper
import matt.fx.control.wrapper.control.text.field.TextFieldWrapper
import matt.fx.control.wrapper.control.text.field.color.WebColorFieldWrapper
import matt.fx.control.wrapper.control.text.field.double.DoubleFieldWrapper
import matt.fx.control.wrapper.control.text.field.int.IntegerFieldWrapper
import matt.fx.control.wrapper.control.text.field.pass.PasswordFieldWrapper
import matt.fx.control.wrapper.control.tree.TreeViewWrapper
import matt.fx.control.wrapper.control.treecol.TreeTableColumnWrapper
import matt.fx.control.wrapper.control.treetable.TreeTableViewWrapper
import matt.fx.control.wrapper.label.LabelWrapper
import matt.fx.control.wrapper.labeled.LabeledWrapper
import matt.fx.control.wrapper.link.HyperlinkWrapper
import matt.fx.control.wrapper.menu.MenuWrapper
import matt.fx.control.wrapper.menu.button.MenuButtonWrapper
import matt.fx.control.wrapper.menu.checkitem.CheckMenuItemWrapper
import matt.fx.control.wrapper.menu.item.MenuItemWrapper
import matt.fx.control.wrapper.menu.item.custom.CustomMenuItemWrapper
import matt.fx.control.wrapper.menu.item.sep.SeparatorMenuItemWrapper
import matt.fx.control.wrapper.menu.radioitem.RadioMenuItemWrapper
import matt.fx.control.wrapper.menu.splitbutton.SplitMenuButtonWrapper
import matt.fx.control.wrapper.menubar.MenuBarWrapper
import matt.fx.control.wrapper.progressbar.ProgressBarWrapper
import matt.fx.control.wrapper.progressindicator.ProgressIndicatorWrapper
import matt.fx.control.wrapper.scroll.ScrollPaneWrapper
import matt.fx.control.wrapper.scroll.bar.ScrollBarWrapper
import matt.fx.control.wrapper.sep.SeparatorWrapper
import matt.fx.control.wrapper.split.SplitPaneWrapper
import matt.fx.control.wrapper.tab.TabPaneWrapper
import matt.fx.control.wrapper.titled.TitledPaneWrapper
import matt.fx.control.wrapper.toolbar.ToolBarWrapper
import matt.fx.control.wrapper.treeitem.CheckBoxTreeItemWrapper
import matt.fx.control.wrapper.treeitem.TreeItemWrapper
import matt.fx.control.wrapper.virtualflow.FlowLessVirtualFlowWrapper
import matt.fx.control.wrapper.virtualflow.VirtualFlowWrapper
import matt.fx.control.wrapper.virtualflow.clip.CLIPPED_CONTAINER_QNAME
import matt.fx.control.wrapper.virtualflow.clip.ClippedContainerWrapper
import matt.fx.control.wrapper.wrapped.unknown.unknownWrapper
import matt.fx.control.wrapper.wrapped.util.cannotFindWrapper
import matt.fx.graphics.service.WrapperService
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.fx.graphics.wrapper.EventTargetWrapperImpl
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.camera.ParallelCameraWrapper
import matt.fx.graphics.wrapper.camera.PerspectiveCameraWrapper
import matt.fx.graphics.wrapper.canvas.CanvasWrapper
import matt.fx.graphics.wrapper.group.GroupWrapper
import matt.fx.graphics.wrapper.imageview.ImageViewWrapper
import matt.fx.graphics.wrapper.light.AmbientLightWrapper
import matt.fx.graphics.wrapper.light.DirectionalLightWrapper
import matt.fx.graphics.wrapper.light.PointLightWrapper
import matt.fx.graphics.wrapper.light.SpotLightWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.line.LineWrapper
import matt.fx.graphics.wrapper.node.line.arc.ArcWrapper
import matt.fx.graphics.wrapper.node.line.cubic.CubicCurveWrapper
import matt.fx.graphics.wrapper.node.line.poly.PolylineWrapper
import matt.fx.graphics.wrapper.node.line.quad.QuadCurveWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.node.path.PathWrapper
import matt.fx.graphics.wrapper.node.shape.ShapeWrapper
import matt.fx.graphics.wrapper.node.shape.circle.CircleWrapper
import matt.fx.graphics.wrapper.node.shape.ellipse.EllipseWrapper
import matt.fx.graphics.wrapper.node.shape.poly.PolygonWrapper
import matt.fx.graphics.wrapper.node.shape.rect.RectangleWrapper
import matt.fx.graphics.wrapper.node.shape.svg.SVGPathWrapper
import matt.fx.graphics.wrapper.node.shape.threed.box.BoxWrapper3D
import matt.fx.graphics.wrapper.node.shape.threed.cylinder.CylinderWrapper
import matt.fx.graphics.wrapper.node.shape.threed.mesh.MeshViewWrapper
import matt.fx.graphics.wrapper.node.shape.threed.sphere.SphereWrapper
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
import matt.fx.graphics.wrapper.transform.AffineWrapper
import matt.fx.graphics.wrapper.transform.ImmutableTransformWrapper
import matt.fx.graphics.wrapper.transform.RotateWrapper
import matt.fx.graphics.wrapper.transform.ScaleWrapper
import matt.fx.graphics.wrapper.transform.ShearWrapper
import matt.fx.graphics.wrapper.transform.TransformWrapper
import matt.fx.graphics.wrapper.transform.TranslateWrapper
import matt.fx.graphics.wrapper.window.WindowWrapper
import matt.lang.NEVER
import matt.lang.assertions.require.requireNull
import matt.lang.classname.JvmQualifiedClassName
import matt.lang.classname.jvmQualifiedClassName


object WrapperServiceImpl : WrapperService {
    override fun <E : EventTarget> wrapped(e: E): EventTargetWrapper = e.wrapped()
}

inline fun <reified W : EventTargetWrapper> EventTarget.findWrapper(): W? {
    val w = SingularEventTargetWrapper[this]
    return w as? W ?: run {
//        require(w == null)
        requireNull(w)
        w
    }
}

val EventTarget.wrapper get() = findWrapper<EventTargetWrapper>() as EventTargetWrapperImpl<*>

fun Scene.wrapped(): SceneWrapper<*> = findWrapper() ?: SceneWrapper<ParentWrapper<*>>(this@wrapped)
fun SubScene.wrapped(): SubSceneWrapper<*> = findWrapper() ?: SubSceneWrapper<ParentWrapper<*>>(this@wrapped)
fun Stage.wrapped(): StageWrapper = findWrapper() ?: StageWrapper(this@wrapped)

fun Tab.wrapped(): TabWrapper<NodeWrapper> = findWrapper() ?: TabWrapper(this@wrapped)


fun <T : IndexedCell<*>> VirtualFlow<T>.wrapped(): VirtualFlowWrapper<T> =
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


fun ToggleButton.wrapped(): ToggleButtonWrapper = findWrapper() ?: ToggleButtonWrapper(this@wrapped)
fun RadioButton.wrapped(): RadioButtonWrapper = findWrapper() ?: RadioButtonWrapper(this@wrapped)

fun <E : Any> TableView<E>.wrapped(): TableViewWrapper<E> = findWrapper() ?: TableViewWrapper(this@wrapped)
fun <E : Any> TreeTableView<E>.wrapped(): TreeTableViewWrapper<E> = findWrapper() ?: TreeTableViewWrapper(this@wrapped)
fun <E : Any> TreeView<E>.wrapped(): TreeViewWrapper<E> = findWrapper() ?: TreeViewWrapper(this@wrapped)
fun <E : Any> ListView<E>.wrapped(): ListViewWrapper<E> = findWrapper() ?: ListViewWrapper(this@wrapped)


fun <E : Any, P> TreeTableColumn<E, P>.wrapped(): TreeTableColumnWrapper<E, P> =
    findWrapper() ?: TreeTableColumnWrapper(this@wrapped)

@Suppress("UNCHECKED_CAST")
fun <E, P> TableColumn<E, P>.wrapped(): TableColumnWrapper<E & Any, P> =
    findWrapper() ?: TableColumnWrapper(
        this@wrapped as TableColumn<E & Any, P>
    )

fun Rectangle.wrapped(): RectangleWrapper = findWrapper() ?: RectangleWrapper(this@wrapped)
fun Circle.wrapped(): CircleWrapper = findWrapper() ?: CircleWrapper(this@wrapped)
fun Arc.wrapped(): ArcWrapper = findWrapper() ?: ArcWrapper(this@wrapped)
fun Ellipse.wrapped(): EllipseWrapper = findWrapper() ?: EllipseWrapper(this@wrapped)
fun Path.wrapped(): PathWrapper = findWrapper() ?: PathWrapper(this@wrapped)
fun Text.wrapped(): TextWrapper = findWrapper() ?: TextWrapper(this@wrapped)


fun Label.wrapped(): LabelWrapper = findWrapper() ?: LabelWrapper(this@wrapped)

fun TextArea.wrapped(): TextAreaWrapper = findWrapper() ?: TextAreaWrapper(this@wrapped)
fun <T : Any> Spinner<T>.wrapped(): SpinnerWrapper<T> = findWrapper() ?: SpinnerWrapper(this@wrapped)
fun <T : Any> ChoiceBox<T>.wrapped(): ChoiceBoxWrapper<T> = findWrapper() ?: ChoiceBoxWrapper(this@wrapped)
fun FakeFocusTextField.wrapped(): FakeFocusTextFieldWrapper = findWrapper() ?: FakeFocusTextFieldWrapper(this@wrapped)
fun TextField.wrapped(): TextFieldWrapper = findWrapper() ?: TextFieldWrapper(this@wrapped)
fun ScrollPane.wrapped(): ScrollPaneWrapper<NodeWrapper> = findWrapper() ?: ScrollPaneWrapper(this@wrapped)

fun Hyperlink.wrapped(): HyperlinkWrapper = findWrapper() ?: HyperlinkWrapper(this@wrapped)

fun TableColumnHeader.wrapped(): TableColumnHeaderWrapper = findWrapper() ?: TableColumnHeaderWrapper(this@wrapped)


fun Group.wrapped(): GroupWrapper<*> = findWrapper() ?: GroupWrapper<NodeWrapper>(this@wrapped)

fun <T> Cell<T>.wrapped(): CellWrapper<T, *> = findWrapper() ?: CellWrapper(this@wrapped)
fun <T> IndexedCell<T>.wrapped(): IndexedCellWrapper<T, *> = findWrapper() ?: IndexedCellWrapper(this@wrapped)
fun <T> TreeTableRow<T>.wrapped(): TreeTableRowWrapper<T> = findWrapper() ?: TreeTableRowWrapper(this@wrapped)
fun <T> TableRow<T>.wrapped(): TableRowWrapper<T> = findWrapper() ?: TableRowWrapper(this@wrapped)


//fun EventTarget.matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.obs.collect.matt.fx.control.wrapper.wrapped.wrapped() = matt.fx.control.wrapper.wrapped.findWrapper<EventTargetWrapper>()!!/* ?: SingularEventTargetWrapper(this@matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.obs.collect.matt.fx.control.wrapper.wrapped.wrapped)*/
//fun <N: Node> N.matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.obs.collect.matt.fx.control.wrapper.wrapped.wrapped(): NodeWrapper = matt.fx.control.wrapper.wrapped.findWrapper()?.let { it as NodeWrapper }!!/* ?: matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl(this@matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.matt.hurricanefx.eye.matt.fx.control.wrapper.wrapped.getWrapper.obs.collect.matt.fx.control.wrapper.wrapped.wrapped)*/
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




fun ToolBar.wrapped(): ToolBarWrapper = findWrapper() ?: ToolBarWrapper(this@wrapped)


//fun TableRow<T>.wrapped()

fun Labeled.wrapped(): LabeledWrapper<*> = findWrapper() ?: when (this) {
    is Label           -> wrapped()
    is TitledPane      -> wrapped()
    is TreeTableRow<*> -> wrapped()
    is TableRow<*>     -> wrapped()
    is IndexedCell<*>  -> wrapped()
    is Cell<*>         -> wrapped()
    is ButtonBase      -> wrapped()
    else               -> cannotFindWrapper()
}

fun ButtonBase.wrapped(): ButtonBaseWrapper<*> = findWrapper() ?: when (this) {
    is MenuButton   -> wrapped()
    is Hyperlink    -> wrapped()
    is RadioButton  -> wrapped()
    is ToggleButton -> wrapped()
    is Button       -> wrapped()
    else            -> cannotFindWrapper()
}


fun MenuButton.wrapped(): MenuButtonWrapper = findWrapper() ?: when (this) {
    is SplitMenuButton -> wrapped()
    else               -> findWrapper() ?: MenuButtonWrapper(this@wrapped)
}


fun Button.wrapped(): ButtonWrapper = findWrapper() ?: ButtonWrapper(this@wrapped)


fun CheckBox.wrapped(): CheckBoxWrapper = findWrapper() ?: CheckBoxWrapper(this@wrapped)
fun PasswordField.wrapped(): PasswordFieldWrapper = findWrapper() ?: PasswordFieldWrapper(this@wrapped)
fun ProgressBar.wrapped(): ProgressBarWrapper = findWrapper() ?: ProgressBarWrapper(this@wrapped)
fun WebColorField.wrapped(): WebColorFieldWrapper = findWrapper() ?: WebColorFieldWrapper(this@wrapped)
fun ComboBox<*>.wrapped(): ComboBoxWrapper<*> = findWrapper() ?: ComboBoxWrapper(this@wrapped)
fun ProgressIndicator.wrapped(): ProgressIndicatorWrapper = findWrapper() ?: ProgressIndicatorWrapper(this@wrapped)
fun Pagination.wrapped(): PaginationWrapper = findWrapper() ?: PaginationWrapper(this@wrapped)
fun ScrollBar.wrapped(): ScrollBarWrapper = findWrapper() ?: ScrollBarWrapper(this@wrapped)
fun Accordion.wrapped(): AccordionWrapper = findWrapper() ?: AccordionWrapper(this@wrapped)
fun ColorPicker.wrapped(): ColorPickerWrapper = findWrapper() ?: ColorPickerWrapper(this@wrapped)
fun DatePicker.wrapped(): DatePickerWrapper = findWrapper() ?: DatePickerWrapper(this@wrapped)
fun ButtonBar.wrapped(): ButtonBarWrapper = findWrapper() ?: ButtonBarWrapper(this@wrapped)
fun Slider.wrapped(): SliderWrapper = findWrapper() ?: SliderWrapper(this@wrapped)
fun DoubleField.wrapped(): DoubleFieldWrapper = findWrapper() ?: DoubleFieldWrapper(this@wrapped)
fun FXVK.wrapped(): FXVKWrapper = findWrapper() ?: FXVKWrapper(this@wrapped)
fun Separator.wrapped(): SeparatorWrapper = findWrapper() ?: SeparatorWrapper(this@wrapped)

fun Control.wrapped(): ControlWrapper = findWrapper() ?: when (this) {
    is ProgressBar        -> wrapped()
    is WebColorField      -> wrapped()
    is CheckBox           -> wrapped()
    is ComboBox<*>        -> wrapped()
    is ProgressIndicator  -> wrapped()
    is Pagination         -> wrapped()
    is TreeTableView<*>   -> wrapped()
    is ListView<*>        -> wrapped()
    is TreeView<*>        -> wrapped()
    is TableView<*>       -> wrapped()
    is ChoiceBox<*>       -> wrapped()
    is ScrollBar          -> wrapped()
    is Accordion          -> wrapped()
    is ColorPicker        -> wrapped()
    is DatePicker         -> wrapped()
    is SplitPane          -> wrapped()
    is ButtonBar          -> wrapped()
    is Slider             -> wrapped()
    is Labeled            -> wrapped()
    is TabPane            -> wrapped()
    is TextArea           -> wrapped()
    is FakeFocusTextField -> wrapped()
    is DoubleField        -> wrapped()
    is IntegerField       -> wrapped()
    is TextField          -> wrapped()
    is MenuBar            -> wrapped()
    is FXVK               -> wrapped()
    is ToolBar            -> wrapped()
    is Spinner<*>         -> wrapped()
    is ScrollPane         -> wrapped()
    is Separator          -> wrapped()
    else                  -> when {
        this::class.simpleName == "ScrollBarWidget" -> {
            val theControl: Control = this
            object : ControlWrapperImpl<Control>(theControl) {
                override fun addChild(
                    child: NodeWrapper,
                    index: Int?
                ) = NEVER
            }
        }

        else                                        -> cannotFindWrapper()
    }
}


fun Region.wrapped(): RegionWrapper<*> = findWrapper() ?: when (this) {
    is GridPane          -> wrapped()
    is FlowPane          -> wrapped()
    is VBox              -> wrapped()
    is HBox              -> wrapped()
    /*is PieChartForWrapper            -> wrapped()*/
    is AnchorPane        -> wrapped()
    is StackPane         -> wrapped()
    /*is CategoryAxisForCatAxisWrapper -> wrapped()*/
    is TilePane          -> wrapped()
    is Pane              -> wrapped()
    is TableColumnHeader -> wrapped()
    is Control           -> wrapped()
    is VirtualFlow<*>    -> wrapped()
    /*is AxisForPackagePrivateProps<*> -> wrapped()*/
    else                 -> when {

        this::class.java.let { it.isAnonymousClass && it.superclass.simpleName == "EndButton" } -> {
            /*this is a private class in ScrollBarSkin and when we get here we usually have an anonymous subclass of it. Without this clause we actually get an error because kotlin can't even find the qualified class name of it.*/
            RegionWrapperImpl<Region, NodeWrapper>(
                this
            )
        }

        else                                                                                    -> when (this::class.jvmQualifiedClassName) {
            CLIPPED_CONTAINER_QNAME                                                                     -> ClippedContainerWrapper(
                this@wrapped
            )

            JvmQualifiedClassName("org.fxmisc.richtext.ParagraphBox")                                   -> ParagraphBoxWrapper(
                this@wrapped
            )

            JvmQualifiedClassName("org.fxmisc.flowless.Navigator")                                      -> NavigatorWrapper(
                this@wrapped
            )

            JvmQualifiedClassName("org.fxmisc.flowless.VirtualFlow")                                    -> FlowLessVirtualFlowWrapper(
                this@wrapped
            )

            JvmQualifiedClassName("eu.hansolo.fx.charts.CoxcombChart")                                  -> CoxCombWrapper(
                this@wrapped
            )

            JvmQualifiedClassName("eu.hansolo.fx.charts.matt.fx.control.wrapper.wrapped.SunburstChart") -> SunburstChart(
                this@wrapped
            )

            else                                                                                        -> findWrapper()
                ?: RegionWrapperImpl<Region, NodeWrapper>(
                    this
                )
        }
    }
}

/*hansolo*/
class SunburstChart(sunburst: Region) : RegionWrapperImpl<Region, NodeWrapper>(sunburst) {
    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }

}

/*hansolo*/
class CoxCombWrapper(coxcomb: Region) : RegionWrapperImpl<Region, NodeWrapper>(coxcomb) {
    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }

}

/*RichTextFX*/
class ParagraphBoxWrapper(paragraphBox: Region) : RegionWrapperImpl<Region, NodeWrapper>(paragraphBox) {
    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }

}

/*FlowLess*/
class NavigatorWrapper(navigator: Region) : RegionWrapperImpl<Region, NodeWrapper>(navigator) {
    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }
}


fun Parent.wrapped(): ParentWrapper<*> = findWrapper() ?: when (this) {
    is Region -> wrapped()
    is Group  -> wrapped()
    else      -> cannotFindWrapper()
}


fun Sphere.wrapped(): SphereWrapper = findWrapper() ?: SphereWrapper(this@wrapped)
fun Box.wrapped(): BoxWrapper3D = findWrapper() ?: BoxWrapper3D(this@wrapped)
fun Cylinder.wrapped(): CylinderWrapper = findWrapper() ?: CylinderWrapper(this@wrapped)
fun MeshView.wrapped(): MeshViewWrapper = findWrapper() ?: MeshViewWrapper(this@wrapped)

fun Shape3D.wrapped(): NodeWrapper = findWrapper() ?: when (this) {
    is Sphere   -> wrapped()
    is Box      -> wrapped()
    is Cylinder -> wrapped()
    is MeshView -> wrapped()
    else        -> cannotFindWrapper()
}

fun ImageView.wrapped(): ImageViewWrapper = findWrapper() ?: ImageViewWrapper(this@wrapped)
fun Line.wrapped(): LineWrapper = findWrapper() ?: LineWrapper(this@wrapped)
fun CubicCurve.wrapped(): CubicCurveWrapper = findWrapper() ?: CubicCurveWrapper(this@wrapped)
fun Polyline.wrapped(): PolylineWrapper = findWrapper() ?: PolylineWrapper(this@wrapped)
fun QuadCurve.wrapped(): QuadCurveWrapper = findWrapper() ?: QuadCurveWrapper(this@wrapped)
fun Polygon.wrapped(): PolygonWrapper = findWrapper() ?: PolygonWrapper(this@wrapped)
fun SVGPath.wrapped(): SVGPathWrapper = findWrapper() ?: SVGPathWrapper(this@wrapped)
fun Shape.wrapped(): ShapeWrapper<*> = findWrapper() ?: when (this) {


    is Polyline   -> wrapped()
    is Line       -> wrapped()
    is QuadCurve  -> wrapped()

    is SVGPath    -> wrapped()
    is Rectangle  -> wrapped()
    is CubicCurve -> wrapped()
    is Circle     -> wrapped()
    is Arc        -> wrapped()
    is Ellipse    -> wrapped()
    is Path       -> wrapped()
    is Text       -> wrapped()
    is Polygon    -> wrapped()
    else          -> cannotFindWrapper()
}


fun AmbientLight.wrapped(): AmbientLightWrapper = findWrapper() ?: AmbientLightWrapper(this@wrapped)
fun SpotLight.wrapped(): SpotLightWrapper = findWrapper() ?: SpotLightWrapper(this@wrapped)
fun PointLight.wrapped(): PointLightWrapper = findWrapper() ?: PointLightWrapper(this@wrapped)
fun DirectionalLight.wrapped(): DirectionalLightWrapper = findWrapper() ?: DirectionalLightWrapper(this@wrapped)

fun PerspectiveCamera.wrapped(): PerspectiveCameraWrapper = findWrapper() ?: PerspectiveCameraWrapper(this@wrapped)
fun ParallelCamera.wrapped(): ParallelCameraWrapper = findWrapper() ?: ParallelCameraWrapper(this@wrapped)

fun Canvas.wrapped(): CanvasWrapper = findWrapper() ?: CanvasWrapper(this@wrapped)


fun Node.wrapped(): NodeWrapper = findWrapper() ?: when (this) {
    is AmbientLight      -> wrapped()
    is SpotLight         -> wrapped()
    is Parent            -> wrapped()

    is SubScene          -> wrapped()
    is Shape3D           -> wrapped()
    is Shape             -> wrapped()
    is PointLight        -> wrapped()
    is PerspectiveCamera -> wrapped()
    is ParallelCamera    -> wrapped()

    is Canvas            -> wrapped()
    is ImageView         -> wrapped()
    is DirectionalLight  -> wrapped()
    else                 -> unknownWrapper() /*for SwingNode*/
}


fun Window.wrapped(): WindowWrapper<*> = findWrapper() ?: when (this) {
    is Stage       -> wrapped()

    is ContextMenu -> wrapped()
    /*is Tooltip      -> wrapped()
    is PopupControl -> wrapped()
    is Popup        -> wrapped()*/
    else           -> findWrapper() ?: WindowWrapper(this)
}


fun MenuBar.wrapped(): MenuBarWrapper = findWrapper() ?: MenuBarWrapper(this@wrapped)
fun ContextMenu.wrapped(): ContextMenuWrapper = findWrapper() ?: ContextMenuWrapper(this@wrapped)
fun IntegerField.wrapped(): IntegerFieldWrapper = findWrapper() ?: IntegerFieldWrapper(this@wrapped)
fun SplitMenuButton.wrapped(): SplitMenuButtonWrapper = findWrapper() ?: SplitMenuButtonWrapper(this@wrapped)

fun Rotate.wrapped(): RotateWrapper = findWrapper() ?: RotateWrapper(this@wrapped)
fun Translate.wrapped(): TranslateWrapper = findWrapper() ?: TranslateWrapper(this@wrapped)
fun Shear.wrapped(): ShearWrapper = findWrapper() ?: ShearWrapper(this@wrapped)
fun Affine.wrapped(): AffineWrapper = findWrapper() ?: AffineWrapper(this@wrapped)
fun Scale.wrapped(): ScaleWrapper = findWrapper() ?: ScaleWrapper(this@wrapped)

fun Transform.wrapped(): TransformWrapper<*> = findWrapper() ?: when (this) {
    is Rotate    -> wrapped()
    is Translate -> wrapped()
    is Shear     -> wrapped()
    is Affine    -> wrapped()
    is Scale     -> wrapped()
    else         -> when (this::class.jvmQualifiedClassName) {
        ImmutableTransformWrapper.JFX_QNAME -> ImmutableTransformWrapper(this)
        else                                -> cannotFindWrapper()
    }
}

fun CheckBoxTreeItem<*>.wrapped(): CheckBoxTreeItemWrapper<*> = findWrapper() ?: CheckBoxTreeItemWrapper(this@wrapped)

fun TreeItem<*>.wrapped(): TreeItemWrapper<*> = findWrapper() ?: when (this) {
    is CheckBoxTreeItem<*> -> wrapped()
    else                   -> findWrapper() ?: TreeItemWrapper(this)
}


fun CheckMenuItem.wrapped(): CheckMenuItemWrapper = findWrapper() ?: CheckMenuItemWrapper(this@wrapped)
fun RadioMenuItem.wrapped(): RadioMenuItemWrapper = findWrapper() ?: RadioMenuItemWrapper(this@wrapped)

fun SeparatorMenuItem.wrapped(): SeparatorMenuItemWrapper = findWrapper() ?: SeparatorMenuItemWrapper(this@wrapped)
fun CustomMenuItem.wrapped(): CustomMenuItemWrapper = findWrapper() ?: CustomMenuItemWrapper(this@wrapped)
fun Menu.wrapped(): MenuWrapper = findWrapper() ?: MenuWrapper(this@wrapped)

fun MenuItem.wrapped(): MenuItemWrapper<out MenuItem> = findWrapper() ?: when (this) {
    is CheckMenuItem     -> wrapped()
    is RadioMenuItem     -> wrapped()
    is SeparatorMenuItem -> wrapped()
    is CustomMenuItem    -> wrapped()
    is Menu              -> wrapped()
    else                 -> findWrapper() ?: MenuItemWrapper(this@wrapped)
}


fun TableColumnBase<*, *>.wrapped(): TableColumnBaseWrapper<*, *, *> = findWrapper() ?: when (this) {
    is TreeTableColumn<*, *> -> wrapped()
    is TableColumn<*, *>     -> wrapped()
    else                     -> cannotFindWrapper()
}

/*there is no guarantee that a wrapper which is specific to this node will be appropriately generated, since those wrappers might exist in outside modules*/
fun EventTarget.wrapped(): EventTargetWrapper = findWrapper() ?: when (this) {
    is Node                  -> wrapped()
    is Scene                 -> wrapped()
    is Window                -> wrapped()
    /*is Dialog                -> wrapped()*/
    is Tab                   -> wrapped()
    is MenuItem              -> wrapped()
    is TreeItem<*>           -> wrapped()
    is TableColumnBase<*, *> -> wrapped()
    is Transform             -> wrapped()
    else                     -> unknownWrapper() /*For Dialog*/
}
