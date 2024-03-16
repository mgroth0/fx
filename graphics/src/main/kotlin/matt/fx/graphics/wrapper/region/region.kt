package matt.fx.graphics.wrapper.region

import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.input.DataFormat
import javafx.scene.input.TransferMode
import javafx.scene.layout.Background
import javafx.scene.layout.Border
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import matt.file.construct.toMFile
import matt.fx.base.wrapper.obs.collect.list.createImmutableWrapper
import matt.fx.base.wrapper.obs.collect.list.createMutableWrapper
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.base.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.graphics.service.wrapperConverter
import matt.fx.graphics.studio.DefaultStudio
import matt.fx.graphics.style.background.backgroundFromColor
import matt.fx.graphics.style.border.FXBorder
import matt.fx.graphics.style.copy
import matt.fx.graphics.style.vertical
import matt.fx.graphics.stylelock.toNullableStyleProp
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapperImpl
import matt.fx.graphics.wrapper.node.parent.parent
import matt.fx.graphics.wrapper.region.RegionWrapper.Companion.computePrefWidthFun
import matt.fx.graphics.wrapper.sizeman.SizeManaged
import matt.fx.image.toBufferedImage
import matt.image.common.Png
import matt.image.common.PngRasterizable
import matt.image.convert.toPng
import matt.lang.anno.Open
import matt.lang.common.NEVER
import matt.lang.common.err
import matt.lang.convert.BiConverter
import matt.lang.delegation.lazyVarDelegate
import matt.lang.model.file.FsFile
import matt.lang.model.file.MacFileSystem
import matt.model.data.rect.IntRectSize
import matt.obs.bindhelp.bindMultipleTargetsTogether
import matt.obs.col.olist.ImmutableObsList
import matt.obs.col.olist.mappedlist.toLazyMappedList
import matt.obs.col.olist.sync.toSyncedList
import matt.obs.prop.ObsVal
import matt.obs.prop.proxy.ProxyProp
import matt.obs.prop.writable.BindableProperty
import matt.obs.prop.writable.Var
import matt.obs.prop.writable.VarProp
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible


interface RegionWrapper<C : NodeWrapper> : ParentWrapper<C>, SizeManaged, PngRasterizable {

    companion object {
        internal val computePrefWidthFun =
            Region::class.java.getDeclaredMethod("computePrefWidth", Double::class.java).apply {
                isAccessible = true
            }
    }


    override val node: Region


    @Open
    fun setMinSize(
        minWidth: Double,
        minHeight: Double
    ) = node.setMinSize(minWidth, minHeight)

    override fun rasterize(size: IntRectSize): Png

    val borderProperty: Var<Border?>
    @Open
    var border: Border?
        get() = node.border
        set(value) = borderProperty.v(value)

    @Open
    fun yellow() = borderProperty.v(FXBorder.dashed(Color.YELLOW))
    @Open
    fun blue() = borderProperty.v(FXBorder.dashed(Color.BLUE))
    @Open
    fun purple() = borderProperty.v(FXBorder.dashed(Color.PURPLE))
    @Open fun green() = borderProperty.v(FXBorder.dashed(Color.GREEN))
    @Open
    fun red() = borderProperty.v(FXBorder.dashed(Color.RED))
    @Open
    fun orange() = borderProperty.v(FXBorder.dashed(Color.ORANGE))
    @Open
    fun white() = borderProperty.v(FXBorder.dashed(Color.WHITE))

    var padding: Insets
    val paddingProperty: Var<Insets>


    val backgroundProperty: Var<Background?>
    var background: Background?

    override val widthProperty: ObsVal<Double>
    override val prefWidthProperty: Var<Double>
    override val minWidthProperty: Var<Double>
    override val maxWidthProperty: Var<Double>
    override val heightProperty: ObsVal<Double>
    override val prefHeightProperty: Var<Double>
    override val minHeightProperty: Var<Double>
    override val maxHeightProperty: Var<Double>

    @Open
    fun setOnFilesDropped(op: (List<FsFile>) -> Unit) {
        node.setOnDragEntered {
            it.acceptTransferModes(*TransferMode.ANY)
        }
        node.setOnDragOver {
            it.acceptTransferModes(*TransferMode.ANY)
        }
        node.setOnDragDropped {
            if (DataFormat.FILES in it.dragboard.contentTypes) {
                op(it.dragboard.files.map { it.toMFile(MacFileSystem) })
            }
            it.consume()
        }
    }

    @Open
    var backgroundFill: Paint?
        set(value) {
            if (value == null) {
                background = null
            } else {
                background = backgroundFromColor(value)
            }
        }
        get() {
            err("no getter yet")
        }


    @Open
    fun fitToParentHeight() {
        fitToHeight((parent as RegionWrapper))
    }

    @Open
    fun fitToParentWidth() {
        fitToWidth((parent as RegionWrapper))
    }

    @Open
    fun fitToParentSize() {
        fitToParentHeight()
        fitToParentWidth()
    }

    @Open
    fun fitToHeight(region: RegionWrapper<*>) {
        prefHeightProperty.bind(region.heightProperty)
    }

    @Open
    fun fitToWidth(region: RegionWrapper<*>) {
        prefWidthProperty.bind(region.widthProperty)
    }

    @Open
    fun fitToSize(region: RegionWrapper<*>) {
        fitToHeight(region)
        fitToWidth(region)
    }


    val paddingVerticalProperty: Var<Double>
    var paddingVertical: Double

    val exactWidthProperty: VarProp<Double>
    val exactHeightProperty: VarProp<Double>


    @Open
    var exactWidth: Number
        set(value) {
            exactWidthProperty.bind(BindableProperty(value.toDouble()))
        }
        get() = NEVER
    @Open
    var exactHeight: Number
        set(value) {
            exactHeightProperty.bind(BindableProperty(value.toDouble()))
        }
        get() = NEVER


    @Open
    var useMaxWidth: Boolean
        get() = maxWidth == Double.MAX_VALUE
        set(value) = if (value) maxWidth = Double.MAX_VALUE else Unit

    @Open
    var useMaxHeight: Boolean
        get() = maxHeight == Double.MAX_VALUE
        set(value) = if (value) maxHeight = Double.MAX_VALUE else Unit

    @Open
    var useMaxSize: Boolean
        get() = maxWidth == Double.MAX_VALUE && maxHeight == Double.MAX_VALUE
        set(value) =
            if (value) {
                useMaxWidth = true
                useMaxHeight = true
            } else Unit

    @Open
    var usePrefWidth: Boolean
        get() = width == prefWidth
        set(value) =
            if (value) run {
                minWidth = (Region.USE_PREF_SIZE)
            } else Unit

    @Open
    var usePrefHeight: Boolean
        get() = height == prefHeight
        set(value) = if (value) run { minHeight = (Region.USE_PREF_SIZE) } else Unit

    @Open
    var usePrefSize: Boolean
        get() = maxWidth == Double.MAX_VALUE && maxHeight == Double.MAX_VALUE
        set(value) = if (value) setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE) else Unit


    @Open override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }


    val children: ImmutableObsList<C>
}

fun RegionWrapper<*>.computePrefWidth(height: Double) =
    (computePrefWidthFun.invoke(node, height) as Double).also {
        println("computed pref width of $node with height $height is $it")
    }


open class RegionWrapperImpl<N : Region, C : NodeWrapper>(node: N, childClass: KClass<C>) : ParentWrapperImpl<N, C>(node, childClass), RegionWrapper<C> {
    /*any temporary border changes might want to come back to this after


    used to be an ugly lazy map that lead to errors. manual is better for this.*/
    var defaultBorder: Border = Border.EMPTY

    companion object {
        private val regionChildrenProp by lazy {
            Parent::class.declaredMemberProperties.first {
                it.name == "children"
            }.apply {
                isAccessible = true
            }
        }
    }

    protected val regionChildren by lazy {
        /*this will clearly fail once I have a non-pane Region. Deal with it then, if ever. I'm moving on from FX...*/
        (node as Pane).children
            /*(regionChildrenProp.call(this@RegionWrapperImpl.node) as ObservableList<Node>)*/.createMutableWrapper()
            .toSyncedList(
                wrapperConverter(
                    eClass = Node::class,
                    wClass = childClass
                )
            )
    }

    final override val borderProperty by lazy {
        node.borderProperty().toNullableProp()
    }

    final override val widthProperty by lazy { node.widthProperty().toNonNullableROProp().cast<Double>(Double::class) }
    final override val prefWidthProperty by lazy { node.prefWidthProperty().toNonNullableProp().cast<Double>(Double::class) }
    final override val minWidthProperty by lazy { node.minWidthProperty().toNonNullableProp().cast<Double>(Double::class) }
    final override val maxWidthProperty by lazy { node.maxWidthProperty().toNonNullableProp().cast<Double>(Double::class) }
    final override val heightProperty by lazy { node.heightProperty().toNonNullableROProp().cast<Double>(Double::class) }
    final override val prefHeightProperty by lazy { node.prefHeightProperty().toNonNullableProp().cast<Double>(Double::class) }
    final override val minHeightProperty by lazy { node.minHeightProperty().toNonNullableProp().cast<Double>(Double::class) }
    final override val maxHeightProperty by lazy { node.maxHeightProperty().toNonNullableProp().cast<Double>(Double::class) }


    @Open override val children: ImmutableObsList<C> by lazy {
        /*trying to avoid initializing wrappers to quickly (and getting the wrong ones as a result)*/
        node.childrenUnmodifiable.createImmutableWrapper().toLazyMappedList {
            wrapperConverter<Node, C>(Node::class, childClass).convertToB(it)
        }
    }

    final override fun rasterize(size: IntRectSize): Png =
        DefaultStudio(
            size
        ).shoot(this).toBufferedImage().toPng()

    final override val paddingProperty: NonNullFXBackedBindableProp<Insets> by lazy { node.paddingProperty().toNonNullableProp() }

    final override var padding by lazyVarDelegate {
        paddingProperty
    }


    final override val paddingVerticalProperty: ProxyProp<Insets, Double> by lazy {
        paddingProperty.proxy(
            object : BiConverter<Insets, Double> {
                override fun convertToB(a: Insets): Double = a.vertical


                override fun convertToA(b: Double): Insets = padding.copy(vertical = b)
            }
        )
    }
    final override var paddingVertical by lazyVarDelegate { paddingVerticalProperty }

    final override val backgroundProperty by lazy { node.backgroundProperty().toNullableStyleProp() }
    final override var background by lazyVarDelegate { backgroundProperty }

    final override val exactWidthProperty by lazy {
        BindableProperty(0.0).also {
            bindMultipleTargetsTogether(
                setOf(minWidthProperty, maxWidthProperty), it
            )
        }
    }

    final override val exactHeightProperty by lazy {
        BindableProperty(0.0).also {
            bindMultipleTargetsTogether(
                setOf(minHeightProperty, maxHeightProperty), it
            )
        }
    }
}


