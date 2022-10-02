package matt.fx.control.control.dsl

import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.chart.PieChart.Data
import javafx.scene.control.ContextMenu
import javafx.scene.control.Control
import javafx.scene.control.TitledPane
import javafx.scene.control.ToggleGroup
import javafx.scene.paint.Color
import javafx.util.StringConverter
import matt.fx.control.wrapper.button.radio.ValuedRadioButton
import matt.fx.control.wrapper.button.toggle.ValuedToggleButton
import matt.fx.control.wrapper.buttonbar.ButtonBarWrapper
import matt.fx.control.wrapper.chart.area.AreaChartWrapper
import matt.fx.control.wrapper.chart.axis.MAxis
import matt.fx.control.wrapper.chart.bar.BarChartWrapper
import matt.fx.control.wrapper.chart.bubble.BubbleChartWrapper
import matt.fx.control.wrapper.chart.line.LineChartWrapper
import matt.fx.control.wrapper.chart.pie.PieChartWrapper
import matt.fx.control.wrapper.chart.scatter.ScatterChartWrapper
import matt.fx.control.wrapper.chart.stackedbar.StackedBarChartWrapper
import matt.fx.control.wrapper.checkbox.CheckBoxWrapper
import matt.fx.control.wrapper.checkbox.bind
import matt.fx.control.wrapper.control.accordion.AccordionWrapper
import matt.fx.control.wrapper.control.button.ButtonWrapper
import matt.fx.control.wrapper.control.colorpick.ColorPickerWrapper
import matt.fx.control.wrapper.control.colorpick.bind
import matt.fx.control.wrapper.control.datepick.DatePickerWrapper
import matt.fx.control.wrapper.control.datepick.bind
import matt.fx.control.wrapper.control.page.PaginationWrapper
import matt.fx.control.wrapper.control.slider.SliderWrapper
import matt.fx.control.wrapper.control.tab.TabWrapper
import matt.fx.control.wrapper.control.text.area.TextAreaWrapper
import matt.fx.control.wrapper.control.text.field.TextFieldWrapper
import matt.fx.control.wrapper.control.text.field.pass.PasswordFieldWrapper
import matt.fx.control.wrapper.label.LabelWrapper
import matt.fx.control.wrapper.link.HyperlinkWrapper
import matt.fx.control.wrapper.menu.button.MenuButtonWrapper
import matt.fx.control.wrapper.menu.splitbutton.SplitMenuButtonWrapper
import matt.fx.control.wrapper.menubar.MenuBarWrapper
import matt.fx.control.wrapper.progressbar.ProgressBarWrapper
import matt.fx.control.wrapper.progressbar.bind
import matt.fx.control.wrapper.progressindicator.ProgressIndicatorWrapper
import matt.fx.control.wrapper.progressindicator.bind
import matt.fx.control.wrapper.scroll.ScrollPaneWrapper
import matt.fx.control.wrapper.sep.SeparatorWrapper
import matt.fx.control.wrapper.split.SplitPaneWrapper
import matt.fx.control.wrapper.tab.TabPaneWrapper
import matt.fx.control.wrapper.titled.TitledPaneWrapper
import matt.fx.control.wrapper.toolbar.ToolBarWrapper
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.canvas.CanvasWrapper
import matt.fx.graphics.wrapper.group.GroupWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.SimplePaneWrapper
import matt.fx.graphics.wrapper.pane.anchor.AnchorPaneWrapperImpl
import matt.fx.graphics.wrapper.pane.anchor.swapper.Swapper
import matt.fx.graphics.wrapper.pane.border.BorderPaneWrapper
import matt.fx.graphics.wrapper.pane.flow.FlowPaneWrapper
import matt.fx.graphics.wrapper.pane.grid.GridPaneWrapper
import matt.fx.graphics.wrapper.pane.stack.StackPaneWrapper
import matt.fx.graphics.wrapper.pane.tile.TilePaneWrapper
import matt.fx.graphics.wrapper.textflow.TextFlowWrapper
import matt.hurricanefx.eye.mtofx.createROFXPropWrapper
import matt.lang.err
import matt.lang.go
import matt.model.convert.MyNumberStringConverter
import matt.obs.bind.binding
import matt.obs.bindings.str.ObsS
import matt.obs.prop.ObsVal
import matt.obs.prop.ValProp
import matt.obs.prop.Var
import matt.obs.prop.VarProp
import java.time.LocalDate
import kotlin.reflect.KFunction


fun ET.getToggleGroup(): ToggleGroup? = properties["tornadofx.togglegroup"] as ToggleGroup?


fun ET.functionButton(func: KFunction<Unit>) {
  button(func.name) {
	setOnAction {
	  func.call() // "instance" is automatically included in KFunction I think!
	  //            this_obj.inv
	}
  }
}

fun ET.toolbar(vararg nodes: Node, op: ToolBarWrapper.()->Unit = {}): ToolBarWrapper {
  val toolbar = ToolBarWrapper()
  if (nodes.isNotEmpty()) toolbar.items.addAll(nodes)
  toolbar.attachTo(this, op)
  return toolbar
}

fun ET.separator(
  orientation: Orientation = Orientation.HORIZONTAL, op: SeparatorWrapper.()->Unit = {}
) = attach(SeparatorWrapper(orientation), op)

fun <C: NodeWrapper> ET.group(initialChildren: Iterable<C>? = null, op: GroupWrapper<C>.()->Unit = {}) =
  attach(GroupWrapper<C>().apply { if (initialChildren != null) children.addAll(initialChildren) }, op)

fun <C: NodeWrapper> ET.stackpane(initialChildren: Iterable<C>? = null, op: StackPaneWrapper<C>.()->Unit = {}) = attach(
  StackPaneWrapper<C>().apply { if (initialChildren != null) children.addAll(initialChildren) }, op
)

fun <C: NodeWrapper> ET.gridpane(op: GridPaneWrapper<C>.()->Unit = {}) = attach(GridPaneWrapper(), op)
fun <C: NodeWrapper> ET.pane(op: PaneWrapperImpl<*, C>.()->Unit = {}) = attach(SimplePaneWrapper(), op)
fun <C: NodeWrapper> ET.flowpane(op: FlowPaneWrapper<C>.()->Unit = {}) = attach(FlowPaneWrapper(), op)
fun <C: NodeWrapper> ET.tilepane(op: TilePaneWrapper<C>.()->Unit = {}) = attach(TilePaneWrapper(), op)
fun <C: NodeWrapper> ET.borderpane(op: BorderPaneWrapper<C>.()->Unit = {}) = attach(BorderPaneWrapper(), op)


fun ET.titledpane(
  title: String? = null, node: NodeWrapper? = null, collapsible: Boolean = true, op: (TitledPaneWrapper).()->Unit = {}
): TitledPaneWrapper {
  val titledPane = TitledPaneWrapper().apply { text = title!!; graphic = node }
  titledPane.isCollapsible = collapsible
  attach(titledPane, op)
  return titledPane
}

fun ET.titledpane(
  title: ValProp<String>,
  node: NodeWrapper? = null,
  collapsible: Boolean = true,
  op: (TitledPaneWrapper).()->Unit = {}
): TitledPaneWrapper {
  val titledPane = TitledPaneWrapper().apply { text = ""; graphic = node }
  titledPane.textProperty.bind(title)
  titledPane.isCollapsible = collapsible
  attach(titledPane, op)
  return titledPane
}

fun ET.pagination(
  pageCount: Int? = null, pageIndex: Int? = null, op: PaginationWrapper.()->Unit = {}
): PaginationWrapper {
  val pagination = PaginationWrapper()
  if (pageCount != null) pagination.pageCount = pageCount
  if (pageIndex != null) pagination.currentPageIndex = pageIndex
  return attach(pagination, op)
}

fun <C: NodeWrapper> ET.scrollpane(
  content: C, fitToWidth: Boolean = false, fitToHeight: Boolean = false, op: ScrollPaneWrapper<C>.()->Unit = {}
): ScrollPaneWrapper<C> {
  val pane = ScrollPaneWrapper(content)
  pane.isFitToWidth = fitToWidth
  pane.isFitToHeight = fitToHeight
  attach(pane, op)
  return pane
}


fun ET.splitpane(
  orientation: Orientation = Orientation.HORIZONTAL, vararg nodes: Node, op: SplitPaneWrapper.()->Unit = {}
): SplitPaneWrapper {
  val splitpane = SplitPaneWrapper()
  splitpane.orientation = orientation
  if (nodes.isNotEmpty()) splitpane.items.addAll(nodes)
  attach(splitpane, op)
  return splitpane
}


fun ET.canvas(width: Double = 0.0, height: Double = 0.0, op: CanvasWrapper.()->Unit = {}) =
  attach(CanvasWrapper(width, height), op)

fun <C: NodeWrapper> ET.anchorpane(
  vararg nodes: C,
  op: AnchorPaneWrapperImpl<C>.()->Unit = {}
): AnchorPaneWrapperImpl<C> {
  val anchorpane = AnchorPaneWrapperImpl<C>()
  if (nodes.isNotEmpty()) anchorpane.children.addAll(nodes)
  attach(anchorpane, op)
  return anchorpane
}

fun ET.accordion(vararg panes: TitledPane, op: AccordionWrapper.()->Unit = {}): AccordionWrapper {
  val accordion = AccordionWrapper()
  if (panes.isNotEmpty()) accordion.panes.addAll(panes)
  attach(accordion, op)
  return accordion
}


fun ET.colorpicker(
  color: Color? = null, op: ColorPickerWrapper.()->Unit = {}
) = ColorPickerWrapper().attachTo(this, op) {
  if (color != null) it.value = color
}

fun ET.colorpicker(
  colorProperty: ObjectProperty<Color>, op: ColorPickerWrapper.()->Unit = {}
) = ColorPickerWrapper().apply { bind(colorProperty) }.attachTo(this, op) {}

fun <C: NodeWrapper> textflow(op: TextFlowWrapper<C>.()->Unit = {}) = TextFlowWrapper<C>().attachTo(this, op)


fun <P, N: NodeWrapper> ET.swapper(
  prop: ObsVal<P>,
  nullMessage: String? = null,
  op: (P & Any).()->N
): Swapper<P, N> {
  val swapper = Swapper<P, N>()
  swapper.setupSwapping(prop, nullMessage = nullMessage, op)
  return attach(swapper)
}


fun ET.contextmenu(op: ContextMenu.()->Unit = {}): ContextMenu {
  val menu = (this as? Control)?.contextMenu ?: ContextMenu()
  op(menu)
  if (this is Control) {
	contextMenu = menu
  } else (this as? Node)?.apply {
	setOnContextMenuRequested { event ->
	  menu.show(this, event.screenX, event.screenY)
	  event.consume()
	}
  }
  return menu
}

/**
 * Add a context menu to the target which will be created on demand.
 */
fun ET.lazyContextmenu(op: ContextMenu.()->Unit = {}) = apply {
  var currentMenu: ContextMenu? = null
  (this as? Node)?.setOnContextMenuRequested { event ->
	currentMenu?.hide()
	currentMenu = ContextMenu().also {
	  it.setOnCloseRequest { currentMenu = null }
	  op(it)
	  it.show(this, event.screenX, event.screenY)
	}
	event.consume()
  }
}


/**
 * Create a PieChart with optional title data and add to the parent pane. The optional op will be performed on the new instance.
 */
fun ET.piechart(
  title: String? = null, data: ObservableList<Data>? = null, op: PieChartWrapper.()->Unit = {}
): PieChartWrapper {
  val chart = if (data != null) PieChartWrapper(data) else PieChartWrapper()
  chart.title = title
  return attach(chart, op)
}


/**
 * Create a LineChart with optional title, axis and add to the parent pane. The optional op will be performed on the new instance.
 */
fun <X, Y> ET.linechart(title: String? = null, x: MAxis<X>, y: MAxis<Y>, op: LineChartWrapper<X, Y>.()->Unit = {}) =
  LineChartWrapper(x, y).attachTo(this, op) { it.title = title }

/**
 * Create an AreaChart with optional title, axis and add to the parent pane. The optional op will be performed on the new instance.
 */
fun <X, Y> ET.areachart(title: String? = null, x: MAxis<X>, y: MAxis<Y>, op: AreaChartWrapper<X, Y>.()->Unit = {}) =
  AreaChartWrapper<X, Y>(x, y).attachTo(this, op) { it.title = title }

/**
 * Create a BubbleChart with optional title, axis and add to the parent pane. The optional op will be performed on the new instance.
 */
fun <X, Y> ET.bubblechart(title: String? = null, x: MAxis<X>, y: MAxis<Y>, op: BubbleChartWrapper<X, Y>.()->Unit = {}) =
  BubbleChartWrapper<X, Y>(x, y).attachTo(this, op) { it.title = title }

/**
 * Create a ScatterChart with optional title, axis and add to the parent pane. The optional op will be performed on the new instance.
 */
fun <X, Y> ET.scatterchart(
  title: String? = null,
  x: MAxis<X>,
  y: MAxis<Y>,
  op: ScatterChartWrapper<X, Y>.()->Unit = {}
) =
  ScatterChartWrapper(x, y).attachTo(this, op) { it.title = title }

/**
 * Create a BarChart with optional title, axis and add to the parent pane. The optional op will be performed on the new instance.
 */
fun <X, Y> ET.barchart(title: String? = null, x: MAxis<X>, y: MAxis<Y>, op: BarChartWrapper<X, Y>.()->Unit = {}) =
  BarChartWrapper<X, Y>(x, y).attachTo(this, op) { it.title = title }

/**
 * Create a BarChart with optional title, axis and add to the parent pane. The optional op will be performed on the new instance.
 */
fun <X, Y> ET.stackedbarchart(
  title: String? = null, x: MAxis<X>, y: MAxis<Y>, op: StackedBarChartWrapper<X, Y>.()->Unit = {}
) = StackedBarChartWrapper<X, Y>(x, y).attachTo(this, op) { it.title = title }


fun ET.textfield(value: String? = null, op: TextFieldWrapper.()->Unit = {}) = TextFieldWrapper().attachTo(this, op) {
  if (value != null) it.text = value
}


fun ET.passwordfield(value: String? = null, op: PasswordFieldWrapper.()->Unit = {}) =
  PasswordFieldWrapper().attachTo(this, op) {
	if (value != null) it.text = value
  }

fun ET.passwordfield(property: VarProp<String>, op: PasswordFieldWrapper.()->Unit = {}) = passwordfield().apply {
  textProperty.bindBidirectional(property)
  op(this)
}

fun <T> ET.textfield(
  property: Var<T>, converter: StringConverter<T>, op: TextFieldWrapper.()->Unit = {}
) = textfield().apply {
  err("textProperty.bindBidirectional(property, converter)")
  @Suppress("UNREACHABLE_CODE")
  op(this)
}

fun ET.datepicker(op: DatePickerWrapper.()->Unit = {}) = DatePickerWrapper().attachTo(this, op)
fun ET.datepicker(property: Property<LocalDate>, op: DatePickerWrapper.()->Unit = {}) = datepicker().apply {
  bind(property)
  op(this)
}

fun ET.textarea(value: String? = null, op: TextAreaWrapper.()->Unit = {}) = TextAreaWrapper().attachTo(this, op) {
  if (value != null) it.text = value
}


fun ET.textarea(property: VarProp<String>, op: TextAreaWrapper.()->Unit = {}) = textarea().apply {
  textProperty.bindBidirectional(property)
  op(this)
}


fun <T> ET.textarea(
  property: VarProp<T>, converter: StringConverter<T>, op: TextAreaWrapper.()->Unit = {}
) = textarea().apply {
  err("textProperty.bindBidirectional(property, converter)")
  @Suppress("UNREACHABLE_CODE")
  op(this)
}


fun ET.buttonbar(buttonOrder: String? = null, op: (ButtonBarWrapper.()->Unit)) = ButtonBarWrapper().attachTo(this, op) {
  if (buttonOrder != null) it.buttonOrder = buttonOrder
}


fun ET.checkbox(
  text: String? = null, property: Var<Boolean>? = null, op: CheckBoxWrapper.()->Unit = {}
) = CheckBoxWrapper().apply { this.text = text!! }.attachTo(this, op) {
  if (property != null) it.bind(property)
}


fun ET.progressindicator(op: ProgressIndicatorWrapper.()->Unit = {}) = ProgressIndicatorWrapper().attachTo(this, op)

fun ET.progressindicator(property: Property<Number>, op: ProgressIndicatorWrapper.()->Unit = {}) =
  progressindicator().apply {
	bind(property)
	op(this)
  }

fun ET.progressbar(initialValue: Double? = null, op: ProgressBarWrapper.()->Unit = {}) =
  ProgressBarWrapper().attachTo(this, op) {
	if (initialValue != null) it.progress = initialValue
  }

fun ET.progressbar(property: ObservableValue<Number>, op: ProgressBarWrapper.()->Unit = {}) = progressbar().apply {
  bind(property)
  op(this)
}

fun ET.slider(
  min: Number? = null,
  max: Number? = null,
  value: Number? = null,
  orientation: Orientation? = null,
  op: SliderWrapper.()->Unit = {}
) = SliderWrapper().attachTo(this, op) {
  if (min != null) it.min = min.toDouble()
  if (max != null) it.max = max.toDouble()
  if (value != null) it.value = value.toDouble()
  if (orientation != null) it.orientation = orientation
}

fun <T> ET.slider(
  range: ClosedRange<T>, value: Number? = null, orientation: Orientation? = null, op: SliderWrapper.()->Unit = {}
): SliderWrapper where T: Comparable<T>, T: Number {
  return slider(range.start, range.endInclusive, value, orientation, op)
}


// Buttons
fun ET.button(
  text: String = "", graphic: NodeWrapper? = null, op: ButtonWrapper.()->Unit = {}
): ButtonWrapper {
  return ButtonWrapper().apply {
	this.text = text
	if (graphic != null) this.graphic = graphic
	apply(op)    //	op()
  }.attachTo(this, op)
}

fun ET.menubutton(text: String = "", graphic: NodeWrapper? = null, op: MenuButtonWrapper.()->Unit = {}) =
  MenuButtonWrapper().apply {
	this.text = text
  }.attachTo(this, op) {
	if (graphic != null) it.graphic = graphic
  }

fun ET.splitmenubutton(
  text: String? = null, graphic: NodeWrapper? = null, op: SplitMenuButtonWrapper.()->Unit = {}
) = SplitMenuButtonWrapper().attachTo(this, op) {
  if (text != null) it.text = text
  if (graphic != null) it.graphic = graphic
}

fun ET.button(
  text: ValProp<String>, graphic: NodeWrapper? = null, op: ButtonWrapper.()->Unit = {}
) = ButtonWrapper().attachTo(this, op) {
  it.textProperty.bind(text)
  if (graphic != null) it.graphic = graphic
}


/**
 * Create a togglebutton inside the current or given toggle group. The optional value parameter will be matched against
 * the extension property `selectedValueProperty()` on Toggle Group. If the #ToggleGroup.selectedValueProperty is used,
 * it's value will be updated to reflect the value for this radio button when it's selected.
 *
 * Likewise, if the `selectedValueProperty` of the ToggleGroup is updated to a value that matches the value for this
 * togglebutton, it will be automatically selected.
 */
fun <V> ET.togglebutton(
  text: String? = null,
  group: ToggleGroup? = getToggleGroup(),
  selectFirst: Boolean = false,
  value: V,
  op: ValuedToggleButton<V>.()->Unit = {}
) = ValuedToggleButton(value).attachTo(this, op) {
  it.text = if (value != null && text == null) value.toString() else text ?: ""
  if (group != null) it.node.toggleGroup = group
  if (it.node.toggleGroup?.selectedToggle == null && selectFirst) it.isSelected = true
}

fun <V> ET.togglebutton(
  text: VarProp<String>? = null,
  group: ToggleGroup? = getToggleGroup(),
  selectFirst: Boolean = false,
  value: V,
  op: ValuedToggleButton<V>.()->Unit = {}
) = ValuedToggleButton(value).attachTo(this, op) {
  val thing = it
  text?.go { thing.textProperty.bind(it) }
  if (group != null) it.node.toggleGroup = group
  if (it.node.toggleGroup?.selectedToggle == null && selectFirst) it.isSelected = true
}

fun <V> ET.togglebutton(
  group: ToggleGroup? = getToggleGroup(),
  selectFirst: Boolean = false,
  value: V,
  op: ValuedToggleButton<V>.()->Unit = {}
) = ValuedToggleButton(value).attachTo(this, op) {
  if (group != null) it.node.toggleGroup = group
  if (it.node.toggleGroup?.selectedToggle == null && selectFirst) it.isSelected = true
}

/**
 * Create a radiobutton inside the current or given toggle group. The optional value parameter will be matched against
 * the extension property `selectedValueProperty()` on Toggle Group. If the #ToggleGroup.selectedValueProperty is used,
 * it's value will be updated to reflect the value for this radio button when it's selected.
 *
 * Likewise, if the `selectedValueProperty` of the ToggleGroup is updated to a value that matches the value for this
 * radiobutton, it will be automatically selected.
 */
fun <V> ET.radiobutton(
  text: String? = null,
  group: ToggleGroup? = getToggleGroup(),
  value: V,
  op: ValuedRadioButton<V>.()->Unit = {}
) = ValuedRadioButton(value).attachTo(this, op) {
  it.text = if (value != null && text == null) value.toString() else text ?: ""
  if (group != null) it.node.toggleGroup = group
}

fun ET.label(text: String = "", graphic: NodeWrapper? = null, wrap: Boolean? = null, op: LabelWrapper.()->Unit = {}) =
  LabelWrapper().apply { this.text = text }.attachTo(this, op) {
	if (graphic != null) it.graphic = graphic
	if (wrap != null) it.isWrapText = wrap
  }


fun ET.hyperlink(text: String = "", graphic: NodeWrapper? = null, op: HyperlinkWrapper.()->Unit = {}) =
  HyperlinkWrapper().apply { this.text = text;this.graphic = graphic }.attachTo(this, op)

fun ET.hyperlink(
  observable: ObsS,
  graphic: NodeWrapper? = null,
  op: HyperlinkWrapper.()->Unit = {}
) = hyperlink(graphic = graphic).apply {
  textProperty.bind(observable)
  op(this)
}

fun ET.menubar(op: MenuBarWrapper.()->Unit = {}) = MenuBarWrapper().attachTo(this, op)


fun ET.textfield(property: Var<String>, op: TextFieldWrapper.()->Unit = {}) =
  textfield().apply {
	textProperty.bindBidirectional(property)
	op(this)
  }

/*@JvmName("textfieldNumber") */fun textfield(
  property: Var<Number>,
  op: TextFieldWrapper.()->Unit = {}
): TextFieldWrapper = textfield().apply {
  textProperty.bindBidirectional(property, MyNumberStringConverter)
  op(this)
}

//@JvmName("textfieldInt") fun EventTargetWrapper.textfield(
//  property: ValProp<Int>,
//  op: TextFieldWrapper.()->Unit = {}
//) = textfield().apply {
//  bind(property)
//  op(this)
//}

fun <T: TabWrapper<*>> ET.tabpane(op: TabPaneWrapper<T>.()->Unit = {}) = TabPaneWrapper<T>().attachTo(this, op)


inline fun <reified T> ET.label(
  observable: ObsVal<T>,
  graphicProperty: ValProp<Node>? = null,
  converter: StringConverter<in T>? = null,
  noinline op: LabelWrapper.()->Unit = {}
) = label().apply {
  if (converter == null) {
	if (T::class == String::class) {
	  @Suppress("UNCHECKED_CAST")
	  textProperty.bind(observable as ValProp<String>)
	} else {
	  textProperty.bind(observable.binding { it?.toString() })
	}
  } else {
	textProperty.bind(observable.binding { converter.toString(it) })
  }
  if (graphic != null) graphicProperty().bind(graphicProperty?.createROFXPropWrapper())
  op(this)
}