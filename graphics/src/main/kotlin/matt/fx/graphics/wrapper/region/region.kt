package matt.fx.graphics.wrapper.region

import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.input.DataFormat
import javafx.scene.input.TransferMode
import javafx.scene.layout.Background
import javafx.scene.layout.Border
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import matt.file.construct.toMFile
import matt.fx.base.wrapper.obs.collect.list.createImmutableWrapper
import matt.fx.base.wrapper.obs.collect.list.createMutableWrapper
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.base.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.graphics.service.uncheckedWrapperConverter
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
import matt.image.Png
import matt.image.convert.toPng
import matt.lang.NEVER
import matt.lang.convert.BiConverter
import matt.lang.delegation.lazyVarDelegate
import matt.lang.err
import matt.lang.model.file.FsFile
import matt.lang.model.file.MacFileSystem
import matt.model.data.rect.IntRectSize
import matt.model.obj.raster.PngRasterizable
import matt.obs.bindhelp.bindMultipleTargetsTogether
import matt.obs.col.olist.ImmutableObsList
import matt.obs.col.olist.mappedlist.toLazyMappedList
import matt.obs.col.olist.sync.toSyncedList
import matt.obs.prop.BindableProperty
import matt.obs.prop.ObsVal
import matt.obs.prop.Var
import matt.obs.prop.VarProp
import matt.obs.prop.proxy.ProxyProp
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


    fun setMinSize(
        minWidth: Double,
        minHeight: Double
    ) = node.setMinSize(minWidth, minHeight)

    override fun rasterize(size: IntRectSize): Png

    val borderProperty: Var<Border?>
    var border: Border?
        get() = node.border
        set(value) = borderProperty.v(value)

    fun yellow() = borderProperty.v(FXBorder.dashed(Color.YELLOW))
    fun blue() = borderProperty.v(FXBorder.dashed(Color.BLUE))
    fun purple() = borderProperty.v(FXBorder.dashed(Color.PURPLE))
    fun green() = borderProperty.v(FXBorder.dashed(Color.GREEN))
    fun red() = borderProperty.v(FXBorder.dashed(Color.RED))
    fun orange() = borderProperty.v(FXBorder.dashed(Color.ORANGE))
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

    var backgroundFill: Paint?
        set(value) {
            if (value == null) {
                this.background = null
            } else {
                background = backgroundFromColor(value)
            }

        }
        get() {
            err("no getter yet")
        }


    fun fitToParentHeight() {
        fitToHeight((parent as RegionWrapper))
    }

    fun fitToParentWidth() {
        fitToWidth((parent as RegionWrapper))
    }

    fun fitToParentSize() {
        fitToParentHeight()
        fitToParentWidth()
    }

    fun fitToHeight(region: RegionWrapper<*>) {
        prefHeightProperty.bind(region.heightProperty)
    }

    fun fitToWidth(region: RegionWrapper<*>) {
        prefWidthProperty.bind(region.widthProperty)
    }

    fun fitToSize(region: RegionWrapper<*>) {
        fitToHeight(region)
        fitToWidth(region)
    }


    val paddingVerticalProperty: Var<Double>
    var paddingVertical: Double

    val exactWidthProperty: VarProp<Double>
    val exactHeightProperty: VarProp<Double>


    var exactWidth: Number
        set(value) {
            exactWidthProperty.bind(BindableProperty(value.toDouble()))
        }
        get() = NEVER
    var exactHeight: Number
        set(value) {
            exactHeightProperty.bind(BindableProperty(value.toDouble()))
        }
        get() = NEVER


    var useMaxWidth: Boolean
        get() = maxWidth == Double.MAX_VALUE
        set(value) = if (value) maxWidth = Double.MAX_VALUE else Unit

    var useMaxHeight: Boolean
        get() = maxHeight == Double.MAX_VALUE
        set(value) = if (value) maxHeight = Double.MAX_VALUE else Unit

    var useMaxSize: Boolean
        get() = maxWidth == Double.MAX_VALUE && maxHeight == Double.MAX_VALUE
        set(value) = if (value) {
            useMaxWidth = true; useMaxHeight = true
        } else Unit

    var usePrefWidth: Boolean
        get() = width == prefWidth
        set(value) = if (value) run {
            minWidth = (Region.USE_PREF_SIZE)
        } else Unit

    var usePrefHeight: Boolean
        get() = height == prefHeight
        set(value) = if (value) run { minHeight = (Region.USE_PREF_SIZE) } else Unit

    var usePrefSize: Boolean
        get() = maxWidth == Double.MAX_VALUE && maxHeight == Double.MAX_VALUE
        set(value) = if (value) setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE) else Unit


    override fun addChild(
        child: NodeWrapper,
        index: Int?
    ) {
        TODO()
    }


    val children: ImmutableObsList<C>


}

fun RegionWrapper<*>.computePrefWidth(height: Double) = (computePrefWidthFun.invoke(node, height) as Double).also {
    println("computed pref width of $node with height $height is $it")
}


open class RegionWrapperImpl<N : Region, C : NodeWrapper>(node: N) : ParentWrapperImpl<N, C>(node), RegionWrapper<C> {
    /*any temporary border changes might want to come back to this after*//*used to be an ugly lazy map that lead to errors. manual is better for this.*/
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

    @Suppress("UNCHECKED_CAST")
    protected val regionChildren by lazy {
        (regionChildrenProp.call(this@RegionWrapperImpl.node) as ObservableList<Node>).createMutableWrapper()
            .toSyncedList(
                uncheckedWrapperConverter()
            )
    }

    override val borderProperty by lazy {
        node.borderProperty().toNullableProp()
    }

    override val widthProperty by lazy { node.widthProperty().toNonNullableROProp().cast<Double>() }
    override val prefWidthProperty by lazy { node.prefWidthProperty().toNonNullableProp().cast<Double>() }
    override val minWidthProperty by lazy { node.minWidthProperty().toNonNullableProp().cast<Double>() }
    override val maxWidthProperty by lazy { node.maxWidthProperty().toNonNullableProp().cast<Double>() }
    override val heightProperty by lazy { node.heightProperty().toNonNullableROProp().cast<Double>() }
    override val prefHeightProperty by lazy { node.prefHeightProperty().toNonNullableProp().cast<Double>() }
    override val minHeightProperty by lazy { node.minHeightProperty().toNonNullableProp().cast<Double>() }
    override val maxHeightProperty by lazy { node.maxHeightProperty().toNonNullableProp().cast<Double>() }


    override val children: ImmutableObsList<C> by lazy {    /*trying to avoid initializing wrappers to quickly (and getting the wrong ones as a result)*/
        node.childrenUnmodifiable
            .createImmutableWrapper().toLazyMappedList {
                uncheckedWrapperConverter<Node, C>().convertToB(it)
            }
    }

    final override fun rasterize(size: IntRectSize): Png {
        return DefaultStudio(
            size
        ).shoot(this).toBufferedImage().toPng()
    }

    override val paddingProperty by lazy { node.paddingProperty().toNonNullableProp() }

    override var padding by lazyVarDelegate {
        paddingProperty
    }


    final override val paddingVerticalProperty: ProxyProp<Insets, Double> by lazy {
        paddingProperty.proxy(object : BiConverter<Insets, Double> {
            override fun convertToB(a: Insets): Double {
                return a.vertical
            }


            override fun convertToA(b: Double): Insets {
                return padding.copy(vertical = b)
            }

        })
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


